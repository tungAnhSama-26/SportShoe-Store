package com.example.server.core.admin.thongKe.dto.response;

import java.math.BigDecimal;

public record ThongKeTongQuanResponse(
        BigDecimal tongDoanhThu,
        Long tongDonHang,
        Long sanPhamDaBan,
        Long khachMoi
) {
}
