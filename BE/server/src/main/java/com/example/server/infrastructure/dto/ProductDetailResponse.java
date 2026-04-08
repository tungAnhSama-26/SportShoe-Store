package com.example.server.infrastructure.dto;

import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String code,
        String name,
        String description,
        String gender,
        String status,
        CatalogOptionResponse brand,
        CatalogOptionResponse category,
        CatalogOptionResponse material,
        PromotionResponse promotion,
        List<ProductVariantResponse> variants
) {
}
