package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;
import com.example.server.core.admin.nhanVien.service.NhanVienService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/nhan-vien")
public class NhanVienController {

    private final NhanVienService nhanVienService;

    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NhanVienResponse>>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer vaiTro,
            @RequestParam(required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay danh sach nhan vien thanh cong",
                nhanVienService.layDanhSach(keyword, vaiTro, trangThai)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NhanVienResponse>> layChiTiet(@PathVariable UUID id) {
        assertCanAccessProfile(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Lay chi tiet nhan vien thanh cong",
                nhanVienService.layChiTiet(id)
        ));
    }

    @GetMapping("/cccd/{cccd}")
    public ResponseEntity<ApiResponse<NhanVienResponse>> layTheoCccd(@PathVariable String cccd) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay nhan vien theo CCCD thanh cong",
                nhanVienService.layTheoCccd(cccd)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NhanVienResponse>> taoNhanVien(
            @Valid @RequestBody TaoNhanVienRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Tao nhan vien thanh cong",
                nhanVienService.taoNhanVien(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NhanVienResponse>> capNhatNhanVien(
            @PathVariable UUID id,
            @Valid @RequestBody CapNhatNhanVienRequest request
    ) {
        assertCanAccessProfile(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Cap nhat nhan vien thanh cong",
                nhanVienService.capNhatNhanVien(id, request)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<NhanVienResponse>> doiTrangThai(
            @PathVariable UUID id,
            @Valid @RequestBody DoiTrangThaiRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doi trang thai thanh cong",
                nhanVienService.doiTrangThai(id, request)
        ));
    }

    @PatchMapping("/{id}/mat-khau")
    public ResponseEntity<ApiResponse<NhanVienResponse>> doiMatKhau(
            @PathVariable UUID id,
            @Valid @RequestBody DoiMatKhauRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doi mat khau thanh cong",
                nhanVienService.doiMatKhau(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaNhanVien(@PathVariable UUID id) {
        nhanVienService.xoaNhanVien(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa nhan vien thanh cong", null));
    }

    private void assertCanAccessProfile(UUID employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof AdminPrincipal adminPrincipal
                && !"ADMIN".equals(adminPrincipal.role())
                && !adminPrincipal.id().equals(employeeId)) {
            throw new AccessDeniedException("Nhan vien chi duoc cap nhat thong tin cua chinh minh");
        }
    }
}
