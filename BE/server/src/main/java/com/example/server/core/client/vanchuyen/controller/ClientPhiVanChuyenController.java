package com.example.server.core.client.vanchuyen.controller;

import com.example.server.core.client.vanchuyen.dto.PhiVanChuyenResponse;
import com.example.server.core.client.vanchuyen.dto.TinhPhiShipRequest;
import com.example.server.core.client.vanchuyen.service.ClientPhiVanChuyenService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API tính phí vận chuyển (GHN) cho trang thanh toán phía khách. */
@RestController
@RequestMapping("/api/v1/client/phi-van-chuyen")
public class ClientPhiVanChuyenController {

    private final ClientPhiVanChuyenService service;

    public ClientPhiVanChuyenController(ClientPhiVanChuyenService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PhiVanChuyenResponse>> tinhPhi(
            @Valid @RequestBody TinhPhiShipRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Tính phí vận chuyển thành công", service.tinhPhi(request)));
    }
}
