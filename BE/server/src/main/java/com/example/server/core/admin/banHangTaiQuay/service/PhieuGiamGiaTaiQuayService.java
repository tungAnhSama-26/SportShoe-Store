package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ApDungPhieuGiamGiaRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.PhieuGiamGiaTaiQuayResponse;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.KhachHang;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.PhieuGiamGiaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.*;

@Component
public class PhieuGiamGiaTaiQuayService {
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final GiaCaTaiQuayService pricingUseCase;

    public PhieuGiamGiaTaiQuayService(
            PhieuGiamGiaRepository phieuGiamGiaRepository,
            PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository,
            GiaCaTaiQuayService pricingUseCase) {
        this.phieuGiamGiaRepository = phieuGiamGiaRepository;
        this.phieuGiamGiaKhachHangRepository = phieuGiamGiaKhachHangRepository;
        this.pricingUseCase = pricingUseCase;
    }

    public List<PhieuGiamGiaTaiQuayResponse> timPhieuGiamGia(
            String keyword,
            HoaDon hoaDonHienTai,
            KhachHang khachHang,
            BigDecimal tongTienHang) {
        BigDecimal tongTienHangHienTai = tongTienHang != null
                ? tongTienHang
                : (hoaDonHienTai != null ? hoaDonHienTai.getTongTienHang() : BigDecimal.ZERO);

        if (tongTienHangHienTai == null || tongTienHangHienTai.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        List<PhieuGiamGiaTaiQuayResponse> ketQua = new ArrayList<>();

        for (PhieuGiamGia phieuGiamGia : phieuGiamGiaRepository.searchByKeyword(
                chuanHoaTuKhoa(keyword),
                PageRequest.of(0, 100))) {
            try {
                validatePhieuGiamGia(phieuGiamGia, khachHang, tongTienHangHienTai, hoaDonHienTai);
                BigDecimal soTienGiam = pricingUseCase.tinhSoTienGiam(phieuGiamGia, tongTienHangHienTai);
                BigDecimal tongTienSauGiam = tongTienHangHienTai.subtract(soTienGiam);
                ketQua.add(mapPhieuGiamGiaTaiQuayResponse(
                        new PhieuGiamGiaDuocApDung(phieuGiamGia, soTienGiam, tongTienSauGiam)));
            } catch (BusinessException exception) {
                // Bo qua cac phieu khong hop le voi gio hang hien tai.
            }

            if (ketQua.size() >= 50) {
                break;
            }
        }

        return ketQua;
    }

    public PhieuGiamGiaDuocApDung tinhPhieuGiamGiaHopLe(
            String maPhieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang,
            boolean validateSoLuong,
            HoaDon hoaDon) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.isBlank()) {
            throw new BusinessException("Mã phiếu giảm giá không được để trống");
        }

        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByMaIgnoreCase(maPhieuGiamGia)
                .orElseThrow(() -> new BusinessException("Phiếu giảm giá không tồn tại"));

        validatePhieuGiamGia(phieuGiamGia, khachHang, tongTienHang, hoaDon);

        boolean isAlreadyApplied = hoaDon != null && hoaDon.getPhieuGiamGia() != null
                && hoaDon.getPhieuGiamGia().getId().equals(phieuGiamGia.getId());

        int tongSoLuong = phieuGiamGia.getSoLuong() == null ? 0 : phieuGiamGia.getSoLuong();
        int daDung = phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung();
        if (validateSoLuong && !isAlreadyApplied && tongSoLuong != 999999 && daDung >= tongSoLuong) {
            throw new BusinessException(
                    "Phiếu giảm giá đã hết lượt sử dụng. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác.");
        }

        BigDecimal soTienGiam = pricingUseCase.tinhSoTienGiam(phieuGiamGia, tongTienHang);
        BigDecimal tongTienSauGiam = tongTienHang.subtract(soTienGiam).max(BigDecimal.ZERO);

