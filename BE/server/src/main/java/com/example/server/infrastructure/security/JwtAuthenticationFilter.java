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
            AdminPrincipal principal = jwtService.parseToken(authorization.substring(7).trim());
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(principal.id());
            if (nhanVienOpt.isEmpty() || nhanVienOpt.get().getTrangThai() != 1) {
                SecurityContextHolder.clearContext();
            } else {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
