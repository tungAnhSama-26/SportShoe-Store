package com.example.server.core.admin.quanLyDanhMuc.thuongHieu.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.request.ThuongHieuRequest;
import com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.response.ThuongHieuResponse;
import com.example.server.core.admin.quanLyDanhMuc.thuongHieu.service.ThuongHieuService;
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
@RequestMapping("/api/v1/admin/danh-muc/thuong-hieu")
public class ThuongHieuController {

    private final ThuongHieuService thuongHieuService;

    public ThuongHieuController(ThuongHieuService thuongHieuService) {
        this.thuongHieuService = thuongHieuService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ThuongHieuResponse>>> danhSachThuongHieu(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao").and(Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách thương hiệu thành công",
                thuongHieuService.danhSachThuongHieu(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ThuongHieuResponse>> chiTietThuongHieu(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết thương hiệu thành công",
                thuongHieuService.chiTietThuongHieu(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ThuongHieuResponse>> taoThuongHieu(@Valid @RequestBody ThuongHieuRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo thương hiệu thành công",
                thuongHieuService.taoThuongHieu(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ThuongHieuResponse>> capNhatThuongHieu(
            @PathVariable Integer id,
            @Valid @RequestBody ThuongHieuRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật thương hiệu thành công",
                thuongHieuService.capNhatThuongHieu(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiThuongHieu(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        thuongHieuService.doiTrangThaiThuongHieu(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaThuongHieu(@PathVariable Integer id) {
        thuongHieuService.xoaThuongHieu(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thương hiệu thành công", null));
    }
}
