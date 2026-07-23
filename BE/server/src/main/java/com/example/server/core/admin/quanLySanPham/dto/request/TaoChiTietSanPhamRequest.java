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
        @Min(0) Integer giayId,
        @Size(max = 100) String ma,
        @Size(min = 3, max = 300) String ten,
        @Min(0) Integer thuongHieuId,
        @Min(0) Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Min(0) Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Min(0) Integer deGiayId,
        @Min(0) Integer coGiayId,
        @Min(0) Integer congNgheDemId,
        @Min(0) Integer trongLuongId,
        @NotNull @Min(0) Integer mauSacId,
        @NotNull @Min(0) Integer kichCoId,
        @NotNull @Min(0) Integer soLuong,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaGoc,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 16, fraction = 2) BigDecimal giaBan
) {}
