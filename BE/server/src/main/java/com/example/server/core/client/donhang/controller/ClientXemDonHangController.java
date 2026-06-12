package com.example.server.core.client.donhang.controller;

import com.example.server.core.client.donhang.dto.DonHangChiTietResponse;
import com.example.server.core.client.donhang.dto.DonHangTomTatResponse;
import com.example.server.core.client.donhang.service.ClientXemDonHangService;
import com.example.server.infrastructure.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API xem đơn hàng của khách (theo dõi tình trạng đơn). Yêu cầu khách đã đăng nhập
 * (FE gửi kèm khachHangId; chi tiết chỉ trả về nếu đơn thuộc khách đó).
 */
@RestController
@RequestMapping("/api/v1/client/don-hang")
public class ClientXemDonHangController {

    private final ClientXemDonHangService service;

    public ClientXemDonHangController(ClientXemDonHangService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DonHangTomTatResponse>>> danhSach(@RequestParam UUID khachHangId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", service.danhSach(khachHangId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonHangChiTietResponse>> chiTiet(
            @PathVariable Integer id,
            @RequestParam UUID khachHangId
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", service.chiTiet(khachHangId, id)));
    }

    /** Khách xác nhận đã nhận hàng. */
    @PostMapping("/{id}/da-nhan-hang")
    public ResponseEntity<ApiResponse<Void>> daNhanHang(
            @PathVariable Integer id,
            @RequestParam UUID khachHangId
    ) {
        service.xacNhanDaNhanHang(khachHangId, id);
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận nhận hàng", null));
    }

    /** Khách gửi yêu cầu hủy khi đơn chưa chuyển sang bước giao hàng. */
    @PostMapping("/{id}/yeu-cau-huy")
    public ResponseEntity<ApiResponse<Void>> yeuCauHuy(
            @PathVariable Integer id,
            @RequestParam UUID khachHangId
    ) {
        service.yeuCauHuy(khachHangId, id);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi yêu cầu hủy đơn hàng", null));
    }
}
