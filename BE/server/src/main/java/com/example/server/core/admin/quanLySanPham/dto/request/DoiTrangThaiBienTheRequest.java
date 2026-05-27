package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.NotNull;

public record DoiTrangThaiBienTheRequest(
        @NotNull Integer kichHoat
) {}
