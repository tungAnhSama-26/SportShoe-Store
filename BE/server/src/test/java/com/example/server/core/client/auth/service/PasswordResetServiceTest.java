package com.example.server.core.client.auth.service;

import com.example.server.core.client.auth.dto.request.ForgotPasswordRequest;
import com.example.server.core.client.auth.dto.request.ResetPasswordRequest;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.KhachHangRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private final KhachHangRepository khachHangRepository = mock(KhachHangRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final PasswordService passwordService = new PasswordService();
    private final PasswordResetService service = new PasswordResetService(
            khachHangRepository,
            emailService,
            passwordService
    );

    @Test
    void resetPasswordHashesNewPasswordAndRemovesOtp() {
        KhachHang khachHang = customer();
        when(khachHangRepository.findAll()).thenReturn(List.of(khachHang));

        service.forgotPassword(new ForgotPasswordRequest("customer@example.com"));
        String otp = captureOtp();

        service.resetPassword(new ResetPasswordRequest("customer@example.com", otp, "new-password"));

        assertNotEquals("new-password", khachHang.getMatKhau());
        assertTrue(passwordService.matches("new-password", khachHang.getMatKhau()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.resetPassword(new ResetPasswordRequest("customer@example.com", otp, "another-password"))
        );
        assertTrue(exception.getMessage().contains("Mã xác nhận không chính xác"));
    }

    @Test
    void removesOtpAfterFiveFailedAttempts() {
        when(khachHangRepository.findAll()).thenReturn(List.of(customer()));
        service.forgotPassword(new ForgotPasswordRequest("customer@example.com"));

        for (int i = 0; i < 4; i++) {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> service.resetPassword(new ResetPasswordRequest("customer@example.com", "000000", "new-password"))
            );
            assertTrue(exception.getMessage().contains("Mã xác nhận không chính xác"));
        }

        BusinessException lockedException = assertThrows(
                BusinessException.class,
                () -> service.resetPassword(new ResetPasswordRequest("customer@example.com", "000000", "new-password"))
        );
        assertTrue(lockedException.getMessage().contains("sai quá 5 lần"));

        BusinessException removedException = assertThrows(
                BusinessException.class,
                () -> service.resetPassword(new ResetPasswordRequest("customer@example.com", "000000", "new-password"))
        );
        assertTrue(removedException.getMessage().contains("Mã xác nhận không chính xác"));
    }

    private String captureOtp() {
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpEmail(org.mockito.ArgumentMatchers.eq("customer@example.com"), otpCaptor.capture());
        return otpCaptor.getValue();
    }

    private KhachHang customer() {
        KhachHang khachHang = new KhachHang();
        khachHang.setId(UUID.randomUUID());
        khachHang.setTenDangNhap("customer");
        khachHang.setEmail("customer@example.com");
        khachHang.setHoTen("Customer");
        khachHang.setMatKhau(passwordService.hash("old-password"));
        khachHang.setTrangThai(1);
        return khachHang;
    }
}
