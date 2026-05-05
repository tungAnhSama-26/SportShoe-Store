package com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.response;

import java.time.Instant;

public record LoaiGiayResponse(
        Integer id,
        String ma,
        String ten,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
