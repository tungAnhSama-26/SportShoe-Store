package com.example.server.core.admin.banHangTaiQuay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TaoHoaDonChoItemRequest(
        @NotNull(message = "Chi tiết sản phẩm không được để trống")
        Integer chiTietId,
        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer soLuong,
        BigDecimal giaBan
) {
}
