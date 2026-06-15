package com.example.server.core.client.dathang.controller;

import com.example.server.core.client.dathang.dto.DatHangRequest;
import com.example.server.core.client.dathang.dto.DatHangResponse;
import com.example.server.core.client.dathang.service.ClientDatHangService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API đặt hàng phía khách hàng. Đây là thời điểm trừ tồn kho.
 */
@RestController
@RequestMapping("/api/v1/client/dat-hang")
public class ClientDatHangController {

    private final ClientDatHangService service;

    public ClientDatHangController(ClientDatHangService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DatHangResponse>> datHang(@Valid @RequestBody DatHangRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Đặt hàng thành công",
                service.datHang(request)
        ));
    }
}
