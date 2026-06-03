package com.example.server.infrastructure.security;

import com.example.server.entity.NhanVien;
import com.example.server.repository.NhanVienRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final NhanVienRepository nhanVienRepository;

    public JwtAuthenticationFilter(JwtService jwtService, NhanVienRepository nhanVienRepository) {
        this.jwtService = jwtService;
        this.nhanVienRepository = nhanVienRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            ParsedAdminToken parsedToken = jwtService.parseAdminToken(authorization.substring(7).trim());
            AdminPrincipal principal = parsedToken.principal();
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(principal.id());
            if (nhanVienOpt.isEmpty() || nhanVienOpt.get().getTrangThai() != 1) {
                SecurityContextHolder.clearContext();
            } else {
                NhanVien nhanVien = nhanVienOpt.get();
                if (parsedToken.authVersion() != resolveAuthVersion(nhanVien)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                String currentRole = resolveRole(nhanVien.getVaiTro());
                AdminPrincipal currentPrincipal = new AdminPrincipal(
                        nhanVien.getId(),
                        nhanVien.getMa(),
                        nhanVien.getTenDangNhap(),
                        nhanVien.getHoTen(),
                        normalizeVaiTro(nhanVien.getVaiTro()),
                        currentRole
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        currentPrincipal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + currentRole))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveRole(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? "ADMIN" : "STAFF";
    }

    private Integer normalizeVaiTro(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? 1 : 2;
    }

    private long resolveAuthVersion(NhanVien nhanVien) {
        Instant ngayCapNhat = nhanVien.getNgayCapNhat();
        return ngayCapNhat != null ? ngayCapNhat.toEpochMilli() : 0L;
    }
}
