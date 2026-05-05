package com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MauSacRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(max = 100) String ten,
        @NotBlank
        @Size(max = 7)
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu chưa đúng định dạng, vui lòng nhập lại")
        String maMauHex
) {}
