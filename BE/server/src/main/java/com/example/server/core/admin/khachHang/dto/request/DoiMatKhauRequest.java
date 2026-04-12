package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DoiMatKhauRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống")
        String matKhauMoi
) {}
