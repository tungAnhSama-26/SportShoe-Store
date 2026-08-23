package com.example.server.core.client.chatbot.dto;

import java.math.BigDecimal;

public record PromotionDto(
        Integer id,
        String ma,
        String ten,
        String moTa,
        String loaiGiamText,
        BigDecimal giaTriGiam,
        String ngayBatDau,
        String ngayKetThuc,
        String trangThaiText
) {
}
