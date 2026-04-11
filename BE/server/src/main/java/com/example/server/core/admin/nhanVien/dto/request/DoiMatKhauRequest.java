package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoiMatKhauRequest(
        @NotBlank @Size(min = 6) String matKhauMoi
) {}
