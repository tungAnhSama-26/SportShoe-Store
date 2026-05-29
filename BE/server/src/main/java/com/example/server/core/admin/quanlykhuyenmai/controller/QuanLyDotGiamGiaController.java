package com.example.server.core.admin.quanlykhuyenmai.controller;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse;
import com.example.server.core.admin.quanlykhuyenmai.service.DotGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dot-giam-gia")
public class QuanLyDotGiamGiaController {

    @Autowired
    private DotGiamGiaService dotGiamGiaService;

    @GetMapping
    public List<QuanLyDotGiamGiaResponse> getAll() {
        return dotGiamGiaService.getAll();
    }

    @GetMapping("check-ten")
    public java.util.Map<String, Boolean> checkTenTrung(
            @RequestParam String ten,
            @RequestParam(required = false) Integer id
    ) {
        return dotGiamGiaService.checkTenTrung(ten, id);
    }

    @GetMapping("detail/{id}")
    public QuanLyDotGiamGiaResponse detail(@PathVariable("id") Integer id) {
        return dotGiamGiaService.getOne(id);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        dotGiamGiaService.remove(id);
    }

    @GetMapping("paging")
    public Page<QuanLyDotGiamGiaResponse> phanTrang(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "loaiGiam", required = false) Integer loaiGiam,
            @RequestParam(value = "tuNgay", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate tuNgay,
            @RequestParam(value = "denNgay", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate denNgay,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        return dotGiamGiaService.phanTrang(keyword, trangThai, loaiGiam, tuNgay, denNgay, pageNo, pageSize);
    }

    @PostMapping("add")
    public com.example.server.entity.DotGiamGia add(@Valid @RequestBody DotGiamGiaRequest request) {
        return dotGiamGiaService.add(request);
    }

    @PutMapping("update")
    public com.example.server.entity.DotGiamGia update(@RequestParam("id") Integer id, @Valid @RequestBody DotGiamGiaRequest request) {
        return dotGiamGiaService.update(id, request);
    }
}
