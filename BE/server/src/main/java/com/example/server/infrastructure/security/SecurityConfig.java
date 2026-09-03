package com.example.server.infrastructure.security;

import com.example.server.infrastructure.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/v1/upload", "/api/v1/upload/**").hasAnyRole("ADMIN", "STAFF", "CUSTOMER")
                        // SSE: để permitAll ở tầng filter (tránh AccessDenied khi async re-dispatch
                        // lúc đóng stream); quyền được kiểm tra trong HoaDonRealtimeBroker.subscribe().
                        .requestMatchers("/api/v1/realtime/**").permitAll()
                        .requestMatchers("/api/v1/client/khach-hang/**").hasAnyRole("ADMIN", "STAFF", "CUSTOMER")
                        .requestMatchers("/api/v1/client/don-hang/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/nhanvien/profile", "/api/v1/nhanvien/profile/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/ban-hang-tai-quay", "/api/v1/admin/ban-hang-tai-quay/**").permitAll()
                        .requestMatchers("/api/v1/admin/danh-muc", "/api/v1/admin/danh-muc/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/san-pham", "/api/v1/admin/san-pham/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/san-pham-chi-tiet", "/api/v1/admin/san-pham-chi-tiet/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/phieu-giam-gia", "/api/v1/admin/phieu-giam-gia/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/phieu-giam-gia-khach-hang", "/api/v1/admin/phieu-giam-gia-khach-hang/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/dot-giam-gia", "/api/v1/admin/dot-giam-gia/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/dot-giam-gia-san-pham", "/api/v1/admin/dot-giam-gia-san-pham/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/hoa-don", "/api/v1/admin/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/khach-hang", "/api/v1/admin/khach-hang/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/lich-lam-viec", "/api/v1/admin/lich-lam-viec/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/ca-lam", "/api/v1/admin/ca-lam/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/chat", "/api/v1/admin/chat/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/giao-ca", "/api/v1/admin/giao-ca/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/nhan-vien", "/api/v1/admin/nhan-vien/*").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/nhan-vien/*").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/danh-gia", "/api/v1/admin/danh-gia/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/thong-bao", "/api/v1/admin/thong-bao/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/thong-ke", "/api/v1/admin/thong-ke/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/chatbot/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        layLyDoTuChoi(request, "Vui lòng đăng nhập để tiếp tục")))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                        layLyDoTuChoi(request, "Bạn không có quyền truy cập chức năng này")))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Lấy lý do cụ thể do JwtAuthenticationFilter ghi lại (vd tài khoản bị khóa), nếu không có thì dùng mặc định. */
    private String layLyDoTuChoi(jakarta.servlet.http.HttpServletRequest request, String macDinh) {
        Object lyDo = request.getAttribute(JwtAuthenticationFilter.THUOC_TINH_LY_DO_TU_CHOI);
        return lyDo instanceof String chuoi && !chuoi.isBlank() ? chuoi : macDinh;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
