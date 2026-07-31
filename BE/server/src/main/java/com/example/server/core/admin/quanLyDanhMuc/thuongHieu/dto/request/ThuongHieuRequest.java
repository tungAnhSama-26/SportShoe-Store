package com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ThuongHieuRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(min = 3, max = 100, message = "Tên thương hiệu phải từ 3 đến 100 ký tự") String ten,
        @Size(max = 100) String xuatXu,
        @Size(max = 500) String logoUrl,
        @Size(max = 300)
        @Pattern(regexp = "^(\\s*|https?://\\S+)$", message = "Website phải bắt đầu bằng http:// hoặc https://")
        String website,
        @Size(max = 500) String moTa
) {
    public ThuongHieuRequest {
        if (ma != null) ma = ma.trim();
        if (ten != null) ten = ten.trim();
        if (xuatXu != null) xuatXu = xuatXu.trim();
        if (logoUrl != null) logoUrl = logoUrl.trim();
        if (website != null) website = website.trim();
        if (moTa != null) moTa = moTa.trim();
    }
}
