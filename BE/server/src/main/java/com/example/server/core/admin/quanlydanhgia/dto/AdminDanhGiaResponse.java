package com.example.server.core.admin.quanlydanhgia.dto;

import java.time.Instant;

/** Một đánh giá hiển thị trong màn quản lý đánh giá của admin (kèm phản hồi shop). */
public record AdminDanhGiaResponse(
        Integer id,
        String hoTenKhach,
        Integer soSao,
        String noiDung,
        String media,
        Instant ngayTao,
        String phanHoi,
        Instant ngayPhanHoi
) {}
