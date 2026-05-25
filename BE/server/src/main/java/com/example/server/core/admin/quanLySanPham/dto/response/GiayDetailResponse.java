package com.example.server.core.admin.quanLySanPham.dto.response;

import java.time.Instant;
import java.util.List;

public record GiayDetailResponse(
        Integer id,
        String ma,
        String ten,
        Integer gioiTinh,
        Integer thuongHieuId,
        String thuongHieu,
        Integer loaiGiayId,
        String loaiGiay,
        String chatLieu,
        String moTa,
        Integer trangThai,
        ThuocTinhResponse thuocTinh,
        List<HinhAnhGiayResponse> hinhAnhs,
        Instant ngayTao,
        Instant ngayCapNhat
) {}
