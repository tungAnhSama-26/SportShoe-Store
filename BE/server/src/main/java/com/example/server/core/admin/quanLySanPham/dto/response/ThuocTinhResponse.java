package com.example.server.core.admin.quanLySanPham.dto.response;

public record ThuocTinhResponse(
        Integer id,
        Integer deGiayId,
        String deGiay,
        Integer coGiayId,
        String coGiay,
        Integer congNgheDemId,
        String congNgheDem,
        Integer chatLieuGiayId,
        String chatLieuGiay,
        Integer trongLuongId,
        String trongLuong
) {}
