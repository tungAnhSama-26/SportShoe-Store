package com.example.server.core.admin.khachHang.service.impl;

import com.example.server.core.admin.khachHang.dto.request.CapNhatKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.request.DiaChiRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiMatKhauRequest;
import com.example.server.core.admin.khachHang.dto.request.DoiTrangThaiRequest;
import com.example.server.core.admin.khachHang.dto.request.TaoKhachHangRequest;
import com.example.server.core.admin.khachHang.dto.responsse.DiaChiResponse;
import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;
import com.example.server.core.admin.khachHang.service.KhachHangService;
import com.example.server.entity.DiaChiKhachHang;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final EmailService emailService;

    public KhachHangServiceImpl(
            KhachHangRepository khachHangRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository,
            EmailService emailService
    ) {

        this.khachHangRepository = khachHangRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHangResponse> layDanhSach(String keyword, Integer trangThai) {
        String kw = normalize(keyword);
        return khachHangRepository.findAll().stream()
                .filter(kh -> matchKeyword(kw, kh))
                .filter(kh -> trangThai == null || trangThai.equals(kh.getTrangThai()))
                .sorted(Comparator.comparing(KhachHang::getNgayTao, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toKhachHangResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KhachHangResponse layChiTiet(UUID id) {
        return toKhachHangResponse(findKhachHang(id));
    }

    @Override
    @Transactional
    public KhachHangResponse taoKhachHang(TaoKhachHangRequest request) {
        if (khachHangRepository.existsByTenDangNhap(request.tenDangNhap())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        if (request.email() != null && !request.email().isBlank() && khachHangRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã được sử dụng");
        }

        KhachHang kh = new KhachHang();
        kh.setId(UUID.randomUUID());
        kh.setTenDangNhap(request.tenDangNhap().trim());
        kh.setHoTen(request.hoTen().trim());
        kh.setEmail(request.email() != null ? request.email().trim().toLowerCase(Locale.ROOT) : null);
        kh.setSdt(request.sdt() != null ? request.sdt().trim() : null);
        kh.setNgaySinh(request.ngaySinh());
        kh.setHinhAnh(request.hinhAnh());
        kh.setMatKhau(request.matKhau());
        kh.setTrangThai(1);
        kh.setNgayTao(Instant.now());

        KhachHang saved = khachHangRepository.save(kh);
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            try {
                emailService.sendCustomerRegistrationEmail(
                        saved.getEmail(),
                        saved.getHoTen(),
                        saved.getTenDangNhap(),
                        request.matKhau()
                );
            } catch (Exception exception) {
                System.err.println("Khong the gui email tai khoan khach hang: " + exception.getMessage());
            }
        }

        return toKhachHangResponse(saved);
    }

    @Override
    @Transactional
    public KhachHangResponse capNhatKhachHang(UUID id, CapNhatKhachHangRequest request) {
        KhachHang kh = findKhachHang(id);

        if (request.email() != null && !request.email().isBlank()) {
            khachHangRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new BusinessException("Email đã được sử dụng"); });
        }

        kh.setHoTen(request.hoTen().trim());
        kh.setEmail(request.email() != null ? request.email().trim().toLowerCase(Locale.ROOT) : null);
        kh.setSdt(request.sdt() != null ? request.sdt().trim() : null);
        kh.setNgaySinh(request.ngaySinh());
        kh.setHinhAnh(request.hinhAnh());
        kh.setNgayCapNhat(Instant.now());

        return toKhachHangResponse(khachHangRepository.save(kh));
    }

    @Override
    @Transactional
    public KhachHangResponse doiTrangThai(UUID id, DoiTrangThaiRequest request) {
        KhachHang kh = findKhachHang(id);
        kh.setTrangThai(request.trangThai());
        kh.setNgayCapNhat(Instant.now());
        return toKhachHangResponse(khachHangRepository.save(kh));
    }
    @Override
    @Transactional
    public KhachHangResponse doiMatKhau(UUID id, DoiMatKhauRequest request) {
        KhachHang kh = findKhachHang(id);
        kh.setMatKhau(request.matKhauMoi());
        kh.setNgayCapNhat(Instant.now());
        return toKhachHangResponse(khachHangRepository.save(kh));
    }
    @Override
    @Transactional
    public void xoaKhachHang(UUID id) {
        KhachHang kh = findKhachHang(id);
        khachHangRepository.delete(kh);
    }

    // --- Address ---

    @Override
    @Transactional(readOnly = true)
    public List<DiaChiResponse> layDanhSachDiaChi(UUID khachHangId) {
        return diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId).stream()
                .map(this::toDiaChiResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiaChiResponse layChiTietDiaChi(Integer diaChiId) {
        return toDiaChiResponse(findDiaChi(diaChiId));
    }

    @Override
    @Transactional
    public DiaChiResponse themDiaChi(UUID khachHangId, DiaChiRequest request) {
        KhachHang kh = findKhachHang(khachHangId);
        if (request.laMacDinh()) {
            resetDefaultAddress(khachHangId);
        }
        DiaChiKhachHang dc = new DiaChiKhachHang();
        mapDiaChi(dc, request);
        dc.setKhachHang(kh);
        dc.setNgayTao(Instant.now());
        dc.setTrangThai(1);

        return toDiaChiResponse(diaChiKhachHangRepository.save(dc));
    }
    @Override
    @Transactional
    public DiaChiResponse capNhatDiaChi(Integer diaChiId, DiaChiRequest request) {
        DiaChiKhachHang dc = findDiaChi(diaChiId);
        if (request.laMacDinh()) {
            resetDefaultAddress(dc.getKhachHang().getId());
        }
        mapDiaChi(dc, request);
        ensureAddressAuditFields(dc);
        dc.setNgayCapNhat(Instant.now());

        return toDiaChiResponse(diaChiKhachHangRepository.save(dc));
    }

    @Override
    @Transactional
    public void xoaDiaChi(Integer diaChiId) {
        DiaChiKhachHang dc = findDiaChi(diaChiId);
        boolean laMacDinh = Boolean.TRUE.equals(dc.getLaMacDinh());
        UUID khachHangId = dc.getKhachHang().getId();
        diaChiKhachHangRepository.delete(dc);
        diaChiKhachHangRepository.flush();

        if (laMacDinh) {
            List<DiaChiKhachHang> remaining = diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId);
            if (!remaining.isEmpty()) {
                DiaChiKhachHang newDefault = remaining.get(0);
                newDefault.setLaMacDinh(true);
                diaChiKhachHangRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void datMacDinhDiaChi(Integer diaChiId) {
        DiaChiKhachHang dc = findDiaChi(diaChiId);
        resetDefaultAddress(dc.getKhachHang().getId());
        ensureAddressAuditFields(dc);
        dc.setLaMacDinh(true);
        dc.setNgayCapNhat(Instant.now());
        diaChiKhachHangRepository.save(dc);
    }

    // --- Helpers ---

    private KhachHang findKhachHang(UUID id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng không tồn tại"));
    }

    private DiaChiKhachHang findDiaChi(Integer id) {
        return diaChiKhachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ không tồn tại"));
    }

    private void resetDefaultAddress(UUID khachHangId) {
        List<DiaChiKhachHang> ds = diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId);
        for (DiaChiKhachHang dc : ds) {
            if (Boolean.TRUE.equals(dc.getLaMacDinh())) {
                ensureAddressAuditFields(dc);
                dc.setLaMacDinh(false);
                dc.setNgayCapNhat(Instant.now());
                diaChiKhachHangRepository.save(dc);
            }
        }
    }

    private void ensureAddressAuditFields(DiaChiKhachHang dc) {
        if (dc.getNgayTao() == null) {
            dc.setNgayTao(Instant.now());
        }
        if (dc.getTrangThai() == null) {
            dc.setTrangThai(1);
        }
        if (dc.getLaMacDinh() == null) {
            dc.setLaMacDinh(false);
        }
    }

    private void mapDiaChi(DiaChiKhachHang dc, DiaChiRequest request) {
        dc.setHoTen(request.hoTen().trim());
        dc.setSdt(request.sdt().trim());
        dc.setTinhThanh(request.tinhThanh());
        dc.setQuanHuyen(request.quanHuyen());
        dc.setPhuongXa(request.phuongXa());
        dc.setDiaChiCuThe(request.diaChiCuThe().trim());
        dc.setLaMacDinh(Boolean.TRUE.equals(request.laMacDinh()));
    }

    private boolean matchKeyword(String keyword, KhachHang kh) {
        if (keyword == null || keyword.isBlank()) return true;
        String ten = normalize(kh.getHoTen());
        String user = normalize(kh.getTenDangNhap());
        String email = normalize(kh.getEmail());
        String sdt = kh.getSdt() != null ? kh.getSdt() : "";
        return (ten != null && ten.contains(keyword)) ||
               (user != null && user.contains(keyword)) ||
               (email != null && email.contains(keyword)) ||
               sdt.contains(keyword);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String s = value.trim().toLowerCase(Locale.ROOT);
        return s.isBlank() ? null : s;
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
                kh.getTrangThai() == 1 ? "Hoạt động" : "Khóa",
                kh.getNgayTao(),
                diaChiMacDinh
        );
    }

    private DiaChiResponse toDiaChiResponse(DiaChiKhachHang dc) {
        return new DiaChiResponse(
                dc.getId(),
                dc.getHoTen(),
                dc.getSdt(),
                dc.getTinhThanh(),
                dc.getQuanHuyen(),
                dc.getPhuongXa(),
                dc.getDiaChiCuThe(),
                dc.getLaMacDinh()
        );
    }
}
