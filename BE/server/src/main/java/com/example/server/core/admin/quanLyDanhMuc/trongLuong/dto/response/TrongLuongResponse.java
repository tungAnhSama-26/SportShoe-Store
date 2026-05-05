package com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.response;

import java.time.Instant;

public record TrongLuongResponse(
        Integer id,
        String ma,
        Integer giaTri,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
