package com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatLieuGiayRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(max = 100)
        @Pattern(regexp = "^.*[a-zA-Z0-9À-ỹ].*$", message = "Tên chất liệu phải chứa ít nhất một chữ cái hoặc số")
        String ten,
        @Size(max = 300) String moTa
) {}
