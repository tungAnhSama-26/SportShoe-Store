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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class ChamCongServiceImpl implements ChamCongService {

    private final ChamCongRepository chamCongRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final NhanVienRepository nhanVienRepository;

    public ChamCongServiceImpl(ChamCongRepository chamCongRepository, LichLamViecRepository lichLamViecRepository, NhanVienRepository nhanVienRepository) {
        this.chamCongRepository = chamCongRepository;
        this.lichLamViecRepository = lichLamViecRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    @Override
    @Transactional
    public ChamCongResponse checkIn(CheckInRequest request) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Instant now = Instant.now();
        LocalTime timeNow = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        NhanVien nhanVien = nhanVienRepository.findById(request.nhanVienId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        LichLamViec lichLamViec = lichLamViecRepository.findByNhanVienIdAndNgay(nhanVien.getId(), today)
                .orElseThrow(() -> new BusinessException("Bạn không có ca làm việc hôm nay"));

        String ca = lichLamViec.getCa();
        Optional<ChamCong> existing = chamCongRepository.findByNhanVienIdAndNgayAndCa(nhanVien.getId(), today, ca);
        if (existing.isPresent()) {
            throw new BusinessException("Bạn đã check-in cho ca này rồi");
        }

        String trangThaiVao = xacDinhTrangThaiVao(ca, timeNow);

        ChamCong chamCong = new ChamCong();
        chamCong.setNhanVien(nhanVien);
        chamCong.setLichLamViec(lichLamViec);
        chamCong.setNgay(today);
        chamCong.setCa(ca);
        chamCong.setThoiGianVao(now);
        chamCong.setTrangThaiVao(trangThaiVao);

        chamCongRepository.save(chamCong);

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
    @Transactional(readOnly = true)
    public List<ChamCongResponse> layDanhSachChamCong(LocalDate tuNgay, LocalDate denNgay) {
        return chamCongRepository.findByNgayBetween(tuNgay, denNgay)
                .stream().map(this::toResponse).toList();
    }

    private String xacDinhTrangThaiVao(String ca, LocalTime timeNow) {
        LocalTime thoiGianChuan = switch (ca) {
            case "sang" -> LocalTime.of(8, 0);
            case "chieu" -> LocalTime.of(13, 0);
            case "toi" -> LocalTime.of(18, 0);
            default -> LocalTime.of(8, 0);
        };
        // Check-in muộn quá 5 phút thì coi là đi trễ
        if (timeNow.isAfter(thoiGianChuan.plusMinutes(5))) {
            return "DI_TRE";
        }
        return "DUNG_GIO";
    }

    private String xacDinhTrangThaiRa(String ca, LocalTime timeNow) {
        LocalTime thoiGianChuan = switch (ca) {
            case "sang" -> LocalTime.of(12, 0);
            case "chieu" -> LocalTime.of(17, 0);
            case "toi" -> LocalTime.of(22, 0);
            default -> LocalTime.of(12, 0);
        };
        // Check-out sớm hơn 5 phút thì coi là về sớm
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
}
