package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TaoChiTietSanPhamRequest(
        @Positive Integer giayId,
        @Size(max = 100) String ma,
        @Size(min = 3, max = 300) String ten,
        @Positive Integer thuongHieuId,
        @Positive Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Positive Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Positive Integer deGiayId,
        @Positive Integer coGiayId,
        @Positive Integer congNgheDemId,
        @Positive Integer trongLuongId,
        @NotNull @Positive Integer mauSacId,
        @NotNull @Positive Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan
) {}
