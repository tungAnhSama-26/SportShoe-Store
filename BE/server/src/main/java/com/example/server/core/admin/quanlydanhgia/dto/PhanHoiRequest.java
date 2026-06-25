package com.example.server.core.admin.quanlydanhgia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nội dung phản hồi của shop cho một đánh giá. */
public record PhanHoiRequest(
        @NotBlank(message = "Nội dung phản hồi không được để trống")
        @Size(max = 1000, message = "Phản hồi không quá 1000 ký tự")
        String noiDung
) {}