        return new PhieuGiamGiaDuocApDung(phieuGiamGia, soTienGiam, tongTienSauGiam);
    }

    public void ganPhieuGiamGiaChoHoaDon(HoaDon hoaDon, String maPhieuGiamGia, KhachHang khachHang,
            BigDecimal tongTienHang) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.isBlank()) {
            hoaDon.setPhieuGiamGia(null);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(tongTienHang);
            return;
        }

        PhieuGiamGiaDuocApDung phieuApDung = tinhPhieuGiamGiaHopLe(
                maPhieuGiamGia,
                khachHang,
                tongTienHang,
                false,
                hoaDon);

        PhieuGiamGia pgg = phieuGiamGiaRepository.findByIdForUpdate(phieuApDung.phieuGiamGia().getId())
                .orElseThrow(() -> new BusinessException("Phiếu giảm giá không tồn tại"));
        int tongSoLuong = pgg.getSoLuong() == null ? 0 : pgg.getSoLuong();
        int daDung = pgg.getSoLuongDaDung() == null ? 0 : pgg.getSoLuongDaDung();
        if (tongSoLuong != 999999 && daDung >= tongSoLuong) {
            throw new BusinessException("Phiếu giảm giá đã hết lượt sử dụng");
        }
        pgg.setSoLuongDaDung(daDung + 1);
        phieuGiamGiaRepository.save(pgg);

        if (khachHang != null) {
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(pgg.getId(), khachHang.getId())
                    .orElse(null);
            if (pggh != null) {
                pggh.setTrangThai(TRANG_THAI_PHIEU_THEO_KH_DA_DUNG);
                pggh.setNgaySuDung(Instant.now());
                phieuGiamGiaKhachHangRepository.save(pggh);
            }
        }

        hoaDon.setPhieuGiamGia(pgg);
        hoaDon.setTienGiam(phieuApDung.soTienGiam());
        hoaDon.setTongTienThanhToan(phieuApDung.tongTienSauGiam());
    }

    public void giaiPhongPhieuGiamGia(PhieuGiamGia phieuGiamGia, KhachHang khachHang) {
        if (phieuGiamGia == null) {
            return;
        }
        phieuGiamGia.setSoLuongDaDung(Math.max(
                0,
                (phieuGiamGia.getSoLuongDaDung() == null ? 0 : phieuGiamGia.getSoLuongDaDung()) - 1));
        phieuGiamGiaRepository.save(phieuGiamGia);

        if (khachHang != null) {
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieuGiamGia.getId(), khachHang.getId())
                    .orElse(null);
            if (pggh != null) {
                pggh.setTrangThai(TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG);
                pggh.setNgaySuDung(null);
                phieuGiamGiaKhachHangRepository.save(pggh);
            }
        }
    }

    private void validatePhieuGiamGia(
            PhieuGiamGia phieuGiamGia,
            KhachHang khachHang,
            BigDecimal tongTienHang,
            HoaDon hoaDon) {
        if (phieuGiamGia.getTrangThai() == null || phieuGiamGia.getTrangThai() != TRANG_THAI_PHIEU_HOAT_DONG) {
            throw new BusinessException(
                    "Phiếu giảm giá không hoạt động. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác.");
        }

        Instant now = Instant.now();
        if (phieuGiamGia.getNgayBatDau() != null && now.isBefore(phieuGiamGia.getNgayBatDau())) {
            throw new BusinessException(
                    "Phiếu giảm giá chưa đến thời gian áp dụng. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác.");
        }

        if (phieuGiamGia.getNgayKetThuc() != null && now.isAfter(phieuGiamGia.getNgayKetThuc())) {
            throw new BusinessException(
                    "Phiếu giảm giá đã hết hạn sử dụng. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác.");
        }

        if (phieuGiamGia.getGiaTriToiThieu() != null && tongTienHang.compareTo(phieuGiamGia.getGiaTriToiThieu()) < 0) {
            throw new BusinessException("Giá trị đơn hàng chưa đạt tối thiểu " + phieuGiamGia.getGiaTriToiThieu());
        }

        boolean isAlreadyApplied = hoaDon != null && hoaDon.getPhieuGiamGia() != null
                && hoaDon.getPhieuGiamGia().getId().equals(phieuGiamGia.getId());

        if (phieuGiamGia.getLoaiPhieu() != null && phieuGiamGia.getLoaiPhieu() == 2) {
            if (khachHang == null) {
                throw new BusinessException(
                        "Phiếu giảm giá này chỉ áp dụng cho khách hàng thành viên. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác.");
            }
            PhieuGiamGiaKhachHang pggh = phieuGiamGiaKhachHangRepository
                    .findByPhieuGiamGiaIdAndKhachHangId(phieuGiamGia.getId(), khachHang.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Khách hàng không sở hữu phiếu giảm giá này. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác."));

            if (!isAlreadyApplied && pggh.getTrangThai() != TRANG_THAI_PHIEU_THEO_KH_CHUA_DUNG) {
                throw new BusinessException(
                        "Phiếu giảm giá đã được khách hàng sử dụng. Vui lòng thông báo cho khách hàng và chọn phiếu giảm giá khác.");
            }
        }
    }

    public BigDecimal xacDinhTongTienHangKhiApPhieu(ApDungPhieuGiamGiaRequest request, HoaDon hoaDonHienTai,
            List<HoaDonChiTiet> itemsTam) {
        if (request.items() != null && !request.items().isEmpty()) {
            return itemsTam.stream()
                    .map(HoaDonChiTiet::getThanhTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (hoaDonHienTai != null) {
            return hoaDonHienTai.getTongTienHang();
        }

        return BigDecimal.ZERO;
    }

    public PhieuGiamGiaTaiQuayResponse mapPhieuGiamGiaTaiQuayResponse(PhieuGiamGiaDuocApDung phieuGiamGia) {
        return new PhieuGiamGiaTaiQuayResponse(
                phieuGiamGia.phieuGiamGia().getId(),
                phieuGiamGia.phieuGiamGia().getMa(),
                phieuGiamGia.phieuGiamGia().getTen(),
                phieuGiamGia.phieuGiamGia().getLoai(),
                phieuGiamGia.phieuGiamGia().getGiaTri(),
                phieuGiamGia.phieuGiamGia().getGiaTriToiThieu(),
                phieuGiamGia.phieuGiamGia().getGiamToiDa(),
                phieuGiamGia.soTienGiam(),
                phieuGiamGia.tongTienHang(),
                phieuGiamGia.tongTienSauGiam());
    }

    private String chuanHoaTuKhoa(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    public record PhieuGiamGiaDuocApDung(
            PhieuGiamGia phieuGiamGia,
            BigDecimal soTienGiam,
            BigDecimal tongTienSauGiam) {
        public BigDecimal tongTienHang() {
            return tongTienSauGiam.add(soTienGiam);
        }
    }
}
