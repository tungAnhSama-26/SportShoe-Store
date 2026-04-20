package com.example.server.core.admin.thongKe.dto.response;

import java.math.BigDecimal;

public record ThongKeSanPhamResponse(
        Integer stt,
        Integer sanPhamId,
        String maSanPham,
        String tenSanPham,
        String thuongHieu,
        Long daBan,
        BigDecimal doanhThu,
        Long tonKho
) {
}
