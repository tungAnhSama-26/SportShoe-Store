package com.example.server.core.client.auth.service;

import com.example.server.core.client.auth.dto.request.RegisterRequest;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.KhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class RegistrationService {

    private final KhachHangRepository khachHangRepository;
    private final EmailService emailService;
    private final PasswordService passwordService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public RegistrationService(
            KhachHangRepository khachHangRepository,
            EmailService emailService,
            PasswordService passwordService
    ) {
        this.khachHangRepository = khachHangRepository;
        this.emailService = emailService;
        this.passwordService = passwordService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (khachHangRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã được sử dụng");
        }

        String username = request.email().split("@")[0];
        String originalUsername = username;
        int counter = 1;
        while (khachHangRepository.existsByTenDangNhap(username)) {
            username = originalUsername + counter++;
        }

        String password = generateNumericCode();

        KhachHang kh = new KhachHang();
        kh.setId(UUID.randomUUID());
        kh.setHoTen(request.hoTen());
        kh.setEmail(request.email());
        kh.setSdt(request.sdt());
        kh.setTenDangNhap(username);
        kh.setMatKhau(passwordService.hash(password));
        kh.setTrangThai(1);
        kh.setNgayTao(Instant.now());

        khachHangRepository.save(kh);
        emailService.sendCustomerRegistrationEmail(kh.getEmail(), kh.getHoTen(), kh.getTenDangNhap(), password);
    }

    private String generateNumericCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
