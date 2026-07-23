package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CapNhatGiayRequest(
        @NotBlank @Size(min = 3, max = 300) String ten,
        @NotNull @Min(0) Integer thuongHieuId,
        @NotNull @Min(0) Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Min(0) Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Min(0) Integer deGiayId,
        @Min(0) Integer coGiayId,
        @Min(0) Integer congNgheDemId,
        @Min(0) Integer trongLuongId
) {}
