package com.example.server.core.admin.quanLyDanhMuc.chatLieuGiay.dto.response;

import java.time.Instant;

public record ChatLieuGiayResponse(
        Integer id,
        String ma,
        String ten,
        String moTa,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
