package com.example.server.core.admin.khachHang.controller;

import com.example.server.core.admin.khachHang.dto.request.CapNhatKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.request.DiaChiRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.khachHang.dto.request.TaoKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.responsse.DiaChiResponse;
import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;
import com.example.server.core.admin.khachHang.service.KhachHangService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/khach-hang")
public class KhachHangController {

    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<KhachHangResponse>>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách khách hàng thành công",
                khachHangService.layDanhSach(keyword, trangThai)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KhachHangResponse>> layChiTiet(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết khách hàng thành công",
                khachHangService.layChiTiet(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KhachHangResponse>> taoKhachHang(
            @Valid @RequestBody TaoKhachHangRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Tạo khách hàng thành công",
                khachHangService.taoKhachHang(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<KhachHangResponse>> capNhatKhachHang(
            @PathVariable UUID id,
            @Valid @RequestBody CapNhatKhachHangRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật khách hàng thành công",
                khachHangService.capNhatKhachHang(id, request)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<KhachHangResponse>> doiTrangThai(
            @PathVariable UUID id,
            @Valid @RequestBody DoiTrangThaiRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi trạng thái thành công",
                khachHangService.doiTrangThai(id, request)
        ));
    }

    @PatchMapping("/{id}/mat-khau")
    public ResponseEntity<ApiResponse<KhachHangResponse>> doiMatKhau(
            @PathVariable UUID id,
            @Valid @RequestBody DoiMatKhauRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi mật khẩu thành công",
                khachHangService.doiMatKhau(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaKhachHang(@PathVariable UUID id) {
        khachHangService.xoaKhachHang(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công", null));
    }

    // --- Address ---

    @GetMapping("/{id}/dia-chi")
    public ResponseEntity<ApiResponse<List<DiaChiResponse>>> layDanhSachDiaChi(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách địa chỉ thành công",
                khachHangService.layDanhSachDiaChi(id)
        ));
    }

    @PostMapping("/{id}/dia-chi")
    public ResponseEntity<ApiResponse<DiaChiResponse>> themDiaChi(
            @PathVariable UUID id,
            @Valid @RequestBody DiaChiRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Thêm địa chỉ thành công",
                khachHangService.themDiaChi(id, request)
        ));
    }

    @PutMapping("/dia-chi/{diaChiId}")
    public ResponseEntity<ApiResponse<DiaChiResponse>> capNhatDiaChi(
            @PathVariable Integer diaChiId,
            @Valid @RequestBody DiaChiRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật địa chỉ thành công",
                khachHangService.capNhatDiaChi(diaChiId, request)
        ));
    }

    @DeleteMapping("/dia-chi/{diaChiId}")
    public ResponseEntity<ApiResponse<Void>> xoaDiaChi(@PathVariable Integer diaChiId) {
        khachHangService.xoaDiaChi(diaChiId);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công", null));
    }
}
