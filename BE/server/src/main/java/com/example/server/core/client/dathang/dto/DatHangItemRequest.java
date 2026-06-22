package com.example.server.core.client.dathang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DatHangItemRequest(
        @NotNull(message = "Thiếu thông tin biến thể sản phẩm")
        Integer giayChiTietId,

        @NotNull(message = "Thiếu số lượng sản phẩm")
        @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn 0")
        Integer soLuong
) {
}
