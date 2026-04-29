package com.example.server.core.client.auth.controller;

import com.example.server.core.client.auth.dto.request.LoginRequest;
import com.example.server.core.client.auth.dto.request.RegisterRequest;
import com.example.server.core.client.auth.dto.request.ForgotPasswordRequest;
import com.example.server.core.client.auth.dto.request.ResetPasswordRequest;
import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.DiaChiKhachHangRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final KhachHangRepository khachHangRepository;
    private final EmailService emailService;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    
    private static final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public AuthController(KhachHangRepository khachHangRepository, EmailService emailService, DiaChiKhachHangRepository diaChiKhachHangRepository) {
        this.khachHangRepository = khachHangRepository;
        this.emailService = emailService;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<KhachHangResponse>> login(@Valid @RequestBody LoginRequest request) {
        Optional<KhachHang> khOptional = khachHangRepository.findAll().stream()
                .filter(kh -> kh.getTenDangNhap().equals(request.username()) || (kh.getEmail() != null && kh.getEmail().equals(request.username())))
                .findFirst();

        if (khOptional.isEmpty()) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        KhachHang kh = khOptional.get();

        if (!kh.getMatKhau().equals(request.password())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        if (kh.getTrangThai() != 1) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Đăng nhập thành công",
                toKhachHangResponse(kh)
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        if (khachHangRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã được sử dụng");
        }

        String username = request.email().split("@")[0];
        String originalUsername = username;
        int counter = 1;
        while (khachHangRepository.existsByTenDangNhap(username)) {
            username = originalUsername + counter++;
        }

        String password = String.format("%06d", new Random().nextInt(999999));

        KhachHang kh = new KhachHang();
        kh.setId(UUID.randomUUID());
        kh.setHoTen(request.hoTen());
        kh.setEmail(request.email());
        kh.setSdt(request.sdt());
        kh.setTenDangNhap(username);
        kh.setMatKhau(password); 
        kh.setTrangThai(1);
        kh.setNgayTao(Instant.now());

        khachHangRepository.save(kh);

        try {
            emailService.sendCustomerRegistrationEmail(kh.getEmail(), kh.getHoTen(), kh.getTenDangNhap(), password);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email.", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Optional<KhachHang> khOptional = khachHangRepository.findAll().stream()
                .filter(kh -> kh.getTenDangNhap().equals(request.account()) || (kh.getEmail() != null && kh.getEmail().equals(request.account())))
                .findFirst();

        if (khOptional.isEmpty()) {
            throw new BusinessException("Không tìm thấy tài khoản với thông tin này");
        }

        KhachHang kh = khOptional.get();
        if (kh.getEmail() == null || kh.getEmail().isEmpty()) {
            throw new BusinessException("Tài khoản này chưa cập nhật email. Vui lòng liên hệ hỗ trợ.");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(request.account(), otp);

        try {
            emailService.sendOtpEmail(kh.getEmail(), otp);
        } catch (Exception e) {
            throw new BusinessException("Gửi mã xác nhận thất bại: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("Mã xác nhận đã được gửi đến email của bạn.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String storedOtp = otpStorage.get(request.account());
        
        if (storedOtp == null || !storedOtp.equals(request.otp())) {
            throw new BusinessException("Mã xác nhận không chính xác hoặc đã hết hạn");
        }

        Optional<KhachHang> khOptional = khachHangRepository.findAll().stream()
                .filter(kh -> kh.getTenDangNhap().equals(request.account()) || (kh.getEmail() != null && kh.getEmail().equals(request.account())))
                .findFirst();

        if (khOptional.isEmpty()) {
            throw new BusinessException("Tài khoản không tồn tại");
        }

        KhachHang kh = khOptional.get();
        kh.setMatKhau(request.newPassword()); 
        khachHangRepository.save(kh);

        otpStorage.remove(request.account());

        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công.", null));
    }

    private KhachHangResponse toKhachHangResponse(KhachHang kh) {
        String diaChiMacDinh = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrue(kh.getId())
                .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuongXa() + ", " + dc.getQuanHuyen() + ", " + dc.getTinhThanh())
                .orElse(null);

        return new KhachHangResponse(
                kh.getId(),
                kh.getTenDangNhap(),
                kh.getHoTen(),
                kh.getEmail(),
                kh.getSdt(),
                kh.getNgaySinh(),
                kh.getHinhAnh(),
                kh.getTrangThai(),
                kh.getTrangThai() == 1 ? "Đang hoạt động" : "Ngừng hoạt động",
                kh.getNgayTao(),
                diaChiMacDinh
        );
    }
}
