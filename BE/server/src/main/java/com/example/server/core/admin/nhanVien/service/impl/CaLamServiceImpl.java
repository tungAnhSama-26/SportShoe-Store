package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.CaLamRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CaLamResponse;
import com.example.server.core.admin.nhanVien.service.CaLamService;
import com.example.server.entity.CaLam;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CaLamServiceImpl implements CaLamService {

    private final CaLamRepository caLamRepository;

    public CaLamServiceImpl(CaLamRepository caLamRepository) {
        this.caLamRepository = caLamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaLamResponse> layDanhSachCaLam() {
        return caLamRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CaLamResponse taoCaLam(CaLamRequest request) {
        if (request.id() != null && caLamRepository.existsById(request.id())) {
            throw new BusinessException("Mã ca làm việc đã tồn tại");
        }

        validateGioCa(request.gioBatDau(), request.gioKetThuc());
        validateTrungKhoangGio(request.id(), request.gioBatDau(), request.gioKetThuc(), request.trangThai());
        
        CaLam caLam = new CaLam();
        String id = request.id();
        if (id == null || id.trim().isEmpty()) {
            long count = caLamRepository.count() + 1;
            id = "CA" + String.format("%05d", count);
            while (caLamRepository.existsById(id)) {
                count++;
                id = "CA" + String.format("%05d", count);
            }
        }
        caLam.setId(id);
        caLam.setTen(request.ten());
        caLam.setGioBatDau(request.gioBatDau());
        caLam.setGioKetThuc(request.gioKetThuc());
        caLam.setTrangThai(request.trangThai());

        try {
            caLamRepository.save(caLam);
        } catch (Exception e) {
            throw new BusinessException("Không thể lưu ca làm việc. Giờ kết thúc phải lớn hơn giờ bắt đầu!");
        }
        return toResponse(caLam);
    }

    @Override
    @Transactional
    public CaLamResponse capNhatCaLam(String id, CaLamRequest request) {
        CaLam caLam = caLamRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc"));

        validateGioCa(request.gioBatDau(), request.gioKetThuc());
        validateTrungKhoangGio(id, request.gioBatDau(), request.gioKetThuc(), request.trangThai());

        caLam.setTen(request.ten());
        caLam.setGioBatDau(request.gioBatDau());
        caLam.setGioKetThuc(request.gioKetThuc());
        caLam.setTrangThai(request.trangThai());

        try {
            caLamRepository.save(caLam);
        } catch (Exception e) {
            throw new BusinessException("Không thể lưu ca làm việc. Giờ kết thúc phải lớn hơn giờ bắt đầu!");
        }
        return toResponse(caLam);
    }

    private void validateGioCa(String gioBatDau, String gioKetThuc) {
        if (gioBatDau == null || gioBatDau.isBlank()) {
            throw new BusinessException("Vui lòng chọn giờ bắt đầu!");
        }
        if (gioKetThuc == null || gioKetThuc.isBlank()) {
            throw new BusinessException("Vui lòng chọn giờ kết thúc!");
        }

        try {
            String s = gioBatDau.trim();
            String e = gioKetThuc.trim();
            java.time.LocalTime start = java.time.LocalTime.parse(s.length() == 5 ? s + ":00" : s);
            java.time.LocalTime end = java.time.LocalTime.parse(e.length() == 5 ? e + ":00" : e);

            if (start.equals(end)) {
                throw new BusinessException("Giờ kết thúc không được trùng với giờ bắt đầu!");
            }
            if (!start.isBefore(end)) {
                throw new BusinessException("Giờ kết thúc (" + e + ") phải lớn hơn giờ bắt đầu (" + s + "). Ví dụ: 08:00 - 12:00!");
            }
        } catch (java.time.format.DateTimeParseException ex) {
            throw new BusinessException("Định dạng giờ không hợp lệ!");
        }
    }

    private void validateTrungKhoangGio(String idHienTai, String gioBatDau, String gioKetThuc, Boolean trangThai) {
        if (!Boolean.TRUE.equals(trangThai)) {
            return;
        }
        if (gioBatDau == null || gioKetThuc == null || gioBatDau.isBlank() || gioKetThuc.isBlank()) {
            return;
        }

        try {
            String s = gioBatDau.trim();
            String e = gioKetThuc.trim();
            java.time.LocalTime startMoi = java.time.LocalTime.parse(s.length() == 5 ? s + ":00" : s);
            java.time.LocalTime endMoi = java.time.LocalTime.parse(e.length() == 5 ? e + ":00" : e);

            List<CaLam> cacCaHoatDong = caLamRepository.findAll().stream()
                    .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                    .filter(ca -> idHienTai == null || !ca.getId().equalsIgnoreCase(idHienTai.trim()))
                    .toList();

            for (CaLam ca : cacCaHoatDong) {
                if (ca.getGioBatDau() == null || ca.getGioKetThuc() == null) continue;
                String cs = ca.getGioBatDau().trim();
                String ce = ca.getGioKetThuc().trim();
                java.time.LocalTime startCu = java.time.LocalTime.parse(cs.length() == 5 ? cs + ":00" : cs);
                java.time.LocalTime endCu = java.time.LocalTime.parse(ce.length() == 5 ? ce + ":00" : ce);

                // Hai khoảng thời gian giao nhau: startMoi < endCu && startCu < endMoi
                if (startMoi.isBefore(endCu) && startCu.isBefore(endMoi)) {
                    throw new BusinessException(
                            "Khoảng thời gian (" + s + " - " + e + ") bị trùng với ca đang hoạt động: "
                                    + ca.getTen() + " (" + cs + " - " + ce + "). "
                                    + "Vui lòng tắt ca bị trùng hoặc chỉnh sửa lại thời gian của ca.");
                }
            }
        } catch (java.time.format.DateTimeParseException ignored) {
        }
    }

    @Override
    @Transactional
    public void xoaCaLam(String id) {
        if (!caLamRepository.existsById(id)) {
            throw new BusinessException("Không tìm thấy ca làm việc");
        }
        if (List.of("sang", "chieu", "toi").contains(id.trim().toLowerCase())) {
            throw new BusinessException("Không thể xóa ca làm việc mặc định");
        }
        caLamRepository.deleteById(id);
    }

    private CaLamResponse toResponse(CaLam caLam) {
        return new CaLamResponse(
                caLam.getId(),
                caLam.getTen(),
                caLam.getGioBatDau(),
                caLam.getGioKetThuc(),
                caLam.getTrangThai()
        );
    }
}
