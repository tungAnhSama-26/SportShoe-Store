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
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.infrastructure.service.EmailService.EmailDispatchResult;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final EmailService emailService;
    private final PasswordService passwordService;

    public KhachHangServiceImpl(
            KhachHangRepository khachHangRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository,
            EmailService emailService,
            PasswordService passwordService
    ) {

        this.khachHangRepository = khachHangRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.emailService = emailService;
        this.passwordService = passwordService;
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
        kiemTraHoTen(request.hoTen());
        if (request.email() != null && !request.email().isBlank() && khachHangRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email đã được sử dụng");
        }
        kiemTraSdtTrung(request.sdt(), null);
        kiemTraNgaySinh(request.ngaySinh());

        KhachHang kh = new KhachHang();
        kh.setId(UUID.randomUUID());
        kh.setTenDangNhap(taoTenDangNhapDuyNhat(request.tenDangNhap()));
        kh.setHoTen(request.hoTen().trim());
        kh.setEmail(request.email() != null ? request.email().trim().toLowerCase(Locale.ROOT) : null);
        kh.setSdt(request.sdt() != null && !request.sdt().isBlank() ? request.sdt().trim() : null);
        kh.setNgaySinh(request.ngaySinh());
        kh.setGioiTinh(request.gioiTinh());
        kh.setHinhAnh(request.hinhAnh());
        kh.setMatKhau(passwordService.hash(request.matKhau()));
        kh.setTrangThai(1);
        kh.setNgayTao(Instant.now());

        // Nhân viên tạo khách hàng tại quầy -> KHÔNG gửi email.
        // Email chào mừng chỉ gửi khi khách TỰ đăng ký (xem RegistrationService).
        KhachHang saved = khachHangRepository.save(kh);
        return toKhachHangResponse(saved);
    }

    @Override
    @Transactional
    public KhachHangResponse capNhatKhachHang(UUID id, CapNhatKhachHangRequest request) {
        kiemTraHoTen(request.hoTen());
        KhachHang kh = findKhachHang(id);

        if (request.email() != null && !request.email().isBlank()) {
            khachHangRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new BusinessException("Email đã được sử dụng"); });
        }
        kiemTraSdtTrung(request.sdt(), id);
        kiemTraNgaySinh(request.ngaySinh());

        kh.setHoTen(request.hoTen().trim());
        kh.setEmail(request.email() != null ? request.email().trim().toLowerCase(Locale.ROOT) : null);
        kh.setSdt(request.sdt() != null && !request.sdt().isBlank() ? request.sdt().trim() : null);
        kh.setNgaySinh(request.ngaySinh());
        kh.setGioiTinh(request.gioiTinh());
        kh.setHinhAnh(request.hinhAnh());
        kh.setNgayCapNhat(Instant.now());

        return toKhachHangResponse(khachHangRepository.save(kh));
    }

    /**
     * Số điện thoại không được trùng với khách hàng khác (bỏ qua chính mình khi cập nhật).
     * Dùng exists thay vì find để không lỗi khi dữ liệu cũ đã có sẵn nhiều khách trùng số.
     */
    private void kiemTraSdtTrung(String sdt, UUID idHienTai) {
        if (sdt == null || sdt.isBlank()) {
            return;
        }
        String sdtChuan = sdt.trim();
        boolean trung = idHienTai == null
                ? khachHangRepository.existsBySdt(sdtChuan)
                : khachHangRepository.existsBySdtAndIdNot(sdtChuan, idHienTai);
        if (trung) {
            throw new BusinessException("Số điện thoại đã được sử dụng");
        }
    }

    /** Ngày sinh: không ở tương lai và tuổi phải từ 18 đến 100. */
    private void kiemTraNgaySinh(java.time.LocalDate ngaySinh) {
        if (ngaySinh == null) {
            return;
        }
        java.time.LocalDate homNay = java.time.LocalDate.now();
        if (ngaySinh.isAfter(homNay)) {
            throw new BusinessException("Ngày sinh không được ở tương lai");
        }
        int tuoi = java.time.Period.between(ngaySinh, homNay).getYears();
        if (tuoi < 18) {
            throw new BusinessException("Khách hàng phải đủ 18 tuổi");
        }
        if (tuoi > 100) {
            throw new BusinessException("Tuổi khách hàng không hợp lệ (tối đa 100 tuổi)");
        }
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
        kh.setMatKhau(passwordService.hash(request.matKhauMoi()));
        kh.setNgayCapNhat(Instant.now());
        KhachHang saved = khachHangRepository.save(kh);
        // Gửi email thông báo mật khẩu mới ở luồng nền để không làm chậm thao tác đổi mật khẩu.
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            emailService.sendCustomerPasswordChangedEmailAsync(
                    saved.getEmail(),
                    saved.getHoTen(),
                    saved.getTenDangNhap(),
                    request.matKhauMoi()
            );
        }
        return toKhachHangResponse(saved);
    }

    // lkjlkjfdkj
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

    /**
     * Sinh tên đăng nhập duy nhất từ tên gợi ý của client. Nếu trùng thì tự thêm
     * hậu tố số tăng dần (vd: an, an1, an2...) để admin không bị kẹt khi 2 email
     * khác nhau nhưng phần trước @ giống nhau.
     */
    private String taoTenDangNhapDuyNhat(String goiY) {
        String base = goiY == null ? "" : goiY.trim();
        if (base.isEmpty()) {
            throw new BusinessException("Không thể tạo tên đăng nhập cho khách hàng");
        }
        String tenDangNhap = base;
        int counter = 1;
        while (khachHangRepository.existsByTenDangNhap(tenDangNhap)) {
            tenDangNhap = base + counter++;
        }
        return tenDangNhap;
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

    private void kiemTraHoTen(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) {
            throw new BusinessException("Vui lòng nhập họ tên khách hàng");
        }
        String trimmed = hoTen.trim();
        if (trimmed.length() > 100) {
            throw new BusinessException("Họ tên không được vượt quá 100 ký tự");
        }
        if (!trimmed.matches("^[a-zA-Z\\s\\u00C0-\\u1EF9]+$")) {
            throw new BusinessException("Họ tên khách hàng không được chứa số hoặc ký tự đặc biệt");
        }
    }

    private void mapDiaChi(DiaChiKhachHang dc, DiaChiRequest request) {
        dc.setHoTen(request.hoTen().trim());
        dc.setSdt(request.sdt().trim());
        dc.setTinhThanh(request.tinhThanh().trim());
        dc.setQuanHuyen(request.quanHuyen().trim());
        dc.setPhuongXa(request.phuongXa().trim());
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
        return toKhachHangResponse(kh, null);
    }

    private KhachHangResponse toKhachHangResponse(KhachHang kh, EmailDispatchResult emailDispatchResult) {
        DiaChiKhachHang diaChiMacDinh = diaChiKhachHangRepository
                .findFirstByKhachHangIdAndLaMacDinhTrue(kh.getId())
                .orElse(null);
        String diaChiMacDinhText = diaChiMacDinh != null
                ? diaChiMacDinh.getDiaChiCuThe() + ", " + diaChiMacDinh.getPhuongXa() + ", "
                        + diaChiMacDinh.getQuanHuyen() + ", " + diaChiMacDinh.getTinhThanh()
                : null;
        String sdtMacDinh = diaChiMacDinh != null ? diaChiMacDinh.getSdt() : null;
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
                kh.getTrangThai() == 1 ? "Hoạt động" : "Khóa",
                kh.getNgayTao(),
                diaChiMacDinhText,
                sdtMacDinh,
                emailDispatchResult != null ? emailDispatchResult.sent() : null,
                emailDispatchResult != null && !emailDispatchResult.sent() ? emailDispatchResult.warningMessage() : null
        );
    }

    private String tenGioiTinh(Integer gioiTinh) {
        if (gioiTinh == null) return null;
        return switch (gioiTinh) {
            case 0 -> "Nữ";
            case 1 -> "Nam";
            case 2 -> "Khác";
            default -> null;
        };
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
