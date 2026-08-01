package com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MauSacRequest(
        @NotBlank @Size(max = 50) String ma,
        @NotBlank @Size(min = 2, max = 50, message = "Tên màu sắc phải từ 2 đến 50 ký tự") String ten,
        @NotBlank
        @Size(max = 7)
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu chưa đúng định dạng, vui lòng nhập lại")
        String maMauHex
) {
    public MauSacRequest {
        if (ma != null) ma = ma.trim();
        if (ten != null) ten = ten.trim();
        if (maMauHex != null) maMauHex = maMauHex.trim();
    }
}
