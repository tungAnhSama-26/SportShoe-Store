package com.example.server.core.client.voucher.service;

import com.example.server.core.admin.banHangTaiQuay.service.usecase.BanHangTaiQuayPricingUseCase;
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
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Áp mã giảm giá (phiếu giảm giá) cho khách hàng. Tái dùng cách tính tiền giảm của POS.
 */
@Service
public class ClientVoucherService {

    private static final int TRANG_THAI_PHIEU_HOAT_DONG = 1;
    private static final int LOAI_PHIEU_CA_NHAN = 2;
    private static final int PHIEU_KH_CHUA_DUNG = 1;
    private static final int PHIEU_KH_DA_DUNG = 2;

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
     * Áp mã vào hóa đơn khi đặt hàng: gán phiếu, trừ lượt, đánh dấu phiếu cá nhân đã dùng.
     * @return số tiền được giảm.
     */
    @Transactional
    public BigDecimal apDungVaoHoaDon(HoaDon hoaDon, String maPhieu, KhachHang khachHang, BigDecimal tongTienHang) {
        PhieuGiamGia phieu = timVaValidate(maPhieu, khachHang != null ? khachHang.getId() : null, tongTienHang, true);
        BigDecimal tienGiam = pricingUseCase.tinhSoTienGiam(phieu, tongTienHang);

        hoaDon.setPhieuGiamGia(phieu);
        phieu.setSoLuong(phieu.getSoLuong() - 1);
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

    private PhieuGiamGia timVaValidate(String maPhieu, UUID khachHangId, BigDecimal tongTienHang, boolean kiemTraLuot) {
        if (maPhieu == null || maPhieu.isBlank()) {
            throw new BusinessException("Vui lòng nhập mã giảm giá");
        }
        PhieuGiamGia phieu = phieuGiamGiaRepository.findByMaIgnoreCase(maPhieu.trim())
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
        if (kiemTraLuot && (phieu.getSoLuong() == null || phieu.getSoLuong() <= 0)) {
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
