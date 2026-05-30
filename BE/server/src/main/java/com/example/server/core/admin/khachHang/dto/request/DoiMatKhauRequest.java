package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoiMatKhauRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải có từ 6 đến 255 ký tự")
        String matKhauMoi
) {}
