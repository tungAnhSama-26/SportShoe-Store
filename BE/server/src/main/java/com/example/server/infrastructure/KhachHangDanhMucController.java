package com.example.server.infrastructure;

import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.core.khachhang.danhmuc.KhachHangDanhMucService;
import com.example.server.infrastructure.dto.CatalogOptionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/catalog")
public class KhachHangDanhMucController {

    private final KhachHangDanhMucService khachHangDanhMucService;

    public KhachHangDanhMucController(KhachHangDanhMucService khachHangDanhMucService) {
        this.khachHangDanhMucService = khachHangDanhMucService;
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<CatalogOptionsResponse>> getOptions() {
        return ResponseEntity.ok(ApiResponse.success("Fetched catalog options", khachHangDanhMucService.getOptions()));
    }
}
