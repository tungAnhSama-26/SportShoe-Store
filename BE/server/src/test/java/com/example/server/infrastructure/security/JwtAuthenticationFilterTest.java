package com.example.server.infrastructure.security;

import com.example.server.entity.NhanVien;
import com.example.server.repository.NhanVienRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final NhanVienRepository nhanVienRepository = mock(NhanVienRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, nhanVienRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesCurrentStaffRoleFromDatabaseInsteadOfTokenRole() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(jwtService.parseAdminToken("token")).thenReturn(
                new ParsedAdminToken(new AdminPrincipal(employeeId, "NV00002", "staff", "Staff", 1, "ADMIN"), 0L)
        );
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee(employeeId, 2)));

        Authentication authentication = runFilterAndCaptureAuthentication("Bearer token");

        assertNotNull(authentication);
        assertEquals("ROLE_STAFF", authentication.getAuthorities().iterator().next().getAuthority());
        assertEquals(2, ((AdminPrincipal) authentication.getPrincipal()).vaiTro());
        assertEquals("STAFF", ((AdminPrincipal) authentication.getPrincipal()).role());
    }

    @Test
    void keepsAdminRoleWhenDatabaseRoleIsAdmin() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(jwtService.parseAdminToken("token")).thenReturn(
                new ParsedAdminToken(new AdminPrincipal(employeeId, "NV00001", "admin", "Admin", 2, "STAFF"), 0L)
        );
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee(employeeId, 1)));

        Authentication authentication = runFilterAndCaptureAuthentication("Bearer token");

        assertNotNull(authentication);
        assertEquals("ROLE_ADMIN", authentication.getAuthorities().iterator().next().getAuthority());
        assertEquals(1, ((AdminPrincipal) authentication.getPrincipal()).vaiTro());
        assertEquals("ADMIN", ((AdminPrincipal) authentication.getPrincipal()).role());
    }

    @Test
    void rejectsTokenWhenAuthVersionIsStale() throws Exception {
        UUID employeeId = UUID.randomUUID();
        NhanVien employee = employee(employeeId, 1);
        employee.setNgayCapNhat(Instant.ofEpochMilli(12345));
        when(jwtService.parseAdminToken("token")).thenReturn(
                new ParsedAdminToken(new AdminPrincipal(employeeId, "NV00001", "admin", "Admin", 1, "ADMIN"), 0L)
        );
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        Authentication authentication = runFilterAndCaptureAuthentication("Bearer token");

        assertEquals(null, authentication);
    }

    private Authentication runFilterAndCaptureAuthentication(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", authorizationHeader);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> authentication = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                authentication.set(SecurityContextHolder.getContext().getAuthentication());

        filter.doFilter(request, response, chain);
        return authentication.get();
    }

    private NhanVien employee(UUID id, Integer vaiTro) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(id);
        nhanVien.setMa(vaiTro == 1 ? "NV00001" : "NV00002");
        nhanVien.setTenDangNhap(vaiTro == 1 ? "admin" : "staff");
        nhanVien.setHoTen(vaiTro == 1 ? "Admin" : "Staff");
        nhanVien.setEmail(vaiTro == 1 ? "admin@example.com" : "staff@example.com");
        nhanVien.setMatKhau("111111");
        nhanVien.setVaiTro(vaiTro);
        nhanVien.setTrangThai(1);
        return nhanVien;
    }
}
