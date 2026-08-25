package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.CaLamRequest;
import com.example.server.core.admin.nhanVien.dto.request.DoiTrangThaiCaLamRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CaLamResponse;
import com.example.server.core.admin.nhanVien.service.CaLamService;
import com.example.server.core.realtime.lichlamviec.LichLamViecRealtimePublisher;
import com.example.server.entity.CaLam;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
public class CaLamServiceImpl implements CaLamService {

    private final CaLamRepository caLamRepository;
    private final LichLamViecRealtimePublisher realtimePublisher;

    public CaLamServiceImpl(
            CaLamRepository caLamRepository,
            LichLamViecRealtimePublisher realtimePublisher
    ) {
        this.caLamRepository = caLamRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaLamResponse> layDanhSachCaLam() {
        return caLamRepository.findAll().stream()
                .sorted(Comparator.comparing(CaLam::getId, String.CASE_INSENSITIVE_ORDER))
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
            throw new BusinessException("Không thể lưu ca làm việc. Vui lòng kiểm tra lại giờ bắt đầu và giờ kết thúc!");
        }
        realtimePublisher.phatSauCommit("TAO_CA_LAM");
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
            throw new BusinessException("Không thể lưu ca làm việc. Vui lòng kiểm tra lại giờ bắt đầu và giờ kết thúc!");
        }
        realtimePublisher.phatSauCommit("CAP_NHAT_CA_LAM");
        return toResponse(caLam);
    }

    @Override
    @Transactional
    public CaLamResponse doiTrangThaiCaLam(String id, DoiTrangThaiCaLamRequest request) {
        CaLam caLam = caLamRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy ca làm việc"));

        if (Boolean.TRUE.equals(request.trangThai())) {
            validateTrungKhoangGio(id, caLam.getGioBatDau(), caLam.getGioKetThuc(), true);
        }

        caLam.setTrangThai(request.trangThai());
        CaLam saved = caLamRepository.save(caLam);
        realtimePublisher.phatSauCommit("DOI_TRANG_THAI_CA_LAM");
        return toResponse(saved);
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
            LocalTime startMoi = parseGio(s);
            LocalTime endMoi = parseGio(e);

            List<CaLam> cacCaHoatDong = caLamRepository.findAll().stream()
                    .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                    .filter(ca -> idHienTai == null || !ca.getId().equalsIgnoreCase(idHienTai.trim()))
                    .toList();

            for (CaLam ca : cacCaHoatDong) {
                if (ca.getGioBatDau() == null || ca.getGioKetThuc() == null) continue;
                String cs = ca.getGioBatDau().trim();
                String ce = ca.getGioKetThuc().trim();
                LocalTime startCu = parseGio(cs);
                LocalTime endCu = parseGio(ce);

                if (khoangGioGiaoNhau(startMoi, endMoi, startCu, endCu)) {
                    throw new BusinessException(
                            "Khoảng thời gian (" + s + " - " + e + ") bị trùng với ca đang hoạt động: "
                                    + ca.getTen() + " (" + cs + " - " + ce + "). "
                                    + "Vui lòng tắt ca bị trùng hoặc chỉnh sửa lại thời gian của ca.");
                }
            }
        } catch (DateTimeParseException ignored) {
        }
    }

    private LocalTime parseGio(String gio) {
        String value = gio.trim();
        return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
    }

    private boolean khoangGioGiaoNhau(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return tachKhoangGio(startA, endA).stream()
                .anyMatch(khoangA -> tachKhoangGio(startB, endB).stream()
                        .anyMatch(khoangB -> khoangA.batDau() < khoangB.ketThuc()
                                && khoangB.batDau() < khoangA.ketThuc()));
    }

    private List<KhoangPhut> tachKhoangGio(LocalTime start, LocalTime end) {
        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();
        if (startMinutes == endMinutes) {
            return List.of();
        }
        if (startMinutes < endMinutes) {
            return List.of(new KhoangPhut(startMinutes, endMinutes));
        }
        return List.of(new KhoangPhut(startMinutes, 24 * 60), new KhoangPhut(0, endMinutes));
    }

    private record KhoangPhut(int batDau, int ketThuc) {
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
        realtimePublisher.phatSauCommit("XOA_CA_LAM");
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
