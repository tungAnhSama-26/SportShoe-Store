package com.example.server.core.client.diachi.controller;

import com.example.server.core.admin.khachHang.dto.responsse.DiaChiResponse;
import com.example.server.core.admin.khachHang.service.KhachHangService;
import com.example.server.infrastructure.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API địa chỉ giao hàng của khách (cho trang thanh toán). Tái dùng service địa chỉ của admin.
 */
@RestController
@RequestMapping("/api/v1/client/khach-hang/{khachHangId}/dia-chi")
public class ClientDiaChiController {

    private final KhachHangService khachHangService;

    public ClientDiaChiController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiaChiResponse>>> layDanhSach(@PathVariable UUID khachHangId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách địa chỉ thành công",
                khachHangService.layDanhSachDiaChi(khachHangId)
        ));
    }
}
