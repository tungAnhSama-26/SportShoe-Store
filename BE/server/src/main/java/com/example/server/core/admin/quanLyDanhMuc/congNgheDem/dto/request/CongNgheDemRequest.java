package com.example.server.core.admin.quanLyDanhMuc.congNgheDem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CongNgheDemRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(max = 200) String ten,
        @Size(max = 500) String moTa
) {}
