package com.example.server.core.admin.quanLyDanhMuc.deGiay.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.request.DeGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.deGiay.dto.response.DeGiayResponse;
import com.example.server.entity.DeGiay;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DeGiayRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeGiayService {

    private final DeGiayRepository deGiayRepository;

    public DeGiayService(DeGiayRepository deGiayRepository) {
        this.deGiayRepository = deGiayRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<DeGiayResponse> danhSachDeGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(deGiayRepository.search(kw, pageable).map(this::toDeGiay));
    }

    @Transactional(readOnly = true)
    public DeGiayResponse chiTietDeGiay(Integer id) {
        return toDeGiay(deGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đế giày #" + id + " không tồn tại")));
    }

    @Transactional
    public DeGiayResponse taoDeGiay(DeGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (deGiayRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã đế giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        var existingOpt = deGiayRepository.findByTenIgnoreCase(req.ten().trim());
        if (existingOpt.isPresent()) {
            DeGiay existing = existingOpt.get();
            if (existing.getTrangThai() != null && existing.getTrangThai() == 1) {
                throw new BusinessException("Đế giày '" + ten + "' đã tồn tại và đang hoạt động.");
            } else {
                existing.setTrangThai(1);
                existing.setNgayCapNhat(Instant.now());
                if (req.moTa() != null && !req.moTa().isBlank()) {
                    existing.setMoTa(req.moTa().trim());
                }
                return toDeGiay(deGiayRepository.save(existing));
            }
        }

        var entity = new DeGiay();
        entity.setMa(ma);
        entity.setTen(ten);
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toDeGiay(deGiayRepository.save(entity));
    }

    @Transactional
    public DeGiayResponse capNhatDeGiay(Integer id, DeGiayRequest req) {
        var entity = deGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đế giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (deGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã đế giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        if (deGiayRepository.existsByTenIgnoreCaseAndIdNot(ten, id)) {
            throw new BusinessException("Đế giày '" + ten + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toDeGiay(entity);
    }

    @Transactional
    public void doiTrangThaiDeGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = deGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đế giày #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaDeGiay(Integer id) {
        if (!deGiayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Đế giày #" + id + " không tồn tại");
        }
        deGiayRepository.deleteById(id);
    }

    private DeGiayResponse toDeGiay(DeGiay entity) {
        return new DeGiayResponse(
                entity.getId(),
                entity.getMa(),
                entity.getTen(),
                entity.getMoTa(),
                entity.getTrangThai(),
                entity.getNgayTao(),
                entity.getNgayCapNhat()
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
