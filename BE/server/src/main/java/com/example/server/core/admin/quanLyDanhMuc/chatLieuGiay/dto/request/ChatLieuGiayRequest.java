package com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatLieuGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(min = 4, max = 100, message = "Tên chất liệu giày phải từ 4 đến 100 ký tự")
        @Pattern(regexp = "^.*[a-zA-Z0-9À-ỹ].*$", message = "Tên chất liệu phải chứa ít nhất một chữ cái hoặc số")
        String ten,
        @Size(max = 300) String moTa
) {
    public ChatLieuGiayRequest {
        if (ma != null) ma = ma.trim();
        if (ten != null) ten = ten.trim();
        if (moTa != null) moTa = moTa.trim();
    }
}
