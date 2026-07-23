package com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrongLuongRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotNull @Min(1) @Max(value = 10000, message = "Trọng lượng tối đa là 10,000 gram") Integer giaTri,
        @Size(max = 300) String moTa
) {}
