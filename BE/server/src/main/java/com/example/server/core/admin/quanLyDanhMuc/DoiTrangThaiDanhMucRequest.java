package com.example.server.core.admin.quanLyDanhMuc;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DoiTrangThaiDanhMucRequest(
        @NotNull
        @Min(0)
        @Max(1)
        Integer trangThai
) {}
