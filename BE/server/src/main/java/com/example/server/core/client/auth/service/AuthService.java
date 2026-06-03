package com.example.server.core.client.auth.service;

import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;
import com.example.server.core.client.auth.dto.request.LoginRequest;
import com.example.server.core.client.auth.dto.response.AdminLoginResponse;
import com.example.server.entity.KhachHang;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.AdminPrincipal;
import com.example.server.infrastructure.security.JwtService;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final JwtService jwtService;
    private final PasswordService passwordService;

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
    public KhachHangResponse login(LoginRequest request) {
        Optional<KhachHang> khOptional = khachHangRepository.findAll().stream()
                .filter(kh -> kh.getTenDangNhap().equals(request.username())
                        || (kh.getEmail() != null && kh.getEmail().equals(request.username())))
                .findFirst();

        if (khOptional.isEmpty()) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        KhachHang kh = khOptional.get();
        if (!passwordService.matches(request.password(), kh.getMatKhau())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        if (kh.getTrangThai() != 1) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }

        migratePasswordIfNeeded(kh, request.password());
        return toKhachHangResponse(kh);
    }

    @Transactional
    public AdminLoginResponse adminLogin(LoginRequest request) {
        String username = request.username().trim();
        Optional<NhanVien> nvOptional = nhanVienRepository.findByTenDangNhapIgnoreCase(username)
                .or(() -> nhanVienRepository.findByEmail(username.toLowerCase(Locale.ROOT)));

        if (nvOptional.isEmpty()) {
            throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác");
        }

        NhanVien nhanVien = nvOptional.get();
        if (!passwordService.matches(request.password(), nhanVien.getMatKhau())) {
            throw new BusinessException("Tài khoản hoặc mật khẩu không chính xác");
        }

        if (nhanVien.getTrangThai() == null || nhanVien.getTrangThai() != 1) {
            throw new BusinessException("Tài khoản nhân viên đã bị khóa");
        }

        migratePasswordIfNeeded(nhanVien, request.password());

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
                nhanVien.getHinhAnh()
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
}
