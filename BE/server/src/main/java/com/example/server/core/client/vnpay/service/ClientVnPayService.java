package com.example.server.core.client.vnpay.service;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.dathang.service.ClientCheckoutItemService;
import com.example.server.core.client.dathang.service.ClientDatHangService;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.core.client.vnpay.dto.TaoMaVnPayResponse;
import com.example.server.core.client.voucher.service.ClientVoucherService;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

/**
 * Thanh toán bằng chuyển khoản thật qua VietQR + SePay.
 *
 * <p>Luồng: tạo phiên (mã QR VietQR với số tiền + nội dung CK duy nhất) -&gt; khách
 * chuyển khoản thật -&gt; SePay phát hiện tiền vào, gọi webhook -&gt; khớp nội dung +
 * số tiền -&gt; mới tạo đơn. Phiên lưu tạm trong bộ nhớ. Tồn kho/đơn chỉ thay đổi khi
 * thanh toán được xác nhận (tái dùng {@link ClientDatHangService}).</p>
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
    private final Map<String, Phien> phienMap = new ConcurrentHashMap<>();

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
            @Value("${sepay.bank:}") String sepayBank,
            @Value("${sepay.account-number:}") String sepayAccount,
            @Value("${sepay.prefix:SHOE}") String sepayPrefix
    ) {
        this.datHangService = datHangService;
        this.checkoutItemService = checkoutItemService;
        this.voucherService = voucherService;
        this.phiVanChuyenService = phiVanChuyenService;
        this.sepayBank = sepayBank;
        this.sepayAccount = sepayAccount;
        this.sepayPrefix = (sepayPrefix == null || sepayPrefix.isBlank()) ? "SHOE" : sepayPrefix;
    }

    private static class Phien {
        final DatHangRequest request;
        final String maThanhToan;   // nội dung chuyển khoản, dùng để khớp đơn + lưu làm mã giao dịch
        final long soTienKyVong;    // số tiền khách phải chuyển
        final ClientDatHangService.KhoaThanhToan khoa; // giá SP + tiền giảm voucher đã khóa lúc tạo QR
        volatile String trangThai = TRANG_THAI_CHO;
        volatile String maHoaDon;

        Phien(DatHangRequest request, String maThanhToan, long soTienKyVong,
                ClientDatHangService.KhoaThanhToan khoa) {
            this.request = request;
            this.maThanhToan = maThanhToan;
            this.soTienKyVong = soTienKyVong;
            this.khoa = khoa;
        }
    }

    /** Tạo phiên thanh toán + ảnh QR VietQR (SePay) hoặc link VNPAY Sandbox. Chưa tạo đơn. */
    public TaoMaVnPayResponse taoMa(DatHangRequest request) {
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
        phienMap.put(token, new Phien(request, maThanhToan, soTien, khoa));

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
        return new TaoMaVnPayResponse(token, qrData, maThanhToan);
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
                request.tinhThanh(),
                request.quanHuyen(),
                request.phuongXa(),
                request.diaChiCuThe(),
                request.toDistrictId(),
                request.toWardCode()
        )).phiVanChuyen();
        if (phiShip != null && phiShip.signum() > 0) {
            tong = tong.add(phiShip);
        }
        return tong.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /** Đã xác nhận thanh toán: tạo đơn (1 lần, lưu mã giao dịch) và đánh dấu đã thanh toán. */
    public synchronized String xacNhan(String token) {
        Phien phien = phienMap.get(token);
        if (phien == null) {
            throw new BusinessException("Mã thanh toán không tồn tại hoặc đã hết hạn");
        }
        if (!TRANG_THAI_DA_THANH_TOAN.equals(phien.trangThai)) {
            // Tạo đơn theo GIÁ + VOUCHER ĐÃ KHÓA lúc tạo mã QR, không tính lại theo trạng thái hiện tại.
            DatHangResponse ketQua = datHangService.datHang(
                    phien.request, phien.maThanhToan, phien.khoa);
            phien.maHoaDon = ketQua.maHoaDon();
            phien.trangThai = TRANG_THAI_DA_THANH_TOAN;
        }
        return phien.maHoaDon;
    }

    /**
     * SePay webhook gọi khi có tiền vào: tìm phiên theo nội dung chuyển khoản, kiểm tra
     * số tiền rồi tạo đơn. Trả mã hóa đơn nếu khớp; null nếu không khớp hoặc chuyển thiếu.
     */
    public synchronized String xacNhanTheoChuyenKhoan(String noiDung, long soTien) {
        if (noiDung == null || noiDung.isBlank()) {
            return null;
        }
        String content = noiDung.toUpperCase(Locale.ROOT);
        for (Map.Entry<String, Phien> entry : phienMap.entrySet()) {
            Phien phien = entry.getValue();
            if (content.contains(phien.maThanhToan)) {
                if (phien.soTienKyVong > 0 && soTien < phien.soTienKyVong) {
                    return null; // chuyển thiếu tiền -> không tạo đơn
                }
                return xacNhan(entry.getKey());
            }
        }
        return null;
    }

    /** FE poll trạng thái phiên thanh toán + mã hóa đơn nếu đã tạo. */
    public Map<String, String> trangThai(String token) {
        Phien phien = phienMap.get(token);
        Map<String, String> result = new HashMap<>();
        if (phien == null) {
            result.put("trangThai", TRANG_THAI_KHONG_TON_TAI);
            return result;
        }
        result.put("trangThai", phien.trangThai);
        if (phien.maHoaDon != null) {
            result.put("maHoaDon", phien.maHoaDon);
        }
        return result;
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.toString()).replace("+", "%20");
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
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        java.time.ZonedDateTime expireGmt7 = nowGmt7.plusMinutes(15);
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
        String vnp_SecureHash = hmactSHA512(vnp_HashSecret, hashData);
        
        System.out.println("=== VNPAY DEBUG REQUEST ===");
        System.out.println("vnp_TmnCode: " + vnp_TmnCode);
        System.out.println("vnp_HashSecret: " + vnp_HashSecret);
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
        String calculatedHash = hmactSHA512(vnp_HashSecret, hashData);
        
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
