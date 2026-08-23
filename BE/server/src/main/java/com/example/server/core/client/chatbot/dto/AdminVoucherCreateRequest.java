package com.example.server.core.client.chatbot.dto;

import java.math.BigDecimal;

public record AdminVoucherCreateRequest(
        String code,
        String name,
        Integer type,
        BigDecimal value,
        BigDecimal minOrder,
        BigDecimal maxDiscount,
        Integer quantity,
        Integer durationDays
) {
}
