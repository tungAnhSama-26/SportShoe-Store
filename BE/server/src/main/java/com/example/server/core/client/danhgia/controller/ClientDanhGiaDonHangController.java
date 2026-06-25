package com.example.server.core.client.danhgia.controller;

import com.example.server.core.client.danhgia.dto.DanhGiaCongKhaiPage;
import com.example.server.core.client.danhgia.dto.DanhGiaResponse;
import com.example.server.core.client.danhgia.dto.TaoDanhGiaDonHangRequest;
import com.example.server.core.client.danhgia.service.ClientDanhGiaService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Đánh giá sản phẩm theo dòng hóa đơn chi tiết (chỉ người đã mua + đã nhận hàng,
 * mỗi dòng đánh giá 1 lần).
 */
@RestController
@RequestMapping("/api/v1/client/danh-gia")
public class ClientDanhGiaDonHangController {

    private final ClientDanhGiaService service;

    public ClientDanhGiaDonHangController(ClientDanhGiaService service) {
        this.service = service;
    }

    /** Trang Đánh giá công khai: toàn bộ đánh giá của shop, lọc theo số sao (1-5) nếu có. */
    @GetMapping("/cong-khai")
    public ResponseEntity<ApiResponse<DanhGiaCongKhaiPage>> congKhai(
            @RequestParam(required = false) Integer soSao,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "10") int kichThuoc
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy đánh giá thành công",
                service.layCongKhai(soSao, trang, kichThuoc)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DanhGiaResponse>> danhGia(@Valid @RequestBody TaoDanhGiaDonHangRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Gửi đánh giá thành công",
                service.taoTheoHoaDonChiTiet(request.khachHangId(), request.hoaDonChiTietId(), request.soSao(), request.noiDung(), request.media())
        ));
    }
}
