package com.example.server.core.admin.quanLyDanhMuc.coGiay.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.coGiay.dto.request.CoGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.coGiay.dto.response.CoGiayResponse;
import com.example.server.entity.CoGiay;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.CoGiayRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoGiayService {

    private final CoGiayRepository coGiayRepository;

    public CoGiayService(CoGiayRepository coGiayRepository) {
        this.coGiayRepository = coGiayRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CoGiayResponse> danhSachCoGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(coGiayRepository.search(kw, pageable).map(this::toCoGiay));
    }

    @Transactional(readOnly = true)
    public CoGiayResponse chiTietCoGiay(Integer id) {
        return toCoGiay(coGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại")));
    }

    @Transactional
    public CoGiayResponse taoCoGiay(CoGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (coGiayRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã cổ giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        var existingOpt = coGiayRepository.findByTenIgnoreCase(req.ten().trim());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getTrangThai() == 0) {
                existing.setTrangThai(1);
                existing.setMoTa(req.moTa());
                existing.setMa(ma);
                return toCoGiay(coGiayRepository.save(existing));
            } else {
                throw new BusinessException("T�n c? gi�y '" + req.ten() + "' d� t?n t?i");
            }
        }

        var entity = new CoGiay();
        entity.setMa(ma);
        entity.setTen(ten);
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toCoGiay(coGiayRepository.save(entity));
    }

    @Transactional
    public CoGiayResponse capNhatCoGiay(Integer id, CoGiayRequest req) {
        var entity = coGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (coGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã cổ giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        if (coGiayRepository.existsByTenIgnoreCaseAndIdNot(ten, id)) {
            throw new BusinessException("Cổ giày '" + ten + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toCoGiay(entity);
    }

    @Transactional
    public void doiTrangThaiCoGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = coGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaCoGiay(Integer id) {
        if (!coGiayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cổ giày #" + id + " không tồn tại");
        }
        coGiayRepository.deleteById(id);
    }

    private CoGiayResponse toCoGiay(CoGiay entity) {
        return new CoGiayResponse(
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
