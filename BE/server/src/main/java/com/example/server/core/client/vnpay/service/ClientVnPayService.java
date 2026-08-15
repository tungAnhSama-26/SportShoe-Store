package com.example.server.core.client.vnpay.service;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.dathang.service.ClientCheckoutItemService;
import com.example.server.core.client.dathang.service.ClientDatHangService;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.core.client.vnpay.dto.TaoMaVnPayResponse;
import com.example.server.core.client.voucher.service.ClientVoucherService;
import com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher;
import com.example.server.core.inventory.TonKhoKhaDungService;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thanh toán bằng VietQR/SePay hoặc VNPAY với phiên giữ hàng bền vững trong database.
 *
 * <p>Luồng: tạo hóa đơn chờ và giao dịch chờ trong cùng transaction -&gt; giữ số lượng
 * khả dụng trong 5 phút nhưng chưa trừ tồn thực tế -&gt; xác nhận thanh toán chuyển hóa
 * đơn sang Chờ xác nhận. Tồn thực tế chỉ giảm khi nhân viên xác nhận đơn.</p>
 */
@Service
public class ClientVnPayService {

    public static final String TRANG_THAI_CHO = "CHO";
    public static final String TRANG_THAI_DA_THANH_TOAN = "DA_THANH_TOAN";
    public static final String TRANG_THAI_KHONG_TON_TAI = "KHONG_TON_TAI";

    private final ClientDatHangService datHangService;
    private final ClientCheckoutItemService checkoutItemService;
    private final ClientVoucherService voucherService;
    private final ClientPhiVanChuyenService phiVanChuyenService;
    private final SanPhamRealtimePublisher sanPhamRealtimePublisher;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final VanChuyenRepository vanChuyenRepository;

    private final String sepayBank;
    private final String sepayAccount;
    private final String sepayPrefix;

    @Value("${vnpay.tmn-code:}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret:}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url:}")
    private String vnp_ReturnUrl;

    public ClientVnPayService(
            ClientDatHangService datHangService,
            ClientCheckoutItemService checkoutItemService,
            ClientVoucherService voucherService,
            ClientPhiVanChuyenService phiVanChuyenService,
            SanPhamRealtimePublisher sanPhamRealtimePublisher,
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository hoaDonChiTietRepository,
            ThanhToanRepository thanhToanRepository,
            VanChuyenRepository vanChuyenRepository,
            @Value("${sepay.bank:}") String sepayBank,
            @Value("${sepay.account-number:}") String sepayAccount,
            @Value("${sepay.prefix:SHOE}") String sepayPrefix
    ) {
        this.datHangService = datHangService;
        this.checkoutItemService = checkoutItemService;
        this.voucherService = voucherService;
        this.phiVanChuyenService = phiVanChuyenService;
        this.sanPhamRealtimePublisher = sanPhamRealtimePublisher;
        this.hoaDonRepository = hoaDonRepository;
        this.hoaDonChiTietRepository = hoaDonChiTietRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.vanChuyenRepository = vanChuyenRepository;
        this.sepayBank = sepayBank;
        this.sepayAccount = sepayAccount;
        this.sepayPrefix = (sepayPrefix == null || sepayPrefix.isBlank()) ? "SHOE" : sepayPrefix;
    }

