package com.example.server.core.admin.quanLySanPham.dto.response;

import java.time.Instant;

public record HinhAnhGiayResponse(
        Integer id,
        Integer loaiHinh,
        String url,
        String moTa,
        Boolean laHinhChinh,
        Integer trangThai,
        Instant ngayTao
) {}
