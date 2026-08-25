package com.example.server.core.client.sanpham.dto;

import java.math.BigDecimal;

/**
 * Giá hiện tại của 1 biến thể để giỏ hàng đồng bộ.
 *
 * @param giayChiTietId id biến thể
 * @param giaNiemYet    giá niêm yết (gốc, trước đợt giảm)
 * @param giaHienTai    giá sau giảm hiện tại (bằng niêm yết nếu không có đợt giảm)
 * @param tonKho        tồn kho hiện tại
 * @param conBan        biến thể còn được bán (đang kích hoạt)
 * @param canNang       cân nặng 1 sản phẩm (gram) - lấy từ thuộc tính trọng lượng của giày
 * @param ma            mã sản phẩm (Giay.ma) để hiển thị ở giỏ hàng
 * @param hinhAnh       hình ảnh biến thể hoặc hình ảnh chính của giày
 */
public record GiaBienTheGioResponse(
        Integer giayChiTietId,
        BigDecimal giaNiemYet,
        BigDecimal giaHienTai,
        Integer tonKho,
        boolean conBan,
        Integer canNang,
        String ma,
        String hinhAnh
) {
}
