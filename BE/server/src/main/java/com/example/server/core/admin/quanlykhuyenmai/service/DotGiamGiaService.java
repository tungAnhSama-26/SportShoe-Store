package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyDotGiamGiaResponse;
import com.example.server.entity.DotGiamGia;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.DotGiamGiaRepository;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<QuanLyDotGiamGiaResponse> phanTrang(String keyword, Integer trangThai, Integer loaiGiam, java.time.LocalDate tuNgay, java.time.LocalDate denNgay, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return dotGiamGiaRepository.timKiemVaPhanTrang(keyword, trangThai, loaiGiam, tuNgay, denNgay, pageable);
    }

    public void remove(Integer id) {
        // Trước khi xóa, lấy các giayId liên quan để cập nhật lại giá sau này
        List<com.example.server.entity.DotGiamGiaSanPham> links = dotGiamGiaSanPhamRepository.findByDotGiamGiaId(id);
        dotGiamGiaRepository.deleteById(id);
        // Cập nhật lại giá cho các sản phẩm từng thuộc đợt này
        for (com.example.server.entity.DotGiamGiaSanPham link : links) {
            dotGiamGiaSanPhamService.updateGiaBanForGiay(link.getGiay().getId());
        }
    }

    public DotGiamGia add(DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = new DotGiamGia();
        mapRequestToEntity(request, dotGiamGia);
        return dotGiamGiaRepository.save(dotGiamGia);
    }

    public DotGiamGia update(Integer id, DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay dot giam gia"));

        mapRequestToEntity(request, dotGiamGia);
        DotGiamGia saved = dotGiamGiaRepository.save(dotGiamGia);

        // Sau khi update thông tin (% giảm, ngày...), cần cập nhật lại gia_ban cho các sản phẩm liên kết
        List<com.example.server.entity.DotGiamGiaSanPham> links = dotGiamGiaSanPhamRepository.findByDotGiamGiaId(id);
        for (com.example.server.entity.DotGiamGiaSanPham link : links) {
            dotGiamGiaSanPhamService.updateGiaBanForGiay(link.getGiay().getId());
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
        
        // Handle defaults for NotNull fields
        dotGiamGia.setKichHoat(request.getKichHoat() == null ? 1 : request.getKichHoat());
        
        if (dotGiamGia.getNgayTao() == null) {
            dotGiamGia.setNgayTao(request.getNgayTao() == null ? java.time.LocalDate.now() : request.getNgayTao());
        }
        
        dotGiamGia.setNgayCapNhat(request.getNgayCapNhat());
    }
}
