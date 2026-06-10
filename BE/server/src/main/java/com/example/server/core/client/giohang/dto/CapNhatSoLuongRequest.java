package com.example.server.core.client.giohang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CapNhatSoLuongRequest(
        @NotNull(message = "Thiếu thông tin khách hàng")
        UUID khachHangId,

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 0, message = "Số lượng không hợp lệ")
        Integer soLuong
) {}
