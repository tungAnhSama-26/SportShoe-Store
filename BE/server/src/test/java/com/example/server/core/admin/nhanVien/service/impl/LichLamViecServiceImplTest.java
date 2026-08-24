package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.CapNhatLichLamViecResponse;
import com.example.server.core.realtime.lichlamviec.LichLamViecRealtimePublisher;
import com.example.server.entity.CaLam;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LichLamViecServiceImplTest {

    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Bangkok");

    @Mock
    private LichLamViecRepository lichLamViecRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private CaLamRepository caLamRepository;
    @Mock
    private LichLamViecRealtimePublisher realtimePublisher;

    private LichLamViecServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LichLamViecServiceImpl(
                lichLamViecRepository,
                nhanVienRepository,
                caLamRepository,
                realtimePublisher
        );
    }

    @Test
    void xepTuDongMoiCaMotNguoiVaMoiNguoiToiDaMotCaMoiNgay() {
        LocalDate today = LocalDate.now(MUI_GIO);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate end = today.plusDays(3);
        when(caLamRepository.findAll()).thenReturn(List.of(
                ca("sang", "08:00", "12:00"),
                ca("chieu", "13:00", "17:00")
        ));
        when(nhanVienRepository.findAll()).thenReturn(List.of(
                nhanVien(), nhanVien(), nhanVien()
        ));
        when(lichLamViecRepository.countByNgayBetween(tomorrow, end)).thenReturn(5L);

        CapNhatLichLamViecResponse result = service.xepCaTuDong(today.minusDays(4), end);

        ArgumentCaptor<LichLamViec> captor = ArgumentCaptor.forClass(LichLamViec.class);
        verify(lichLamViecRepository, times(6)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allSatisfy(lich -> assertThat(lich.getNgay()).isAfter(today));
        Map<LocalDate, List<LichLamViec>> theoNgay = captor.getAllValues().stream()
                .collect(Collectors.groupingBy(LichLamViec::getNgay));
        assertThat(theoNgay).hasSize(3);
        theoNgay.values().forEach(lichTrongNgay -> {
            assertThat(lichTrongNgay).hasSize(2);
            assertThat(lichTrongNgay).extracting(lich -> lich.getCaLam().getId()).doesNotHaveDuplicates();
            assertThat(lichTrongNgay).extracting(lich -> lich.getNhanVien().getId()).doesNotHaveDuplicates();
        });
        assertThat(result.tuNgay()).isEqualTo(tomorrow);
        assertThat(result.soLichDaXoa()).isEqualTo(5);
        assertThat(result.soLichDaTao()).isEqualTo(6);
        assertThat(result.soCaChuaCoNhanVien()).isZero();
        verify(realtimePublisher).phatSauCommit("XEP_CA_TU_DONG");
    }

    @Test
    void xepTuDongDeCaTrongKhiKhongDuNhanVien() {
        LocalDate tomorrow = LocalDate.now(MUI_GIO).plusDays(1);
        when(caLamRepository.findAll()).thenReturn(List.of(
                ca("sang", "08:00", "11:00"),
                ca("chieu", "12:00", "15:00"),
                ca("toi", "16:00", "20:00")
        ));
        when(nhanVienRepository.findAll()).thenReturn(List.of(nhanVien(), nhanVien()));

        CapNhatLichLamViecResponse result = service.xepCaTuDong(tomorrow, tomorrow);

        assertThat(result.soLichDaTao()).isEqualTo(2);
        assertThat(result.soCaChuaCoNhanVien()).isEqualTo(1);
    }

    @Test
    void xepTuDongLapLaiChoKetQuaOnDinhKhongTaoTrungTrongMotLuot() {
        LocalDate tomorrow = LocalDate.now(MUI_GIO).plusDays(1);
        when(caLamRepository.findAll()).thenReturn(List.of(
                ca("sang", "08:00", "11:00"),
                ca("chieu", "12:00", "16:00")
        ));
        when(nhanVienRepository.findAll()).thenReturn(List.of(nhanVien(), nhanVien(), nhanVien()));

        for (int repeat = 0; repeat < 6; repeat++) {
            CapNhatLichLamViecResponse result = service.xepCaTuDong(tomorrow, tomorrow);
            assertThat(result.soLichDaTao()).isEqualTo(2);
            assertThat(result.soCaChuaCoNhanVien()).isZero();
        }

        verify(lichLamViecRepository, times(6)).deleteByNgayBetween(tomorrow, tomorrow);
        verify(lichLamViecRepository, times(12)).save(any(LichLamViec.class));
        verify(realtimePublisher, times(6)).phatSauCommit("XEP_CA_TU_DONG");
    }

    @Test
    void datLaiChiXoaTuNgayMai() {
        LocalDate today = LocalDate.now(MUI_GIO);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate end = today.plusDays(5);
        when(lichLamViecRepository.countByNgayBetween(tomorrow, end)).thenReturn(9L);

        CapNhatLichLamViecResponse result = service.datLaiLich(today.minusDays(10), end);

        verify(lichLamViecRepository).deleteByNgayBetween(tomorrow, end);
        verify(lichLamViecRepository, never()).deleteByNgayBetween(today.minusDays(10), end);
        assertThat(result.soLichDaXoa()).isEqualTo(9);
        assertThat(result.tuNgay()).isEqualTo(tomorrow);
    }

    @Test
    void phanThuCongKhongConGioiHanBaNguoiMoiCa() {
        ZonedDateTime now = ZonedDateTime.of(2026, 8, 20, 10, 0, 0, 0, MUI_GIO);
        service = serviceVoiDongHo(now.toInstant());
        CaLam shift = ca("sang", "08:00", "12:00");
        when(caLamRepository.findByIdForUpdate("sang")).thenReturn(Optional.of(shift));
        when(nhanVienRepository.findById(any(UUID.class)))
                .thenAnswer(invocation -> Optional.of(nhanVien(invocation.getArgument(0))));
        when(lichLamViecRepository.save(any(LichLamViec.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        for (int index = 0; index < 4; index++) {
            service.phanCa(new PhanCaRequest(
                    UUID.randomUUID(),
                    now.toLocalDate(),
                    "sang"
            ));
        }

        verify(lichLamViecRepository, times(4)).save(any(LichLamViec.class));
    }

    @Test
    void phanCaQuaKhuBiTuChoi() {
        UUID employeeId = UUID.randomUUID();
        NhanVien employee = nhanVien(employeeId);
        CaLam shift = ca("sang", "08:00", "12:00");
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(caLamRepository.findByIdForUpdate("sang")).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> service.phanCa(new PhanCaRequest(
                employeeId,
                LocalDate.now(MUI_GIO).minusDays(1),
                "sang"
        ))).isInstanceOf(BusinessException.class);

        verify(lichLamViecRepository, never()).save(any());
    }

    @Test
    void phanCaChoCaDangDienRaTrongHomNay() {
        ZonedDateTime now = ZonedDateTime.of(2026, 8, 20, 10, 0, 0, 0, MUI_GIO);
        service = serviceVoiDongHo(now.toInstant());
        UUID employeeId = UUID.randomUUID();
        NhanVien employee = nhanVien(employeeId);
        CaLam shift = ca("sang", "08:00", "12:00");
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(caLamRepository.findByIdForUpdate("sang")).thenReturn(Optional.of(shift));
        when(lichLamViecRepository.save(any(LichLamViec.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.phanCa(new PhanCaRequest(employeeId, now.toLocalDate(), "sang"));

        verify(lichLamViecRepository).save(any(LichLamViec.class));
        verify(realtimePublisher).phatSauCommit("PHAN_CA");
    }

    @Test
    void phanCaChoCaTuongLai() {
        ZonedDateTime now = ZonedDateTime.of(2026, 8, 20, 10, 0, 0, 0, MUI_GIO);
        service = serviceVoiDongHo(now.toInstant());
        UUID employeeId = UUID.randomUUID();
        NhanVien employee = nhanVien(employeeId);
        CaLam shift = ca("sang", "08:00", "12:00");
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(caLamRepository.findByIdForUpdate("sang")).thenReturn(Optional.of(shift));
        when(lichLamViecRepository.save(any(LichLamViec.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.phanCa(new PhanCaRequest(employeeId, now.toLocalDate().plusDays(1), "sang"));

        verify(lichLamViecRepository).save(any(LichLamViec.class));
        verify(realtimePublisher).phatSauCommit("PHAN_CA");
    }

    @Test
    void phanCaChoCaHomNayDaKetThucBiTuChoi() {
        ZonedDateTime now = ZonedDateTime.of(2026, 8, 20, 13, 0, 0, 0, MUI_GIO);
        service = serviceVoiDongHo(now.toInstant());
        UUID employeeId = UUID.randomUUID();
        NhanVien employee = nhanVien(employeeId);
        CaLam shift = ca("sang", "08:00", "12:00");
        when(nhanVienRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(caLamRepository.findByIdForUpdate("sang")).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> service.phanCa(new PhanCaRequest(
                employeeId,
                now.toLocalDate(),
                "sang"
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã kết thúc");

        verify(lichLamViecRepository, never()).save(any());
    }

    private LichLamViecServiceImpl serviceVoiDongHo(Instant instant) {
        return new LichLamViecServiceImpl(
                Clock.fixed(instant, MUI_GIO),
                lichLamViecRepository,
                nhanVienRepository,
                caLamRepository,
                realtimePublisher
        );
    }

    private CaLam ca(String id, String batDau, String ketThuc) {
        CaLam ca = new CaLam();
        ca.setId(id);
        ca.setTen(id);
        ca.setGioBatDau(batDau);
        ca.setGioKetThuc(ketThuc);
        ca.setTrangThai(true);
        return ca;
    }

    private NhanVien nhanVien() {
        return nhanVien(UUID.randomUUID());
    }

    private NhanVien nhanVien(UUID id) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(id);
        nhanVien.setTrangThai(1);
        nhanVien.setVaiTro(2);
        return nhanVien;
    }
}
