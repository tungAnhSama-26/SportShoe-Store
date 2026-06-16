package com.example.server.core.client.donhang.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Khách cập nhật số lượng sản phẩm trong đơn (chỉ COD + đang chờ xác nhận).
 * Danh sách là các dòng KHÁCH MUỐN GIỮ LẠI; dòng cũ không có trong danh sách sẽ bị xóa.
 * Đơn không được rỗng (phải còn tối thiểu 1 dòng, mỗi dòng số lượng &gt;= 1).
 */
public record CapNhatSoLuongRequest(
        @NotEmpty(message = "Đơn hàng phải còn ít nhất 1 sản phẩm")
        @Valid
        List<Dong> items
) {
    public record Dong(
            @NotNull(message = "Thiếu mã dòng sản phẩm")
            Integer hoaDonChiTietId,

            @NotNull(message = "Thiếu số lượng")
            @Min(value = 1, message = "Số lượng mỗi sản phẩm phải lớn hơn 0")
            Integer soLuong
    ) {}
}
