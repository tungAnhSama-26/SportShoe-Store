package com.example.server.core.admin.quanLyDanhMuc.loaiGiay.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.request.LoaiGiayRequest;
import com.example.server.core.admin.quanLyDanhMuc.loaiGiay.dto.response.LoaiGiayResponse;
import com.example.server.entity.LoaiGiay;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.LoaiGiayRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoaiGiayService {

    private final LoaiGiayRepository loaiGiayRepository;

    public LoaiGiayService(LoaiGiayRepository loaiGiayRepository) {
        this.loaiGiayRepository = loaiGiayRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<LoaiGiayResponse> danhSachLoaiGiay(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(loaiGiayRepository.search(kw, pageable).map(this::toLoaiGiay));
    }

    @Transactional(readOnly = true)
    public LoaiGiayResponse chiTietLoaiGiay(Integer id) {
        return toLoaiGiay(loaiGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày #" + id + " không tồn tại")));
    }

    @Transactional
    public LoaiGiayResponse taoLoaiGiay(LoaiGiayRequest req) {
        String ma = req.ma().trim().toUpperCase();
        if (loaiGiayRepository.existsByMaIgnoreCase(ma)) {
            throw new BusinessException("Mã loại giày '" + ma + "' đã tồn tại");
        }

        String ten = req.ten().trim();
        if (loaiGiayRepository.existsByTenIgnoreCase(ten)) {
            throw new BusinessException("Loại giày '" + ten + "' đã tồn tại trong hệ thống. Nếu không thấy, vui lòng kiểm tra xem nó có đang bị ngừng hoạt động không.");
        }

        var entity = new LoaiGiay();
        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toLoaiGiay(loaiGiayRepository.save(entity));
    }

    @Transactional
    public LoaiGiayResponse capNhatLoaiGiay(Integer id, LoaiGiayRequest req) {
        var entity = loaiGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày #" + id + " không tồn tại"));
        String ma = req.ma().trim().toUpperCase();
        if (loaiGiayRepository.existsByMaIgnoreCaseAndIdNot(ma, id)) {
            throw new BusinessException("Mã loại giày '" + ma + "' đã tồn tại");
        }
        
        String ten = req.ten().trim();
        if (loaiGiayRepository.existsByTenIgnoreCaseAndIdNot(ten, id)) {
            throw new BusinessException("Loại giày '" + ten + "' đã tồn tại trong hệ thống. Nếu không thấy, vui lòng kiểm tra xem nó có đang bị ngừng hoạt động không.");
        }

        entity.setMa(ma);
        entity.setTen(req.ten().trim());
        entity.setMoTa(req.moTa());
        entity.setNgayCapNhat(Instant.now());
        return toLoaiGiay(entity);
    }

    @Transactional
    public void doiTrangThaiLoaiGiay(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = loaiGiayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại giày #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaLoaiGiay(Integer id) {
        if (!loaiGiayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Loại giày #" + id + " không tồn tại");
        }
        loaiGiayRepository.deleteById(id);
    }

    private LoaiGiayResponse toLoaiGiay(LoaiGiay entity) {
        return new LoaiGiayResponse(
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
