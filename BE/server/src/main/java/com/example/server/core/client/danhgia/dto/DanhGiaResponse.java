package com.example.server.core.client.danhgia.dto;

import java.time.Instant;

/** Một đánh giá của khách hàng. */
public record DanhGiaResponse(
        Integer id,
        String hoTenKhach,
        String hinhAnhKhach,
        Integer soSao,
        String noiDung,
        String media,
        String phanHoi,
        Instant ngayPhanHoi,
        Instant ngayTao
) {}
