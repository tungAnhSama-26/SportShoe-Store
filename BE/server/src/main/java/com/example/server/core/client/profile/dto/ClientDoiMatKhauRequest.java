package com.example.server.core.client.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientDoiMatKhauRequest(
        @NotBlank(message = "Mật khẩu cũ không được để trống")
        String matKhauCu,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
        String matKhauMoi
) {
}
