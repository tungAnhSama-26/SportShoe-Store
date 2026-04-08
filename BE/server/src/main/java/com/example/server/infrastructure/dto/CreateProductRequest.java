package com.example.server.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank @Size(max = 100) String code,
        @NotBlank @Size(max = 300) String name,
        @NotNull UUID brandId,
        UUID categoryId,
        UUID materialId,
        UUID promotionId,
        String gender,
        String description,
        String status,
        @Valid @NotEmpty List<CreateProductVariantRequest> variants
) {
    public record CreateProductVariantRequest(
            @NotBlank @Size(max = 150) String variantCode,
            @NotNull UUID colorId,
            @NotNull UUID sizeId,
            @NotBlank @Size(max = 150) String sku,
            @NotNull Integer quantity,
            @NotNull @DecimalMin("0.0") BigDecimal originalPrice,
            @NotNull @DecimalMin("0.0") BigDecimal salePrice,
            Boolean active,
            @Valid List<CreateProductImageRequest> images
    ) {
    }

    public record CreateProductImageRequest(
            @NotBlank String type,
            @NotBlank @Size(max = 1000) String url,
            @Size(max = 300) String description,
            Boolean primary
    ) {
    }
}
