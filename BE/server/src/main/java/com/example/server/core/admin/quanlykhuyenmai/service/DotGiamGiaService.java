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

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DotGiamGiaService {

    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    private final DotGiamGiaSanPhamService dotGiamGiaSanPhamService;

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

    public void remove(Integer id) {
        // Trước khi xóa, lấy các giayId liên quan để cập nhật lại giá sau này
        List<DotGiamGiaSanPham> links = dotGiamGiaSanPhamRepository.findByDotGiamGiaId(id);
        dotGiamGiaRepository.deleteById(id);
        // Cập nhật lại giá cho các biến thể sản phẩm từng thuộc đợt này
        for (DotGiamGiaSanPham link : links) {
            dotGiamGiaSanPhamService.updateGiaBanForGiayChiTiet(link.getGiayChiTiet().getId());
        }
    }

    public DotGiamGia add(DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = new DotGiamGia();
        mapRequestToEntity(request, dotGiamGia);
        return dotGiamGiaRepository.save(dotGiamGia);
    }

    public DotGiamGia update(Integer id, DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt giảm giá"));

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
            dotGiamGia.setKichHoat(2); // Hết hạn
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
            dotGiamGia.setNgayTao(request.getNgayTao() == null ? LocalDate.now() : request.getNgayTao());
        }

        if (dotGiamGia.getId() != null) {
            dotGiamGia.setNgayCapNhat(LocalDate.now());
        }
    }
}
