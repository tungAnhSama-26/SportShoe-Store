package com.example.server.core.admin.quanLyDanhMuc.coGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(min = 3, max = 100, message = "Tên cổ giày phải từ 3 đến 100 ký tự") String ten,
        @Size(max = 300) String moTa
) {
    public CoGiayRequest {
        if (ma != null) ma = ma.trim();
        if (ten != null) ten = ten.trim();
        if (moTa != null) moTa = moTa.trim();
    }
}
