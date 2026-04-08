package com.example.server.infrastructure.dto;

import java.util.List;

public record CatalogOptionsResponse(
        List<CatalogOptionResponse> brands,
        List<CatalogOptionResponse> categories,
        List<CatalogOptionResponse> materials,
        List<CatalogOptionResponse> colors,
        List<CatalogOptionResponse> sizes
) {
}
