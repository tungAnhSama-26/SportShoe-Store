package com.example.server.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String code,
        String name,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean active
) {
}
