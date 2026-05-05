package com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ThuongHieuRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(max = 200) String ten,
        @Size(max = 100) String xuatXu,
        @Size(max = 500) String logoUrl,
        @Size(max = 300)
        @Pattern(regexp = "^(\\s*|https?://\\S+)$", message = "Website phải bắt đầu bằng http:// hoặc https://")
        String website,
        @Size(max = 500) String moTa
) {}
