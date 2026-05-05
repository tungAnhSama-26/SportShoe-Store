package com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoaiGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(max = 200) String ten,
        @Size(max = 500) String moTa
) {}
