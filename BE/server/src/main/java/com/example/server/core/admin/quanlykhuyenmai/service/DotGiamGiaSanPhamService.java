package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaSanPhamRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaSanPhamResponse;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DotGiamGiaRepository;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.entity.GiayChiTiet;
import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaSanPhamBulkRequest;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DotGiamGiaSanPhamService {

    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final GiayChiTietRepository giayChiTietRepository;
    private final com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher sanPhamRealtimePublisher;

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
            Integer giayChiTietId = dgs.getGiayChiTiet().getId();
            dotGiamGiaSanPhamRepository.deleteById(id);
            dotGiamGiaSanPhamRepository.flush();
            updateGiaBanForGiayChiTiet(giayChiTietId);
        }
    }

    @Transactional
    public DotGiamGiaSanPham add(DotGiamGiaSanPhamRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt giảm giá"));
        GiayChiTiet gct = giayChiTietRepository.findById(request.getGiayChiTietId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm"));

        if (dotGiamGiaSanPhamRepository.existsByDotGiamGiaIdAndGiayChiTietId(
                request.getDotGiamGiaId(),
                request.getGiayChiTietId()
        )) {
            throw new BusinessException("Biến thể sản phẩm đã có trong đợt giảm giá này");
        }

        DotGiamGiaSanPham dotGiamGiaSanPham = new DotGiamGiaSanPham();
        dotGiamGiaSanPham.setDotGiamGia(dotGiamGia);
        dotGiamGiaSanPham.setGiayChiTiet(gct);
        dotGiamGiaSanPham.setTrangThai(request.getTrangThai());
        dotGiamGiaSanPham.setNgayTao(LocalDate.now());

        DotGiamGiaSanPham saved = dotGiamGiaSanPhamRepository.save(dotGiamGiaSanPham);
        dotGiamGiaSanPhamRepository.flush();
        updateGiaBanForGiayChiTiet(gct.getId());
        return saved;
    }

    @Transactional
    public DotGiamGiaSanPham update(Integer id, DotGiamGiaSanPhamRequest request) {
        DotGiamGiaSanPham dotGiamGiaSanPham = dotGiamGiaSanPhamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt giảm giá san pham"));

        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt giảm giá"));
        GiayChiTiet gct = giayChiTietRepository.findById(request.getGiayChiTietId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm"));

        if (dotGiamGiaSanPhamRepository.existsByDotGiamGiaIdAndGiayChiTietIdAndIdNot(
                request.getDotGiamGiaId(),
                request.getGiayChiTietId(),
                id
        )) {
            throw new BusinessException("Biến thể sản phẩm đã có trong đợt giảm giá này");
        }

        dotGiamGiaSanPham.setDotGiamGia(dotGiamGia);
        dotGiamGiaSanPham.setGiayChiTiet(gct);
        dotGiamGiaSanPham.setTrangThai(request.getTrangThai());
        if (dotGiamGiaSanPham.getNgayTao() == null) {
            dotGiamGiaSanPham.setNgayTao(LocalDate.now());
        }

        DotGiamGiaSanPham saved = dotGiamGiaSanPhamRepository.save(dotGiamGiaSanPham);
        dotGiamGiaSanPhamRepository.flush();
        updateGiaBanForGiayChiTiet(gct.getId());
        return saved;
    }

    @Transactional
    public void saveAll(DotGiamGiaSanPhamBulkRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(request.getDotGiamGiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt giảm giá"));

        // 1. Lấy danh sách ID đã có
        List<DotGiamGiaSanPham> currentLinks = dotGiamGiaSanPhamRepository
                .findByDotGiamGiaId(request.getDotGiamGiaId());
        Set<Integer> targetVariantIds = request.getGiayChiTietIds() == null
                ? new HashSet<>()
                : request.getGiayChiTietIds().stream()
                        .filter(variantId -> variantId != null && variantId > 0)
                        .collect(Collectors.toCollection(HashSet::new));

        // 2. Xóa những cái không còn trong list mới
        Set<Integer> seenVariantIds = new HashSet<>();
        List<DotGiamGiaSanPham> toDelete = currentLinks.stream()
                .filter(l -> {
                    Integer currentVariantId = l.getGiayChiTiet().getId();
                    if (!targetVariantIds.contains(currentVariantId)) {
                        return true;
                    }
                    return !seenVariantIds.add(currentVariantId);
                })
                .toList();

        if (!toDelete.isEmpty()) {
            dotGiamGiaSanPhamRepository.deleteAll(toDelete);
            dotGiamGiaSanPhamRepository.flush();
        }

        // 3. Thêm những cái mới
        Set<Integer> deletedLinkIds = toDelete.stream()
                .map(DotGiamGiaSanPham::getId)
                .collect(Collectors.toSet());
        Set<Integer> currentVariantIds = currentLinks.stream()
                .filter(l -> !deletedLinkIds.contains(l.getId()))
                .map(l -> l.getGiayChiTiet().getId())
                .collect(Collectors.toSet());

        // 3. Thêm những cái mới
        List<DotGiamGiaSanPham> toInsert = new java.util.ArrayList<>();
        for (Integer vId : targetVariantIds) {
            if (!currentVariantIds.contains(vId)) {
                GiayChiTiet gct = giayChiTietRepository.findById(vId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể #" + vId));

                DotGiamGiaSanPham dgs = new DotGiamGiaSanPham();
                dgs.setDotGiamGia(dotGiamGia);
                dgs.setGiayChiTiet(gct);
                dgs.setTrangThai(1);
                dgs.setNgayTao(LocalDate.now());
                toInsert.add(dgs);
            }
        }
        if (!toInsert.isEmpty()) {
            dotGiamGiaSanPhamRepository.saveAll(toInsert);
        }

        // 4. Flush tất cả thay đổi (delete + insert) trước khi tính lại giá
        dotGiamGiaSanPhamRepository.flush();

        // 5. Cập nhật lại giá cho tất cả biến thể liên quan (cũ, mới và cả những biến
        // thể bị tích bỏ)
        Set<Integer> deletedVariantIds = toDelete.stream()
                .map(l -> l.getGiayChiTiet().getId())
                .collect(Collectors.toSet());

        Set<Integer> allAffectedIds = new HashSet<>(currentVariantIds);
        allAffectedIds.addAll(targetVariantIds);
        allAffectedIds.addAll(deletedVariantIds);

        for (Integer vId : allAffectedIds) {
            updateGiaBanForGiayChiTiet(vId);
        }
    }

    public void updateGiaBanForGiayChiTiet(Integer giayChiTietId) {
        GiayChiTiet gct = giayChiTietRepository.findById(giayChiTietId).orElse(null);
        if (gct == null)
            return;

        gct.setNgayCapNhat(Instant.now());
        giayChiTietRepository.save(gct);
        // Báo realtime để giỏ hàng khách tự đồng bộ lại giá khi đợt giảm thay đổi.
        sanPhamRealtimePublisher.phatSauCommit("DOT_GIAM_GIA");
    }
}
