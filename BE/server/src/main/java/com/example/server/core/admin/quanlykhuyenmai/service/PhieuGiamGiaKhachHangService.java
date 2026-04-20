package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaKhachHangRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaKhachHangResponse;
import com.example.server.entity.KhachHang;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.entity.PhieuGiamGiaKhachHang;
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
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(request.getPhieuGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu giam gia"));
        KhachHang khachHang = khachHangRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khach hang voi email nay"));

        PhieuGiamGiaKhachHang phieuGiamGiaKhachHang = new PhieuGiamGiaKhachHang();
        phieuGiamGiaKhachHang.setPhieuGiamGia(phieuGiamGia);
        phieuGiamGiaKhachHang.setKhachHang(khachHang);
        phieuGiamGiaKhachHang.setNgaySuDung(toInstant(request.getNgaySuDung()));
        phieuGiamGiaKhachHang.setTrangThai(request.getTrangThai());
        phieuGiamGiaKhachHang.setNgayTao(toInstant(request.getNgayTao()));

        return phieuGiamGiaKhachHangRepository.save(phieuGiamGiaKhachHang);
    }

    public PhieuGiamGiaKhachHang update(Integer id, PhieuGiamGiaKhachHangRequest request) {
        PhieuGiamGiaKhachHang phieuGiamGiaKhachHang = phieuGiamGiaKhachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu giam gia khach hang"));

        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(request.getPhieuGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu giam gia"));
        KhachHang khachHang = khachHangRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay khach hang voi email nay"));

        phieuGiamGiaKhachHang.setPhieuGiamGia(phieuGiamGia);
        phieuGiamGiaKhachHang.setKhachHang(khachHang);
        phieuGiamGiaKhachHang.setNgaySuDung(toInstant(request.getNgaySuDung()));
        phieuGiamGiaKhachHang.setTrangThai(request.getTrangThai());
        phieuGiamGiaKhachHang.setNgayTao(toInstant(request.getNgayTao()));

        return phieuGiamGiaKhachHangRepository.save(phieuGiamGiaKhachHang);
    }

    private Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public List<String> getEmailSuggestions() {
        return khachHangRepository.findAllActiveEmails();
    }
}
