package com.example.server.core.admin.quanLyDanhMuc.congNgheDem.controller;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.congNgheDem.dto.request.CongNgheDemRequest;
import com.example.server.core.admin.quanLyDanhMuc.congNgheDem.dto.response.CongNgheDemResponse;
import com.example.server.core.admin.quanLyDanhMuc.congNgheDem.service.CongNgheDemService;
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
@RequestMapping("/api/v1/admin/danh-muc/cong-nghe-dem")
public class CongNgheDemController {

    private final CongNgheDemService congNgheDemService;

    public CongNgheDemController(CongNgheDemService congNgheDemService) {
        this.congNgheDemService = congNgheDemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CongNgheDemResponse>>> danhSachCongNgheDem(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao").and(Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách công nghệ đệm thành công",
                congNgheDemService.danhSachCongNgheDem(keyword, pageable)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CongNgheDemResponse>> chiTietCongNgheDem(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết công nghệ đệm thành công",
                congNgheDemService.chiTietCongNgheDem(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CongNgheDemResponse>> taoCongNgheDem(@Valid @RequestBody CongNgheDemRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo công nghệ đệm thành công",
                congNgheDemService.taoCongNgheDem(req)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CongNgheDemResponse>> capNhatCongNgheDem(
            @PathVariable Integer id,
            @Valid @RequestBody CongNgheDemRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật công nghệ đệm thành công",
                congNgheDemService.capNhatCongNgheDem(id, req)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<Void>> doiTrangThaiCongNgheDem(
            @PathVariable Integer id,
            @Valid @RequestBody DoiTrangThaiDanhMucRequest req
    ) {
        congNgheDemService.doiTrangThaiCongNgheDem(id, req);
        return ResponseEntity.ok(ApiResponse.success("Đổi trạng thái thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCongNgheDem(@PathVariable Integer id) {
        congNgheDemService.xoaCongNgheDem(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa công nghệ đệm thành công", null));
    }
}