    /** Tạo hóa đơn chờ giữ hàng và trả ảnh VietQR (SePay) hoặc link VNPAY Sandbox. */
    @Transactional
    public TaoMaVnPayResponse taoMa(DatHangRequest request) {
        // Mỗi khách chỉ giữ 1 phiên QR đang CHỜ: huỷ phiên chờ cũ của cùng khách trước khi tạo phiên mới
        // (tránh khoá cùng 1 voucher ở nhiều QR rồi thanh toán nhiều lần -> dùng voucher quá số lượt).
        huyPhienChoCu(request);
        String token = UUID.randomUUID().toString().replace("-", "");
        String maThanhToan = sepayPrefix + token.substring(0, 10).toUpperCase(Locale.ROOT);
        // Khóa giá sản phẩm tại thời điểm tạo mã QR: đợt giảm/giá đổi sau đó không ảnh hưởng đơn.
        ClientCheckoutItemService.KetQua checkout = checkoutItemService.chuanBi(request.sanPhams());
        Map<Integer, BigDecimal> giaKhoa = new HashMap<>();
        for (HoaDonChiTiet ct : checkout.chiTiets()) {
            giaKhoa.putIfAbsent(ct.getGiayChiTiet().getId(), ct.getGiaDonVi());
        }
        // Khóa voucher: nếu mã còn hợp lệ lúc tạo QR thì chốt tiền giảm; sau đó dù bị hủy vẫn giữ.
        BigDecimal tienGiamKhoa = tinhTienGiamKhoa(request, checkout.tongTienHang());
        ClientDatHangService.KhoaThanhToan khoa =
                new ClientDatHangService.KhoaThanhToan(giaKhoa, tienGiamKhoa);
        long soTien = tinhSoTienPhaiTra(request, checkout.tongTienHang(), tienGiamKhoa);
        // Hóa đơn chờ + chi tiết là bản ghi giữ hàng bền vững; tồn thực tế chưa bị trừ.
        DatHangResponse hoaDonCho = datHangService.taoHoaDonChoThanhToan(
                request, maThanhToan, token, khoa);
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonCho.hoaDonId())
                .orElseThrow(() -> new BusinessException("Không thể tạo phiên giữ hàng"));
        Instant hetHanLuc = hoaDon.getNgayTao().plus(TonKhoKhaDungService.THOI_GIAN_GIU_QR);

