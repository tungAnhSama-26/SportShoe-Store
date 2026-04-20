package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record HoaDonChoDongSanPhamResponse(
        Integer chiTietId,
        String maSanPham,
        String tenSanPham,
        Integer soLuong,
        BigDecimal giaBan,
        BigDecimal thanhTien
) {
}
