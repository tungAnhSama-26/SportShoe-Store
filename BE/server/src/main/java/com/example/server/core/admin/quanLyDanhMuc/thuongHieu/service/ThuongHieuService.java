package com.example.server.core.admin.quanLyDanhMuc.thuongHieu.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.request.ThuongHieuRequest;
import com.example.server.core.admin.quanLyDanhMuc.thuongHieu.dto.response.ThuongHieuResponse;
import com.example.server.entity.ThuongHieu;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.ThuongHieuRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThuongHieuService {

    private final ThuongHieuRepository thuongHieuRepository;

    public ThuongHieuService(ThuongHieuRepository thuongHieuRepository) {
        this.thuongHieuRepository = thuongHieuRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ThuongHieuResponse> danhSachThuongHieu(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(thuongHieuRepository.search(kw, pageable).map(this::toThuongHieu));
    }

    @Transactional(readOnly = true)
    public ThuongHieuResponse chiTietThuongHieu(Integer id) {
        return toThuongHieu(thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại")));
    }

    @Transactional
    public ThuongHieuResponse taoThuongHieu(ThuongHieuRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (thuongHieuRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã thương hiệu '" + ma + "' đã tồn tại");
        }

        var entity = new ThuongHieu();
        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setXuatXu(req.xuatXu());
        entity.setLogoUrl(req.logoUrl());
        entity.setWebsite(req.website());
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toThuongHieu(thuongHieuRepository.save(entity));
    }

    @Transactional
    public ThuongHieuResponse capNhatThuongHieu(Integer id, ThuongHieuRequest req) {
        var entity = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (thuongHieuRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã thương hiệu '" + ma + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setXuatXu(req.xuatXu());
        entity.setLogoUrl(req.logoUrl());
        entity.setWebsite(req.website());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toThuongHieu(entity);
    }

    @Transactional
    public void doiTrangThaiThuongHieu(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaThuongHieu(Integer id) {
        if (!thuongHieuRepository.existsById(id)) {
            throw new ResourceNotFoundException("Thương hiệu #" + id + " không tồn tại");
        }
        thuongHieuRepository.deleteById(id);
    }

    private ThuongHieuResponse toThuongHieu(ThuongHieu entity) {
        return new ThuongHieuResponse(
                entity.getId(),
                entity.getMa(),
                entity.getTen(),
                entity.getXuatXu(),
                entity.getLogoUrl(),
                entity.getWebsite(),
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