        String qrData;
        if ("VNPAY".equalsIgnoreCase(request.hinhThucThanhToan())
                && vnp_TmnCode != null && !vnp_TmnCode.isBlank() && !vnp_TmnCode.contains("${")
                && vnp_HashSecret != null && !vnp_HashSecret.isBlank() && !vnp_HashSecret.contains("${")) {
            // Sinh link thanh toán VNPAY Sandbox thật
            qrData = generateVnPayUrl(token, soTien, "127.0.0.1");
        } else {
            // Ảnh QR VietQR do SePay sinh: ngân hàng + STK + số tiền + nội dung CK.
            qrData = "https://qr.sepay.vn/img?bank=" + sepayBank
                    + "&acc=" + sepayAccount
                    + "&amount=" + soTien
                    + "&des=" + maThanhToan
                    + "&template=compact";
        }
        return new TaoMaVnPayResponse(token, qrData, maThanhToan, hetHanLuc);
    }

    /** Huỷ các phiên QR đang CHỜ của cùng khách (theo khachHangId; khách vãng lai dùng SĐT người nhận). */
    private void huyPhienChoCu(DatHangRequest request) {
        List<HoaDon> phienCu = hoaDonRepository.findOnlineQrChoTheoChuSoHuuForUpdate(
                request.khachHangId(), request.sdtNguoiNhan());
        for (HoaDon hoaDon : phienCu) {
            huyHoaDonCho(hoaDon, "Khách tạo phiên QR mới");
        }
    }

    private void huyHoaDonCho(HoaDon hoaDon, String lyDo) {
        if (hoaDon == null || hoaDon.getTrangThai() == null || hoaDon.getTrangThai() != 11) {
            return;
        }
        voucherService.hoanVoucherHoaDonCho(hoaDon);
        List<ThanhToan> thanhToans = thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId());
        if (!thanhToans.isEmpty()) {
            thanhToanRepository.deleteAll(thanhToans);
        }
        vanChuyenRepository.findByHoaDonId(hoaDon.getId())
                .ifPresent(vanChuyenRepository::delete);
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
        if (!chiTiets.isEmpty()) {
            hoaDonChiTietRepository.deleteAll(chiTiets);
        }
        hoaDonRepository.delete(hoaDon);
        sanPhamRealtimePublisher.phatSauCommit("QR_GIAI_PHONG_HANG");
    }

    /** Tiền giảm voucher đã khóa lúc tạo QR: mã còn hợp lệ -> chốt tiền giảm; không hợp lệ -> 0. */
    private BigDecimal tinhTienGiamKhoa(DatHangRequest request, BigDecimal tongTienHang) {
        if (request.maPhieuGiamGia() == null || request.maPhieuGiamGia().isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return voucherService
                    .kiemTra(request.khachHangId(), request.maPhieuGiamGia(), tongTienHang)
                    .tienGiam();
        } catch (RuntimeException ignored) {
            return BigDecimal.ZERO; // mã không hợp lệ lúc tạo QR -> không giảm (đúng quy tắc "mất")
        }
    }

    /** Số tiền khách phải chuyển = tổng tiền hàng (đã khóa) - giảm voucher (đã khóa) + phí ship. */
    private long tinhSoTienPhaiTra(
            DatHangRequest request, BigDecimal tongTienHangDaKhoa, BigDecimal tienGiamKhoa) {
        BigDecimal tong = tongTienHangDaKhoa
                .subtract(tienGiamKhoa == null ? BigDecimal.ZERO : tienGiamKhoa)
                .max(BigDecimal.ZERO);

        // Cộng phí vận chuyển GHN theo địa chỉ nhận (cùng cách tính với lúc đặt hàng).
        BigDecimal phiShip = phiVanChuyenService.tinhPhi(new TinhPhiShipRequest(
                request.khachHangId(),
                request.sanPhams(),
                request.diaChiGiaoHang()
        )).phiVanChuyen();
        if (phiShip != null && phiShip.signum() > 0) {
            tong = tong.add(phiShip);
        }
        return tong.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /** Xác nhận thanh toán idempotent từ phiên đã lưu trong database. */
    @Transactional
    public String xacNhan(String token) {
        return datHangService.xacNhanHoaDonChoThanhToan(token);
    }

    @Scheduled(fixedRate = 60_000L)
    @Transactional
    public void donPhien() {
        Instant moc = Instant.now().minus(TonKhoKhaDungService.THOI_GIAN_GIU_QR);
        for (HoaDon hoaDon : hoaDonRepository.findExpiredOnlineQrForUpdate(moc)) {
            huyHoaDonCho(hoaDon, "Phiên QR hết hạn sau 5 phút");
        }
    }

    /**
     * SePay webhook gọi khi có tiền vào: tìm phiên theo nội dung chuyển khoản, kiểm tra
     * số tiền rồi tạo đơn. Trả mã hóa đơn nếu khớp; null nếu không khớp hoặc chuyển thiếu.
     */
    @Transactional
    public String xacNhanTheoChuyenKhoan(String noiDung, long soTien) {
        if (noiDung == null || noiDung.isBlank()) {
            return null;
        }
        String content = noiDung.toUpperCase(Locale.ROOT);
        for (ThanhToan thanhToan : thanhToanRepository.findByTrangThaiAndLoaiGiaoDich(0, 1)) {
            if (thanhToan.getMaGiaoDich() != null
                    && content.contains(thanhToan.getMaGiaoDich().toUpperCase(Locale.ROOT))) {
                long soTienKyVong = thanhToan.getSoTien() == null
                        ? 0L : thanhToan.getSoTien().setScale(0, RoundingMode.HALF_UP).longValue();
                if (soTienKyVong > 0 && soTien < soTienKyVong) {
                    return null; // chuyển thiếu tiền -> không tạo đơn
                }
                return xacNhan(thanhToan.getNoiDungCk());
            }
        }
        return null;
    }

    /** FE poll trạng thái phiên thanh toán + mã hóa đơn nếu đã tạo. */
    @Transactional(readOnly = true)
    public Map<String, String> trangThai(String token) {
        Map<String, String> result = new HashMap<>();
        ThanhToan thanhToan = thanhToanRepository
                .findByNoiDungCkAndLoaiGiaoDich(token, 1).orElse(null);
        if (thanhToan == null) {
            result.put("trangThai", TRANG_THAI_KHONG_TON_TAI);
            return result;
        }
        HoaDon hoaDon = thanhToan.getHoaDon();
        Instant hetHanLuc = hoaDon.getNgayTao().plus(TonKhoKhaDungService.THOI_GIAN_GIU_QR);
        result.put("hetHanLuc", hetHanLuc.toString());
        if (thanhToan.getTrangThai() != null && thanhToan.getTrangThai() == 1) {
            result.put("trangThai", TRANG_THAI_DA_THANH_TOAN);
            result.put("maHoaDon", hoaDon.getMa());
        } else if (hoaDon.getTrangThai() != null && hoaDon.getTrangThai() == 11
                && !hetHanLuc.isBefore(Instant.now())) {
            result.put("trangThai", TRANG_THAI_CHO);
        } else {
            result.put("trangThai", "HET_HAN");
        }
        return result;
    }

    @Transactional
    public void huy(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String key = token.trim();
        ThanhToan thamChieu = thanhToanRepository
                .findByNoiDungCkAndLoaiGiaoDich(key, 1)
                .or(() -> thanhToanRepository.findByMaGiaoDichForUpdate(key))
                .orElse(null);
        if (thamChieu == null) {
            return;
        }
        HoaDon hoaDon = hoaDonRepository.findDetailByIdForUpdate(thamChieu.getHoaDon().getId())
                .orElse(null);
        if (hoaDon != null && hoaDon.getTrangThai() != null && hoaDon.getTrangThai() == 11) {
            huyHoaDonCho(hoaDon, "Khách hủy phiên QR");
        }
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.US_ASCII.toString());
        } catch (Exception ex) {
            return "";
        }
    }

    public static String hmactSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                return "";
            }
            final javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            final javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public String generateVnPayUrl(String token, long amount, String clientIp) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don hang " + token.substring(0, 8);
        String vnp_OrderType = "other";
        String vnp_TxnRef = token;
        
        java.time.ZonedDateTime nowGmt7 = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = nowGmt7.format(formatter);
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode.trim());
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl != null ? vnp_ReturnUrl.trim() : "");
        vnp_Params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        java.time.ZonedDateTime expireGmt7 = nowGmt7.plusMinutes(5);
        String vnp_ExpireDate = expireGmt7.format(formatter);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        List<String> hashParts = new ArrayList<>();
        List<String> queryParts = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedKey = urlEncode(fieldName);
                String encodedValue = urlEncode(fieldValue);
                hashParts.add(fieldName + "=" + encodedValue);
                queryParts.add(encodedKey + "=" + encodedValue);
            }
        }
        
        String hashData = String.join("&", hashParts);
        String queryUrl = String.join("&", queryParts);
        String vnp_SecureHash = hmactSHA512(vnp_HashSecret.trim(), hashData);
        
        System.out.println("=== VNPAY DEBUG REQUEST ===");
        System.out.println("vnp_TmnCode: " + vnp_TmnCode.trim());
        System.out.println("vnp_HashSecret: " + vnp_HashSecret.trim());
        System.out.println("hashData: " + hashData);
        System.out.println("vnp_SecureHash: " + vnp_SecureHash);
        System.out.println("==========================");
        
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnp_PayUrl + "?" + queryUrl;
    }

    public synchronized String xacNhanCallback(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            throw new BusinessException("Thiếu chữ ký bảo mật");
        }
        
        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");
        
        List<String> fieldNames = new ArrayList<>(signParams.keySet());
        Collections.sort(fieldNames);
        List<String> hashParts = new ArrayList<>();
        for (String fieldName : fieldNames) {
            if (fieldName != null && fieldName.startsWith("vnp_")) {
                String fieldValue = signParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashParts.add(fieldName + "=" + urlEncode(fieldValue));
                }
            }
        }
        
        String hashData = String.join("&", hashParts);
        String calculatedHash = hmactSHA512(vnp_HashSecret.trim(), hashData);
        
        try {
            java.io.FileWriter fw = new java.io.FileWriter("D:\\SportShoe-Store\\vnpay_debug.txt", true);
            java.io.PrintWriter pw = new java.io.PrintWriter(fw);
            pw.println("=== NEW CALLBACK TIME: " + new java.util.Date() + " ===");
            pw.println("Params: " + params);
            pw.println("HashData: " + hashData);
            pw.println("Secret: " + vnp_HashSecret);
            pw.println("Calculated Hash: " + calculatedHash);
            pw.println("Received Hash: " + vnp_SecureHash);
            pw.println("========================================\n");
            pw.close();
        } catch (Exception ignored) {}
        
        System.out.println("=== VNPAY DEBUG CALLBACK ===");
        System.out.println("vnp_SecureHash received: " + vnp_SecureHash);
        System.out.println("hashData String generated: " + hashData);
        System.out.println("vnp_SecureHash calculated: " + calculatedHash);
        System.out.println("===========================");
        
        if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
            throw new BusinessException("Chữ ký bảo mật không hợp lệ");
        }
        
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        
        if ("00".equals(responseCode)) {
            return xacNhan(txnRef);
        } else {
            throw new BusinessException("Thanh toán không thành công. Mã lỗi: " + responseCode);
        }
    }
}
