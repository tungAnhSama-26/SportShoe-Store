package com.example.server.core.client.giohang.controller;

import com.example.server.core.client.giohang.dto.CapNhatSoLuongRequest;
import com.example.server.core.client.giohang.dto.GioHangResponse;
import com.example.server.core.client.giohang.dto.ThemVaoGioRequest;
import com.example.server.core.client.giohang.service.ClientGioHangService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API giỏ hàng phía khách hàng. Yêu cầu khách đã đăng nhập (FE gửi kèm khachHangId của phiên).
 */
@RestController
@RequestMapping("/api/v1/client/gio-hang")
public class ClientGioHangController {

    private final ClientGioHangService service;

    public ClientGioHangController(ClientGioHangService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<GioHangResponse>> layGio(@RequestParam UUID khachHangId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công", service.layGio(khachHangId)));
    }

    @PostMapping("/them")
    public ResponseEntity<ApiResponse<GioHangResponse>> them(@Valid @RequestBody ThemVaoGioRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", service.them(request)));
    }

    @PutMapping("/chi-tiet/{id}")
    public ResponseEntity<ApiResponse<GioHangResponse>> capNhat(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatSoLuongRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật giỏ hàng", service.capNhatSoLuong(id, request)));
    }

    @DeleteMapping("/chi-tiet/{id}")
    public ResponseEntity<ApiResponse<GioHangResponse>> xoa(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khỏi giỏ hàng", service.xoa(id)));
    }

    /** Giữ hàng tạm khi khách vào thanh toán (trừ tồn tạm trong 90 giây). */
    @PostMapping("/giu-hang")
    public ResponseEntity<ApiResponse<Void>> giuHang(@RequestParam UUID khachHangId) {
        service.giuHang(khachHangId);
        return ResponseEntity.ok(ApiResponse.success("Đã giữ hàng", null));
    }

    /** Hủy giữ hàng khi khách rời thanh toán (hoàn tồn về cũ). */
    @PostMapping("/huy-giu")
    public ResponseEntity<ApiResponse<Void>> huyGiu(@RequestParam UUID khachHangId) {
        service.huyGiuHang(khachHangId);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy giữ hàng", null));
    }
}
