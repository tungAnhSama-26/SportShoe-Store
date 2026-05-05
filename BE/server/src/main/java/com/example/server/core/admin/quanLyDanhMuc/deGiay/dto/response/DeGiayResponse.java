package com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.response;

import java.time.Instant;

public record DeGiayResponse(
        Integer id,
        String ma,
        String ten,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
