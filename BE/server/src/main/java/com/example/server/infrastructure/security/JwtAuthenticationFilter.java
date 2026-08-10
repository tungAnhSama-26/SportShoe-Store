package com.example.server.infrastructure.security;

import com.example.server.entity.NhanVien;
import com.example.server.entity.KhachHang;
import com.example.server.repository.KhachHangRepository;
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
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            NhanVienRepository nhanVienRepository,
            KhachHangRepository khachHangRepository
    ) {
        this.jwtService = jwtService;
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
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
            String token = authorization.substring(7).trim();
            ParsedSubjectToken subjectToken = jwtService.parseSubjectToken(token);
            if ("CUSTOMER".equals(subjectToken.role())) {
                authenticateCustomer(token);
                filterChain.doFilter(request, response);
                return;
            }

            ParsedAdminToken parsedToken = jwtService.parseAdminToken(token);
            AdminPrincipal principal = parsedToken.principal();
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(principal.id());
            if (nhanVienOpt.isEmpty()
                    || (nhanVienOpt.get().getTrangThai() != 1
                    && nhanVienOpt.get().getTrangThai() != 2)) {
                SecurityContextHolder.clearContext();
            } else {
                NhanVien nhanVien = nhanVienOpt.get();
                Integer currentVaiTro = normalizeVaiTro(nhanVien.getVaiTro());
                if (!currentVaiTro.equals(normalizeVaiTro(principal.vaiTro()))) {
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
                        currentVaiTro,
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

    private void authenticateCustomer(String token) {
        CustomerPrincipal tokenPrincipal = jwtService.parseCustomerToken(token).principal();
        Optional<KhachHang> customerOpt = khachHangRepository.findById(tokenPrincipal.id());
        if (customerOpt.isEmpty() || customerOpt.get().getTrangThai() != 1) {
            SecurityContextHolder.clearContext();
            return;
        }

        KhachHang customer = customerOpt.get();
        CustomerPrincipal currentPrincipal = new CustomerPrincipal(
                customer.getId(),
                customer.getTenDangNhap(),
                customer.getHoTen(),
                "CUSTOMER"
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                currentPrincipal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveRole(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? "ADMIN" : "STAFF";
    }

    private Integer normalizeVaiTro(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? 1 : 2;
    }
}
