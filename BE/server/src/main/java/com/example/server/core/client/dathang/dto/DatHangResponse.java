package com.example.server.core.client.dathang.dto;

import java.math.BigDecimal;

/** Kết quả đặt hàng. */
public record DatHangResponse(
        Integer hoaDonId,
        String maHoaDon,
        BigDecimal tongTien,
        Integer trangThai,
        String hinhThucThanhToan
) {}
