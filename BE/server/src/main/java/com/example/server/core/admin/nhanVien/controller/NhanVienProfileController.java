package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;
import com.example.server.core.admin.nhanVien.service.NhanVienService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nhanvien/profile")
public class NhanVienProfileController {

    private final NhanVienService nhanVienService;

    public NhanVienProfileController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NhanVienResponse>> layHoSoCaNhan() {
        AdminPrincipal principal = currentPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hồ sơ cá nhân thành công",
                nhanVienService.layChiTiet(principal.id())
        ));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NhanVienResponse>> capNhatHoSoCaNhan(
            @Valid @RequestBody CapNhatNhanVienRequest request
    ) {
        AdminPrincipal principal = currentPrincipal();
        CapNhatNhanVienRequest safeRequest = new CapNhatNhanVienRequest(
                request.hoTen(),
                request.tenDangNhap(),
                request.email(),
                request.sdt(),
                request.gioiTinh(),
                request.ngaySinh(),
                request.diaChi(),
                request.hinhAnh(),
                principal.vaiTro()
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật hồ sơ cá nhân thành công",
                nhanVienService.capNhatNhanVien(principal.id(), safeRequest)
        ));
    }

    @PatchMapping("/mat-khau")
    public ResponseEntity<ApiResponse<NhanVienResponse>> doiMatKhauCaNhan(
            @Valid @RequestBody DoiMatKhauRequest request
    ) {
        AdminPrincipal principal = currentPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                "Đổi mật khẩu thành công",
                nhanVienService.doiMatKhau(principal.id(), request)
        ));
    }

    private AdminPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;
        if (principal instanceof AdminPrincipal adminPrincipal) {
            return adminPrincipal;
        }
        throw new BusinessException("Vui lòng đăng nhập hệ thống admin");
    }
}
