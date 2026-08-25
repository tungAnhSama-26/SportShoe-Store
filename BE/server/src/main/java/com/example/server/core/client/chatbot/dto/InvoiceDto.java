package com.example.server.core.client.chatbot.dto;

import java.math.BigDecimal;

public record InvoiceDto(
        Integer id,
        String ma,
        String tenNguoiNhan,
        String sdtNguoiNhan,
        BigDecimal tongTienThanhToan,
        String trangThaiText,
        String ngayLap
) {
}
