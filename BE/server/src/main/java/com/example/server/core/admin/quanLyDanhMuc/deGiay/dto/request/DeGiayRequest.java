package com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(min = 4, max = 100, message = "Tên đế giày phải từ 4 đến 100 ký tự") String ten,
        @Size(max = 300) String moTa
) {
    public DeGiayRequest {
        if (ma != null) ma = ma.trim();
        if (ten != null) ten = ten.trim();
        if (moTa != null) moTa = moTa.trim();
    }
}
