package com.example.server.core.client.giohang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ThemVaoGioRequest(
        @NotNull(message = "Thiếu thông tin khách hàng")
        UUID khachHangId,

        @NotNull(message = "Thiếu sản phẩm")
        Integer giayChiTietId,

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        Integer soLuong
) {}
