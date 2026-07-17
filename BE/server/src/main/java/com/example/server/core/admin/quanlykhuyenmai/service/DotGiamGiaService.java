package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DotGiamGiaService {

    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaSanPhamService dotGiamGiaSanPhamService;
    private final com.example.server.core.client.thongbao.service.ClientThongBaoService clientThongBaoService;

    public java.util.Map<String, Boolean> checkTenTrung(String ten, Integer id) {
        boolean exists = false;
        if (id != null && id > 0) {
            exists = dotGiamGiaRepository.existsByTenIgnoreCaseAndIdNot(ten, id);
        } else {
            exists = dotGiamGiaRepository.existsByTenIgnoreCase(ten);
        }
        return java.util.Map.of("exists", exists);
    }

    public List<QuanLyDotGiamGiaResponse> getAll() {
        return dotGiamGiaRepository.hienThiDotGiamGia();
    }

    public QuanLyDotGiamGiaResponse getOne(Integer id) {
        return dotGiamGiaRepository.detailDotGiamGia(id);
    }

    public Page<QuanLyDotGiamGiaResponse> phanTrang(String keyword, Integer trangThai, Integer loaiGiam,
            LocalDate tuNgay, LocalDate denNgay, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return dotGiamGiaRepository.timKiemVaPhanTrang(keyword, trangThai, loaiGiam, tuNgay, denNgay, pageable);
    }

    @Transactional
    public void remove(Integer id) {
        List<DotGiamGiaSanPham> links = dotGiamGiaSanPhamRepository.findByDotGiamGiaId(id);
        if (!links.isEmpty()) {
            dotGiamGiaSanPhamRepository.deleteAll(links);
            dotGiamGiaSanPhamRepository.flush();
        }
        dotGiamGiaRepository.deleteById(id);
        for (DotGiamGiaSanPham link : links) {
            dotGiamGiaSanPhamService.updateGiaBanForGiayChiTiet(link.getGiayChiTiet().getId());
        }
    }

    public DotGiamGia add(DotGiamGiaRequest request) {
        normalize(request);
        validateBusinessRules(request, null);
        DotGiamGia dotGiamGia = new DotGiamGia();
        mapRequestToEntity(request, dotGiamGia);
        dotGiamGia.setNgayTao(LocalDate.now());
        DotGiamGia saved = dotGiamGiaRepository.save(dotGiamGia);
        // Báo vào chuông thông báo của mọi khách về đợt giảm giá mới.
        clientThongBaoService.guiChoTatCaKhach(
                "GIAM_GIA",
                "Đợt giảm giá mới: " + saved.getTen(),
                com.example.server.core.client.thongbao.service.ClientThongBaoService.moTaDotGiamGia(saved),
                "/khachhang/san-pham");
        return saved;
    }

    public DotGiamGia update(Integer id, DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt giảm giá"));
        normalize(request);
        validateBusinessRules(request, id);

        // Cho phép toggle kichHoat 0 <-> 1 (ngừng/đang hoạt động)

        mapRequestToEntity(request, dotGiamGia);
        DotGiamGia saved = dotGiamGiaRepository.save(dotGiamGia);

        // Sau khi update thông tin (% giảm, ngày...), cần cập nhật lại gia_ban cho các
        // biến thể liên kết
        List<DotGiamGiaSanPham> links = dotGiamGiaSanPhamRepository.findByDotGiamGiaId(id);
        for (DotGiamGiaSanPham link : links) {
            dotGiamGiaSanPhamService.updateGiaBanForGiayChiTiet(link.getGiayChiTiet().getId());
        }

        return saved;
    }

    private void mapRequestToEntity(DotGiamGiaRequest request, DotGiamGia dotGiamGia) {
        dotGiamGia.setMa(request.getMa());
        dotGiamGia.setTen(request.getTen());
        dotGiamGia.setMoTa(request.getMoTa());
        dotGiamGia.setLoaiGiam(request.getLoaiGiam());
        dotGiamGia.setGiaTriGiam(request.getGiaTriGiam());
        dotGiamGia.setNgayBatDau(request.getNgayBatDau());
        dotGiamGia.setNgayKetThuc(request.getNgayKetThuc());

        // Tự động tính toán trạng thái
        LocalDate now = LocalDate.now();
        LocalDate start = dotGiamGia.getNgayBatDau();
        LocalDate end = dotGiamGia.getNgayKetThuc();

        if (end != null && end.isBefore(now)) {
            dotGiamGia.setKichHoat(2);
        } else {
            Integer requestedStatus = request.getKichHoat();
            if (requestedStatus != null && requestedStatus == 0) {
                dotGiamGia.setKichHoat(0); // Ngừng hoạt động thủ công
            } else if (start != null && start.isAfter(now)) {
                dotGiamGia.setKichHoat(4); // Sắp diễn ra
            } else {
                dotGiamGia.setKichHoat(1); // Hoạt động
            }
        }

        if (dotGiamGia.getNgayTao() == null) {
            dotGiamGia.setNgayTao(LocalDate.now());
        }

        if (dotGiamGia.getId() != null) {
            dotGiamGia.setNgayCapNhat(LocalDate.now());
        }
    }

    private void normalize(DotGiamGiaRequest request) {
        request.setMa(request.getMa() == null ? null : request.getMa().trim().toUpperCase());
        request.setTen(request.getTen() == null ? null : request.getTen().trim());
        request.setMoTa(request.getMoTa() == null ? null : request.getMoTa().trim());
    }

    private void validateBusinessRules(DotGiamGiaRequest request, Integer currentId) {
        Map<String, String> errors = new LinkedHashMap<>();
        boolean duplicateCode = currentId == null
                ? dotGiamGiaRepository.existsByMaIgnoreCase(request.getMa())
                : dotGiamGiaRepository.existsByMaIgnoreCaseAndIdNot(request.getMa(), currentId);
        boolean duplicateName = currentId == null
                ? dotGiamGiaRepository.existsByTenIgnoreCase(request.getTen())
                : dotGiamGiaRepository.existsByTenIgnoreCaseAndIdNot(request.getTen(), currentId);

        if (duplicateCode) {
            errors.put("ma", "Mã đợt giảm giá đã tồn tại");
        }
        if (duplicateName) {
            errors.put("ten", "Tên đợt giảm giá đã tồn tại");
        }
        if (request.getNgayBatDau() != null
                && request.getNgayKetThuc() != null
                && request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            errors.put("ngayKetThuc", "Ngày kết thúc không được trước ngày bắt đầu");
        }
        if (request.getKichHoat() != null
                && request.getKichHoat() != 0
                && request.getKichHoat() != 1
                && request.getKichHoat() != 2
                && request.getKichHoat() != 4) {
            errors.put("kichHoat", "Trạng thái đợt giảm giá không hợp lệ");
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(
                    com.example.server.infrastructure.exception.ErrorCode.VALIDATION_ERROR,
                    "Vui lòng kiểm tra lại thông tin đợt giảm giá",
                    errors
            );
        }
    }
}
