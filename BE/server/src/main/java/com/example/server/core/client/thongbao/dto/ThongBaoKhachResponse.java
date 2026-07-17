package com.example.server.core.client.thongbao.dto;

import java.time.Instant;

/** Một thông báo trong chuông của khách hàng. */
public record ThongBaoKhachResponse(
        Integer id,
        String loai,
        String tieuDe,
        String noiDung,
        String lienKet,
        Boolean daXem,
        Instant ngayTao
) {}
