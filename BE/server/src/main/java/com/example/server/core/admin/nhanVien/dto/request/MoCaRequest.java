package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record MoCaRequest(
        @NotNull @PositiveOrZero BigDecimal tienDauCa,
        String ghiChu
) {}
