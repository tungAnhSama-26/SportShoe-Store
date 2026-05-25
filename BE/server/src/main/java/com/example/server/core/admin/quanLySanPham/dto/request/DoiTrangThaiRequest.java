package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DoiTrangThaiRequest(
        @NotNull @Min(0) @Max(2) Integer trangThai
) {}
