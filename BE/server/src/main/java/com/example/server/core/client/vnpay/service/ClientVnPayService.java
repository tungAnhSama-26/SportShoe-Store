package com.example.server.core.client.vnpay.service;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.dathang.service.ClientCheckoutItemService;
import com.example.server.core.client.dathang.service.ClientDatHangService;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.core.client.vnpay.dto.TaoMaVnPayResponse;
import com.example.server.core.client.voucher.service.ClientVoucherService;
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
        volatile String trangThai = TRANG_THAI_CHO;
        volatile String maHoaDon;

        Phien(DatHangRequest request, String maThanhToan, long soTienKyVong) {
            this.request = request;
            this.maThanhToan = maThanhToan;
            this.soTienKyVong = soTienKyVong;
        }
    }

    /** Tạo phiên thanh toán + ảnh QR VietQR (SePay). Chưa tạo đơn. */
    public TaoMaVnPayResponse taoMa(DatHangRequest request) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String maThanhToan = sepayPrefix + token.substring(0, 10).toUpperCase(Locale.ROOT);
        long soTien = tinhSoTienPhaiTra(request);
        phienMap.put(token, new Phien(request, maThanhToan, soTien));

        // Ảnh QR VietQR do SePay sinh: ngân hàng + STK + số tiền + nội dung CK.
        String qrData = "https://qr.sepay.vn/img?bank=" + sepayBank
                + "&acc=" + sepayAccount
                + "&amount=" + soTien
                + "&des=" + maThanhToan
                + "&template=compact";
        return new TaoMaVnPayResponse(token, qrData, maThanhToan);
    }

    /** Số tiền khách phải chuyển = tổng tiền hàng - giảm giá voucher (nếu có) + phí vận chuyển. */
    private long tinhSoTienPhaiTra(DatHangRequest request) {
        BigDecimal tong = checkoutItemService.chuanBi(request.sanPhams()).tongTienHang();
        if (request.maPhieuGiamGia() != null && !request.maPhieuGiamGia().isBlank()) {
            try {
                BigDecimal giam = voucherService
                        .kiemTra(request.khachHangId(), request.maPhieuGiamGia(), tong)
                        .tienGiam();
                tong = tong.subtract(giam).max(BigDecimal.ZERO);
            } catch (RuntimeException ignored) {
                // Mã không hợp lệ -> giữ nguyên tổng (đặt hàng sẽ xử lý lại voucher sau).
            }
        }

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
            DatHangResponse ketQua = datHangService.datHang(phien.request, phien.maThanhToan);
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
}
