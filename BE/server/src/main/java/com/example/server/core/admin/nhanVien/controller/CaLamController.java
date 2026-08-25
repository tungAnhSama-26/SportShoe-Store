package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.CaLamRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiCaLamRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CaLamResponse;
import com.example.server.core.admin.nhanVien.service.CaLamService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ca-lam")
public class CaLamController {

    private final CaLamService caLamService;

    public CaLamController(CaLamService caLamService) {
        this.caLamService = caLamService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CaLamResponse>>> layDanhSachCaLam() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách ca làm việc thành công",
                caLamService.layDanhSachCaLam()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CaLamResponse>> taoCaLam(
            @Valid @RequestBody CaLamRequest request
    ) {
        assertIsAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo ca làm việc thành công",
                caLamService.taoCaLam(request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaLamResponse>> capNhatCaLam(
            @PathVariable String id,
            @Valid @RequestBody CaLamRequest request
    ) {
        assertIsAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật ca làm việc thành công",
                caLamService.capNhatCaLam(id, request)
        ));
    }

    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<ApiResponse<CaLamResponse>> doiTrangThaiCaLam(
            @PathVariable String id,
            @Valid @RequestBody DoiTrangThaiCaLamRequest request
    ) {
        assertIsAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi trạng thái ca làm việc thành công",
                caLamService.doiTrangThaiCaLam(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoaCaLam(
            @PathVariable String id
    ) {
        assertIsAdmin();
        caLamService.xoaCaLam(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Xóa ca làm việc thành công",
                null
        ));
    }

    private void assertIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof AdminPrincipal adminPrincipal) {
            if (!"ADMIN".equals(adminPrincipal.role())) {
                throw new AccessDeniedException("Chỉ quản trị viên mới được thực hiện chức năng này");
            }
        } else {
            throw new AccessDeniedException("Vui lòng đăng nhập hệ thống admin");
        }
    }
}
