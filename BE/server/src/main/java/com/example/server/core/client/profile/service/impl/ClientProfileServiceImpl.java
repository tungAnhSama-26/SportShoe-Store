package com.example.server.core.client.profile.service.impl;

import com.example.server.core.client.profile.dto.ClientDoiMatKhauRequest;
import com.example.server.core.client.profile.dto.ClientDiaChiRequest;
import com.example.server.core.client.profile.dto.ClientDiaChiResponse;
import com.example.server.core.client.profile.dto.ClientProfileRequest;
import com.example.server.core.client.profile.dto.ClientProfileResponse;
import com.example.server.core.client.profile.service.ClientProfileService;
import com.example.server.entity.DiaChiKhachHang;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ClientProfileServiceImpl implements ClientProfileService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final PasswordService passwordService;

    public ClientProfileServiceImpl(
            KhachHangRepository khachHangRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository,
            PasswordService passwordService
    ) {
        this.khachHangRepository = khachHangRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
        this.passwordService = passwordService;
    }

    private KhachHang findKhachHang(UUID id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản khách hàng"));
    }

    private ClientProfileResponse toResponse(KhachHang khachHang) {
        return new ClientProfileResponse(
                khachHang.getId(),
                khachHang.getTenDangNhap(),
                khachHang.getHoTen(),
                khachHang.getEmail(),
                khachHang.getSdt(),
                khachHang.getNgaySinh(),
                khachHang.getGioiTinh(),
                khachHang.getHinhAnh()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClientProfileResponse layThongTin(UUID khachHangId) {
        return toResponse(findKhachHang(khachHangId));
    }

    @Override
    @Transactional
    public ClientProfileResponse capNhatThongTin(UUID khachHangId, ClientProfileRequest request) {
        KhachHang khachHang = findKhachHang(khachHangId);

        // Check if email changed and is already taken
        String newEmail = request.email() != null ? request.email().trim() : null;
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(khachHang.getEmail())) {
            if (khachHangRepository.existsByEmail(newEmail)) {
                throw new BusinessException("Email này đã được sử dụng bởi một tài khoản khác.");
            }
        }

        khachHang.setHoTen(request.hoTen().trim());
        khachHang.setEmail(newEmail);
        khachHang.setSdt(request.sdt() != null ? request.sdt().trim() : null);
        khachHang.setGioiTinh(request.gioiTinh());
        khachHang.setNgaySinh(request.ngaySinh());
        if (request.hinhAnh() != null) {
            khachHang.setHinhAnh(request.hinhAnh().trim());
        }
        khachHang.setNgayCapNhat(Instant.now());

        return toResponse(khachHangRepository.save(khachHang));
    }

    @Override
    @Transactional
    public void doiMatKhau(UUID khachHangId, ClientDoiMatKhauRequest request) {
        KhachHang khachHang = findKhachHang(khachHangId);

        // Verify old password
        if (!passwordService.matches(request.matKhauCu(), khachHang.getMatKhau())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác.");
        }

        // Check if new password is same as old password
        if (Objects.equals(request.matKhauCu(), request.matKhauMoi())) {
            throw new BusinessException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        khachHang.setMatKhau(passwordService.hash(request.matKhauMoi()));
        khachHang.setNgayCapNhat(Instant.now());
        khachHangRepository.save(khachHang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDiaChiResponse> layDanhSachDiaChi(UUID khachHangId) {
        findKhachHang(khachHangId);
        return diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId).stream()
                .map(this::toDiaChiResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientDiaChiResponse themDiaChi(UUID khachHangId, ClientDiaChiRequest request) {
        KhachHang khachHang = findKhachHang(khachHangId);
        boolean laDiaChiDauTien =
                diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId).isEmpty();
        boolean laMacDinh = laDiaChiDauTien || Boolean.TRUE.equals(request.laMacDinh());

        if (laMacDinh) {
            resetDiaChiMacDinh(khachHangId);
        }

        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setKhachHang(khachHang);
        mapDiaChi(diaChi, request, laMacDinh);
        diaChi.setTrangThai(1);
        diaChi.setNgayTao(Instant.now());
        return toDiaChiResponse(diaChiKhachHangRepository.save(diaChi));
    }

    @Override
    @Transactional
    public ClientDiaChiResponse capNhatDiaChi(
            UUID khachHangId,
            Integer diaChiId,
            ClientDiaChiRequest request
    ) {
        DiaChiKhachHang diaChi = findOwnedDiaChi(khachHangId, diaChiId);
        boolean laMacDinh = Boolean.TRUE.equals(request.laMacDinh());
        if (laMacDinh) {
            resetDiaChiMacDinh(khachHangId);
        }

        mapDiaChi(diaChi, request, laMacDinh);
        diaChi.setNgayCapNhat(Instant.now());
        return toDiaChiResponse(diaChiKhachHangRepository.save(diaChi));
    }

    @Override
    @Transactional
    public void xoaDiaChi(UUID khachHangId, Integer diaChiId) {
        DiaChiKhachHang diaChi = findOwnedDiaChi(khachHangId, diaChiId);
        boolean dangLaMacDinh = Boolean.TRUE.equals(diaChi.getLaMacDinh());
        diaChiKhachHangRepository.delete(diaChi);
        diaChiKhachHangRepository.flush();

        if (dangLaMacDinh) {
            List<DiaChiKhachHang> conLai =
                    diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId);
            if (!conLai.isEmpty()) {
                DiaChiKhachHang diaChiMoi = conLai.get(0);
                diaChiMoi.setLaMacDinh(true);
                diaChiMoi.setNgayCapNhat(Instant.now());
                diaChiKhachHangRepository.save(diaChiMoi);
            }
        }
    }

    @Override
    @Transactional
    public void datMacDinhDiaChi(UUID khachHangId, Integer diaChiId) {
        DiaChiKhachHang diaChi = findOwnedDiaChi(khachHangId, diaChiId);
        resetDiaChiMacDinh(khachHangId);
        diaChi.setLaMacDinh(true);
        diaChi.setNgayCapNhat(Instant.now());
        diaChiKhachHangRepository.save(diaChi);
    }

    private DiaChiKhachHang findOwnedDiaChi(UUID khachHangId, Integer diaChiId) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(diaChiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
        if (!khachHangId.equals(diaChi.getKhachHang().getId())) {
            throw new BusinessException("Bạn không có quyền thao tác địa chỉ này");
        }
        return diaChi;
    }

    private void resetDiaChiMacDinh(UUID khachHangId) {
        List<DiaChiKhachHang> danhSach =
                diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHangId);
        for (DiaChiKhachHang diaChi : danhSach) {
            if (Boolean.TRUE.equals(diaChi.getLaMacDinh())) {
                diaChi.setLaMacDinh(false);
                diaChi.setNgayCapNhat(Instant.now());
                diaChiKhachHangRepository.save(diaChi);
            }
        }
    }

    private void mapDiaChi(
            DiaChiKhachHang diaChi,
            ClientDiaChiRequest request,
            boolean laMacDinh
    ) {
        diaChi.setHoTen(request.hoTen().trim());
        diaChi.setSdt(request.sdt().trim());
        diaChi.setTinhThanh(request.tinhThanh().trim());
        diaChi.setQuanHuyen(request.quanHuyen().trim());
        diaChi.setPhuongXa(request.phuongXa().trim());
        diaChi.setDiaChiCuThe(request.diaChiCuThe().trim());
        diaChi.setLaMacDinh(laMacDinh);
        if (diaChi.getTrangThai() == null) {
            diaChi.setTrangThai(1);
        }
        if (diaChi.getNgayTao() == null) {
            diaChi.setNgayTao(Instant.now());
        }
    }

    private ClientDiaChiResponse toDiaChiResponse(DiaChiKhachHang diaChi) {
        return new ClientDiaChiResponse(
                diaChi.getId(),
                diaChi.getHoTen(),
                diaChi.getSdt(),
                diaChi.getTinhThanh(),
                diaChi.getQuanHuyen(),
                diaChi.getPhuongXa(),
                diaChi.getDiaChiCuThe(),
                diaChi.getLaMacDinh()
        );
    }
}
