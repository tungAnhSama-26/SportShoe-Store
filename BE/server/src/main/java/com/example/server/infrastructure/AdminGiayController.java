package com.example.server.infrastructure;

import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.dto.CreateProductRequest;
import com.example.server.infrastructure.dto.ProductDetailResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminGiayController {

    private final AdminGiayService adminGiayService;

    public AdminGiayController(AdminGiayService adminGiayService) {
        this.adminGiayService = adminGiayService;
    }

    @PostMapping("${app.api.base-path}/admin/products")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Created product successfully", adminGiayService.createProduct(request)));
    }
}
