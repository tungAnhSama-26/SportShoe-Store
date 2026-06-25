package com.example.server.core.admin.quanlydanhgia.dto;

import java.time.Instant;

/** Một sản phẩm có đánh giá (cho bảng quản lý đánh giá của admin). */
public record SanPhamCoDanhGiaResponse(
        Integer giayId,
        String ma,
        String ten,
        String hinhAnh,
        long soDanhGia,
        double diemTrungBinh,
        Instant ngayMoiNhat,
        long soChuaXem
) {}
