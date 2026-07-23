package com.example.server.core.client.danhgia.dto;

import java.time.Instant;

/** Một đánh giá hiển thị ở trang Đánh giá công khai (kèm sản phẩm + phản hồi shop). */
public record DanhGiaCongKhaiResponse(
        Integer id,
        String hoTenKhach,
        Integer soSao,
        String noiDung,
        String media,
        Instant ngayTao,
        String phanHoi,
        Instant ngayPhanHoi,
        Integer giayId,
        String tenSanPham,
        String hinhAnhSanPham,
        String hinhAnhKhach
) {}
