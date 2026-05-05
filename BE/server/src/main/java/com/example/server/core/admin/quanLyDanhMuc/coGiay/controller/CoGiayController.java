package com.example.server.core.admin.quanLyDanhMuc.coGiay.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.coGiay.dto.request.CoGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.coGiay.dto.response.CoGiayResponse;
import com.example.server.core.admin.quanLyDanhMuc.coGiay.service.CoGiayService;
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
@RequestMapping("/api/v1/admin/danh-muc/co-giay")
public class CoGiayController {

    private final CoGiayService coGiayService;

    public CoGiayController(CoGiayService coGiayService) {
        this.coGiayService = coGiayService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CoGiayResponse>>> danhSachCoGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách cổ giày thành công",
                coGiayService.danhSachCoGiay(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoGiayResponse>> chiTietCoGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết cổ giày thành công",
                coGiayService.chiTietCoGiay(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CoGiayResponse>> taoCoGiay(@Valid @RequestBody CoGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo cổ giày thành công",
                coGiayService.taoCoGiay(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CoGiayResponse>> capNhatCoGiay(
            @PathVariable Integer id,
            @Valid @RequestBody CoGiayRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật cổ giày thành công",
                coGiayService.capNhatCoGiay(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiCoGiay(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        coGiayService.doiTrangThaiCoGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCoGiay(@PathVariable Integer id) {
        coGiayService.xoaCoGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa cổ giày thành công", null));
    }
}
