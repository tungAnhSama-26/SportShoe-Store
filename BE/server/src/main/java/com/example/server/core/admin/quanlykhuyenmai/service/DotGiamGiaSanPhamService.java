package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaSanPhamRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import com.example.server.entity.Giay;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DotGiamGiaRepository;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import com.example.server.repository.GiayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaSanPhamService {

    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final GiayRepository giayRepository;

    public List<QuanLyDotGiamGiaSanPhamResponse> getAll() {
        return dotGiamGiaSanPhamRepository.hienThiQuanLyDotGiamGiaSanPham();
    }

    public QuanLyDotGiamGiaSanPhamResponse getOne(Integer id) {
        return dotGiamGiaSanPhamRepository.detailQuanLyDotGiamGiaSanPham(id);
    }

    public Page<QuanLyDotGiamGiaSanPhamResponse> phanTrang(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return dotGiamGiaSanPhamRepository.phanTrangQuanLyDotGiamGiaSanPham(pageable);
    }

    public void remove(Integer id) {
        dotGiamGiaSanPhamRepository.deleteById(id);
    }

    public DotGiamGiaSanPham add(DotGiamGiaSanPhamRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia"));
        Giay giay = giayRepository.findById(request.getGiayId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay giay"));

        DotGiamGiaSanPham dotGiamGiaSanPham = new DotGiamGiaSanPham();
        dotGiamGiaSanPham.setDotGiamGia(dotGiamGia);
        dotGiamGiaSanPham.setGiay(giay);
        dotGiamGiaSanPham.setTrangThai(request.getTrangThai());
        dotGiamGiaSanPham.setNgayTao(request.getNgayTao());

        return dotGiamGiaSanPhamRepository.save(dotGiamGiaSanPham);
    }

    public DotGiamGiaSanPham update(Integer id, DotGiamGiaSanPhamRequest request) {
        DotGiamGiaSanPham dotGiamGiaSanPham = dotGiamGiaSanPhamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia san pham"));

        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia"));
        Giay giay = giayRepository.findById(request.getGiayId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay giay"));

        dotGiamGiaSanPham.setDotGiamGia(dotGiamGia);
        dotGiamGiaSanPham.setGiay(giay);
        dotGiamGiaSanPham.setTrangThai(request.getTrangThai());
        dotGiamGiaSanPham.setNgayTao(request.getNgayTao());

        return dotGiamGiaSanPhamRepository.save(dotGiamGiaSanPham);
    }
}
