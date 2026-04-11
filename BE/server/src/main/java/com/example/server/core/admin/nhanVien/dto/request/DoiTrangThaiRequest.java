package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;

public record DoiTrangThaiRequest(
        @NotNull Integer trangThai
) {}
