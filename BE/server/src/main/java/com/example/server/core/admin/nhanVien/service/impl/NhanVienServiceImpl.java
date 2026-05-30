package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.NhanVienResponses.NhanVienResponse;
import com.example.server.core.admin.nhanVien.service.NhanVienService;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.infrastructure.service.EmailService.EmailDispatchResult;
import com.example.server.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final EmailService emailService;

    public NhanVienServiceImpl(NhanVienRepository nhanVienRepository, EmailService emailService) {
        this.nhanVienRepository = nhanVienRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NhanVienResponse> layDanhSach(String keyword, Integer vaiTro, Integer trangThai) {
        String kw = normalize(keyword);
        return nhanVienRepository.findAll().stream()
                .filter(nv -> matchKeyword(kw, nv))
                .filter(nv -> vaiTro == null || vaiTro.equals(nv.getVaiTro()))
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
    @Transactional(readOnly = true)
    public NhanVienResponse layTheoCccd(String cccd) {
        return toItem(findNhanVienTheoCccd(cccd));
    }

    @Override
    @Transactional
    public NhanVienResponse taoNhanVien(TaoNhanVienRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (nhanVienRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email đã được sử dụng");
        }
        String generatedTenDangNhap = generateTenDangNhapFromEmail(normalizedEmail);

        String normalizedCccd = normalizeCccd(request.cccd());
        if (normalizedCccd != null && nhanVienRepository.existsByCccd(normalizedCccd)) {
            throw new BusinessException("CCCD đã được sử dụng");
        }



        NhanVien nv = new NhanVien();
        nv.setId(UUID.randomUUID());

        String generatedMa;
        do {
            generatedMa = "NV" + String.format("%05d", new java.util.Random().nextInt(99999));
        } while (nhanVienRepository.existsByMa(generatedMa));

        nv.setMa(generatedMa);
        nv.setTenDangNhap(generatedTenDangNhap);
        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(normalizedEmail);
        String randomMatKhau = UUID.randomUUID().toString().substring(0, 8);
        nv.setMatKhau(randomMatKhau);
        nv.setSdt(normalizeOptional(request.sdt()));
        nv.setCccd(normalizedCccd);
        nv.setGioiTinh(normalizeOptional(request.gioiTinh()));
        nv.setNgaySinh(request.ngaySinh());
        nv.setDiaChi(normalizeOptional(request.diaChi()));
        nv.setHinhAnh(normalizeOptional(request.hinhAnh()));
        nv.setVaiTro(request.vaiTro());
        nv.setTrangThai(1);
        nv.setNgayTao(Instant.now());

        NhanVien saved = nhanVienRepository.save(nv);
        EmailDispatchResult emailDispatchResult = emailService.trySendRegistrationEmail(
                saved.getEmail(),
                saved.getHoTen(),
                saved.getTenDangNhap(),
                randomMatKhau
        );

        return toItem(saved, randomMatKhau, emailDispatchResult);
    }

    @Override
    @Transactional
    public NhanVienResponse capNhatNhanVien(UUID id, CapNhatNhanVienRequest request) {
        NhanVien nv = findNhanVien(id);

        String normalizedTenDangNhap = normalizeRequired(request.tenDangNhap(), "Ten dang nhap khong duoc de trong");
        nhanVienRepository.findByTenDangNhapIgnoreCase(normalizedTenDangNhap)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Ten dang nhap da duoc su dung");
                });

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        nhanVienRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Email đã được sử dụng");
                });

        String normalizedCccd = normalizeCccd(request.cccd());
        if (normalizedCccd != null) {
            nhanVienRepository.findByCccd(normalizedCccd)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new BusinessException("CCCD đã được sử dụng");
                    });
        }



        nv.setTenDangNhap(normalizedTenDangNhap);
        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(normalizedEmail);
        nv.setSdt(normalizeOptional(request.sdt()));
        nv.setCccd(normalizedCccd);
        nv.setGioiTinh(normalizeOptional(request.gioiTinh()));
        nv.setNgaySinh(request.ngaySinh());
        nv.setDiaChi(normalizeOptional(request.diaChi()));
        nv.setHinhAnh(normalizeOptional(request.hinhAnh()));
        nv.setVaiTro(request.vaiTro());
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
        nv.setMatKhau(request.matKhauMoi());
        nv.setNgayCapNhat(Instant.now());
        return toItem(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public void xoaNhanVien(UUID id) {
        NhanVien nv = findNhanVien(id);
        nhanVienRepository.delete(nv);
    }

    private NhanVien findNhanVien(UUID id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));
    }

    private NhanVien findNhanVienTheoCccd(String cccd) {
        String normalizedCccd = normalizeCccd(cccd);
        if (normalizedCccd == null) {
            throw new ResourceNotFoundException("Nhân viên không tồn tại");
        }
        return nhanVienRepository.findByCccd(normalizedCccd)
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
        String cccd = nv.getCccd() != null ? nv.getCccd() : "";
        return (ten != null && ten.contains(keyword))
                || (ma != null && ma.contains(keyword))
                || (email != null && email.contains(keyword))
                || sdt.contains(keyword)
                || cccd.contains(keyword);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String resolved = value.trim().toLowerCase(Locale.ROOT);
        return resolved.isBlank() ? null : resolved;
    }

    private String normalizeRequired(String value, String message) {
        String resolved = normalize(value);
        if (resolved == null) {
            throw new BusinessException(message);
        }
        return resolved;
    }

    private String generateTenDangNhapFromEmail(String email) {
        String baseUsername = email.split("@")[0].trim().toLowerCase(Locale.ROOT);
        if (baseUsername.isBlank()) {
            throw new BusinessException("Email khong hop le de tao ten dang nhap");
        }

        String username = baseUsername;
        int counter = 1;
        while (nhanVienRepository.existsByTenDangNhapIgnoreCase(username)) {
            username = baseUsername + counter++;
        }
        return username;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String resolved = value.trim();
        return resolved.isBlank() ? null : resolved;
    }

    private String normalizeCccd(String value) {
        String resolved = normalizeOptional(value);
        return resolved == null ? null : resolved.replaceAll("\\s+", "");
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
                nv.getCccd(),
                nv.getGioiTinh(),
                nv.getNgaySinh(),
                nv.getDiaChi(),
                nv.getHinhAnh(),
                nv.getVaiTro(),
                mapVaiTro(nv.getVaiTro()),
                nv.getTrangThai(),
                nv.getTrangThai() == 1 ? "Đang làm" : "Nghỉ làm",
                nv.getNgayTao(),
                matKhauTamThoi,
                emailDispatchResult != null ? emailDispatchResult.sent() : null,
                emailDispatchResult != null && !emailDispatchResult.sent() ? emailDispatchResult.warningMessage() : null
        );
    }

    private String mapVaiTro(Integer vaiTro) {
        if (vaiTro == null) {
            return "Không xác định";
        }
        return switch (vaiTro) {
            case 1 -> "Admin";
            case 2 -> "Nhân viên";
            case 3 -> "Kho";
            default -> "Không xác định";
        };
    }
}
