package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CapNhatGiayRequest(
        @NotBlank @Size(min = 3, max = 300) String ten,
        @NotNull @Positive Integer thuongHieuId,
        @NotNull @Positive Integer loaiGiayId,
        @Min(1) @Max(3) Integer gioiTinh,
        String chatLieu,
        @Positive Integer chatLieuGiayId,
        @Size(max = 2000) String moTa,
        @Positive Integer deGiayId,
        @Positive Integer coGiayId,
        @Positive Integer congNgheDemId,
        @Positive Integer trongLuongId
) {}
