package com.example.server.core.client.donhang.dto;

import java.util.List;

/**
 * Kết quả cập nhật số lượng: đơn hàng sau cập nhật + danh sách cảnh báo giá đổi
 * (mỗi phần tử dạng "[Sản phẩm]: giá cũ → giá mới") để FE hiển thị nổi bật - mục 6.
 */
public record CapNhatSoLuongResponse(
        DonHangChiTietResponse donHang,
        List<String> canhBaoDoiGia
) {
}
