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
    public NhanVienServiceImpl(NhanVienRepository nhanVienRepository) {
        this.nhanVienRepository = nhanVienRepository;
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
    @Transactional
    public NhanVienResponse taoNhanVien(TaoNhanVienRequest request) {
        if (nhanVienRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã được sử dụng");
        }
        NhanVien nv = new NhanVien();
        nv.setId(UUID.randomUUID());
        
        String generatedMa;
        do {
            generatedMa = "NV" + String.format("%05d", new java.util.Random().nextInt(99999));
        } while (nhanVienRepository.existsByMa(generatedMa));
        
        nv.setMa(generatedMa);
        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        nv.setMatKhau(request.matKhau());
        nv.setSdt(request.sdt());
        nv.setDiaChi(request.diaChi());
        nv.setHinhAnh(request.hinhAnh());
        nv.setVaiTro(request.vaiTro());
        nv.setTrangThai(1);
        nv.setNgayTao(Instant.now());
        return toItem(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public NhanVienResponse capNhatNhanVien(UUID id, CapNhatNhanVienRequest request) {
        NhanVien nv = findNhanVien(id);
        // Check email uniqueness (skip self)
        nhanVienRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new BusinessException("Email đã được sử dụng"); });

        nv.setHoTen(request.hoTen().trim());
        nv.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        nv.setSdt(request.sdt());
        nv.setDiaChi(request.diaChi());
        nv.setHinhAnh(request.hinhAnh());
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

    // ---- helpers ----

    private NhanVien findNhanVien(UUID id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));
    }

    private boolean matchKeyword(String keyword, NhanVien nv) {
        if (keyword == null || keyword.isBlank()) return true;
        String ten = normalize(nv.getHoTen());
        String ma = normalize(nv.getMa());
        String email = normalize(nv.getEmail());
        String sdt = nv.getSdt() != null ? nv.getSdt() : "";
        return (ten != null && ten.contains(keyword)) ||
               (ma != null && ma.contains(keyword)) ||
               (email != null && email.contains(keyword)) ||
               sdt.contains(keyword);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String s = value.trim().toLowerCase(Locale.ROOT);
        return s.isBlank() ? null : s;
    }

    private NhanVienResponse toItem(NhanVien nv) {
        return new NhanVienResponse(
                nv.getId(),
                nv.getMa(),
                nv.getHoTen(),
                nv.getEmail(),
                nv.getSdt(),
                nv.getDiaChi(),
                nv.getHinhAnh(),
                nv.getVaiTro(),
                mapVaiTro(nv.getVaiTro()),
                nv.getTrangThai(),
                nv.getTrangThai() == 1 ? "Hoạt động" : "Khóa",
                nv.getNgayTao()
        );
    }

    private String mapVaiTro(Integer vaiTro) {
        if (vaiTro == null) return "Không xác định";
        return switch (vaiTro) {
            case 1 -> "Admin";
            case 2 -> "Bán hàng";
            case 3 -> "Kho";
            default -> "Không xác định";
        };
    }
}
