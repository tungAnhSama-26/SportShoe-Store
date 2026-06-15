package com.example.server.core.admin.quanlyhoadon.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CapNhatSanPhamHoaDonRequest(
        @NotEmpty(message = "Hóa đơn phải có ít nhất một sản phẩm")
        @Size(max = 100, message = "Hóa đơn không được vượt quá 100 dòng sản phẩm")
        List<@Valid SanPhamItemRequest> items
) {
    public record SanPhamItemRequest(
            @NotNull(message = "Vui lòng chọn sản phẩm")
            Integer chiTietId,

            @NotNull(message = "Vui lòng nhập số lượng")
            @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn 0")
            Integer soLuong
    ) {}
}
