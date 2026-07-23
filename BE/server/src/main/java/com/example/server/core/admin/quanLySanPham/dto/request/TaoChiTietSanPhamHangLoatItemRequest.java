package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TaoChiTietSanPhamHangLoatItemRequest(
        @NotNull @Min(0) Integer mauSacId,
        @NotNull @Min(0) Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan
) {}
