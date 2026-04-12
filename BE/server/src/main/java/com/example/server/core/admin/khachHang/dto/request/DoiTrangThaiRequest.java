package com.example.server.core.admin.khachHang.dto.request;

import jakarta.validation.constraints.NotNull;

public record DoiTrangThaiRequest(
        @NotNull(message = "Trạng thái không được để trống")
        Integer trangThai
) {}
