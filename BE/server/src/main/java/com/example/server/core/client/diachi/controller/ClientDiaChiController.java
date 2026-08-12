package com.example.server.core.client.diachi.controller;

import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.address.DiaChiCuRequest;
import com.example.server.infrastructure.address.DiaChiCuResponse;
import com.example.server.infrastructure.address.LegacyAddressMappingService;
import com.example.server.infrastructure.address.VietnamAddressCatalogService;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.PhuongXa;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.TinhThanh;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API danh mục hành chính Việt Nam hai cấp cho toàn bộ form địa chỉ.
 * GHN không còn là nguồn dữ liệu hiển thị; GHN chỉ được dùng nội bộ khi tính phí.
 */
@RestController
@RequestMapping("/api/v1/client/dia-chi")
public class ClientDiaChiController {

    private final VietnamAddressCatalogService addressCatalogService;
    private final LegacyAddressMappingService legacyAddressMappingService;

    public ClientDiaChiController(
            VietnamAddressCatalogService addressCatalogService,
            LegacyAddressMappingService legacyAddressMappingService
    ) {
        this.addressCatalogService = addressCatalogService;
        this.legacyAddressMappingService = legacyAddressMappingService;
    }

    @GetMapping("/tinh-thanh")
    public ResponseEntity<ApiResponse<List<TinhThanh>>> dsTinh() {
        return ResponseEntity.ok(ApiResponse.success("OK", addressCatalogService.layDanhSachTinhThanh()));
    }

    @GetMapping("/phuong-xa")
    public ResponseEntity<ApiResponse<List<PhuongXa>>> dsPhuongXa(@RequestParam String tinhThanhCode) {
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                addressCatalogService.layDanhSachPhuongXa(tinhThanhCode)
        ));
    }

    @PostMapping("/doi-chieu-dia-chi-cu")
    public ResponseEntity<ApiResponse<DiaChiCuResponse>> doiChieuDiaChiCu(
            @Valid @RequestBody DiaChiCuRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                legacyAddressMappingService.doiChieu(request.diaChiCu())
        ));
    }
}
