package com.example.server.core.client.auth.service;

import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;
import com.example.server.core.client.auth.dto.request.LoginRequest;
import com.example.server.core.client.auth.dto.response.AdminLoginResponse;
import com.example.server.core.client.auth.dto.response.CustomerLoginResponse;
import com.example.server.entity.KhachHang;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.AdminPrincipal;
import com.example.server.infrastructure.security.CustomerPrincipal;
import com.example.server.infrastructure.security.JwtService;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;

    private static final int MAX_LOGIN_FAILED_ATTEMPTS = 5;
    private static final Duration LOGIN_ATTEMPT_TTL = Duration.ofMinutes(15);
    private static final Duration TEMP_PASSWORD_CHANGE_DEADLINE = Duration.ofMinutes(5);
    private static final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    public AuthService(
            KhachHangRepository khachHangRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository,
            NhanVienRepository nhanVienRepository,
            JwtService jwtService,
            PasswordService passwordService
    ) {
        this.khachHangRepository = khachHangRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.jwtService = jwtService;
        this.passwordService = passwordService;
    }

    @Transactional
    public CustomerLoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        String attemptKey = loginAttemptKey("customer", username);
        assertLoginAllowed(attemptKey);

        Optional<KhachHang> khOptional = khachHangRepository.findAll().stream()
                .filter(kh -> kh.getTenDangNhap().equals(username))
                .findFirst();

        if (khOptional.isEmpty()) {
            recordFailedLogin(attemptKey);
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        KhachHang kh = khOptional.get();
        if (!passwordService.matches(request.password(), kh.getMatKhau())) {
            recordFailedLogin(attemptKey);
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        if (kh.getTrangThai() != 1) {
            recordFailedLogin(attemptKey);
            throw new BusinessException("Tài khoản đã bị khóa");
        }

        clearFailedLogin(attemptKey);
        migratePasswordIfNeeded(kh, request.password());
        String token = jwtService.generateCustomerToken(new CustomerPrincipal(
                kh.getId(),
                kh.getTenDangNhap(),
                kh.getHoTen(),
                "CUSTOMER"
        ));
        return new CustomerLoginResponse(token, "Bearer", toKhachHangResponse(kh));
    }

    @Transactional
    public AdminLoginResponse adminLogin(LoginRequest request) {
        String username = request.username().trim();
        String attemptKey = loginAttemptKey("admin", username);
        assertLoginAllowed(attemptKey);

        Optional<NhanVien> nvOptional = nhanVienRepository.findByTenDangNhapIgnoreCase(username);

        if (nvOptional.isEmpty()) {
            recordFailedLogin(attemptKey);
            throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác");
        }

        NhanVien nhanVien = nvOptional.get();
        if (!passwordService.matches(request.password(), nhanVien.getMatKhau())) {
            recordFailedLogin(attemptKey);
            throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác");
        }

        if (nhanVien.getTrangThai() == null || nhanVien.getTrangThai() != 1) {
            recordFailedLogin(attemptKey);
            throw new BusinessException("Tài khoản nhân viên đã bị khóa");
        }

        clearFailedLogin(attemptKey);
        migratePasswordIfNeeded(nhanVien, request.password());
        initializeTemporaryPasswordDeadlineIfNeeded(nhanVien);

        Integer vaiTro = normalizeVaiTro(nhanVien.getVaiTro());
        String role = resolveAdminRole(vaiTro);
        String token = jwtService.generateToken(new AdminPrincipal(
                nhanVien.getId(),
                nhanVien.getMa(),
                nhanVien.getTenDangNhap(),
                nhanVien.getHoTen(),
                vaiTro,
                role
        ), resolveAuthVersion(nhanVien));

        return new AdminLoginResponse(
                token,
                "Bearer",
                nhanVien.getId(),
                nhanVien.getMa(),
                nhanVien.getTenDangNhap(),
                nhanVien.getHoTen(),
                nhanVien.getEmail(),
                nhanVien.getCccd(),
                vaiTro,
                isAdmin(nhanVien) ? "Quản trị viên" : "Nhân viên",
                nhanVien.getHinhAnh(),
                mustChangeTemporaryPassword(nhanVien),
                mustChangeTemporaryPassword(nhanVien) ? nhanVien.getHanDoiMatKhau() : null,
                nhanVien.getFaceDescriptor()
        );
    }

    private KhachHangResponse toKhachHangResponse(KhachHang kh) {
        var diaChiMacDinhOpt = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrue(kh.getId());
        String diaChiMacDinh = diaChiMacDinhOpt
                .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuongXa() + ", " + dc.getQuanHuyen() + ", " + dc.getTinhThanh())
                .orElse(null);
        String sdtMacDinh = diaChiMacDinhOpt.map(dc -> dc.getSdt()).orElse(null);

        return new KhachHangResponse(
                kh.getId(),
                kh.getTenDangNhap(),
                kh.getHoTen(),
                kh.getEmail(),
                kh.getSdt(),
                kh.getNgaySinh(),
                kh.getGioiTinh(),
                tenGioiTinh(kh.getGioiTinh()),
                kh.getHinhAnh(),
                kh.getTrangThai(),
                kh.getTrangThai() == 1 ? "Đang hoạt động" : "Ngừng hoạt động",
                kh.getNgayTao(),
                diaChiMacDinh,
                sdtMacDinh,
                null,
                null
        );
    }

    private String tenGioiTinh(Integer gioiTinh) {
        if (gioiTinh == null) {
            return null;
        }
        return switch (gioiTinh) {
            case 0 -> "Nữ";
            case 1 -> "Nam";
            case 2 -> "Khác";
            default -> null;
        };
    }

    private boolean isAdmin(NhanVien nhanVien) {
        return Integer.valueOf(1).equals(nhanVien.getVaiTro());
    }

    private boolean mustChangeTemporaryPassword(NhanVien nhanVien) {
        return !isAdmin(nhanVien)
                && Boolean.TRUE.equals(nhanVien.getBatBuocDoiMatKhau())
                && nhanVien.getHanDoiMatKhau() != null;
    }

    private void initializeTemporaryPasswordDeadlineIfNeeded(NhanVien nhanVien) {
        if (isAdmin(nhanVien)
                || !Boolean.TRUE.equals(nhanVien.getBatBuocDoiMatKhau())
                || nhanVien.getHanDoiMatKhau() != null) {
            return;
        }
        Instant now = Instant.now();
        nhanVien.setHanDoiMatKhau(now.plus(TEMP_PASSWORD_CHANGE_DEADLINE));
        nhanVien.setNgayCapNhat(now);
        nhanVienRepository.save(nhanVien);
    }

    private String resolveAdminRole(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? "ADMIN" : "STAFF";
    }

    private Integer normalizeVaiTro(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? 1 : 2;
    }

    private long resolveAuthVersion(NhanVien nhanVien) {
        Instant ngayCapNhat = nhanVien.getNgayCapNhat();
        return ngayCapNhat != null ? ngayCapNhat.toEpochMilli() : 0L;
    }

    private void migratePasswordIfNeeded(KhachHang khachHang, String rawPassword) {
        if (passwordService.needsRehash(khachHang.getMatKhau())) {
            khachHang.setMatKhau(passwordService.hash(rawPassword));
            khachHangRepository.save(khachHang);
        }
    }

    private void migratePasswordIfNeeded(NhanVien nhanVien, String rawPassword) {
        if (passwordService.needsRehash(nhanVien.getMatKhau())) {
            nhanVien.setMatKhau(passwordService.hash(rawPassword));
            nhanVienRepository.save(nhanVien);
        }
    }

    private String loginAttemptKey(String scope, String username) {
        return scope + ":" + String.valueOf(username).trim().toLowerCase(Locale.ROOT);
    }

    private void assertLoginAllowed(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null) {
            return;
        }
        if (attempt.isExpired()) {
            loginAttempts.remove(key);
            return;
        }
        if (attempt.failedAttempts() >= MAX_LOGIN_FAILED_ATTEMPTS) {
            throw tooManyLoginAttempts();
        }
    }

    private void recordFailedLogin(String key) {
        LoginAttempt attempt = loginAttempts.compute(key, (ignored, current) -> {
            if (current == null || current.isExpired()) {
                return new LoginAttempt(1, Instant.now().plus(LOGIN_ATTEMPT_TTL));
            }
            return current.withFailedAttempts(current.failedAttempts() + 1);
        });
        if (attempt != null && attempt.failedAttempts() >= MAX_LOGIN_FAILED_ATTEMPTS) {
            throw tooManyLoginAttempts();
        }
    }

    private void clearFailedLogin(String key) {
        loginAttempts.remove(key);
    }

    private ResponseStatusException tooManyLoginAttempts() {
        return new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Bạn đã đăng nhập sai quá nhiều lần. Vui lòng chờ 15 phút rồi thử lại."
        );
    }

    private record LoginAttempt(int failedAttempts, Instant expiredAt) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiredAt);
        }

        private LoginAttempt withFailedAttempts(int failedAttempts) {
            return new LoginAttempt(failedAttempts, expiredAt);
        }
    }
}
