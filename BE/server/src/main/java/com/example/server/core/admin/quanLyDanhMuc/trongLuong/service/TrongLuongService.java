package com.example.server.core.admin.quanLyDanhMuc.trongLuong.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.request.TrongLuongRequest;
import com.example.server.core.admin.quanLyDanhMuc.trongLuong.dto.response.TrongLuongResponse;
import com.example.server.entity.TrongLuong;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.TrongLuongRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrongLuongService {

    private final TrongLuongRepository trongLuongRepository;

    public TrongLuongService(TrongLuongRepository trongLuongRepository) {
        this.trongLuongRepository = trongLuongRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<TrongLuongResponse> danhSachTrongLuong(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(trongLuongRepository.search(kw, pageable).map(this::toTrongLuong));
    }

    @Transactional(readOnly = true)
    public TrongLuongResponse chiTietTrongLuong(Integer id) {
        return toTrongLuong(trongLuongRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại")));
    }

    @Transactional
    public TrongLuongResponse taoTrongLuong(TrongLuongRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (trongLuongRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã trọng lượng '" + ma + "' đã tồn tại");
        }

        String giaTri = normalizeTrongLuongGiaTri(req.giaTri());
        var existingOpt = trongLuongRepository.findByGiaTri(giaTri);
        if (existingOpt.isPresent()) {
            TrongLuong existing = existingOpt.get();
            if (existing.getTrangThai() != null && existing.getTrangThai() == 1) {
                throw new BusinessException("Trọng lượng '" + req.giaTri() + "g' đã tồn tại và đang hoạt động.");
            } else {
                existing.setTrangThai(1);
                existing.setNgayCapNhat(Instant.now());
                if (req.donVi() != null && !req.donVi().isBlank()) {
                    existing.setDonVi(req.donVi().trim());
                }
                return toTrongLuong(trongLuongRepository.save(existing));
            }
        }

        var entity = new TrongLuong();
        entity.setMa(ma);
        entity.setGiaTri(req.giaTri());
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toTrongLuong(trongLuongRepository.save(entity));
    }

    @Transactional
    public TrongLuongResponse capNhatTrongLuong(Integer id, TrongLuongRequest req) {
        var entity = trongLuongRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (trongLuongRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã trọng lượng '" + ma + "' đã tồn tại");
        }

        if (trongLuongRepository.existsByGiaTriAndIdNot(req.giaTri(), id)) {
            throw new BusinessException("Giá trị trọng lượng '" + req.giaTri() + "' đã tồn tại");
        }

        entity.setMa(ma);
        entity.setGiaTri(req.giaTri());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toTrongLuong(entity);
    }

    @Transactional
    public void doiTrangThaiTrongLuong(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = trongLuongRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaTrongLuong(Integer id) {
        if (!trongLuongRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trọng lượng #" + id + " không tồn tại");
        }
        trongLuongRepository.deleteById(id);
    }

    private TrongLuongResponse toTrongLuong(TrongLuong entity) {
        return new TrongLuongResponse(
                entity.getId(),
                entity.getMa(),
                entity.getGiaTri(),
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
