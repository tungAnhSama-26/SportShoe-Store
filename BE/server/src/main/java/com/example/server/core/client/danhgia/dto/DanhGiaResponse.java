package com.example.server.core.client.danhgia.dto;

import java.time.Instant;

/** Một đánh giá của khách hàng. */
public record DanhGiaResponse(
        Integer id,
        String hoTenKhach,
        Integer soSao,
        String noiDung,
        Instant ngayTao
) {}
