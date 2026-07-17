package com.example.server.core.client.thongbao.controller;

import com.example.server.core.client.thongbao.dto.ThongBaoKhachResponse;
import com.example.server.core.client.thongbao.service.ClientThongBaoService;
import com.example.server.infrastructure.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Chuông thông báo của khách hàng ở header màn khách. */
@RestController
@RequestMapping("/api/v1/client/thong-bao")
public class ClientThongBaoController {

    private final ClientThongBaoService service;

    public ClientThongBaoController(ClientThongBaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ThongBaoKhachResponse>>> layDanhSach(@RequestParam UUID khachHangId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông báo thành công", service.layDanhSach(khachHangId)));
    }

    @GetMapping("/chua-xem")
    public ResponseEntity<ApiResponse<Long>> demChuaXem(@RequestParam UUID khachHangId) {
        return ResponseEntity.ok(ApiResponse.success("Đếm thông báo thành công", service.demChuaXem(khachHangId)));
    }

    @PutMapping("/da-xem")
    public ResponseEntity<ApiResponse<Void>> danhDauDaXem(@RequestParam UUID khachHangId) {
        service.danhDauDaXem(khachHangId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã xem", null));
    }
}
