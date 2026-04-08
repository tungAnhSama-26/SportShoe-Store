package com.example.server.infrastructure.dto;

import com.example.server.entity.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductFilterRequest(
        String keyword,
        UUID brandId,
        UUID categoryId,
        UUID materialId,
        Gender gender,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {

    public int resolvedPage() {
        return page == null ? 0 : page;
    }

    public int resolvedSize() {
        return size == null ? 12 : size;
    }
}
