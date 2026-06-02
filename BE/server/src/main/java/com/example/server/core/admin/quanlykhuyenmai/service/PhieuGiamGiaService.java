package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaRequest;
import com.example.server.core.admin.quanlykhuyenmai.dto.response.QuanLyPhieuGiamGiaResponse;
import com.example.server.entity.PhieuGiamGia;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.PhieuGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuGiamGiaService {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    public java.util.Map<String, Boolean> checkTenTrung(String ten, Integer id) {
        boolean exists = false;
        if (id != null && id > 0) {
            exists = phieuGiamGiaRepository.existsByTenIgnoreCaseAndIdNot(ten, id);
        } else {
            exists = phieuGiamGiaRepository.existsByTenIgnoreCase(ten);
        }
        return java.util.Map.of("exists", exists);
    }

    public List<QuanLyPhieuGiamGiaResponse> getAll() {
        return phieuGiamGiaRepository.hienThiPhieuGiamGia();
    }

    public QuanLyPhieuGiamGiaResponse getOne(Integer id) {
        return phieuGiamGiaRepository.DetailPhieuGiamGia(id);
    }

    public Page<QuanLyPhieuGiamGiaResponse> phanTrang(String keyword, Integer trangThai, Integer loai, LocalDate tuNgay,
            LocalDate denNgay, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Instant start = tuNgay == null ? null : tuNgay.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = denNgay == null ? null : denNgay.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        return phieuGiamGiaRepository.timKiemVaPhanTrang(keyword, trangThai, loai, start, end, pageable);
    }

    public void remove(Integer id) {
        phieuGiamGiaRepository.deleteById(id);
    }

    public PhieuGiamGia add(PhieuGiamGiaRequest request) {
        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        mapRequestToEntity(request, phieuGiamGia);
        return phieuGiamGiaRepository.save(phieuGiamGia);
    }

    public PhieuGiamGia update(Integer id, PhieuGiamGiaRequest request) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu giảm giá"));

        // Cho phép toggle trangThai 0 <-> 1 (ngừng/kích hoạt)

        mapRequestToEntity(request, phieuGiamGia);

        return phieuGiamGiaRepository.save(phieuGiamGia);
    }

    private void mapRequestToEntity(PhieuGiamGiaRequest request, PhieuGiamGia phieuGiamGia) {
        phieuGiamGia.setMa(request.getMa());
        phieuGiamGia.setTen(request.getTen());
        phieuGiamGia.setLoai(request.getLoai());
        phieuGiamGia.setLoaiPhieu(request.getLoaiPhieu() == null ? 1 : request.getLoaiPhieu());
        phieuGiamGia.setGiaTri(request.getGiaTri());
        phieuGiamGia.setGiaTriToiThieu(request.getGiaTriToiThieu());
        phieuGiamGia.setGiamToiDa(request.getGiamToiDa());
        phieuGiamGia.setNgayBatDau(toInstant(request.getNgayBatDau()));
        phieuGiamGia.setNgayKetThuc(toInstant(request.getNgayKetThuc()));
        phieuGiamGia.setSoLuong(request.getSoLuong());

        // Handle defaults for NotNull fields to prevent validation errors
        phieuGiamGia.setSoLuongDaDung(request.getSoLuongDaDung() == null ? 0 : request.getSoLuongDaDung());

        // Tự động tính toán trạng thái
        Instant now = Instant.now();
        Instant end = phieuGiamGia.getNgayKetThuc();

        if (end != null && !end.isAfter(now)) {
            phieuGiamGia.setTrangThai(2); // Hết hạn (bao gồm cả thời điểm hiện tại)
        } else {
            Integer requestedStatus = request.getTrangThai();
            if (requestedStatus != null && requestedStatus == 0) {
                // FE yêu cầu ngừng hoạt động và chưa hết date
                phieuGiamGia.setTrangThai(0);
            } else {
                Instant start = phieuGiamGia.getNgayBatDau();
                int soLuongDaDung = phieuGiamGia.getSoLuongDaDung();
                int soLuong = phieuGiamGia.getSoLuong();

                if (soLuongDaDung >= soLuong) {
                    phieuGiamGia.setTrangThai(3); // Hết số lượng
                } else if (start != null && start.isAfter(now)) {
                    phieuGiamGia.setTrangThai(4); // Sắp diễn ra
                } else {
                    phieuGiamGia.setTrangThai(1); // Hoạt động
                }
            }
        }

        if (phieuGiamGia.getNgayTao() == null) {
            phieuGiamGia.setNgayTao(request.getNgayTao() == null ? Instant.now() : toInstant(request.getNgayTao()));
        }

        phieuGiamGia.setNgayCapNhat(toInstant(request.getNgayCapNhat()));
    }

    private Instant toInstant(LocalDate value) {
        return value == null ? null : value.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
