package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CapNhatNhanVienRequest(
        @NotBlank @Size(max = 100) String hoTen,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String sdt,
        @Size(max = 200) String diaChi,
        String hinhAnh,
        @NotNull Integer vaiTro
) {}
