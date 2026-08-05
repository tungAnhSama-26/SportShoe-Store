package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BaoCaoSuCoGiaoCaRequest(
        @PositiveOrZero BigDecimal tienKiemDem,
        @NotBlank @Size(max = 500) String noiDung
) {}
