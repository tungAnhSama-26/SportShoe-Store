package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.LichLamViecResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.CapNhatLichLamViecResponse;
import com.example.server.core.admin.nhanVien.service.LichLamViecService;
import com.example.server.core.realtime.lichlamviec.LichLamViecRealtimePublisher;
import com.example.server.entity.CaLam;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.exception.ResourceNotFoundException;
import com.example.server.repository.CaLamRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class LichLamViecServiceImpl implements LichLamViecService {

    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Bangkok");

    private final Clock clock;
    private final LichLamViecRepository lichLamViecRepository;
    private final NhanVienRepository nhanVienRepository;
    private final CaLamRepository caLamRepository;
    private final LichLamViecRealtimePublisher realtimePublisher;

    @Autowired
    public LichLamViecServiceImpl(
            LichLamViecRepository lichLamViecRepository,
            NhanVienRepository nhanVienRepository,
            CaLamRepository caLamRepository,
            LichLamViecRealtimePublisher realtimePublisher
    ) {
        this(
                Clock.system(MUI_GIO),
                lichLamViecRepository,
                nhanVienRepository,
                caLamRepository,
                realtimePublisher
        );
    }

    LichLamViecServiceImpl(
            Clock clock,
            LichLamViecRepository lichLamViecRepository,
            NhanVienRepository nhanVienRepository,
            CaLamRepository caLamRepository,
            LichLamViecRealtimePublisher realtimePublisher
    ) {
        this.clock = clock;
        this.lichLamViecRepository = lichLamViecRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.caLamRepository = caLamRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichLamViecResponse> layLichLamViecTheoTuan(LocalDate tuNgay, LocalDate denNgay) {
        return lichLamViecRepository.findByNgayBetween(tuNgay, denNgay).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public LichLamViecResponse phanCa(PhanCaRequest request) {
        NhanVien nhanVien = nhanVienRepository.findById(request.nhanVienId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));
        if (!Integer.valueOf(1).equals(nhanVien.getTrangThai())) {
            throw new BusinessException("Không thể phân ca cho nhân viên đã nghỉ việc");
        }

        String caLamId = request.caLamId().trim();
        CaLam caLam = caLamRepository.findByIdForUpdate(caLamId)
                .or(() -> caLamRepository.findByIdForUpdate(caLamId.toLowerCase()))
                .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                .orElseThrow(() -> new BusinessException("Ca làm việc không tồn tại hoặc đã ngừng hoạt động"));

        kiemTraCaChuaKetThuc(request.ngay(), caLam);

        if (lichLamViecRepository.existsByNhanVienIdAndNgayAndCaLamId(
                request.nhanVienId(), request.ngay(), caLam.getId())) {
            throw new BusinessException("Nhân viên đã được phân vào ca này trong ngày đã chọn");
        }

        kiemTraKhongChongGio(
                lichLamViecRepository.findByNhanVienIdAndNgay(request.nhanVienId(), request.ngay()),
                caLam
        );

        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nhanVien);
        lich.setNgay(request.ngay());
        lich.setCaLam(caLam);
        LichLamViecResponse response = toResponse(lichLamViecRepository.save(lich));
        realtimePublisher.phatSauCommit("PHAN_CA");
        return response;
    }

    @Override
    @Transactional
    public void xoaLich(UUID id) {
        LichLamViec lich = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch làm việc không tồn tại"));
        kiemTraCoTheChinhSua(lich.getNgay(), lich.getCaLam());
        lichLamViecRepository.delete(lich);
        realtimePublisher.phatSauCommit("XOA_LICH");
    }

    @Override
    @Transactional
    public CapNhatLichLamViecResponse xepCaTuDong(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new BusinessException("Khoảng thời gian không hợp lệ");
        }

        LocalDate tuNgayThucTe = ngayMaiHoacSau(tuNgay, denNgay);

        List<CaLam> cacCa = caLamRepository.findAll().stream()
                .filter(ca -> Boolean.TRUE.equals(ca.getTrangThai()))
                .sorted(Comparator.comparing(ca -> LocalTime.parse(ca.getGioBatDau())))
                .toList();
        if (cacCa.isEmpty()) {
            throw new BusinessException("Chưa có ca làm việc đang hoạt động");
        }

        long soLichDaXoa = lichLamViecRepository.countByNgayBetween(tuNgayThucTe, denNgay);
        lichLamViecRepository.deleteByNgayBetween(tuNgayThucTe, denNgay);
        lichLamViecRepository.flush();

        List<NhanVien> nhanViens = nhanVienRepository.findAll().stream()
                .filter(nv -> Integer.valueOf(1).equals(nv.getTrangThai()))
                .filter(nv -> Integer.valueOf(2).equals(nv.getVaiTro()))
                .sorted(Comparator.comparing(
                        NhanVien::getMa,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();

        int soLichDaTao = 0;
        int soCaChuaCoNhanVien = 0;
        int soNgayDaXuLy = 0;
        for (LocalDate ngay = tuNgayThucTe; !ngay.isAfter(denNgay); ngay = ngay.plusDays(1)) {
            int soCaCoThePhu = Math.min(cacCa.size(), nhanViens.size());
            int viTriNhanVienBatDau = nhanViens.isEmpty() ? 0 : soNgayDaXuLy % nhanViens.size();
            int viTriCaBatDau = soNgayDaXuLy % cacCa.size();
            for (int i = 0; i < soCaCoThePhu; i++) {
                NhanVien nhanVien = nhanViens.get((viTriNhanVienBatDau + i) % nhanViens.size());
                CaLam caLam = cacCa.get((viTriCaBatDau + i) % cacCa.size());
                saveLich(nhanVien, ngay, caLam);
                soLichDaTao++;
            }
            soCaChuaCoNhanVien += cacCa.size() - soCaCoThePhu;
            soNgayDaXuLy++;
        }

        realtimePublisher.phatSauCommit("XEP_CA_TU_DONG");
        return new CapNhatLichLamViecResponse(
                tuNgayThucTe, denNgay, soLichDaXoa, soLichDaTao, soCaChuaCoNhanVien);
    }

    @Override
    @Transactional
    public CapNhatLichLamViecResponse datLaiLich(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new BusinessException("Khoảng thời gian không hợp lệ");
        }
        LocalDate tuNgayThucTe = ngayMaiHoacSau(tuNgay, denNgay);
        long soLichDaXoa = lichLamViecRepository.countByNgayBetween(tuNgayThucTe, denNgay);
        lichLamViecRepository.deleteByNgayBetween(tuNgayThucTe, denNgay);
        realtimePublisher.phatSauCommit("DAT_LAI_LICH");
        return new CapNhatLichLamViecResponse(tuNgayThucTe, denNgay, soLichDaXoa, 0, 0);
    }

    private void kiemTraKhongChongGio(List<LichLamViec> lichTrongNgay, CaLam caMoi) {
        LocalTime batDauMoi = LocalTime.parse(caMoi.getGioBatDau());
        LocalTime ketThucMoi = LocalTime.parse(caMoi.getGioKetThuc());
        for (LichLamViec lich : lichTrongNgay) {
            CaLam caCu = lich.getCaLam();
            LocalTime batDauCu = LocalTime.parse(caCu.getGioBatDau());
            LocalTime ketThucCu = LocalTime.parse(caCu.getGioKetThuc());
            if (khoangGioGiaoNhau(batDauMoi, ketThucMoi, batDauCu, ketThucCu)) {
                throw new BusinessException("Ca " + caMoi.getTen() + " bị chồng giờ với " + caCu.getTen());
            }
        }
    }

    private boolean khoangGioGiaoNhau(LocalTime batDauA, LocalTime ketThucA, LocalTime batDauB, LocalTime ketThucB) {
        return tachKhoangGio(batDauA, ketThucA).stream()
                .anyMatch(khoangA -> tachKhoangGio(batDauB, ketThucB).stream()
                        .anyMatch(khoangB -> khoangA.batDau() < khoangB.ketThuc()
                                && khoangB.batDau() < khoangA.ketThuc()));
    }

    private List<KhoangPhut> tachKhoangGio(LocalTime batDau, LocalTime ketThuc) {
        int batDauPhut = batDau.getHour() * 60 + batDau.getMinute();
        int ketThucPhut = ketThuc.getHour() * 60 + ketThuc.getMinute();
        if (batDauPhut == ketThucPhut) {
            return List.of();
        }
        if (batDauPhut < ketThucPhut) {
            return List.of(new KhoangPhut(batDauPhut, ketThucPhut));
        }
        return List.of(new KhoangPhut(batDauPhut, 24 * 60), new KhoangPhut(0, ketThucPhut));
    }

    private record KhoangPhut(int batDau, int ketThuc) {
    }

    private void saveLich(NhanVien nhanVien, LocalDate ngay, CaLam caLam) {
        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nhanVien);
        lich.setNgay(ngay);
        lich.setCaLam(caLam);
        lichLamViecRepository.save(lich);
    }

    private LocalDate ngayMaiHoacSau(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate ngayMai = LocalDate.now(clock).plusDays(1);
        LocalDate tuNgayThucTe = tuNgay.isBefore(ngayMai) ? ngayMai : tuNgay;
        if (tuNgayThucTe.isAfter(denNgay)) {
            throw new BusinessException("Khoảng đã chọn không còn ngày tương lai để thực hiện");
        }
        return tuNgayThucTe;
    }

    private void kiemTraCoTheChinhSua(LocalDate ngay, CaLam caLam) {
        ZonedDateTime hienTai = ZonedDateTime.now(clock);
        ZonedDateTime thoiDiemKetThuc = thoiDiemKetThucCa(ngay, caLam);
        if (!hienTai.isBefore(thoiDiemKetThuc)) {
            throw new BusinessException("Ca làm việc đã kết thúc nên chỉ được xem lịch sử");
        }
    }

    private void kiemTraCaChuaKetThuc(LocalDate ngay, CaLam caLam) {
        ZonedDateTime hienTai = ZonedDateTime.now(clock);
        ZonedDateTime thoiDiemKetThuc = thoiDiemKetThucCa(ngay, caLam);
        if (!hienTai.isBefore(thoiDiemKetThuc)) {
            throw new BusinessException("Ca làm việc đã kết thúc nên không thể thêm nhân viên");
        }
    }

    private ZonedDateTime thoiDiemKetThucCa(LocalDate ngay, CaLam caLam) {
        LocalTime gioBatDau = LocalTime.parse(caLam.getGioBatDau());
        LocalTime gioKetThuc = LocalTime.parse(caLam.getGioKetThuc());
        LocalDate ngayKetThuc = ngay;
        if (!gioKetThuc.isAfter(gioBatDau)) {
            ngayKetThuc = ngayKetThuc.plusDays(1);
        }
        return ZonedDateTime.of(ngayKetThuc, gioKetThuc, MUI_GIO);
    }

    private LichLamViecResponse toResponse(LichLamViec lich) {
        return new LichLamViecResponse(
                lich.getId(),
                lich.getNhanVien().getId(),
                lich.getNgay(),
                lich.getCaLam().getId()
        );
    }
}
