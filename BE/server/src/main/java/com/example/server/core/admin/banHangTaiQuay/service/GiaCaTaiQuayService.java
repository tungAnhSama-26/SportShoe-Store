package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.entity.PhieuGiamGia;
import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

import static com.example.server.core.admin.banHangTaiQuay.constant.BanHangTaiQuayConstants.*;

@Component
public class GiaCaTaiQuayService {

    public BigDecimal tinhSoTienGiam(PhieuGiamGia phieuGiamGia, BigDecimal tongTienHang) {
        BigDecimal soTienGiam;

        if (phieuGiamGia.getLoai() != null && phieuGiamGia.getLoai() == LOAI_PHIEU_PHAN_TRAM) {
            soTienGiam = tongTienHang
                    .multiply(phieuGiamGia.getGiaTri())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (phieuGiamGia.getGiamToiDa() != null
                    && phieuGiamGia.getGiamToiDa().signum() > 0
                    && soTienGiam.compareTo(phieuGiamGia.getGiamToiDa()) > 0) {
                soTienGiam = phieuGiamGia.getGiamToiDa();
            }
        } else if (phieuGiamGia.getLoai() != null && phieuGiamGia.getLoai() == LOAI_PHIEU_TIEN_MAT) {
            soTienGiam = phieuGiamGia.getGiaTri();
        } else {
            throw new BusinessException("Loại phiếu giảm giá không được hỗ trợ");
        }

        if (soTienGiam.compareTo(BigDecimal.ZERO) < 0) {
            soTienGiam = BigDecimal.ZERO;
        }

        if (soTienGiam.compareTo(tongTienHang) > 0) {
            soTienGiam = tongTienHang;
        }

        return soTienGiam;
    }

    public BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
