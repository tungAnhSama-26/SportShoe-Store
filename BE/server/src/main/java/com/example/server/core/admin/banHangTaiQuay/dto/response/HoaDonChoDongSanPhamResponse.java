package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record HoaDonChoDongSanPhamResponse(
        Integer chiTietId,
        String maSanPham,
        String tenSanPham,
        String mauSac,
        String kichCo,
        String sku,
        String hinhAnh,
        Integer soLuong,
        Integer soLuongTon,
        BigDecimal giaBan,
        BigDecimal giaGoc,
        BigDecimal thanhTien
) {
}
