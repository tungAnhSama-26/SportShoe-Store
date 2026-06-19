package com.example.server.core.client.voucher.service;

import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayPricingUseCase;
import com.example.server.core.client.voucher.dto.VoucherKhaDungResponse;
import com.example.server.core.client.voucher.dto.VoucherResponse;
import com.example.server.entity.HoaDon;
import com.example.server.entity.KhachHang;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.PhieuGiamGiaRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Áp mã giảm giá (phiếu giảm giá) cho khách hàng. Tái dùng cách tính tiền giảm của POS.
 */
@Service
public class ClientVoucherService {

    private static final int TRANG_THAI_PHIEU_HOAT_DONG = 1;
    private static final int LOAI_PHIEU_TOAN_SAN = 1;
    private static final int LOAI_PHIEU_CA_NHAN = 2;
    private static final int PHIEU_KH_CHUA_DUNG = 1;
    private static final int PHIEU_KH_DA_DUNG = 2;
    private static final int KIEU_GIAM_PHAN_TRAM = 1;
    private static final int KIEU_GIAM_TIEN_MAT = 2;

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanHangTaiQuayPricingUseCase pricingUseCase;

    public ClientVoucherService(
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository,
            KhachHangRepository khachHangRepository,
            BanHangTaiQuayPricingUseCase pricingUseCase
    ) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phieuGiamGiaKhachHangRepository = phieuGiamGiaKhachHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.pricingUseCase = pricingUseCase;
    }

    /** Kiểm tra mã + tính tiền giảm trên tổng tiền hàng (không thay đổi dữ liệu). */
    @Transactional(readOnly = true)
    public VoucherResponse kiemTra(UUID khachHangId, String maPhieu, BigDecimal tongTienHang) {
        PhieuGiamGia phieu = timVaValidate(maPhieu, khachHangId, tongTienHang, false);
        BigDecimal tienGiam = pricingUseCase.tinhSoTienGiam(phieu, tongTienHang);
        BigDecimal sauGiam = tongTienHang.subtract(tienGiam).max(BigDecimal.ZERO);
        return new VoucherResponse(phieu.getId(), phieu.getMa(), phieu.getTen(), tienGiam, tongTienHang, sauGiam);
    }

    /**
     * Liệt kê voucher khách có thể dùng cho giỏ hiện tại: voucher cá nhân được gửi riêng
     * cho khách (chưa dùng) + voucher toàn sàn đang hoạt động. Không thay đổi dữ liệu.
     */
    @Transactional(readOnly = true)
    public List<VoucherKhaDungResponse> layVoucherKhaDung(UUID khachHangId, BigDecimal tongTienHang) {
        BigDecimal tong = tongTienHang == null ? BigDecimal.ZERO : tongTienHang;
        Instant now = Instant.now();
        Map<Integer, VoucherKhaDungResponse> ketQua = new LinkedHashMap<>();

        // Voucher cá nhân (gửi riêng cho khách) - đưa lên trước.
        if (khachHangId != null) {
            for (PhieuGiamGiaKhachHang pggh : phieuGiamGiaKhachHangRepository.findKhaDungByKhachHang(khachHangId)) {
                themNeuHieuLuc(ketQua, pggh.getPhieuGiamGia(), tong, now, true);
            }
        }
        // Voucher toàn sàn (công khai) - ai cũng dùng được.
        for (PhieuGiamGia phieu : phieuGiamGiaRepository
                .findByLoaiPhieuAndTrangThai(LOAI_PHIEU_TOAN_SAN, TRANG_THAI_PHIEU_HOAT_DONG)) {
            themNeuHieuLuc(ketQua, phieu, tong, now, false);
        }

        // Đủ điều kiện áp lên trước, rồi tới mức giảm cao hơn.
        return ketQua.values().stream()
                .sorted(Comparator.comparing(VoucherKhaDungResponse::apDung).reversed()
                        .thenComparing(Comparator.comparing(VoucherKhaDungResponse::tienGiam).reversed()))
                .toList();
    }

    private void themNeuHieuLuc(
            Map<Integer, VoucherKhaDungResponse> ketQua,
            PhieuGiamGia phieu,
            BigDecimal tong,
            Instant now,
            boolean rieng
    ) {
        if (phieu == null || ketQua.containsKey(phieu.getId()) || !phieuConHieuLuc(phieu, now)) {
            return;
        }
        BigDecimal tienGiam = tong.signum() > 0 ? pricingUseCase.tinhSoTienGiam(phieu, tong) : BigDecimal.ZERO;
        boolean apDung = phieu.getGiaTriToiThieu() == null || tong.compareTo(phieu.getGiaTriToiThieu()) >= 0;
        ketQua.put(phieu.getId(), new VoucherKhaDungResponse(
                phieu.getId(), phieu.getMa(), phieu.getTen(),
                phieu.getLoai(), phieu.getGiaTri(), phieu.getGiamToiDa(), phieu.getGiaTriToiThieu(),
                tienGiam, rieng, apDung, phieu.getNgayKetThuc()));
    }

    /** Phiếu còn hiệu lực: trạng thái hoạt động, đúng thời gian, còn lượt, kiểu giảm hợp lệ. */
    private boolean phieuConHieuLuc(PhieuGiamGia phieu, Instant now) {
        if (phieu.getTrangThai() == null || phieu.getTrangThai() != TRANG_THAI_PHIEU_HOAT_DONG) {
            return false;
        }
        if (phieu.getLoai() == null
                || (phieu.getLoai() != KIEU_GIAM_PHAN_TRAM && phieu.getLoai() != KIEU_GIAM_TIEN_MAT)) {
            return false;
        }
        if (phieu.getNgayBatDau() != null && now.isBefore(phieu.getNgayBatDau())) {
            return false;
        }
        if (phieu.getNgayKetThuc() != null && now.isAfter(phieu.getNgayKetThuc())) {
            return false;
        }
        int tongSoLuong = phieu.getSoLuong() == null ? 0 : phieu.getSoLuong();
        int daDung = phieu.getSoLuongDaDung() == null ? 0 : phieu.getSoLuongDaDung();
        return tongSoLuong == 999999 || daDung < tongSoLuong;
    }

    /**
     * Áp mã vào hóa đơn khi đặt hàng: gán phiếu, trừ lượt, đánh dấu phiếu cá nhân đã dùng.
     * @return số tiền được giảm.
     */
    @Transactional
    public BigDecimal apDungVaoHoaDon(HoaDon hoaDon, String maPhieu, KhachHang khachHang, BigDecimal tongTienHang) {
        PhieuGiamGia phieu = timVaValidate(maPhieu, khachHang != null ? khachHang.getId() : null, tongTienHang, true);
        BigDecimal tienGiam = pricingUseCase.tinhSoTienGiam(phieu, tongTienHang);

        hoaDon.setPhieuGiamGia(phieu);
        phieu.setSoLuongDaDung((phieu.getSoLuongDaDung() == null ? 0 : phieu.getSoLuongDaDung()) + 1);
        phieuGiamGiaRepository.save(phieu);

        if (phieu.getLoaiPhieu() != null && phieu.getLoaiPhieu() == LOAI_PHIEU_CA_NHAN && khachHang != null) {
            phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieu.getId(), khachHang.getId())
                    .ifPresent(pggh -> {
                        pggh.setTrangThai(PHIEU_KH_DA_DUNG);
                        pggh.setNgaySuDung(Instant.now());
                        phieuGiamGiaKhachHangRepository.save(pggh);
                    });
        }
        return tienGiam;
    }

    /**
     * Áp voucher ĐÃ KHÓA (đã chốt lúc tạo mã QR VNPAY/VietQR) vào hóa đơn khi tạo đơn:
     * gán phiếu, trừ lượt, đánh dấu phiếu cá nhân đã dùng — KHÔNG kiểm tra lại hiệu lực/lượt
     * (vì đã khóa lúc mã còn hợp lệ; dù sau đó bị hủy kích hoạt vẫn giữ cho khách).
     *
     * @param tienGiamDaKhoa tiền giảm đã chốt lúc tạo mã QR.
     * @return chính {@code tienGiamDaKhoa} (hoặc 0 nếu phiếu đã bị xóa khỏi hệ thống).
     */
    @Transactional
    public BigDecimal apDungVoucherDaKhoa(
            HoaDon hoaDon, String maPhieu, KhachHang khachHang, BigDecimal tienGiamDaKhoa) {
        if (maPhieu == null || maPhieu.isBlank()) {
            return BigDecimal.ZERO;
        }
        PhieuGiamGia phieu = phieuGiamGiaRepository.findByMaIgnoreCaseForUpdate(maPhieu.trim())
                .orElse(null);
        if (phieu == null) {
            return BigDecimal.ZERO; // phiếu đã bị xóa -> không áp, nhưng không chặn tạo đơn
        }
        hoaDon.setPhieuGiamGia(phieu);
        phieu.setSoLuongDaDung((phieu.getSoLuongDaDung() == null ? 0 : phieu.getSoLuongDaDung()) + 1);
        phieuGiamGiaRepository.save(phieu);

        if (phieu.getLoaiPhieu() != null && phieu.getLoaiPhieu() == LOAI_PHIEU_CA_NHAN && khachHang != null) {
            phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieu.getId(), khachHang.getId())
                    .ifPresent(pggh -> {
                        pggh.setTrangThai(PHIEU_KH_DA_DUNG);
                        pggh.setNgaySuDung(Instant.now());
                        phieuGiamGiaKhachHangRepository.save(pggh);
                    });
        }
        return tienGiamDaKhoa == null ? BigDecimal.ZERO : tienGiamDaKhoa;
    }

    private PhieuGiamGia timVaValidate(String maPhieu, UUID khachHangId, BigDecimal tongTienHang, boolean kiemTraLuot) {
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new BusinessException("Vui lòng nhập mã giảm giá");
        }
        PhieuGiamGia phieu = (kiemTraLuot
                ? phieuGiamGiaRepository.findByMaIgnoreCaseForUpdate(maPhieu.trim())
                : phieuGiamGiaRepository.findByMaIgnoreCase(maPhieu.trim()))
                .orElseThrow(() -> new BusinessException("Mã giảm giá không tồn tại"));

        if (phieu.getTrangThai() == null || phieu.getTrangThai() != TRANG_THAI_PHIEU_HOAT_DONG) {
            throw new BusinessException("Mã giảm giá không còn hiệu lực");
        }
        Instant now = Instant.now();
        if (phieu.getNgayBatDau() != null && now.isBefore(phieu.getNgayBatDau())) {
            throw new BusinessException("Mã giảm giá chưa đến thời gian áp dụng");
        }
        if (phieu.getNgayKetThuc() != null && now.isAfter(phieu.getNgayKetThuc())) {
            throw new BusinessException("Mã giảm giá đã hết hạn sử dụng");
        }
        if (phieu.getGiaTriToiThieu() != null && tongTienHang.compareTo(phieu.getGiaTriToiThieu()) < 0) {
            throw new BusinessException("Đơn hàng cần tối thiểu " + dinhDangTien(phieu.getGiaTriToiThieu()) + " để dùng mã này");
        }
        int tongSoLuong = phieu.getSoLuong() == null ? 0 : phieu.getSoLuong();
        int daDung = phieu.getSoLuongDaDung() == null ? 0 : phieu.getSoLuongDaDung();
        if (kiemTraLuot && tongSoLuong != 999999 && daDung >= tongSoLuong) {
            throw new BusinessException("Mã giảm giá đã hết lượt sử dụng");
        }

        if (phieu.getLoaiPhieu() != null && phieu.getLoaiPhieu() == LOAI_PHIEU_CA_NHAN) {
            if (khachHangId == null || khachHangRepository.findById(khachHangId).isEmpty()) {
                throw new BusinessException("Mã giảm giá này chỉ dành cho thành viên");
            }
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieu.getId(), khachHangId)
                    .orElseThrow(() -> new BusinessException("Bạn không sở hữu mã giảm giá này"));
            if (pggh.getTrangThai() != null && pggh.getTrangThai() != PHIEU_KH_CHUA_DUNG) {
                throw new BusinessException("Bạn đã sử dụng mã giảm giá này rồi");
            }
        }
        return phieu;
    }

    private String dinhDangTien(BigDecimal value) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(value) + "đ";
    }
}
