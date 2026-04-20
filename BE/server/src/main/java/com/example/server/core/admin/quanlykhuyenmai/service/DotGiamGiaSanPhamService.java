package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaSanPhamRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import com.example.server.entity.Giay;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DotGiamGiaRepository;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import com.example.server.repository.GiayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.entity.GiayChiTiet;

@Service
@RequiredArgsConstructor
public class DotGiamGiaSanPhamService {

    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final GiayRepository giayRepository;
    private final GiayChiTietRepository giayChiTietRepository;

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

    @Transactional
    public void remove(Integer id) {
        DotGiamGiaSanPham dgs = dotGiamGiaSanPhamRepository.findById(id).orElse(null);
        if (dgs != null) {
            Integer giayId = dgs.getGiay().getId();
            dotGiamGiaSanPhamRepository.deleteById(id);
            updateGiaBanForGiay(giayId);
        }
    }

    @Transactional
    public DotGiamGiaSanPham add(DotGiamGiaSanPhamRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia"));
        Giay giay = giayRepository.findById(request.getGiayId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay giay"));

        List<DotGiamGiaSanPham> actives = dotGiamGiaSanPhamRepository.findActiveByGiayId(giay.getId());
        if (!actives.isEmpty() && request.getTrangThai() == 1) {
            throw new BusinessException("Sản phẩm này đang được áp dụng trong một đợt giảm giá khác. Tính năng chỉ cho phép 1 sản phẩm tham gia 1 đợt giảm giá kích hoạt.");
        }

        DotGiamGiaSanPham dotGiamGiaSanPham = new DotGiamGiaSanPham();
        dotGiamGiaSanPham.setDotGiamGia(dotGiamGia);
        dotGiamGiaSanPham.setGiay(giay);
        dotGiamGiaSanPham.setTrangThai(request.getTrangThai());
        dotGiamGiaSanPham.setNgayTao(request.getNgayTao());

        DotGiamGiaSanPham saved = dotGiamGiaSanPhamRepository.save(dotGiamGiaSanPham);
        updateGiaBanForGiay(giay.getId());
        return saved;
    }

    @Transactional
    public DotGiamGiaSanPham update(Integer id, DotGiamGiaSanPhamRequest request) {
        DotGiamGiaSanPham dotGiamGiaSanPham = dotGiamGiaSanPhamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia san pham"));

        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia"));
        Giay giay = giayRepository.findById(request.getGiayId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay giay"));

        if (request.getTrangThai() == 1) {
            List<DotGiamGiaSanPham> actives = dotGiamGiaSanPhamRepository.findActiveByGiayId(giay.getId());
            for (DotGiamGiaSanPham active : actives) {
                if (!active.getId().equals(id)) {
                    throw new BusinessException("Sản phẩm này đang được áp dụng trong một đợt giảm giá khác.");
                }
            }
        }

        dotGiamGiaSanPham.setDotGiamGia(dotGiamGia);
        dotGiamGiaSanPham.setGiay(giay);
        dotGiamGiaSanPham.setTrangThai(request.getTrangThai());
        dotGiamGiaSanPham.setNgayTao(request.getNgayTao());

        DotGiamGiaSanPham saved = dotGiamGiaSanPhamRepository.save(dotGiamGiaSanPham);
        updateGiaBanForGiay(giay.getId());
        return saved;
    }

    public void updateGiaBanForGiay(Integer giayId) {
        List<DotGiamGiaSanPham> activeDiscounts = dotGiamGiaSanPhamRepository.findActiveByGiayId(giayId);
        List<GiayChiTiet> chiTiets = giayChiTietRepository.findByGiayIdEager(giayId);
        
        LocalDate now = LocalDate.now();
        for (GiayChiTiet gct : chiTiets) {
            BigDecimal bestGiaBan = gct.getGiaGoc();
            // Optional: If user already manually set a lower giaBan and there are no active discounts,
            // we could revert it to giaGoc or keep it if it's lower.
            // But realistically, an active discount is applied ON TOP of giaGoc.
            if (activeDiscounts.isEmpty()) {
                gct.setGiaBan(gct.getGiaGoc()); // reset to original price if no discounts exist
                giayChiTietRepository.save(gct);
                continue;
            }
            
            for (DotGiamGiaSanPham dgs : activeDiscounts) {
                DotGiamGia dg = dgs.getDotGiamGia();
                if (dg.getKichHoat() == null || dg.getKichHoat() == 0) continue;
                if (dg.getNgayBatDau() != null && now.isBefore(dg.getNgayBatDau())) continue;
                if (dg.getNgayKetThuc() != null && now.isAfter(dg.getNgayKetThuc())) continue;
                
                BigDecimal expectedGiaBan = gct.getGiaGoc();
                if (dg.getLoaiGiam() == 1) { // percent
                    BigDecimal discountAmt = gct.getGiaGoc().multiply(dg.getGiaTriGiam()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    expectedGiaBan = gct.getGiaGoc().subtract(discountAmt);
                } else if (dg.getLoaiGiam() == 2) { // cash
                    expectedGiaBan = gct.getGiaGoc().subtract(dg.getGiaTriGiam());
                }
                
                if (expectedGiaBan.compareTo(BigDecimal.ZERO) < 0) {
                    expectedGiaBan = BigDecimal.ZERO;
                }
                
                // Compare with bestGiaBan
                if (expectedGiaBan.compareTo(bestGiaBan) < 0) {
                    bestGiaBan = expectedGiaBan;
                }
            }
            gct.setGiaBan(bestGiaBan);
            giayChiTietRepository.save(gct);
        }
    }
}
