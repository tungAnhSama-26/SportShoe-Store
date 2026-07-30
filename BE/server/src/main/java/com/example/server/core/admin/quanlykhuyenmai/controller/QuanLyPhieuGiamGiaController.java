package com.example.server.core.admin.quanlykhuyenmai.controller;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse;
import com.example.server.core.admin.quanlykhuyenmai.service.PhieuGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/phieu-giam-gia")
public class QuanLyPhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping
    public List<QuanLyPhieuGiamGiaResponse> getAll() {
        return phieuGiamGiaService.getAll();
    }

    @GetMapping("check-ten")
    public java.util.Map<String, Boolean> checkTenTrung(
            @RequestParam String ten,
            @RequestParam(required = false) Integer id
    ) {
        return phieuGiamGiaService.checkTenTrung(ten, id);
    }

    @GetMapping("check-ma")
    public java.util.Map<String, Boolean> checkMaTrung(
            @RequestParam String ma,
            @RequestParam(required = false) Integer id
    ) {
        return phieuGiamGiaService.checkMaTrung(ma, id);
    }

    @GetMapping("detail/{id}")
    public QuanLyPhieuGiamGiaResponse detail(@PathVariable("id") Integer id) {
        return phieuGiamGiaService.getOne(id);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        phieuGiamGiaService.remove(id);
    }

    @GetMapping("paging")
    public Page<QuanLyPhieuGiamGiaResponse> phanTrang(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "loai", required = false) Integer loai,
            @RequestParam(value = "loaiPhieu", required = false) Integer loaiPhieu,
            @RequestParam(value = "tuNgay", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate tuNgay,
            @RequestParam(value = "denNgay", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate denNgay,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        return phieuGiamGiaService.phanTrang(keyword, trangThai, loai, loaiPhieu, tuNgay, denNgay, pageNo, pageSize);
    }

    @PostMapping("add")
    public com.example.server.entity.PhieuGiamGia add(@Valid @RequestBody PhieuGiamGiaRequest request) {
        return phieuGiamGiaService.add(request);
    }

    @PutMapping("update")
    public com.example.server.entity.PhieuGiamGia update(@RequestParam("id") Integer id, @Valid @RequestBody PhieuGiamGiaRequest request) {
        return phieuGiamGiaService.update(id, request);
    }
}
