package com.example.server.core.client.profile.service.impl;

import com.example.server.core.client.profile.dto.ClientDoiMatKhauRequest;
import com.example.server.core.client.profile.dto.ClientProfileRequest;
import com.example.server.core.client.profile.dto.ClientProfileResponse;
import com.example.server.core.client.profile.service.ClientProfileService;
import com.example.server.entity.KhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.repository.KhachHangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class ClientProfileServiceImpl implements ClientProfileService {

    private final KhachHangRepository khachHangRepository;
    private final PasswordService passwordService;

    public ClientProfileServiceImpl(KhachHangRepository khachHangRepository, PasswordService passwordService) {
        this.khachHangRepository = khachHangRepository;
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
                khachHang.getGioiTinh()
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
}
