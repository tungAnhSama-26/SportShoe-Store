package com.example.server.core.client.donhang.controller;

import com.example.server.core.client.donhang.dto.DonHangChiTietResponse;
import com.example.server.core.client.donhang.service.ClientXemDonHangService;
import com.example.server.infrastructure.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tra cứu đơn hàng theo mã hóa đơn - CÔNG KHAI, không cần đăng nhập.
 * Dùng cho khách vãng lai (mua không tài khoản) tra cứu đơn bằng mã được cấp sau khi đặt.
 */
@RestController
@RequestMapping("/api/v1/client/tra-cuu-don")
public class ClientTraCuuDonHangController {

    private final ClientXemDonHangService service;

    public ClientTraCuuDonHangController(ClientXemDonHangService service) {
        this.service = service;
    }

    @GetMapping("/{ma}")
    public ResponseEntity<ApiResponse<DonHangChiTietResponse>> traCuu(@PathVariable String ma) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tra cứu đơn hàng thành công",
                service.traCuuTheoMa(ma)
        ));
    }
}
