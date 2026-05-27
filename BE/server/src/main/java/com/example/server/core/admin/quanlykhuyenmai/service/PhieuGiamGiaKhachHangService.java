package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaKhachHangRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse;
import com.example.server.entity.KhachHang;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.PhieuGiamGiaKhachHangRepository;
import com.example.server.repository.PhieuGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaKhachHangService {

    private static final int LOAI_PHIEU_CA_NHAN = 2;

    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final KhachHangRepository khachHangRepository;

    public List<QuanLyPhieuGiamGiaKhachHangResponse> getAll() {
        return phieuGiamGiaKhachHangRepository.hienThiPhieuGiamGiaKhachHang();
    }

    public QuanLyPhieuGiamGiaKhachHangResponse getOne(Integer id) {
        return phieuGiamGiaKhachHangRepository.detailPhieuGiamGiaKhachHang(id);
    }

    public Page<QuanLyPhieuGiamGiaKhachHangResponse> phanTrang(String keyword, Integer trangThai, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return phieuGiamGiaKhachHangRepository.timKiemVaPhanTrang(keyword, trangThai, pageable);
    }

    public void remove(Integer id) {
        phieuGiamGiaKhachHangRepository.deleteById(id);
    }

    public PhieuGiamGiaKhachHang add(PhieuGiamGiaKhachHangRequest request) {
        PhieuGiamGia phieuGiamGia = getPhieuGiamGia(request.getPhieuGiamGiaId());
        KhachHang khachHang = getKhachHangByEmail(request.getEmail());
        validateLienKet(phieuGiamGia, khachHang, null);

        PhieuGiamGiaKhachHang phieuGiamGiaKhachHang = new PhieuGiamGiaKhachHang();
        phieuGiamGiaKhachHang.setPhieuGiamGia(phieuGiamGia);
        phieuGiamGiaKhachHang.setKhachHang(khachHang);
        phieuGiamGiaKhachHang.setNgaySuDung(toInstant(request.getNgaySuDung()));
        phieuGiamGiaKhachHang.setTrangThai(request.getTrangThai() == null ? 1 : request.getTrangThai());
        phieuGiamGiaKhachHang.setNgayTao(resolveNgayTao(request.getNgayTao()));

        return phieuGiamGiaKhachHangRepository.save(phieuGiamGiaKhachHang);
    }

    public PhieuGiamGiaKhachHang update(Integer id, PhieuGiamGiaKhachHangRequest request) {
        PhieuGiamGiaKhachHang phieuGiamGiaKhachHang = phieuGiamGiaKhachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu giảm giá khách hàng"));

        if (phieuGiamGiaKhachHang.getTrangThai() != null && (phieuGiamGiaKhachHang.getTrangThai() == 0 || phieuGiamGiaKhachHang.getTrangThai() == 2)) {
            throw new BusinessException("Không thể chỉnh sửa liên kết phiếu đã ngừng hoạt động hoặc hết hạn.");
        }

        PhieuGiamGia phieuGiamGia = getPhieuGiamGia(request.getPhieuGiamGiaId());
        KhachHang khachHang = getKhachHangByEmail(request.getEmail());
        validateLienKet(phieuGiamGia, khachHang, phieuGiamGiaKhachHang);

        phieuGiamGiaKhachHang.setPhieuGiamGia(phieuGiamGia);
        phieuGiamGiaKhachHang.setKhachHang(khachHang);
        phieuGiamGiaKhachHang.setNgaySuDung(toInstant(request.getNgaySuDung()));
        phieuGiamGiaKhachHang.setTrangThai(request.getTrangThai() == null ? phieuGiamGiaKhachHang.getTrangThai() : request.getTrangThai());
        if (request.getNgayTao() != null) {
            phieuGiamGiaKhachHang.setNgayTao(toInstant(request.getNgayTao()));
        } else if (phieuGiamGiaKhachHang.getNgayTao() == null) {
            phieuGiamGiaKhachHang.setNgayTao(Instant.now());
        }

        return phieuGiamGiaKhachHangRepository.save(phieuGiamGiaKhachHang);
    }

    private Instant resolveNgayTao(LocalDate value) {
        return value == null ? Instant.now() : toInstant(value);
    }

    private Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private PhieuGiamGia getPhieuGiamGia(Integer phieuGiamGiaId) {
        return phieuGiamGiaRepository.findById(phieuGiamGiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu giảm giá"));
    }

    private KhachHang getKhachHangByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim();
        return khachHangRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với email này"));
    }

    private void validateLienKet(
            PhieuGiamGia phieuGiamGia,
            KhachHang khachHang,
            PhieuGiamGiaKhachHang lienKetHienTai
    ) {
        if (phieuGiamGia.getLoaiPhieu() == null || phieuGiamGia.getLoaiPhieu() != LOAI_PHIEU_CA_NHAN) {
            throw new BusinessException("Chi co the tang phieu ca nhan cho khach hang.");
        }

        Integer lienKetHienTaiId = lienKetHienTai == null ? null : lienKetHienTai.getId();
        var lienKetTrung = phieuGiamGiaKhachHangRepository.findByPhieuGiamGiaIdAndKhachHangId(
                phieuGiamGia.getId(),
                khachHang.getId()
        );
        if (lienKetTrung.isPresent() && !lienKetTrung.get().getId().equals(lienKetHienTaiId)) {
            throw new BusinessException("Khach hang nay da duoc tang phieu nay.");
        }

        long soLienKetHienTai = phieuGiamGiaKhachHangRepository.countByPhieuGiamGiaId(phieuGiamGia.getId());
        boolean giuNguyenSlotPhieu = lienKetHienTai != null
                && lienKetHienTai.getPhieuGiamGia() != null
                && phieuGiamGia.getId().equals(lienKetHienTai.getPhieuGiamGia().getId());
        long tongLienKetSauCapNhat = giuNguyenSlotPhieu ? soLienKetHienTai : soLienKetHienTai + 1;

        if (phieuGiamGia.getSoLuong() != null && tongLienKetSauCapNhat > phieuGiamGia.getSoLuong()) {
            throw new BusinessException("Phieu nay da duoc tang het so luong.");
        }
    }

    public List<String> getEmailSuggestions() {
        return khachHangRepository.findAllActiveEmails();
    }
}
