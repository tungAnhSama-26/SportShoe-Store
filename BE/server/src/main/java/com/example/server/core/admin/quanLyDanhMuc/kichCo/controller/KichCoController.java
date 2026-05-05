package com.example.server.core.admin.quanLyDanhMuc.kichCo.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.kichCo.dto.request.KichCoRequest;
import com.example.server.core.admin.quanLyDanhMuc.kichCo.dto.response.KichCoResponse;
import com.example.server.core.admin.quanLyDanhMuc.kichCo.service.KichCoService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/danh-muc/kich-co")
public class KichCoController {

    private final KichCoService kichCoService;

    public KichCoController(KichCoService kichCoService) {
        this.kichCoService = kichCoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<KichCoResponse>>> danhSachKichCo(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách kích cỡ thành công",
                kichCoService.danhSachKichCo(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KichCoResponse>> chiTietKichCo(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết kích cỡ thành công",
                kichCoService.chiTietKichCo(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KichCoResponse>> taoKichCo(@Valid @RequestBody KichCoRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo kích cỡ thành công",
                kichCoService.taoKichCo(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KichCoResponse>> capNhatKichCo(
            @PathVariable Integer id,
            @Valid @RequestBody KichCoRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật kích cỡ thành công",
                kichCoService.capNhatKichCo(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiKichCo(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        kichCoService.doiTrangThaiKichCo(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaKichCo(@PathVariable Integer id) {
        kichCoService.xoaKichCo(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa kích cỡ thành công", null));
    }
}
