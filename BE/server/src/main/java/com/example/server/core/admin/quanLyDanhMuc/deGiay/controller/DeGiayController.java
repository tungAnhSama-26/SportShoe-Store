package com.example.server.core.admin.quanLyDanhMuc.deGiay.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.request.DeGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.response.DeGiayResponse;
import com.example.server.core.admin.quanLyDanhMuc.deGiay.service.DeGiayService;
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
@RequestMapping("/api/v1/admin/danh-muc/de-giay")
public class DeGiayController {

    private final DeGiayService deGiayService;

    public DeGiayController(DeGiayService deGiayService) {
        this.deGiayService = deGiayService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeGiayResponse>>> danhSachDeGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao").and(Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đế giày thành công",
                deGiayService.danhSachDeGiay(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeGiayResponse>> chiTietDeGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết đế giày thành công",
                deGiayService.chiTietDeGiay(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeGiayResponse>> taoDeGiay(@Valid @RequestBody DeGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo đế giày thành công",
                deGiayService.taoDeGiay(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeGiayResponse>> capNhatDeGiay(
            @PathVariable Integer id,
            @Valid @RequestBody DeGiayRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật đế giày thành công",
                deGiayService.capNhatDeGiay(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiDeGiay(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        deGiayService.doiTrangThaiDeGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaDeGiay(@PathVariable Integer id) {
        deGiayService.xoaDeGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đế giày thành công", null));
    }
}
