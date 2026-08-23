package com.example.server.core.client.chatbot.dto;

import java.math.BigDecimal;

public record CouponDto(
        Integer id,
        String ma,
        String ten,
        String loaiText,
        String loaiPhieuText,
        BigDecimal giaTri,
        BigDecimal giaTriToiThieu,
        BigDecimal giamToiDa,
        String ngayBatDau,
        String ngayKetThuc,
        Integer soLuong,
        Integer soLuongDaDung,
        String trangThaiText
) {
}
