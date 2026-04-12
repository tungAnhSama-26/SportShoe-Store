package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
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
public class PhieuGiamGiaService {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    public List<QuanLyPhieuGiamGiaResponse> getAll() {
        return phieuGiamGiaRepository.hienThiPhieuGiamGia();
    }

    public QuanLyPhieuGiamGiaResponse getOne(Integer id) {
        return phieuGiamGiaRepository.DetailPhieuGiamGia(id);
    }

    public Page<QuanLyPhieuGiamGiaResponse> phanTrang(String keyword, Integer trangThai, Integer loai, LocalDate tuNgay, LocalDate denNgay, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Instant start = tuNgay == null ? null : tuNgay.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = denNgay == null ? null : denNgay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        return phieuGiamGiaRepository.timKiemVaPhanTrang(keyword, trangThai, loai, start, end, pageable);
    }

    public void remove(Integer id) {
        phieuGiamGiaRepository.deleteById(id);
    }

    public PhieuGiamGia add(PhieuGiamGiaRequest request) {
        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        mapRequestToEntity(request, phieuGiamGia);
        return phieuGiamGiaRepository.save(phieuGiamGia);
    }

    public PhieuGiamGia update(Integer id, PhieuGiamGiaRequest request) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phieu giam gia"));

        mapRequestToEntity(request, phieuGiamGia);

        return phieuGiamGiaRepository.save(phieuGiamGia);
    }

    private void mapRequestToEntity(PhieuGiamGiaRequest request, PhieuGiamGia phieuGiamGia) {
        phieuGiamGia.setMa(request.getMa());
        phieuGiamGia.setTen(request.getTen());
        phieuGiamGia.setLoai(request.getLoai());
        phieuGiamGia.setGiaTri(request.getGiaTri());
        phieuGiamGia.setGiaTriToiThieu(request.getGiaTriToiThieu());
        phieuGiamGia.setGiamToiDa(request.getGiamToiDa());
        phieuGiamGia.setNgayBatDau(toInstant(request.getNgayBatDau()));
        phieuGiamGia.setNgayKetThuc(toInstant(request.getNgayKetThuc()));
        phieuGiamGia.setSoLuong(request.getSoLuong());
        phieuGiamGia.setSoLuongDaDung(request.getSoLuongDaDung());
        phieuGiamGia.setTrangThai(request.getTrangThai());
        phieuGiamGia.setNgayTao(toInstant(request.getNgayTao()));
        phieuGiamGia.setNgayCapNhat(toInstant(request.getNgayCapNhat()));
    }

    private Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
