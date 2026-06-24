package com.example.server.core.admin.thongKe.dto.response;

import java.math.BigDecimal;

public record ThongKeTongQuanResponse(
        BigDecimal tongDoanhThu,
        BigDecimal tongTienMat,
        BigDecimal tongChuyenKhoan,
        Long tongDonHang,
        Long sanPhamDaBan,
        Long khachMoi
) {
}
