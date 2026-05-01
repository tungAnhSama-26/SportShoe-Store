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
import com.example.server.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
            throw new BusinessException("Email da duoc su dung");
        }

        String normalizedCccd = normalizeCccd(request.cccd());
        if (normalizedCccd != null && nhanVienRepository.existsByCccd(normalizedCccd)) {
            throw new BusinessException("CCCD da duoc su dung");
        }

        NhanVien nv = new NhanVien();
        nv.setId(UUID.randomUUID());

        String generatedMa;
        do {
            generatedMa = "NV" + String.format("%05d", new java.util.Random().nextInt(99999));
        } while (nhanVienRepository.existsByMa(generatedMa));

        nv.setMa(generatedMa);
        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(normalizedEmail);
        nv.setMatKhau(request.matKhau());
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
        emailService.sendRegistrationEmail(
                saved.getEmail(),
                saved.getHoTen(),
                saved.getMa(),
                request.matKhau()
        );

        return toItem(saved);
    }

    @Override
    @Transactional
    public NhanVienResponse capNhatNhanVien(UUID id, CapNhatNhanVienRequest request) {
        NhanVien nv = findNhanVien(id);

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        nhanVienRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Email da duoc su dung");
                });

        String normalizedCccd = normalizeCccd(request.cccd());
        if (normalizedCccd != null) {
            nhanVienRepository.findByCccd(normalizedCccd)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new BusinessException("CCCD da duoc su dung");
                    });
        }

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
            throw new BusinessException("Trang thai khong hop le");
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
                .orElseThrow(() -> new ResourceNotFoundException("Nhan vien khong ton tai"));
    }

    private NhanVien findNhanVienTheoCccd(String cccd) {
        String normalizedCccd = normalizeCccd(cccd);
        if (normalizedCccd == null) {
            throw new ResourceNotFoundException("Nhan vien khong ton tai");
        }
        return nhanVienRepository.findByCccd(normalizedCccd)
                .orElseThrow(() -> new ResourceNotFoundException("Nhan vien khong ton tai"));
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
        return new NhanVienResponse(
                nv.getId(),
                nv.getMa(),
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
                nv.getTrangThai() == 1 ? "Hoat dong" : "Khoa",
                nv.getNgayTao()
        );
    }

    private String mapVaiTro(Integer vaiTro) {
        if (vaiTro == null) {
            return "Khong xac dinh";
        }
        return switch (vaiTro) {
            case 1 -> "Admin";
            case 2 -> "Ban hang";
            case 3 -> "Kho";
            default -> "Khong xac dinh";
        };
    }
}
