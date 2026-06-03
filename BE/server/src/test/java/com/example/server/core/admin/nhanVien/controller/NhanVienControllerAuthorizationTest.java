package com.example.server.core.admin.nhanVien.controller;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;
import com.example.server.core.admin.nhanVien.service.NhanVienService;
import com.example.server.infrastructure.security.AdminPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NhanVienControllerAuthorizationTest {

    private final FakeNhanVienService nhanVienService = new FakeNhanVienService();
    private final NhanVienController controller = new NhanVienController(nhanVienService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void staffCannotUpdateAnotherEmployee() {
        UUID staffId = UUID.randomUUID();
        UUID anotherEmployeeId = UUID.randomUUID();
        authenticate(staffPrincipal(staffId));

        assertThrows(
                AccessDeniedException.class,
                () -> controller.capNhatNhanVien(anotherEmployeeId, updateRequest(2))
        );
    }

    @Test
    void staffCannotChangeOwnRole() {
        UUID staffId = UUID.randomUUID();
        authenticate(staffPrincipal(staffId));

        assertThrows(
                AccessDeniedException.class,
                () -> controller.capNhatNhanVien(staffId, updateRequest(1))
        );
    }

    @Test
    void staffCanUpdateOwnProfileWhenRoleIsUnchanged() {
        UUID staffId = UUID.randomUUID();
        authenticate(staffPrincipal(staffId));

        assertDoesNotThrow(() -> controller.capNhatNhanVien(staffId, updateRequest(2)));
        assertEquals(2, nhanVienService.lastUpdateRequest.vaiTro());
    }

    @Test
    void adminCanChangeEmployeeRole() {
        UUID adminId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        authenticate(new AdminPrincipal(adminId, "NV00001", "admin", "Admin", 1, "ADMIN"));

        assertDoesNotThrow(() -> controller.capNhatNhanVien(employeeId, updateRequest(1)));
        assertEquals(1, nhanVienService.lastUpdateRequest.vaiTro());
    }

    private void authenticate(AdminPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    private AdminPrincipal staffPrincipal(UUID id) {
        return new AdminPrincipal(id, "NV00002", "staff", "Staff", 2, "STAFF");
    }

    private CapNhatNhanVienRequest updateRequest(Integer vaiTro) {
        return new CapNhatNhanVienRequest(
                "Nhan Vien",
                "staff",
                "staff@example.com",
                "0987654321",
                "001086000001",
                "Nam",
                LocalDate.of(2000, 1, 1),
                "Ha Noi",
                null,
                vaiTro
        );
    }

    private NhanVienResponse response(UUID id, Integer vaiTro) {
        return new NhanVienResponse(
                id,
                "NV00002",
                "staff",
                "Nhan Vien",
                "staff@example.com",
                "0987654321",
                "001086000001",
                "Nam",
                LocalDate.of(2000, 1, 1),
                "Ha Noi",
                null,
                vaiTro,
                vaiTro == 1 ? "Admin" : "Nhan vien",
                1,
                "Dang lam",
                Instant.now(),
                null,
                null,
                null
        );
    }

    private class FakeNhanVienService implements NhanVienService {
        private CapNhatNhanVienRequest lastUpdateRequest;

        @Override
        public List<NhanVienResponse> layDanhSach(String keyword, Integer vaiTro, Integer trangThai) {
            return List.of();
        }

        @Override
        public NhanVienResponse layChiTiet(UUID id) {
            return response(id, 2);
        }

        @Override
        public NhanVienResponse layTheoCccd(String cccd) {
            return response(UUID.randomUUID(), 2);
        }

        @Override
        public NhanVienResponse taoNhanVien(TaoNhanVienRequest request) {
            return response(UUID.randomUUID(), request.vaiTro());
        }

        @Override
        public NhanVienResponse capNhatNhanVien(UUID id, CapNhatNhanVienRequest request) {
            lastUpdateRequest = request;
            return response(id, request.vaiTro());
        }

        @Override
        public NhanVienResponse doiTrangThai(UUID id, DoiTrangThaiRequest request) {
            return response(id, 2);
        }

        @Override
        public NhanVienResponse doiMatKhau(UUID id, DoiMatKhauRequest request) {
            return response(id, 2);
        }

        @Override
        public void xoaNhanVien(UUID id) {
        }
    }
}
