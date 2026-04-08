package com.example.server.infrastructure.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String code,
        String name,
        String gender,
        String status,
        CatalogOptionResponse brand,
        CatalogOptionResponse category,
        CatalogOptionResponse material,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String thumbnail
) {
}
