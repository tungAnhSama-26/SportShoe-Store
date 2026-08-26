package com.example.server.core.admin.banHangTaiQuay.service;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.TRANG_THAI_HOA_DON_CHO_TAI_QUAY;
import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.TRANG_THAI_HOA_DON_HUY;

import com.example.server.core.admin.banHangTaiQuay.dto.response.QrChuyenKhoanResponse;
import com.example.server.core.client.vnpay.service.ClientVnPayService;
import com.example.server.entity.HoaDon;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.HoaDonRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mã QR chuyển khoản cho màn bán hàng tại quầy.
 *
 * <p>Trước đây QR được dựng thẳng ở FE với số tài khoản viết cứng nên tiền không về đúng
 * tài khoản đã cấu hình cho SePay, webhook không bao giờ khớp và thu ngân buộc phải bấm
 * "Đã thanh toán" bằng tay. Nay QR dựng từ cấu hình {@code sepay.*} ở backend, nội dung
 * chuyển khoản là {@code <prefix><mã hóa đơn>} — đúng thứ mà
 * {@link ThucThiThanhToanTaiQuayService#xacNhanThanhToanSePay(String, long)} dùng để đối chiếu.</p>
 *
 * <p>Khi tiền về, webhook SePay tự ghi bản ghi thanh toán và chuyển trạng thái hóa đơn.
 * Màn POS chỉ cần poll {@link #trangThai(Integer)} để biết lúc nào đóng mã QR.</p>
 *
 * <p>Mã sống tối đa {@link ClientVnPayService#THOI_GIAN_HIEU_LUC_QR} (5 phút) giống hệt QR mua
 * online; hết hạn thì thu ngân sinh mã mới. Hóa đơn chờ vẫn giữ nguyên, và nếu khách chuyển
 * muộn thì webhook vẫn hoàn tất hóa đơn — tiền đã vào tài khoản thì không được bỏ.</p>
 */
@Service
public class QrChuyenKhoanTaiQuayService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonTaiQuayService invoiceUseCase;
    private final String sepayBank;
    private final String sepayAccount;
    private final String sepayPrefix;

    public QrChuyenKhoanTaiQuayService(
            HoaDonRepository hoaDonRepository,
            HoaDonTaiQuayService invoiceUseCase,
            @Value("${sepay.bank:}") String sepayBank,
            @Value("${sepay.account-number:}") String sepayAccount,
            @Value("${sepay.prefix:SHOE}") String sepayPrefix
    ) {
        this.hoaDonRepository = hoaDonRepository;
        this.invoiceUseCase = invoiceUseCase;
        this.sepayBank = sepayBank;
        this.sepayAccount = sepayAccount;
        this.sepayPrefix = (sepayPrefix == null || sepayPrefix.isBlank()) ? "SHOE" : sepayPrefix;
    }

    /**
     * Dựng mã QR cho hóa đơn chờ tại quầy.
     *
     * @param soTien số tiền cần chuyển; để trống thì lấy tổng tiền phải trả của hóa đơn
     *               (hình thức kết hợp truyền riêng phần chuyển khoản).
     */
    @Transactional(readOnly = true)
    public QrChuyenKhoanResponse taoQr(Integer hoaDonId, BigDecimal soTien) {
        HoaDon hoaDon = layHoaDonCho(hoaDonId);
        invoiceUseCase.kiemTraKhachHangHoatDong(hoaDon.getKhachHang());

        BigDecimal soTienCanTra = soTien != null ? soTien : hoaDon.getTongTienThanhToan();
        if (soTienCanTra == null || soTienCanTra.signum() <= 0) {
            throw new BusinessException("Số tiền chuyển khoản phải lớn hơn 0");
        }

        if (sepayBank == null || sepayBank.isBlank() || sepayAccount == null || sepayAccount.isBlank()) {
            throw new BusinessException("Chưa cấu hình tài khoản nhận chuyển khoản. Vui lòng liên hệ quản trị viên.");
        }

        long soTienTron = soTienCanTra.setScale(0, RoundingMode.HALF_UP).longValue();
        String noiDungCk = taoNoiDungCk(hoaDon.getMa());

        return new QrChuyenKhoanResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                noiDungCk,
                dungUrlQr(soTienTron, noiDungCk),
                soTienTron,
                false,
                hoaDon.getTrangThai(),
                Instant.now().plus(ClientVnPayService.THOI_GIAN_HIEU_LUC_QR)
        );
    }

    /**
     * Màn POS poll: hóa đơn đã được webhook SePay ghi nhận thanh toán chưa.
     * Hóa đơn rời khỏi trạng thái chờ tại quầy (và không phải bị hủy) nghĩa là đã thanh toán.
     */
    @Transactional(readOnly = true)
    public QrChuyenKhoanResponse trangThai(Integer hoaDonId) {
        if (hoaDonId == null) {
            throw new BusinessException("Chưa chọn hóa đơn chờ để kiểm tra chuyển khoản");
        }
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));

        Integer trangThai = hoaDon.getTrangThai();
        boolean conCho = Objects.equals(trangThai, TRANG_THAI_HOA_DON_CHO_TAI_QUAY);
        boolean daHuy = Objects.equals(trangThai, TRANG_THAI_HOA_DON_HUY);
        BigDecimal tongTien = hoaDon.getTongTienThanhToan();
        long soTien = tongTien == null ? 0L : tongTien.setScale(0, RoundingMode.HALF_UP).longValue();
        String noiDungCk = taoNoiDungCk(hoaDon.getMa());

        return new QrChuyenKhoanResponse(
                hoaDon.getId(),
                hoaDon.getMa(),
                noiDungCk,
                dungUrlQr(soTien, noiDungCk),
                soTien,
                !conCho && !daHuy,
                trangThai,
                null
        );
    }

    private HoaDon layHoaDonCho(Integer hoaDonId) {
        if (hoaDonId == null) {
            throw new BusinessException("Chưa chọn hóa đơn chờ để tạo mã QR");
        }
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn không tồn tại"));
        if (!Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_HOA_DON_CHO_TAI_QUAY)) {
            throw new BusinessException("Chỉ tạo được mã QR cho hóa đơn chờ tại quầy");
        }
        return hoaDon;
    }

    private String taoNoiDungCk(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.isBlank()) {
            return null;
        }
        return sepayPrefix + maHoaDon.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String dungUrlQr(long soTien, String noiDungCk) {
        if (noiDungCk == null) {
            return null;
        }
        return "https://qr.sepay.vn/img?bank=" + sepayBank
                + "&acc=" + sepayAccount
                + "&amount=" + soTien
                + "&des=" + noiDungCk
                + "&template=compact";
    }
}
