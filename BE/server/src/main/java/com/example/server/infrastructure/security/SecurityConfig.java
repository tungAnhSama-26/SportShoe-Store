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
                        .requestMatchers("/api/v1/client/tra-hang/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/nhanvien/profile", "/api/v1/nhanvien/profile/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/ban-hang-tai-quay", "/api/v1/admin/ban-hang-tai-quay/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/hoa-don", "/api/v1/admin/hoa-don/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/tra-hang", "/api/v1/admin/tra-hang/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/khach-hang", "/api/v1/admin/khach-hang/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/lich-lam-viec", "/api/v1/admin/lich-lam-viec/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/cham-cong", "/api/v1/admin/cham-cong/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/chat", "/api/v1/admin/chat/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/nhan-vien/*").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/nhan-vien/*").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
