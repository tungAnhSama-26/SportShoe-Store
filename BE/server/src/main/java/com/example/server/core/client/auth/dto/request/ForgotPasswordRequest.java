package com.example.server.core.client.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
        String account
) {
}
