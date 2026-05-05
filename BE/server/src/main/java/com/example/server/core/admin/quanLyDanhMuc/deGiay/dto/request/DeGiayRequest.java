package com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(max = 100) String ten,
        @Size(max = 300) String moTa
) {}
