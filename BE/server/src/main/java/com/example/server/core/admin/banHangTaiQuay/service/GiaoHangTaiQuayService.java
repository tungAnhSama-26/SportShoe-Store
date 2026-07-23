package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.ThongTinGiaoHangTaiQuayRequest;
import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class GiaoHangTaiQuayService {

    private final GiaCaTaiQuayService pricingUseCase;

    public GiaoHangTaiQuayService(GiaCaTaiQuayService pricingUseCase) {
        this.pricingUseCase = pricingUseCase;
    }

    public boolean laDonGiaoHang(ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        return thongTinGiaoHang != null && Boolean.TRUE.equals(thongTinGiaoHang.giaoHang());
    }

    public String requireDiaChiGiaoHang(ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        String diaChi = thongTinGiaoHang != null ? thongTinGiaoHang.diaChiGiaoHang() : null;
        if (diaChi == null || diaChi.isBlank()) {
            throw new BusinessException("Vui lòng nhập địa chỉ giao hàng");
        }
        return diaChi.trim();
    }

    public BigDecimal resolvePhiVanChuyen(ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        BigDecimal phiVanChuyen = pricingUseCase.defaultMoney(thongTinGiaoHang != null ? thongTinGiaoHang.phiVanChuyen() : null);
        if (phiVanChuyen.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Phí vận chuyển không hợp lệ");
        }
        return phiVanChuyen;
    }

    public String resolveDonViVanChuyen(ThongTinGiaoHangTaiQuayRequest thongTinGiaoHang) {
        String donViVanChuyen = thongTinGiaoHang != null ? thongTinGiaoHang.donViVanChuyen() : null;
        if (donViVanChuyen == null || donViVanChuyen.isBlank()) {
            return "GHN";
        }
        return donViVanChuyen.trim();
    }

    public String resolveGiaTriChuoi(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return fallback;
    }
}
