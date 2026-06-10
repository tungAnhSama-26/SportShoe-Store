package com.example.server.core.client.danhgia.dto;

import java.util.List;

/** Tổng hợp đánh giá của một sản phẩm: điểm trung bình, số lượng và danh sách. */
public record DanhGiaTongHopResponse(
        double diemTrungBinh,
        int soLuong,
        List<DanhGiaResponse> danhSach
) {}
