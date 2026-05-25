package com.example.server.core.admin.quanLySanPham.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThemHinhAnhRequest(
        @NotBlank String url,
        Integer loaiHinh,
        @Size(max = 300) String moTa
) {}
