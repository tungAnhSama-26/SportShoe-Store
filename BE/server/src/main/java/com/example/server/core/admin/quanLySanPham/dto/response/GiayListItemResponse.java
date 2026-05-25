package com.example.server.core.admin.quanLySanPham.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record GiayListItemResponse(
        Integer id,
        String ma,
        String ten,
        String loaiGiay,
        String thuongHieu,
        String chatLieu,
        String deGiay,
        String coGiay,
        String congNgheDem,
        String trongLuong,
        Integer gioiTinh,
        Integer trangThai,
        String hinhAnh,
        BigDecimal giaMin,
        BigDecimal giaMax,
        BigDecimal giaGocMin,
        BigDecimal giaGocMax,
        Long tongBienThe,
        Long tongSoLuong,
        Instant ngayTao,
        Boolean coGiamGia
) {}
