package com.example.server.core.client.auth.service;

import com.example.server.core.client.auth.dto.request.ForgotPasswordRequest;
import com.example.server.core.client.auth.dto.request.ResetPasswordRequest;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.KhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final KhachHangRepository khachHangRepository;
    private final EmailService emailService;
    private final PasswordService passwordService;
    private final Map<String, OtpChallenge> otpStorage = new ConcurrentHashMap<>();

    public PasswordResetService(
            KhachHangRepository khachHangRepository,
            EmailService emailService,
            PasswordService passwordService
    ) {
        this.khachHangRepository = khachHangRepository;
        this.emailService = emailService;
        this.passwordService = passwordService;
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        KhachHang kh = findCustomerByAccount(request.account())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản với thông tin này"));

        if (kh.getEmail() == null || kh.getEmail().isBlank()) {
            throw new BusinessException("Tài khoản này chưa cập nhật email. Vui lòng liên hệ hỗ trợ.");
        }

        String otp = generateNumericCode();
        emailService.sendOtpEmail(kh.getEmail(), otp);
        otpStorage.put(normalizeAccount(request.account()), new OtpChallenge(
                passwordService.hash(otp),
                Instant.now().plus(OTP_TTL),
                0
        ));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String accountKey = normalizeAccount(request.account());
        OtpChallenge challenge = otpStorage.get(accountKey);

        if (challenge == null) {
            throw new BusinessException("Mã xác nhận không chính xác hoặc đã hết hạn");
        }

        if (challenge.isExpired()) {
            otpStorage.remove(accountKey);
            throw new BusinessException("Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        if (!passwordService.matches(request.otp(), challenge.otpHash())) {
            int nextFailedAttempts = challenge.failedAttempts() + 1;
            if (nextFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                otpStorage.remove(accountKey);
                throw new BusinessException("Mã xác nhận đã nhập sai quá 5 lần. Vui lòng yêu cầu mã mới.");
            }
            otpStorage.put(accountKey, challenge.withFailedAttempts(nextFailedAttempts));
            throw new BusinessException("Mã xác nhận không chính xác. Bạn còn "
                    + (MAX_FAILED_ATTEMPTS - nextFailedAttempts) + " lần thử.");
        }

        KhachHang kh = findCustomerByAccount(request.account())
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại"));

        kh.setMatKhau(passwordService.hash(request.newPassword()));
        khachHangRepository.save(kh);
        otpStorage.remove(accountKey);
    }

    private Optional<KhachHang> findCustomerByAccount(String account) {
        String accountKey = normalizeAccount(account);
        return khachHangRepository.findAll().stream()
                .filter(kh -> normalizeAccount(kh.getTenDangNhap()).equals(accountKey)
                        || (kh.getEmail() != null && normalizeAccount(kh.getEmail()).equals(accountKey)))
                .findFirst();
    }

    private String normalizeAccount(String account) {
        return account == null ? "" : account.trim().toLowerCase();
    }

    private String generateNumericCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private record OtpChallenge(
            String otpHash,
            Instant expiredAt,
            int failedAttempts
    ) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiredAt);
        }

        private OtpChallenge withFailedAttempts(int failedAttempts) {
            return new OtpChallenge(otpHash, expiredAt, failedAttempts);
        }
    }
}
