package com.example.server.core.admin.quanlyhoadon.controller;

import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatSanPhamHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatTrangThaiHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonDetailResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonSummaryResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/hoa-don")
public class QuanLyHoaDonController {

    private final QuanLyHoaDonService quanLyHoaDonService;

    public QuanLyHoaDonController(QuanLyHoaDonService quanLyHoaDonService) {
        this.quanLyHoaDonService = quanLyHoaDonService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HoaDonSummaryResponse>>> layDanhSachHoaDon(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "loaiDon", required = false) String loaiDon,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach hoa don thanh cong",
                quanLyHoaDonService.layDanhSachHoaDon(keyword, loaiDon, trangThai, tuNgay, denNgay)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> layChiTietHoaDon(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet hoa don thanh cong",
                quanLyHoaDonService.layChiTietHoaDon(id)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> capNhatTrangThaiHoaDon(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatTrangThaiHoaDonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat trang thai hoa don thanh cong",
                quanLyHoaDonService.capNhatTrangThaiHoaDon(id, request)
        ));
    }

    @PutMapping("/{id}/san-pham")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> capNhatSanPhamHoaDon(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatSanPhamHoaDonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat san pham hoa don thanh cong",
                quanLyHoaDonService.capNhatSanPhamHoaDon(id, request)
        ));
    }

    @PostMapping("/{id}/phi-van-chuyen/ghn")
    public ResponseEntity<ApiResponse<TinhPhiVanChuyenGhnResponse>> tinhPhiVanChuyenGhn(
            @PathVariable Integer id,
            @Valid @RequestBody TinhPhiVanChuyenGhnRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tinh phi van chuyen GHN thanh cong",
                quanLyHoaDonService.tinhVaCapNhatPhiVanChuyenGhn(id, request)
        ));
    }
}
