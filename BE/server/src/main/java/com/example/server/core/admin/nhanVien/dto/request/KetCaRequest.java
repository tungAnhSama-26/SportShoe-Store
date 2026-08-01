package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record KetCaRequest(
        @NotNull @PositiveOrZero BigDecimal tienCuoiCaThucTe,
        String lyDoChenhLech,
        String ghiChu
) {
}
