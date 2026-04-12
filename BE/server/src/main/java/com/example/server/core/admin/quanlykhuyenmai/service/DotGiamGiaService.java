package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse;
import com.example.server.entity.DotGiamGia;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DotGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaService {

    private final DotGiamGiaRepository dotGiamGiaRepository;

    public List<QuanLyDotGiamGiaResponse> getAll() {
        return dotGiamGiaRepository.hienThiDotGiamGia();
    }

    public QuanLyDotGiamGiaResponse getOne(Integer id) {
        return dotGiamGiaRepository.detailDotGiamGia(id);
    }

    public Page<QuanLyDotGiamGiaResponse> phanTrang(String keyword, Integer trangThai, Integer loaiGiam, java.time.LocalDate tuNgay, java.time.LocalDate denNgay, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return dotGiamGiaRepository.timKiemVaPhanTrang(keyword, trangThai, loaiGiam, tuNgay, denNgay, pageable);
    }

    public void remove(Integer id) {
        dotGiamGiaRepository.deleteById(id);
    }

    public DotGiamGia add(DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = new DotGiamGia();
        BeanUtils.copyProperties(request, dotGiamGia);
        return dotGiamGiaRepository.save(dotGiamGia);
    }

    public DotGiamGia update(Integer id, DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia"));

        dotGiamGia.setMa(request.getMa());
        dotGiamGia.setTen(request.getTen());
        dotGiamGia.setMoTa(request.getMoTa());
        dotGiamGia.setLoaiGiam(request.getLoaiGiam());
        dotGiamGia.setGiaTriGiam(request.getGiaTriGiam());
        dotGiamGia.setNgayBatDau(request.getNgayBatDau());
        dotGiamGia.setNgayKetThuc(request.getNgayKetThuc());
        dotGiamGia.setKichHoat(request.getKichHoat());
        dotGiamGia.setNgayTao(request.getNgayTao());
        dotGiamGia.setNgayCapNhat(request.getNgayCapNhat());

        return dotGiamGiaRepository.save(dotGiamGia);
    }
}
