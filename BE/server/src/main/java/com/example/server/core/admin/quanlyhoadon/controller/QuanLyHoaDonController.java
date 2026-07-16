package com.example.server.core.admin.quanlyhoadon.controller;

import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatSanPhamHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatThongTinGiaoHangRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatTrangThaiHoaDonRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.XacNhanHoanTienRequest;
import com.example.server.core.admin.quanlyhoadon.dto.request.XacNhanThanhToanCodRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonDetailResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.QuanLyHoaDonResponses.HoaDonSummaryResponse;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(name = "giaoCaId", required = false) UUID giaoCaId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách hóa đơn thành công",
                quanLyHoaDonService.layDanhSachHoaDon(keyword, loaiDon, trangThai, tuNgay, denNgay, giaoCaId)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> layChiTietHoaDon(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết hóa đơn thành công",
                quanLyHoaDonService.layChiTietHoaDon(id)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> capNhatTrangThaiHoaDon(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatTrangThaiHoaDonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái hóa đơn thành công",
                quanLyHoaDonService.capNhatTrangThaiHoaDon(id, request)
        ));
    }

    @PutMapping("/{id}/san-pham")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> capNhatSanPhamHoaDon(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatSanPhamHoaDonRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật sản phẩm hóa đơn thành công",
                quanLyHoaDonService.capNhatSanPhamHoaDon(id, request)
        ));
    }

    @PutMapping("/{id}/thong-tin-giao-hang")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> capNhatThongTinGiaoHang(
            @PathVariable Integer id,
            @Valid @RequestBody CapNhatThongTinGiaoHangRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật thông tin giao hàng thành công",
                quanLyHoaDonService.capNhatThongTinGiaoHang(id, request)
        ));
    }

    @PostMapping("/{id}/phi-van-chuyen/ghn")
    public ResponseEntity<ApiResponse<TinhPhiVanChuyenGhnResponse>> tinhPhiVanChuyenGhn(
            @PathVariable Integer id,
            @Valid @RequestBody TinhPhiVanChuyenGhnRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Tính phí vận chuyển GHN thành công",
                quanLyHoaDonService.tinhVaCapNhatPhiVanChuyenGhn(id, request)
        ));
    }

    @PostMapping("/{id}/thanh-toan-cod")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> xacNhanThanhToanCod(
            @PathVariable Integer id,
            @Valid @RequestBody XacNhanThanhToanCodRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Xác nhận thanh toán COD thành công",
                quanLyHoaDonService.xacNhanThanhToanCod(id, request)
        ));
    }

    @PostMapping("/{id}/hoan-tien")
    public ResponseEntity<ApiResponse<HoaDonDetailResponse>> xacNhanHoanTien(
            @PathVariable Integer id,
            @Valid @RequestBody XacNhanHoanTienRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Xác nhận hoàn tiền thành công",
                quanLyHoaDonService.xacNhanHoanTien(id, request)
        ));
    }

    @GetMapping("/{id}/check-mua-lai")
    public ResponseEntity<ApiResponse<com.example.server.core.admin.quanlyhoadon.dto.responsse.MuaLaiCheckResponse>> checkMuaLai(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Kiểm tra mua lại thành công",
                quanLyHoaDonService.checkMuaLai(id)
        ));
    }
}
