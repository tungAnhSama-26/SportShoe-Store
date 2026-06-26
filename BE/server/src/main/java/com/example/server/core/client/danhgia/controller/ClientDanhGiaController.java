package com.example.server.core.client.danhgia.controller;

import com.example.server.core.client.danhgia.dto.DanhGiaTongHopResponse;
import com.example.server.core.client.danhgia.service.ClientDanhGiaService;
import com.example.server.infrastructure.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API đánh giá sản phẩm phía khách hàng (chỉ GET công khai để xem đánh giá của 1 sản phẩm).
 * Việc gửi đánh giá chỉ thực hiện qua đơn đã mua (xem ClientDanhGiaDonHangController) để đảm bảo
 * chỉ người đã mua + đã nhận hàng mới đánh giá được.
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
}
