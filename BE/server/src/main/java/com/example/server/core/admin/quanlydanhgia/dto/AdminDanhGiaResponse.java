package com.example.server.core.admin.quanlydanhgia.dto;

import java.time.Instant;

/**
 * Một đánh giá trong màn quản lý đánh giá của admin (kèm phản hồi shop, trạng thái ẩn/hiện,
 * lý do bị ẩn và thông tin sản phẩm cho chế độ "Tất cả đánh giá").
 */
public record AdminDanhGiaResponse(
        Integer id,
        String hoTenKhach,
        String hinhAnhKhach,
        Integer soSao,
        String noiDung,
        String media,
        Instant ngayTao,
        String phanHoi,
        Instant ngayPhanHoi,
        Integer trangThai,
        String lyDoAn,
        Integer giayId,
        String tenSanPham,
        String hinhAnhSanPham
) {}
