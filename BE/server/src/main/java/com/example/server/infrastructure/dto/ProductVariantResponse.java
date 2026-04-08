package com.example.server.infrastructure.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        String variantCode,
        String sku,
        Integer quantity,
        BigDecimal originalPrice,
        BigDecimal salePrice,
        boolean active,
        CatalogOptionResponse color,
        CatalogOptionResponse size,
        List<ProductImageResponse> images
) {
}
