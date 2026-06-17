package com.example.server.core.client.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ClientProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
        String hoTen,

        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        String email,

        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
        String sdt,

        Integer gioiTinh,
        LocalDate ngaySinh,

        @Size(max = 500, message = "Đường dẫn ảnh không được vượt quá 500 ký tự")
        String hinhAnh
) {
}
