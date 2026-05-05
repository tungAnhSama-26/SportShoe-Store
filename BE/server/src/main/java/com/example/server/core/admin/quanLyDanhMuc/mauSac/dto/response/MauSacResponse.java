package com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.response;

import java.time.Instant;

public record MauSacResponse(
        Integer id,
        String ma,
        String ten,
        String maMauHex,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
