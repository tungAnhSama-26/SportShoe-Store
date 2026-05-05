package com.example.server.core.admin.quanLyDanhMuc.congNgheDem.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.congNgheDem.dto.request.CongNgheDemRequest;
import com.example.server.core.admin.quanLyDanhMuc.congNgheDem.dto.response.CongNgheDemResponse;
import com.example.server.entity.CongNgheDem;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.CongNgheDemRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CongNgheDemService {

    private final CongNgheDemRepository congNgheDemRepository;

    public CongNgheDemService(CongNgheDemRepository congNgheDemRepository) {
        this.congNgheDemRepository = congNgheDemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CongNgheDemResponse> danhSachCongNgheDem(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(congNgheDemRepository.search(kw, pageable).map(this::toCongNgheDem));
    }

    @Transactional(readOnly = true)
    public CongNgheDemResponse chiTietCongNgheDem(Integer id) {
        return toCongNgheDem(congNgheDemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại")));
    }

    @Transactional
    public CongNgheDemResponse taoCongNgheDem(CongNgheDemRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (congNgheDemRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã công nghệ đệm '" + ma + "' đã tồn tại");
        }

        var entity = new CongNgheDem();
        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toCongNgheDem(congNgheDemRepository.save(entity));
    }

    @Transactional
    public CongNgheDemResponse capNhatCongNgheDem(Integer id, CongNgheDemRequest req) {
        var entity = congNgheDemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (congNgheDemRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã công nghệ đệm '" + ma + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toCongNgheDem(entity);
    }

    @Transactional
    public void doiTrangThaiCongNgheDem(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = congNgheDemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaCongNgheDem(Integer id) {
        if (!congNgheDemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Công nghệ đệm #" + id + " không tồn tại");
        }
        congNgheDemRepository.deleteById(id);
    }

    private CongNgheDemResponse toCongNgheDem(CongNgheDem entity) {
        return new CongNgheDemResponse(
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
