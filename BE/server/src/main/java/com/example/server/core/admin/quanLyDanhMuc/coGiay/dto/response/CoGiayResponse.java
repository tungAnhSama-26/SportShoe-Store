package com.example.server.core.admin.quanLyDanhMuc.coGiay.dto.response;

import java.time.Instant;

public record CoGiayResponse(
        Integer id,
        String ma,
        String ten,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
