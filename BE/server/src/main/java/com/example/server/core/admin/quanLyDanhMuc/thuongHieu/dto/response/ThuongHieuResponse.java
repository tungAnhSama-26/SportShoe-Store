package com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.response;

import java.time.Instant;

public record ThuongHieuResponse(
        Integer id,
        String ma,
        String ten,
        String xuatXu,
        String logoUrl,
        String website,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
