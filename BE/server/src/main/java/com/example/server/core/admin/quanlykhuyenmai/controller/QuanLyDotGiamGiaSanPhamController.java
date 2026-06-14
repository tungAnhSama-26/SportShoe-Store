package com.example.server.core.admin.quanlykhuyenmai.controller;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaSanPhamRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaSanPhamBulkRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse;
import com.example.server.core.admin.quanlykhuyenmai.service.DotGiamGiaSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dot-giam-gia-san-pham")
public class QuanLyDotGiamGiaSanPhamController {

    @Autowired
    private DotGiamGiaSanPhamService dotGiamGiaSanPhamService;

    @GetMapping
    public List<QuanLyDotGiamGiaSanPhamResponse> getAll() {
        return dotGiamGiaSanPhamService.getAll();
    }

    @GetMapping("detail/{id}")
    public QuanLyDotGiamGiaSanPhamResponse detail(@PathVariable("id") Integer id) {
        return dotGiamGiaSanPhamService.getOne(id);
    }

    @DeleteMapping("delete")
    public void delete(@RequestParam("id") Integer id) {
        dotGiamGiaSanPhamService.remove(id);
    }

    @GetMapping("paging")
    public List<QuanLyDotGiamGiaSanPhamResponse> phanTrang(
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        return dotGiamGiaSanPhamService.phanTrang(pageNo, pageSize).getContent();
    }

    @PostMapping("add")
    public void add(@Valid @RequestBody DotGiamGiaSanPhamRequest request) {
        dotGiamGiaSanPhamService.add(request);
    }

    @PutMapping("update")
    public void update(@RequestParam("id") Integer id, @Valid @RequestBody DotGiamGiaSanPhamRequest request) {
        dotGiamGiaSanPhamService.update(id, request);
    }

    @PostMapping("bulk-sync")
    public void bulkSync(@Valid @RequestBody DotGiamGiaSanPhamBulkRequest request) {
        dotGiamGiaSanPhamService.saveAll(request);
    }
}
