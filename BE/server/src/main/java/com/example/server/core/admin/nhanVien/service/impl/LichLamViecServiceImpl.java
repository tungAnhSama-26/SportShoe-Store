package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.LichLamViecResponse;
import com.example.server.core.admin.nhanVien.service.LichLamViecService;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.ChamCongRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LichLamViecServiceImpl implements LichLamViecService {

    private static final int MAX_NHAN_VIEN_MOI_CA = 3;

    private final LichLamViecRepository lichLamViecRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ChamCongRepository chamCongRepository;

    public LichLamViecServiceImpl(LichLamViecRepository lichLamViecRepository, NhanVienRepository nhanVienRepository, ChamCongRepository chamCongRepository) {
        this.lichLamViecRepository = lichLamViecRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.chamCongRepository = chamCongRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichLamViecResponse> layLichLamViecTheoTuan(LocalDate tuNgay, LocalDate denNgay) {
        List<com.example.server.entity.ChamCong> chamCongs = chamCongRepository.findByNgayBetween(tuNgay, denNgay);
        Map<String, com.example.server.entity.ChamCong> chamCongMap = chamCongs.stream()
                .collect(Collectors.toMap(
                        cc -> cc.getNhanVien().getId() + "_" + cc.getNgay() + "_" + cc.getCa(),
                        cc -> cc,
                        (existing, replacement) -> existing
                ));

        return lichLamViecRepository.findByNgayBetween(tuNgay, denNgay).stream()
                .map(l -> {
                    String key = l.getNhanVien().getId() + "_" + l.getNgay() + "_" + l.getCa();
                    com.example.server.entity.ChamCong cc = chamCongMap.get(key);
                    String trangThai = null;
                    if (cc != null) {
                        if (cc.getThoiGianVao() != null && cc.getThoiGianRa() == null) {
                            trangThai = "check in";
                        } else if (cc.getThoiGianRa() != null) {
                            trangThai = "check out";
                        }
                    }
                    return new LichLamViecResponse(l.getId(), l.getNhanVien().getId(), l.getNgay(), l.getCa(), trangThai);
                })
                .toList();
    }

    @Override
    @Transactional
    public LichLamViecResponse phanCa(PhanCaRequest request) {
        NhanVien nhanVien = nhanVienRepository.findById(request.nhanVienId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        if (nhanVien.getTrangThai() != 1) {
            throw new BusinessException("Không thể phân ca cho nhân viên đã nghỉ việc");
        }

        Optional<LichLamViec> optLich = lichLamViecRepository.findByNhanVienIdAndNgay(request.nhanVienId(), request.ngay());

        if (request.ca() == null || request.ca().trim().isEmpty()) {
            // Delete schedule
            if (optLich.isPresent()) {
                LichLamViec lichLamViec = optLich.get();
                if (chamCongRepository.existsByLichLamViecId(lichLamViec.getId())) {
                    throw new BusinessException("Không thể xóa ca làm việc vì nhân viên đã điểm danh.");
                }
                lichLamViecRepository.delete(lichLamViec);
            }
            return new LichLamViecResponse(null, request.nhanVienId(), request.ngay(), null, null);
        }

        String ca = request.ca().trim().toLowerCase();
        if (!List.of("sang", "chieu", "toi").contains(ca)) {
            throw new BusinessException("Ca làm việc không hợp lệ. Phải là 'sang', 'chieu' hoặc 'toi'");
        }

        // Validate max people per shift rule:
        // If the employee is not already scheduled for this shift on this day, we must check the capacity.
        boolean isAlreadyAssignedToThisShift = lichLamViecRepository.existsByNhanVienIdAndNgayAndCa(request.nhanVienId(), request.ngay(), ca);
        if (!isAlreadyAssignedToThisShift) {
            long count = lichLamViecRepository.countByNgayAndCa(request.ngay(), ca);
            if (count >= MAX_NHAN_VIEN_MOI_CA) {
                String tenCa = mapCaName(ca);
                String formatNgay = request.ngay().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                throw new BusinessException("Ca " + tenCa + " ngay " + formatNgay + " da dat so luong toi da " + MAX_NHAN_VIEN_MOI_CA + " nguoi!");
            }
        }

        LichLamViec lich = optLich.orElse(new LichLamViec());
        lich.setNhanVien(nhanVien);
        lich.setNgay(request.ngay());
        lich.setCa(ca);

        LichLamViec saved = lichLamViecRepository.save(lich);
        return new LichLamViecResponse(saved.getId(), saved.getNhanVien().getId(), saved.getNgay(), saved.getCa(), null);
    }

    @Override
    @Transactional
    public void xepCaTuDong(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new BusinessException("Khoang thoi gian khong hop le");
        }

        if (chamCongRepository.existsByNgayBetween(tuNgay, denNgay)) {
            throw new BusinessException("Khong the xep ca tu dong vi da co du lieu diem danh trong khoang thoi gian nay.");
        }

        // Delete existing shifts in the date range
        lichLamViecRepository.deleteByNgayBetween(tuNgay, denNgay);
        lichLamViecRepository.flush();

        // Fetch active employees
        List<NhanVien> activeEmployees = nhanVienRepository.findAll().stream()
                .filter(nv -> nv.getTrangThai() == 1)
                .toList();

        if (activeEmployees.isEmpty()) {
            return;
        }

        String[] caTypes = {"sang", "chieu", "toi", null}; // null is off day

        for (LocalDate date = tuNgay; !date.isAfter(denNgay); date = date.plusDays(1)) {
            // Use date's hash or day of year to shift assignments so different days rotate roles
            int dayOffset = date.getDayOfYear();

            int countSang = 0;
            int countChieu = 0;
            int countToi = 0;

            for (int i = 0; i < activeEmployees.size(); i++) {
                NhanVien nv = activeEmployees.get(i);
                int shiftIndex = (i + dayOffset) % caTypes.length;
                String ca = caTypes[shiftIndex];

                if (ca != null) {
                    if ("sang".equals(ca) && countSang < MAX_NHAN_VIEN_MOI_CA) {
                        saveLich(nv, date, ca);
                        countSang++;
                    } else if ("chieu".equals(ca) && countChieu < MAX_NHAN_VIEN_MOI_CA) {
                        saveLich(nv, date, ca);
                        countChieu++;
                    } else if ("toi".equals(ca) && countToi < MAX_NHAN_VIEN_MOI_CA) {
                        saveLich(nv, date, ca);
                        countToi++;
                    }
                }
            }
        }
    }

    private void saveLich(NhanVien nv, LocalDate date, String ca) {
        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nv);
        lich.setNgay(date);
        lich.setCa(ca);
        lichLamViecRepository.save(lich);
    }

    private String mapCaName(String ca) {
        return switch (ca) {
            case "sang" -> "Sang";
            case "chieu" -> "Chieu";
            case "toi" -> "Toi";
            default -> ca;
        };
    }
}
