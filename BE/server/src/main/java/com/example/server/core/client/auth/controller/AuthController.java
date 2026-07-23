package com.example.server.core.client.auth.controller;

import com.example.server.core.client.auth.dto.request.ForgotPasswordRequest;
import com.example.server.core.client.auth.dto.request.LoginRequest;
import com.example.server.core.client.auth.dto.request.RegisterRequest;
import com.example.server.core.client.auth.dto.request.ResetPasswordRequest;
import com.example.server.core.client.auth.dto.response.AdminLoginResponse;
import com.example.server.core.client.auth.dto.response.CustomerLoginResponse;
import com.example.server.core.client.auth.service.AuthService;
import com.example.server.core.client.auth.service.PasswordResetService;
import com.example.server.core.client.auth.service.RegistrationService;
import com.example.server.infrastructure.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.server.infrastructure.security.ratelimit.RateLimit;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthService authService,
            RegistrationService registrationService,
            PasswordResetService passwordResetService
    ) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    @RateLimit(limit = 10, durationInSeconds = 60)
    public ResponseEntity<ApiResponse<CustomerLoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng nhập thành công",
                authService.login(request)
        ));
    }

    @PostMapping("/admin/login")
    @RateLimit(limit = 10, durationInSeconds = 60)
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng nhập admin thành công",
                authService.adminLogin(request)
        ));
    }

    @PostMapping("/register")
    @RateLimit(limit = 5, durationInSeconds = 60)
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        registrationService.register(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Đăng ký thành công. Vui lòng kiểm tra email.",
                null
        ));
    }

    @PostMapping("/forgot-password")
    @RateLimit(limit = 3, durationInSeconds = 60)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Mã xác nhận đã được gửi đến email của bạn.",
                null
        ));
    }

    @PostMapping("/reset-password")
    @RateLimit(limit = 5, durationInSeconds = 60)
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Đặt lại mật khẩu thành công.",
                null
        ));
    }

}
