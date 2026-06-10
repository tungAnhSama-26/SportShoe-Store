package com.example.server.core.admin.quanlytrahang.domain;

import com.example.server.infrastructure.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TinhTienHoanTraHang {

    private static final int MONEY_SCALE = 2;

    private TinhTienHoanTraHang() {
    }

    public static BigDecimal tinh(
            BigDecimal giaDonVi,
            int soLuongChapNhan,
            BigDecimal tongTienHang,
            BigDecimal tienGiam
    ) {
        if (giaDonVi == null || giaDonVi.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Đơn giá sản phẩm không hợp lệ");
        }
        if (soLuongChapNhan <= 0) {
            throw new BusinessException("Số lượng chấp nhận trả phải lớn hơn 0");
        }
        if (tongTienHang == null || tongTienHang.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Tổng tiền hàng phải lớn hơn 0");
        }

        BigDecimal thanhTienDong = giaDonVi.multiply(BigDecimal.valueOf(soLuongChapNhan));
        BigDecimal giamGia = tienGiam == null ? BigDecimal.ZERO : tienGiam.max(BigDecimal.ZERO);
        BigDecimal tyLeGiam = giamGia
                .divide(tongTienHang, 10, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE);

        return thanhTienDong
                .multiply(BigDecimal.ONE.subtract(tyLeGiam))
                .max(BigDecimal.ZERO)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
