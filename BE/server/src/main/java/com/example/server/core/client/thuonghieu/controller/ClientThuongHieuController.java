package com.example.server.core.client.thuonghieu.controller;

import com.example.server.core.client.thuonghieu.dto.ThuongHieuNoiBatResponse;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.repository.ThuongHieuRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API hãng (thương hiệu) cho trang chủ khách hàng. Public - không cần đăng nhập.
 */
@RestController
@RequestMapping("/api/v1/client/thuong-hieu")
public class ClientThuongHieuController {

    private final ThuongHieuRepository thuongHieuRepository;

    public ClientThuongHieuController(ThuongHieuRepository thuongHieuRepository) {
        this.thuongHieuRepository = thuongHieuRepository;
    }

    /** Lấy các hãng nổi bật nhất (nhiều sản phẩm đang bán nhất) cho trang chủ. */
    @GetMapping("/noi-bat")
    public ResponseEntity<ApiResponse<List<ThuongHieuNoiBatResponse>>> noiBat(
            @RequestParam(defaultValue = "4") int limit
    ) {
        int n = Math.max(1, Math.min(limit, 20));
        List<ThuongHieuNoiBatResponse> data = thuongHieuRepository.findThuongHieuNoiBat().stream()
                .limit(n)
                .map(t -> new ThuongHieuNoiBatResponse(t.getId(), t.getTen(), t.getMoTa(), t.getLogoUrl()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy hãng nổi bật thành công", data));
    }
}
