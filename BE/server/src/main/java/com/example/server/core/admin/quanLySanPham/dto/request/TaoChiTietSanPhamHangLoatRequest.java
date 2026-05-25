package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TaoChiTietSanPhamHangLoatRequest(
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
        @NotEmpty List<@Valid TaoChiTietSanPhamHangLoatItemRequest> bienThes
) {}
