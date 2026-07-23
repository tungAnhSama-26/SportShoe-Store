package com.example.server.core.admin.banHangTaiQuay.dto.response;

import java.math.BigDecimal;

public record SanPhamTaiQuayResponse(
        Integer chiTietId,
        String maSanPham,
        String tenSanPham,
        String sku,
        String maBienThe,
        Integer soLuongTon,
        BigDecimal giaGoc,
        BigDecimal giaBan,
        String hinhAnh,
        String loaiGiay,
        String thuongHieu,
        String deGiay,
        String coGiay,
        String congNgheDem,
        String mauSac,
        String kichCo,
        String trongLuong,
        String tenDotGiamGia
) {
}
