package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;
import com.example.server.core.admin.nhanVien.service.NhanVienService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.exception.BusinessException;
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
                "Lấy danh sách nhân viên thành công",
                nhanVienService.layDanhSach(keyword, vaiTro, trangThai)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NhanVienResponse>> layChiTiet(@PathVariable UUID id) {
        assertCanAccessProfile(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết nhân viên thành công",
                nhanVienService.layChiTiet(id)
        ));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<NhanVienResponse>> taoNhanVien(
            @Valid @RequestBody TaoNhanVienRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Tạo nhân viên thành công",
                nhanVienService.taoNhanVien(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NhanVienResponse>> capNhatNhanVien(
            @PathVariable UUID id,
            @Valid @RequestBody CapNhatNhanVienRequest request
    ) {
        assertCanUpdateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật nhân viên thành công",
                nhanVienService.capNhatNhanVien(id, request)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<NhanVienResponse>> doiTrangThai(
            @PathVariable UUID id,
            @Valid @RequestBody DoiTrangThaiRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal adminPrincipal) {
            if (adminPrincipal.id().equals(id) && request.trangThai() == 0) {
                throw new BusinessException("Bạn không thể tự khóa tài khoản của chính mình");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi trạng thái thành công",
                nhanVienService.doiTrangThai(id, request)
        ));
    }

    @PatchMapping("/{id}/mat-khau")
    public ResponseEntity<ApiResponse<NhanVienResponse>> doiMatKhau(
            @PathVariable UUID id,
            @Valid @RequestBody DoiMatKhauRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi mật khẩu thành công",
                nhanVienService.doiMatKhau(id, request)
        ));
    }

    @PutMapping("/{id}/face-id")
    public ResponseEntity<ApiResponse<NhanVienResponse>> capNhatFaceId(
            @PathVariable UUID id,
            @Valid @RequestBody com.example.server.core.admin.nhanVien.dto.request.CapNhatFaceIdRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật dữ liệu khuôn mặt thành công",
                nhanVienService.capNhatFaceId(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaNhanVien(@PathVariable UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal adminPrincipal) {
            if (adminPrincipal.id().equals(id)) {
                throw new BusinessException("Bạn không thể tự xóa tài khoản của chính mình");
            }
        }
        nhanVienService.xoaNhanVien(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
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

    private void assertCanUpdateProfile(UUID employeeId, CapNhatNhanVienRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (!(principal instanceof AdminPrincipal adminPrincipal) || "ADMIN".equals(adminPrincipal.role())) {
            return;
        }
        if (!adminPrincipal.id().equals(employeeId)) {
            throw new AccessDeniedException("Nhan vien chi duoc cap nhat thong tin cua chinh minh");
        }
        if (request.vaiTro() != null && !request.vaiTro().equals(adminPrincipal.vaiTro())) {
            throw new AccessDeniedException("Nhan vien khong duoc tu thay doi vai tro");
        }
    }
}
