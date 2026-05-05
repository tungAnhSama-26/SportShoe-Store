package com.example.server.core.admin.quanLyDanhMuc.mauSac.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.request.MauSacRequest;
import com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.response.MauSacResponse;
import com.example.server.core.admin.quanLyDanhMuc.mauSac.service.MauSacService;
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
@RequestMapping("/api/v1/admin/danh-muc/mau-sac")
public class MauSacController {

    private final MauSacService mauSacService;

    public MauSacController(MauSacService mauSacService) {
        this.mauSacService = mauSacService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MauSacResponse>>> danhSachMauSac(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách màu sắc thành công",
                mauSacService.danhSachMauSac(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MauSacResponse>> chiTietMauSac(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết màu sắc thành công",
                mauSacService.chiTietMauSac(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MauSacResponse>> taoMauSac(@Valid @RequestBody MauSacRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo màu sắc thành công",
                mauSacService.taoMauSac(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MauSacResponse>> capNhatMauSac(
            @PathVariable Integer id,
            @Valid @RequestBody MauSacRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật màu sắc thành công",
                mauSacService.capNhatMauSac(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiMauSac(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        mauSacService.doiTrangThaiMauSac(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaMauSac(@PathVariable Integer id) {
        mauSacService.xoaMauSac(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa màu sắc thành công", null));
    }
}
