package com.example.server.core.admin.quanLyDanhMuc.trongLuong.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.request.TrongLuongRequest;
import com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.response.TrongLuongResponse;
import com.example.server.core.admin.quanLyDanhMuc.trongLuong.service.TrongLuongService;
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
@RequestMapping("/api/v1/admin/danh-muc/trong-luong")
public class TrongLuongController {

    private final TrongLuongService trongLuongService;

    public TrongLuongController(TrongLuongService trongLuongService) {
        this.trongLuongService = trongLuongService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TrongLuongResponse>>> danhSachTrongLuong(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao").and(Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách trọng lượng thành công",
                trongLuongService.danhSachTrongLuong(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrongLuongResponse>> chiTietTrongLuong(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết trọng lượng thành công",
                trongLuongService.chiTietTrongLuong(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrongLuongResponse>> taoTrongLuong(@Valid @RequestBody TrongLuongRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo trọng lượng thành công",
                trongLuongService.taoTrongLuong(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrongLuongResponse>> capNhatTrongLuong(
            @PathVariable Integer id,
            @Valid @RequestBody TrongLuongRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trọng lượng thành công",
                trongLuongService.capNhatTrongLuong(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiTrongLuong(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        trongLuongService.doiTrangThaiTrongLuong(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaTrongLuong(@PathVariable Integer id) {
        trongLuongService.xoaTrongLuong(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa trọng lượng thành công", null));
    }
}
