package com.example.server.core.admin.quanLyDanhMuc.mauSac.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.request.MauSacRequest;
import com.example.server.core.admin.quanLyDanhMuc.mauSac.dto.response.MauSacResponse;
import com.example.server.entity.MauSac;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.MauSacRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MauSacService {

    private final MauSacRepository mauSacRepository;

    public MauSacService(MauSacRepository mauSacRepository) {
        this.mauSacRepository = mauSacRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<MauSacResponse> danhSachMauSac(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(mauSacRepository.search(kw, pageable).map(this::toMauSac));
    }

    @Transactional(readOnly = true)
    public MauSacResponse chiTietMauSac(Integer id) {
        return toMauSac(mauSacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại")));
    }

    @Transactional
    public MauSacResponse taoMauSac(MauSacRequest req) {
        String ten = req.ten().trim();
        var existingOpt = mauSacRepository.findByTenIgnoreCase(ten);
        
        if (existingOpt.isPresent()) {
            MauSac existing = existingOpt.get();
            if (existing.getTrangThai() != null && existing.getTrangThai() == 1) {
                throw new BusinessException("Màu sắc '" + ten + "' đã tồn tại và đang hoạt động.");
            } else {
                // If it is disabled, reactivate it
                existing.setTrangThai(1);
                // Also update MaMauHex if provided
                if (req.maMauHex() != null && !req.maMauHex().isBlank()) {
                    existing.setMaMauHex(req.maMauHex());
                }
                existing.setNgayCapNhat(Instant.now());
                return toMauSac(mauSacRepository.save(existing));
            }
        }

        String ma = req.ma().trim().toUpperCase();
        if (mauSacRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã màu sắc '" + ma + "' đã tồn tại");
        }

        var entity = new MauSac();
        entity.setMa(ma);
        entity.setTen(ten);
        entity.setMaMauHex(req.maMauHex());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toMauSac(mauSacRepository.save(entity));
    }

    @Transactional
    public MauSacResponse capNhatMauSac(Integer id, MauSacRequest req) {
        var entity = mauSacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (mauSacRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã màu sắc '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        if (mauSacRepository.existsByTenIgnoreCaseAndIdNot(ten, id)) {
            throw new BusinessException("Tên màu sắc '" + ten + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setTen(ten);
        entity.setMaMauHex(req.maMauHex());
        entity.setNgayCapNhat(Instant.now());
        return toMauSac(entity);
    }

    @Transactional
    public void doiTrangThaiMauSac(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = mauSacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaMauSac(Integer id) {
        if (!mauSacRepository.existsById(id)) {
            throw new ResourceNotFoundException("Màu sắc #" + id + " không tồn tại");
        }
        mauSacRepository.deleteById(id);
    }

    private MauSacResponse toMauSac(MauSac entity) {
        return new MauSacResponse(
                entity.getId(),
                entity.getMa(),
                entity.getTen(),
                entity.getMaMauHex(),
                entity.getTrangThai(),
                entity.getNgayTao(),
                entity.getNgayCapNhat()
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
