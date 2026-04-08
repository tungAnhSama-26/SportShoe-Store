package com.example.server.infrastructure;

import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.core.khachhang.giay.KhachHangGiayService;
import com.example.server.entity.enums.Gender;
import com.example.server.infrastructure.dto.ProductDetailResponse;
import com.example.server.infrastructure.dto.ProductFilterRequest;
import com.example.server.infrastructure.dto.ProductSummaryResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KhachHangGiayController {

    private final KhachHangGiayService khachHangGiayService;

    public KhachHangGiayController(KhachHangGiayService khachHangGiayService) {
        this.khachHangGiayService = khachHangGiayService;
    }

    @GetMapping("${app.api.base-path}/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID materialId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        ProductFilterRequest filter = new ProductFilterRequest(
                keyword,
                brandId,
                categoryId,
                materialId,
                gender == null || gender.isBlank() ? null : Gender.valueOf(gender.trim().toUpperCase()),
                minPrice,
                maxPrice,
                page,
                size
        );
        Page<ProductSummaryResponse> products = khachHangGiayService.getProducts(filter);
        return ResponseEntity.ok(ApiResponse.success("Fetched products", PageResponse.from(products)));
    }

    @GetMapping("${app.api.base-path}/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched product detail", khachHangGiayService.getProductDetail(productId)));
    }
}
