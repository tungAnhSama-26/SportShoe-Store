package com.example.server.core.client.danhgia.controller;

import com.example.server.core.client.danhgia.dto.DanhGiaResponse;
import com.example.server.core.client.danhgia.dto.DanhGiaTongHopResponse;
import com.example.server.core.client.danhgia.dto.TaoDanhGiaRequest;
import com.example.server.core.client.danhgia.service.ClientDanhGiaService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API đánh giá sản phẩm phía khách hàng. GET public; POST yêu cầu khách đã đăng nhập
 * (FE gửi kèm khachHangId của phiên đăng nhập).
 */
@RestController
@RequestMapping("/api/v1/client/san-pham/{giayId}/danh-gia")
public class ClientDanhGiaController {

    private final ClientDanhGiaService service;

    public ClientDanhGiaController(ClientDanhGiaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DanhGiaTongHopResponse>> layDanhGia(@PathVariable Integer giayId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy đánh giá thành công",
                service.layTheoSanPham(giayId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DanhGiaResponse>> guiDanhGia(
            @PathVariable Integer giayId,
            @Valid @RequestBody TaoDanhGiaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Gửi đánh giá thành công",
                service.tao(giayId, request)
        ));
    }
}
