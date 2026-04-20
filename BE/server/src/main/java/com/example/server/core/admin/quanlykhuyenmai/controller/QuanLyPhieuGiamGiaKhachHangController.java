package com.example.server.core.admin.quanlykhuyenmai.controller;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaKhachHangRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse;
import com.example.server.core.admin.quanlykhuyenmai.service.PhieuGiamGiaKhachHangService;
import com.example.server.infrastructure.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/phieu-giam-gia-khach-hang")
public class QuanLyPhieuGiamGiaKhachHangController {

    @Autowired
    private PhieuGiamGiaKhachHangService phieuGiamGiaKhachHangService;

    @GetMapping
    public List<QuanLyPhieuGiamGiaKhachHangResponse> getAll() {
        return phieuGiamGiaKhachHangService.getAll();
    }

    @GetMapping("detail/{id}")
    public QuanLyPhieuGiamGiaKhachHangResponse detail(@PathVariable("id") Integer id) {
        return phieuGiamGiaKhachHangService.getOne(id);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        phieuGiamGiaKhachHangService.remove(id);
    }

    @GetMapping("paging")
    public Page<QuanLyPhieuGiamGiaKhachHangResponse> phanTrang(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        return phieuGiamGiaKhachHangService.phanTrang(keyword, trangThai, pageNo, pageSize);
    }

    @PostMapping("add")
    public void add(@RequestBody PhieuGiamGiaKhachHangRequest request) {
        phieuGiamGiaKhachHangService.add(request);
    }

    @PutMapping("update")
    public void update(@RequestParam("id") Integer id, @RequestBody PhieuGiamGiaKhachHangRequest request) {
        phieuGiamGiaKhachHangService.update(id, request);
    }

    @GetMapping("email-suggestions")
    public ResponseEntity<ApiResponse<List<String>>> emailSuggestions() {
        return ResponseEntity.ok(ApiResponse.success(
            "Lấy danh sách email thành công",
            phieuGiamGiaKhachHangService.getEmailSuggestions()
        ));
    }
}
