package com.example.server.core.admin.quanLyDanhMuc.kichCo.dto.response;

import java.time.Instant;

public record KichCoResponse(
        Integer id,
        String giaTri,
        String ghiChu,
        Integer trangThai,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
