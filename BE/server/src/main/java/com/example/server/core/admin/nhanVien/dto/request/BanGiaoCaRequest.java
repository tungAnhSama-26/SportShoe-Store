package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record BanGiaoCaRequest(
        @NotNull @PositiveOrZero BigDecimal tienCuoiCaThucTe,
        @NotNull UUID nhanVienNhanId,
        String lyDoChenhLech,
        String ghiChu
) {}
