package com.example.server.core.admin.quanLyDanhMuc.kichCo.service;

import com.example.server.core.admin.quanLyDanhMuc.DoiTrangThaiDanhMucRequest;
import com.example.server.core.admin.quanLyDanhMuc.kichCo.dto.request.KichCoRequest;
import com.example.server.core.admin.quanLyDanhMuc.kichCo.dto.response.KichCoResponse;
import com.example.server.entity.KichCo;
import com.example.server.infrastructure.api.PageResponse;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.KichCoRepository;
import java.time.Instant;
import java.util.regex.Pattern;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KichCoService {

    private static final Pattern KICH_CO_PATTERN = Pattern.compile(
            "^(?:(EU)\\s*)?(\\d{1,2})([.]5)?$",
            Pattern.CASE_INSENSITIVE
    );

    private final KichCoRepository kichCoRepository;

    public KichCoService(KichCoRepository kichCoRepository) {
        this.kichCoRepository = kichCoRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<KichCoResponse> danhSachKichCo(String keyword, Pageable pageable) {
        String kw = hasText(keyword) ? keyword.trim() : null;
        return PageResponse.from(kichCoRepository.search(kw, pageable).map(this::toKichCo));
    }

    @Transactional(readOnly = true)
    public KichCoResponse chiTietKichCo(Integer id) {
        return toKichCo(kichCoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại")));
    }

    @Transactional
    public KichCoResponse taoKichCo(KichCoRequest req) {
        String giaTri = normalizeKichCoGiaTri(req.giaTri());
        var existingOpt = kichCoRepository.findByGiaTriIgnoreCase(giaTri);
        
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();
            if (existing.getTrangThai() == 0) {
                existing.setTrangThai(1);
                existing.setGhiChu(hasText(req.ghiChu()) ? req.ghiChu().trim() : null);
                return toKichCo(kichCoRepository.save(existing));
            } else {
                throw new BusinessException("Kích cỡ '" + req.giaTri() + "' đã tồn tại");
            }
        }

        var entity = new KichCo();
        entity.setGiaTri(giaTri);
        entity.setGhiChu(hasText(req.ghiChu()) ? req.ghiChu().trim() : null);
        entity.setTrangThai(1);
        entity.setNgayTao(Instant.now());
        return toKichCo(kichCoRepository.save(entity));
    }

    @Transactional
    public KichCoResponse capNhatKichCo(Integer id, KichCoRequest req) {
        var entity = kichCoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại"));
        String giaTri = normalizeKichCoGiaTri(req.giaTri());
        if (kichCoRepository.existsByGiaTriIgnoreCaseAndIdNot(giaTri, id)) {
            throw new BusinessException("Kích cỡ '" + giaTri + "' đã tồn tại");
        }

        entity.setGiaTri(giaTri);
        entity.setGhiChu(hasText(req.ghiChu()) ? req.ghiChu().trim() : null);
        entity.setNgayCapNhat(Instant.now());
        return toKichCo(entity);
    }

    @Transactional
    public void doiTrangThaiKichCo(Integer id, DoiTrangThaiDanhMucRequest req) {
        var entity = kichCoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại"));
        entity.setTrangThai(req.trangThai());
        entity.setNgayCapNhat(Instant.now());
    }

    @Transactional
    public void xoaKichCo(Integer id) {
        if (!kichCoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kích cỡ #" + id + " không tồn tại");
        }
        kichCoRepository.deleteById(id);
    }

    private KichCoResponse toKichCo(KichCo entity) {
        return new KichCoResponse(
                entity.getId(),
                entity.getGiaTri(),
                entity.getGhiChu(),
                entity.getTrangThai(),
                entity.getNgayTao(),
                entity.getNgayCapNhat()
        );
    }

    private String normalizeKichCoGiaTri(String value) {
        if (!hasText(value)) {
            throw new BusinessException("Kích cỡ không được để trống");
        }

        String normalized = value.trim()
                .toUpperCase()
                .replace(',', '.')
                .replaceAll("\\s+", " ");

        var matcher = KICH_CO_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            throw new BusinessException("Kích cỡ chưa đúng định dạng, vui lòng nhập lại");
        }

        int baseValue = Integer.parseInt(matcher.group(2));
        if (baseValue < 1 || baseValue > 60) {
            throw new BusinessException("Kích cỡ phải nằm trong khoảng hợp lệ từ 1 đến 60");
        }

        String prefix = matcher.group(1) != null ? matcher.group(1).toUpperCase() + " " : "";
        String decimal = matcher.group(3) != null ? matcher.group(3) : "";
        return (prefix + baseValue + decimal).trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
