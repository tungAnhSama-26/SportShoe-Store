package com.example.server.core.admin.quanLyDanhMuc.congNgheDem.dto.response;

import java.time.Instant;

public record CongNgheDemResponse(
        Integer id,
        String ma,
        String ten,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
