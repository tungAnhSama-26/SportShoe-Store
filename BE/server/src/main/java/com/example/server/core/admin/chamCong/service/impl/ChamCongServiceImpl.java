package com.example.server.core.admin.chamCong.service.impl;

import com.example.server.core.admin.chamCong.dto.request.CheckInRequest;
import com.example.server.core.admin.chamCong.dto.request.CheckOutRequest;
import com.example.server.core.admin.chamCong.dto.response.ChamCongResponse;
import com.example.server.core.admin.chamCong.service.ChamCongService;
import com.example.server.entity.ChamCong;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.ChamCongRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.CaLamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChamCongServiceImpl implements ChamCongService {

    private final ChamCongRepository chamCongRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final NhanVienRepository nhanVienRepository;
    private final CaLamRepository caLamRepository;

    public ChamCongServiceImpl(ChamCongRepository chamCongRepository, LichLamViecRepository lichLamViecRepository, NhanVienRepository nhanVienRepository, CaLamRepository caLamRepository) {
        this.chamCongRepository = chamCongRepository;
        this.lichLamViecRepository = lichLamViecRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.caLamRepository = caLamRepository;
    }

    @Override
    @Transactional
    public ChamCongResponse checkIn(CheckInRequest request) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Instant now = Instant.now();
        LocalTime timeNow = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        NhanVien nhanVien = nhanVienRepository.findById(request.nhanVienId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        // Force check-out any forgotten shifts first
        checkAndForceCheckOut(nhanVien.getId());

        LichLamViec lichLamViec = lichLamViecRepository.findByNhanVienIdAndNgay(nhanVien.getId(), today)
                .orElseThrow(() -> new BusinessException("Bạn không có ca làm việc hôm nay"));

        String ca = lichLamViec.getCa();
        Optional<ChamCong> existing = chamCongRepository.findByNhanVienIdAndNgayAndCa(nhanVien.getId(), today, ca);
        if (existing.isPresent()) {
            throw new BusinessException("Bạn đã check-in cho ca này rồi");
        }

        LocalTime thoiGianChuan = getThoiGianBatDauCa(ca);
        LocalTime thoiGianKetThuc = getThoiGianKetThucCa(ca);
        LocalTime openingTime = thoiGianChuan.minusMinutes(30);

        long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(thoiGianChuan, thoiGianKetThuc);
        if (durationMinutes < 0) {
            durationMinutes += 24 * 60;
        }
        LocalTime midpoint = thoiGianChuan.plusMinutes(durationMinutes / 2);

        // Mốc 1: Vùng Sớm
        if (timeNow.isBefore(openingTime)) {
            String openingTimeStr = openingTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            throw new BusinessException("Chưa tới giờ điểm danh. Cổng Check-in ca " + getFriendlyCaName(ca) + " mở lúc " + openingTimeStr);
        }

        // Mốc 4: Vùng Đỏ
        if (timeNow.isAfter(midpoint)) {
            throw new BusinessException("Đã quá 50% thời gian của ca làm việc, ca làm đã bị khóa.");
        }

        // Mốc 2 & 3
        String trangThaiVao;
        Instant checkInInstant;
        if (!timeNow.isAfter(thoiGianChuan.plusMinutes(5))) {
            trangThaiVao = "DUNG_GIO";
            checkInInstant = toInstant(today, thoiGianChuan);
        } else {
            trangThaiVao = "DI_TRE";
            checkInInstant = now;
        }

        ChamCong chamCong = new ChamCong();
        chamCong.setNhanVien(nhanVien);
        chamCong.setLichLamViec(lichLamViec);
        chamCong.setNgay(today);
        chamCong.setCa(ca);
        chamCong.setThoiGianVao(checkInInstant);
        chamCong.setTrangThaiVao(trangThaiVao);

        try {
            chamCongRepository.saveAndFlush(chamCong);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new BusinessException("Bạn đã check-in cho ca này rồi");
        }

        return toResponse(chamCong);
    }

    @Override
    @Transactional
    public ChamCongResponse checkOut(CheckOutRequest request) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Instant now = Instant.now();
        LocalTime timeNow = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        NhanVien nhanVien = nhanVienRepository.findById(request.nhanVienId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        // Force check-out any forgotten shifts first
        checkAndForceCheckOut(nhanVien.getId());

        LichLamViec lichLamViec = lichLamViecRepository.findByNhanVienIdAndNgay(nhanVien.getId(), today)
                .orElseThrow(() -> new BusinessException("Bạn không có ca làm việc hôm nay"));

        String ca = lichLamViec.getCa();
        ChamCong chamCong = chamCongRepository.findByNhanVienIdAndNgayAndCa(nhanVien.getId(), today, ca)
                .orElseThrow(() -> new BusinessException("Bạn chưa check-in cho ca này"));

        if (chamCong.getThoiGianRa() != null) {
            throw new BusinessException("Bạn đã check-out cho ca này rồi");
        }

        String trangThaiRa = xacDinhTrangThaiRa(ca, timeNow);

        chamCong.setThoiGianRa(now);
        chamCong.setTrangThaiRa(trangThaiRa);

        chamCongRepository.save(chamCong);

        return toResponse(chamCong);
    }

    @Override
    @Transactional
    public List<ChamCongResponse> layDanhSachChamCong(LocalDate tuNgay, LocalDate denNgay) {
        // Force check-out any forgotten shifts in this range
        checkAndForceCheckOutForRange(tuNgay, denNgay);

        List<LichLamViec> lichLamViecs = lichLamViecRepository.findByNgayBetween(tuNgay, denNgay);
        List<ChamCong> chamCongs = chamCongRepository.findByNgayBetween(tuNgay, denNgay);

        Map<String, ChamCong> chamCongMap = chamCongs.stream()
                .collect(Collectors.toMap(
                        cc -> cc.getNhanVien().getId() + "_" + cc.getNgay() + "_" + cc.getCa(),
                        cc -> cc,
                        (existing, replacement) -> existing
                ));

        return lichLamViecs.stream()
                .map(l -> {
                    String key = l.getNhanVien().getId() + "_" + l.getNgay() + "_" + l.getCa();
                    ChamCong cc = chamCongMap.get(key);
                    return toResponse(l, cc);
                })
                .sorted((a, b) -> b.ngay().compareTo(a.ngay()))
                .toList();
    }

    private LocalTime getThoiGianBatDauCa(String ca) {
        return caLamRepository.findById(ca)
                .map(c -> {
                    try {
                        return LocalTime.parse(c.getGioBatDau().trim());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElseGet(() -> switch (ca) {
                    case "sang" -> LocalTime.of(8, 0);
                    case "chieu" -> LocalTime.of(13, 0);
                    case "toi" -> LocalTime.of(18, 0);
                    default -> LocalTime.of(8, 0);
                });
    }

    private LocalTime getThoiGianKetThucCa(String ca) {
        return caLamRepository.findById(ca)
                .map(c -> {
                    try {
                        return LocalTime.parse(c.getGioKetThuc().trim());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElseGet(() -> switch (ca) {
                    case "sang" -> LocalTime.of(12, 0);
                    case "chieu" -> LocalTime.of(17, 0);
                    case "toi" -> LocalTime.of(22, 0);
                    default -> LocalTime.of(12, 0);
                });
    }

    private String xacDinhTrangThaiVao(String ca, LocalTime timeNow) {
        LocalTime thoiGianChuan = getThoiGianBatDauCa(ca);
        // Check-in muon qua 5 phut thi coi la di tre
        if (timeNow.isAfter(thoiGianChuan.plusMinutes(5))) {
            return "DI_TRE";
        }
        return "DUNG_GIO";
    }

    private String xacDinhTrangThaiRa(String ca, LocalTime timeNow) {
        LocalTime thoiGianChuan = getThoiGianKetThucCa(ca);
        // Check-out som hon 5 phut thi coi la ve som
        if (timeNow.isBefore(thoiGianChuan.minusMinutes(5))) {
            return "VE_SOM";
        }
        return "DUNG_GIO";
    }

    private ChamCongResponse toResponse(ChamCong c) {
        return new ChamCongResponse(
                c.getId(),
                c.getNhanVien().getId(),
                c.getNhanVien().getHoTen(),
                c.getNgay(),
                c.getCa(),
                c.getThoiGianVao(),
                c.getThoiGianRa(),
                c.getTrangThaiVao(),
                c.getTrangThaiRa(),
                c.getGhiChu()
        );
    }

    private ChamCongResponse toResponse(LichLamViec l, ChamCong c) {
        if (c == null) {
            return new ChamCongResponse(
                    l.getId(), // use lichLamViec id as key
                    l.getNhanVien().getId(),
                    l.getNhanVien().getHoTen(),
                    l.getNgay(),
                    l.getCa(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        return toResponse(c);
    }

    private Instant toInstant(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
    }

    private String getFriendlyCaName(String ca) {
        if ("sang".equalsIgnoreCase(ca)) return "sáng";
        if ("chieu".equalsIgnoreCase(ca)) return "chiều";
        if ("toi".equalsIgnoreCase(ca)) return "tối";
        return ca;
    }

    private void checkAndForceCheckOut(UUID nhanVienId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime timeNow = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<ChamCong> unresolved = chamCongRepository.findByNhanVienIdAndThoiGianRaIsNull(nhanVienId);
        for (ChamCong cc : unresolved) {
            LocalTime thoiGianKetThuc = getThoiGianKetThucCa(cc.getCa());
            if (cc.getNgay().isBefore(today) || 
               (cc.getNgay().isEqual(today) && timeNow.isAfter(thoiGianKetThuc.plusHours(1)))) {
                cc.setThoiGianRa(toInstant(cc.getNgay(), thoiGianKetThuc));
                cc.setTrangThaiRa("QUEN_CHECKOUT");
                cc.setGhiChu("Hệ thống tự động check-out (thiếu check-out)");
                chamCongRepository.save(cc);
            }
        }
    }

    private void checkAndForceCheckOutForRange(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalTime timeNow = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<ChamCong> unresolved = chamCongRepository.findByNgayBetweenAndThoiGianRaIsNull(tuNgay, denNgay);
        for (ChamCong cc : unresolved) {
            LocalTime thoiGianKetThuc = getThoiGianKetThucCa(cc.getCa());
            if (cc.getNgay().isBefore(today) || 
               (cc.getNgay().isEqual(today) && timeNow.isAfter(thoiGianKetThuc.plusHours(1)))) {
                cc.setThoiGianRa(toInstant(cc.getNgay(), thoiGianKetThuc));
                cc.setTrangThaiRa("QUEN_CHECKOUT");
                cc.setGhiChu("Hệ thống tự động check-out (thiếu check-out)");
                chamCongRepository.save(cc);
            }
        }
    }
}
