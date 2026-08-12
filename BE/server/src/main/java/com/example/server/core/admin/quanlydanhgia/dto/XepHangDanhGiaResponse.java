package com.example.server.core.admin.quanlydanhgia.dto;

import com.example.server.entity.Giay;

/** Một sản phẩm cùng điểm trung bình và tổng lượt đánh giá đang hiển thị. */
public record XepHangDanhGiaResponse(
        Giay giay,
        double diemTrungBinh,
        long soDanhGia
) {
}
