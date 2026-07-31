package com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoaiGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(min = 3, max = 100, message = "Tên loại giày phải từ 3 đến 100 ký tự") String ten,
        @Size(max = 500) String moTa
) {
    public LoaiGiayRequest {
        if (ma != null) ma = ma.trim();
        if (ten != null) ten = ten.trim();
        if (moTa != null) moTa = moTa.trim();
    }
}
