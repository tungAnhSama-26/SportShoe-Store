package com.example.server.core.client.donhang.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Một đơn hàng trong danh sách "Đơn hàng của bạn". */
public record DonHangTomTatResponse(
        Integer id,
        String ma,
        Instant ngayLap,
        Integer trangThai,
        String trangThaiText,
        int soLuong,
        BigDecimal tongThanhToan
) {}
