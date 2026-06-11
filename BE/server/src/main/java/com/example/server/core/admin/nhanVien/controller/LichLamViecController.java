package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.LichLamViecResponse;
import com.example.server.core.admin.nhanVien.service.LichLamViecService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.AdminPrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/lich-lam-viec")
public class LichLamViecController {

    private final LichLamViecService lichLamViecService;

    public LichLamViecController(LichLamViecService lichLamViecService) {
        this.lichLamViecService = lichLamViecService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LichLamViecResponse>>> layLichLamViec(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy lịch làm việc thành công",
                lichLamViecService.layLichLamViecTheoTuan(tuNgay, denNgay)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LichLamViecResponse>> phanCa(
            @Valid @RequestBody PhanCaRequest request
    ) {
        assertIsAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                "Phân ca thành công",
                lichLamViecService.phanCa(request)
        ));
    }

    @PostMapping("/auto-assign")
    public ResponseEntity<ApiResponse<Void>> xepCaTuDong(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay
    ) {
        assertIsAdmin();
        lichLamViecService.xepCaTuDong(tuNgay, denNgay);
        return ResponseEntity.ok(ApiResponse.success("Xếp ca tự động thành công", null));
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
