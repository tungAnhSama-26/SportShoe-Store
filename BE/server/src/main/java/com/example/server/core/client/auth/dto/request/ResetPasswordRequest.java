package com.example.server.core.client.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
    String account,

    @NotBlank(message = "Mã xác nhận không được để trống")
    @Size(min = 6, max = 6, message = "Mã xác nhận phải có 6 chữ số")
    String otp,

    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword
) {}
