package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;
import com.example.server.core.admin.nhanVien.event.NhanVienAccountCreatedEvent;
import com.example.server.core.admin.nhanVien.service.NhanVienService;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.address.DiaChiHaiCapMapper;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService.EmailDispatchResult;
import com.example.server.repository.NhanVienRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final PasswordService passwordService;
    private final ApplicationEventPublisher eventPublisher;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789".toCharArray();

    public NhanVienServiceImpl(
            NhanVienRepository nhanVienRepository,
            PasswordService passwordService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.nhanVienRepository = nhanVienRepository;
        this.passwordService = passwordService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NhanVienResponse> layDanhSach(String keyword, Integer vaiTro, Integer trangThai) {
        String kw = normalize(keyword);
        return nhanVienRepository.findAll().stream()
                .filter(nv -> matchKeyword(kw, nv))
                .filter(nv -> vaiTro == null || vaiTro.equals(normalizeVaiTro(nv.getVaiTro())))
                .filter(nv -> trangThai == null || trangThai.equals(nv.getTrangThai()))
                .sorted(Comparator.comparing(NhanVien::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NhanVienResponse layChiTiet(UUID id) {
        return toItem(findNhanVien(id));
    }



    @Override
    @Transactional
    public NhanVienResponse taoNhanVien(TaoNhanVienRequest request) {
        validateNgaySinhNhanVien(request.ngaySinh());
        Integer vaiTro = validateVaiTro(request.vaiTro());
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (nhanVienRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email đã được sử dụng");
        }
        String generatedTenDangNhap = generateTenDangNhapFromEmail(normalizedEmail, null);

        NhanVien nv = new NhanVien();
        nv.setId(UUID.randomUUID());

        String generatedMa;
        do {
            generatedMa = "NV" + String.format("%05d", SECURE_RANDOM.nextInt(100_000));
        } while (nhanVienRepository.existsByMa(generatedMa));

        nv.setMa(generatedMa);
        nv.setTenDangNhap(generatedTenDangNhap);
        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(normalizedEmail);
        String randomMatKhau = generateTemporaryPassword();
        nv.setMatKhau(passwordService.hash(randomMatKhau));
        nv.setSdt(normalizeOptional(request.sdt()));
        nv.setGioiTinh(normalizeOptional(request.gioiTinh()));
        nv.setNgaySinh(request.ngaySinh());
        nv.setDiaChi(request.diaChi() != null ? DiaChiHaiCapMapper.toEntity(request.diaChi()) : null);
        nv.setHinhAnh(normalizeOptional(request.hinhAnh()));
        nv.setVaiTro(vaiTro);
        nv.setFaceDescriptor(normalizeOptional(request.faceDescriptor()));
        nv.setTrangThai(isStaffRole(vaiTro) ? 2 : 1);
        Instant now = Instant.now();
        nv.setNgayTao(now);

        NhanVien saved = nhanVienRepository.save(nv);
        eventPublisher.publishEvent(new NhanVienAccountCreatedEvent(
                saved.getEmail(),
                saved.getHoTen(),
                saved.getTenDangNhap(),
                randomMatKhau
        ));

        return toItem(saved, randomMatKhau, null);
    }

    @Override
    @Transactional
    public NhanVienResponse capNhatNhanVien(UUID id, CapNhatNhanVienRequest request) {
        validateNgaySinhNhanVien(request.ngaySinh());
        Integer vaiTro = validateVaiTro(request.vaiTro());
        NhanVien nv = findNhanVien(id);

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        nhanVienRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Email đã được sử dụng");
                });
        String normalizedTenDangNhap = generateTenDangNhapFromEmail(normalizedEmail, id);

        nv.setTenDangNhap(normalizedTenDangNhap);
        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(normalizedEmail);
        nv.setSdt(normalizeOptional(request.sdt()));
        nv.setGioiTinh(normalizeOptional(request.gioiTinh()));
        nv.setNgaySinh(request.ngaySinh());
        nv.setDiaChi(request.diaChi() != null ? DiaChiHaiCapMapper.toEntity(request.diaChi()) : null);
        nv.setHinhAnh(normalizeOptional(request.hinhAnh()));
        nv.setVaiTro(vaiTro);
        if (!isStaffRole(vaiTro) && Integer.valueOf(2).equals(nv.getTrangThai())) {
            nv.setTrangThai(1);
        }
        nv.setNgayCapNhat(Instant.now());
        return toItem(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public NhanVienResponse doiTrangThai(UUID id, DoiTrangThaiRequest request) {
        NhanVien nv = findNhanVien(id);
        if (request.trangThai() != 0 && request.trangThai() != 1) {
            throw new BusinessException("Trạng thái không hợp lệ");
        }
        nv.setTrangThai(request.trangThai());
        nv.setNgayCapNhat(Instant.now());
        return toItem(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public NhanVienResponse doiMatKhau(UUID id, DoiMatKhauRequest request) {
        NhanVien nv = findNhanVien(id);
        nv.setMatKhau(passwordService.hash(request.matKhauMoi()));
        if (Integer.valueOf(2).equals(nv.getTrangThai())) {
            nv.setTrangThai(1);
        }
        nv.setNgayCapNhat(Instant.now());
        return toItem(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public void xoaNhanVien(UUID id) {
        NhanVien nv = findNhanVien(id);
        nhanVienRepository.delete(nv);
    }

    @Override
    @Transactional
    public NhanVienResponse capNhatFaceId(UUID id, com.example.server.core.admin.nhanVien.dto.request.CapNhatFaceIdRequest request) {
        NhanVien nv = findNhanVien(id);
        nv.setFaceDescriptor(request.faceDescriptor());
        nv.setNgayCapNhat(Instant.now());
        return toItem(nhanVienRepository.save(nv));
    }

    private NhanVien findNhanVien(UUID id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));
    }



    private boolean matchKeyword(String keyword, NhanVien nv) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String ten = normalize(nv.getHoTen());
        String ma = normalize(nv.getMa());
        String email = normalize(nv.getEmail());
        String sdt = nv.getSdt() != null ? nv.getSdt() : "";
        return (ten != null && ten.contains(keyword))
                || (ma != null && ma.contains(keyword))
                || (email != null && email.contains(keyword))
                || sdt.contains(keyword);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String resolved = value.trim().toLowerCase(Locale.ROOT);
        return resolved.isBlank() ? null : resolved;
    }

    private String generateTenDangNhapFromEmail(String email, UUID currentEmployeeId) {
        String baseUsername = email.split("@")[0].trim().toLowerCase(Locale.ROOT);
        if (baseUsername.isBlank()) {
            throw new BusinessException("Email không hợp lệ để tạo tên đăng nhập");
        }

        String username = baseUsername;
        int counter = 1;
        while (isUsernameUsedByAnotherEmployee(username, currentEmployeeId)) {
            username = baseUsername + counter++;
        }
        return username;
    }

    private boolean isUsernameUsedByAnotherEmployee(String username, UUID currentEmployeeId) {
        return nhanVienRepository.findByTenDangNhapIgnoreCase(username)
                .filter(existing -> currentEmployeeId == null || !existing.getId().equals(currentEmployeeId))
                .isPresent();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String resolved = value.trim();
        return resolved.isBlank() ? null : resolved;
    }



    private NhanVienResponse toItem(NhanVien nv) {
        return toItem(nv, null, null);
    }

    private NhanVienResponse toItem(NhanVien nv, String matKhauTamThoi, EmailDispatchResult emailDispatchResult) {
        return new NhanVienResponse(
                nv.getId(),
                nv.getMa(),
                nv.getTenDangNhap(),
                nv.getHoTen(),
                nv.getEmail(),
                nv.getSdt(),
                nv.getGioiTinh(),
                nv.getNgaySinh(),
                DiaChiHaiCapMapper.toResponse(nv.getDiaChi()),
                nv.getHinhAnh(),
                normalizeVaiTro(nv.getVaiTro()),
                mapVaiTro(nv.getVaiTro()),
                nv.getTrangThai(),
                mapTrangThai(nv.getTrangThai()),
                nv.getNgayTao(),
                matKhauTamThoi,
                emailDispatchResult != null ? emailDispatchResult.sent() : null,
                emailDispatchResult != null && !emailDispatchResult.sent() ? emailDispatchResult.warningMessage() : null,
                nv.getFaceDescriptor()
        );
    }

    private String mapVaiTro(Integer vaiTro) {
        if (vaiTro == null) {
            return "Không xác định";
        }
        if (!Integer.valueOf(1).equals(vaiTro)) {
            return "Nhân viên";
        }
        return switch (vaiTro) {
            case 1 -> "Quản lý";
            case 2 -> "Nhân viên";
            default -> "Không xác định";
        };
    }

    private String mapTrangThai(Integer trangThai) {
        if (Integer.valueOf(1).equals(trangThai)) {
            return "Đang làm";
        }
        if (Integer.valueOf(2).equals(trangThai)) {
            return "Chờ đổi mật khẩu";
        }
        return "Nghỉ làm";
    }

    private Integer validateVaiTro(Integer vaiTro) {
        if (Integer.valueOf(1).equals(vaiTro) || Integer.valueOf(2).equals(vaiTro)) {
            return vaiTro;
        }
        throw new BusinessException("Vai tro khong hop le");
    }

    private void validateNgaySinhNhanVien(LocalDate ngaySinh) {
        if (ngaySinh == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (ngaySinh.isAfter(today)) {
            throw new BusinessException("Ngày sinh không được là ngày trong tương lai");
        }
        if (ngaySinh.isAfter(today.minusYears(18))) {
            throw new BusinessException("Nhân viên phải từ đủ 18 tuổi");
        }
        if (ngaySinh.isBefore(today.minusYears(80))) {
            throw new BusinessException("Tuổi nhân viên không được lớn hơn 80");
        }
    }

    private Integer normalizeVaiTro(Integer vaiTro) {
        return Integer.valueOf(1).equals(vaiTro) ? 1 : 2;
    }

    private boolean isStaffRole(Integer vaiTro) {
        return Integer.valueOf(2).equals(normalizeVaiTro(vaiTro));
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            password.append(TEMP_PASSWORD_CHARS[SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length)]);
        }
        return password.toString();
    }
}
