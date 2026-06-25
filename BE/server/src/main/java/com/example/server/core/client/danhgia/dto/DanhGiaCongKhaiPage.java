package com.example.server.core.client.danhgia.dto;

import java.util.List;

/** Một trang đánh giá công khai. */
public record DanhGiaCongKhaiPage(
        List<DanhGiaCongKhaiResponse> danhSach,
        int trang,
        int tongTrang,
        long tongSo
) {}
