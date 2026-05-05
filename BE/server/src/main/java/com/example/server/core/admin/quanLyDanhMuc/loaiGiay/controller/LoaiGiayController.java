package com.example.server.core.admin.quanLyDanhMuc.loaiGiay.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.request.LoaiGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.response.LoaiGiayResponse;
import com.example.server.core.admin.quanLyDanhMuc.loaiGiay.service.LoaiGiayService;
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
@RequestMapping("/api/v1/admin/danh-muc/loai-giay")
public class LoaiGiayController {

    private final LoaiGiayService loaiGiayService;

    public LoaiGiayController(LoaiGiayService loaiGiayService) {
        this.loaiGiayService = loaiGiayService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LoaiGiayResponse>>> danhSachLoaiGiay(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách loại giày thành công",
                loaiGiayService.danhSachLoaiGiay(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoaiGiayResponse>> chiTietLoaiGiay(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết loại giày thành công",
                loaiGiayService.chiTietLoaiGiay(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoaiGiayResponse>> taoLoaiGiay(@Valid @RequestBody LoaiGiayRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo loại giày thành công",
                loaiGiayService.taoLoaiGiay(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoaiGiayResponse>> capNhatLoaiGiay(
            @PathVariable Integer id,
            @Valid @RequestBody LoaiGiayRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật loại giày thành công",
                loaiGiayService.capNhatLoaiGiay(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiLoaiGiay(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        loaiGiayService.doiTrangThaiLoaiGiay(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaLoaiGiay(@PathVariable Integer id) {
        loaiGiayService.xoaLoaiGiay(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa loại giày thành công", null));
    }
}
