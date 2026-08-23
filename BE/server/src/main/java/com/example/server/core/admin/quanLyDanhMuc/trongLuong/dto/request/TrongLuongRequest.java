package com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrongLuongRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotNull @Min(value = 100, message = "Trọng lượng tối thiểu từ 100 gram trở lên") @Max(value = 2500, message = "Trọng lượng tối đa không vượt quá 2,500 gram") Integer giaTri,
        @Size(max = 300) String moTa
) {}
