package com.example.server.core.client.sanpham.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Chi tiết sản phẩm cho khách hàng: thông tin sản phẩm + ảnh đại diện (cấp sản phẩm)
 * + danh sách biến thể (màu, size, giá, tồn, ảnh) để chọn khi mua.
 */
public record ClientChiTietSanPhamResponse(
        Integer id,
        String ten,
        String moTa,
        String thuongHieu,
        String loaiGiay,
        String chatLieu,
        String deGiay,
        String coGiay,
        String congNgheDem,
        String trongLuong,
        Integer gioiTinh,
        String hinhAnh,
        Long daBan,
        List<BienTheItem> bienThe,
        Integer trangThai
) {
    public record BienTheItem(
            Integer id,
            String mauSac,
            String maMauHex,
            String kichCo,
            BigDecimal giaBan,
            BigDecimal giaGoc,
            Integer soLuong,
            String hinhAnh
    ) {}
}
