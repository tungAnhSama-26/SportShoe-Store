package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.BanGiaoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaOptionsResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.service.TrangThaiGiaoCa;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.CaLam;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.HoaDon;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiaoCaServiceImplTest {

    @Mock private GiaoCaRepository giaoCaRepository;
    @Mock private NhanVienRepository nhanVienRepository;
    @Mock private ThongBaoService thongBaoService;
    @Mock private LichLamViecRepository lichLamViecRepository;
    @Mock private CaLamRepository caLamRepository;
    @Mock private HoaDonRepository hoaDonRepository;

    private GiaoCaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GiaoCaServiceImpl(
                giaoCaRepository,
                nhanVienRepository,
                thongBaoService,
                lichLamViecRepository,
                caLamRepository,
                hoaDonRepository
        );
    }

    @Test
    void adminKhongDuocMoCaLamViec() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin", 1);

        when(nhanVienRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.moCa(admin.getId(), new MoCaRequest(
                BigDecimal.ZERO, "", "toi", ""
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Quản trị viên không sử dụng ca làm việc.");

        verify(giaoCaRepository, never()).save(any());
    }

    @Test
    void nhanVienCoLichCaHienTaiDuocMoCaDocLap() {
        NhanVien nhanVien = nhanVien(UUID.randomUUID(), "NV001", "Nhân viên", 0);
        LocalTime hienTai = LocalTime.now(ZoneId.of("Asia/Bangkok"));
        LocalTime batDau = hienTai.isBefore(LocalTime.of(1, 0))
                ? LocalTime.of(0, 30)
                : hienTai.minusMinutes(1);
        CaLam caHienTai = caLam("ca-hien-tai", "Ca hiện tại", batDau.toString(), LocalTime.MAX.toString());
        LocalDate homNay = LocalDate.now(ZoneId.of("Asia/Bangkok"));
        LichLamViec lichLamViec = new LichLamViec();
        lichLamViec.setNhanVien(nhanVien);
        lichLamViec.setNgay(homNay);
        lichLamViec.setCaLam(caHienTai);

        when(nhanVienRepository.findById(nhanVien.getId())).thenReturn(Optional.of(nhanVien));
        when(giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(
                nhanVien.getId(), trangThaiChuaKetThuc())).thenReturn(false);
        when(lichLamViecRepository.findByNhanVienIdAndNgay(nhanVien.getId(), homNay))
                .thenReturn(List.of(lichLamViec));
        when(giaoCaRepository.save(any(GiaoCa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(giaoCaRepository.calculateTienMatTrongCa(any())).thenReturn(BigDecimal.ZERO);
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(any())).thenReturn(BigDecimal.ZERO);

        GiaoCaResponse response = service.moCa(nhanVien.getId(), new MoCaRequest(
                BigDecimal.valueOf(500000),
                "Bắt đầu ca",
                caHienTai.getId(),
                ""
        ));

        assertThat(response.caLamId()).isEqualTo(caHienTai.getId());
        assertThat(response.nhanVienTrongCaId()).isEqualTo(nhanVien.getId());
        assertThat(response.trangThai()).isEqualTo(TrangThaiGiaoCa.MO_CA.ma());
        assertThat(response.ghiChu()).isEqualTo("Bắt đầu ca");
        verify(giaoCaRepository).existsByNhanVienTrongCaIdAndTrangThaiIn(
                nhanVien.getId(), trangThaiChuaKetThuc());
    }

    @Test
    void lyDoMoCaChiBatBuocKhiMuonQuaBaMuoiPhut() {
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");

        assertThat(service.batBuocNhapLyDoMoCaMuon(caSang, LocalTime.of(8, 1))).isFalse();
        assertThat(service.batBuocNhapLyDoMoCaMuon(caSang, LocalTime.of(8, 30))).isFalse();
        assertThat(service.batBuocNhapLyDoMoCaMuon(
                caSang, LocalTime.of(8, 30, 0, 1))).isTrue();

        CaLam caQuaDem = caLam("dem", "Ca đêm", "23:45", "02:00");
        assertThat(service.batBuocNhapLyDoMoCaMuon(caQuaDem, LocalTime.of(23, 55))).isFalse();
        assertThat(service.batBuocNhapLyDoMoCaMuon(caQuaDem, LocalTime.of(0, 15))).isFalse();
        assertThat(service.batBuocNhapLyDoMoCaMuon(caQuaDem, LocalTime.of(0, 16))).isTrue();
    }

    @Test
    void nhanVienVanKhongDuocMoThemCaKhiCaCuaMinhChuaKetThuc() {
        NhanVien nhanVien = nhanVien(UUID.randomUUID(), "NV001", "Nhân viên", 0);
        when(nhanVienRepository.findById(nhanVien.getId())).thenReturn(Optional.of(nhanVien));
        when(giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(
                nhanVien.getId(), trangThaiChuaKetThuc())).thenReturn(true);

        assertThatThrownBy(() -> service.moCa(nhanVien.getId(), new MoCaRequest(
                BigDecimal.ZERO, "", "ca-hien-tai", ""
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bạn đang có ca làm việc chưa kết thúc.");

        verify(lichLamViecRepository, never()).findByNhanVienIdAndNgay(any(), any());
    }

    @Test
    void nhanVienKhongDuocMoCaKhiDaCoCaKhacDangHoatDongInCuahang() {
        NhanVien nhanVien1 = nhanVien(UUID.randomUUID(), "NV001", "Nhân viên 1", 0);
        NhanVien nhanVien2 = nhanVien(UUID.randomUUID(), "NV002", "Nhân viên 2", 0);
        GiaoCa caDangHoatDong = giaoCaDangMo(nhanVien1, caLam("sang", "Ca sáng", "08:00", "12:00"), LocalDate.now());

        when(nhanVienRepository.findById(nhanVien2.getId())).thenReturn(Optional.of(nhanVien2));
        when(giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(
                nhanVien2.getId(), trangThaiChuaKetThuc())).thenReturn(false);
        when(giaoCaRepository.findFirstByTrangThaiInOrderByThoiGianVaoDesc(trangThaiChuaKetThuc()))
                .thenReturn(Optional.of(caDangHoatDong));

        assertThatThrownBy(() -> service.moCa(nhanVien2.getId(), new MoCaRequest(
                BigDecimal.ZERO, "", "sang", ""
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đang hoạt động. Không thể mở thêm ca mới.");

        verify(lichLamViecRepository, never()).findByNhanVienIdAndNgay(any(), any());
    }

    @Test
    void nhanVienCaCuoiDuocBanGiaoChoNhanVienCaDauNgayKeTiep() {
        NhanVien nguoiGiao = nhanVien(UUID.randomUUID(), "NV001", "Người giao", 2);
        NhanVien nguoiNhan = nhanVien(UUID.randomUUID(), "NV002", "Người nhận", 2);
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");
        CaLam caToi = caLam("toi", "Ca tối", "18:00", "22:00");
        LocalDate homNay = LocalDate.of(2026, 8, 24);
        GiaoCa giaoCa = giaoCaDangMo(nguoiGiao, caToi, homNay);

        when(giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(
                nguoiGiao.getId(), TrangThaiGiaoCa.MO_CA.ma())).thenReturn(Optional.of(giaoCa));
        when(caLamRepository.findAll()).thenReturn(List.of(caToi, caSang));
        lenient().when(lichLamViecRepository.findByNgayAndCaLamId(homNay, caToi.getId())).thenReturn(List.of());
        lenient().when(lichLamViecRepository.findByNgayAndCaLamId(homNay.plusDays(1), caSang.getId()))
                .thenReturn(List.of(lichLamViec(nguoiNhan, caSang, homNay.plusDays(1))));

        GiaoCaOptionsResponse options = service.layTuyChonBanGiao(nguoiGiao.getId());

        assertThat(options.coTheKetCa()).isFalse();
        assertThat(options.caKeTiep()).isEqualTo(caSang.getId());
        assertThat(options.nhanVienNhanCa())
                .extracting(GiaoCaOptionsResponse.NhanVienNhanCaResponse::id)
                .containsExactly(nguoiNhan.getId());
    }

    @Test
    void banGiaoCaChiDoiChieuTienMatKhongCongChuyenKhoan() {
        NhanVien nguoiGiao = nhanVien(UUID.randomUUID(), "NV001", "Người giao", 2);
        NhanVien nguoiNhan = nhanVien(UUID.randomUUID(), "NV002", "Người nhận", 2);
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");
        CaLam caChieu = caLam("chieu", "Ca chiều", "13:00", "17:00");
        LocalDate homNay = LocalDate.of(2026, 8, 28);
        GiaoCa giaoCa = giaoCaDangMo(nguoiGiao, caSang, homNay);
        giaoCa.setTienDauCa(BigDecimal.ZERO);

        when(giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(
                nguoiGiao.getId(), TrangThaiGiaoCa.MO_CA.ma())).thenReturn(Optional.of(giaoCa));
        when(caLamRepository.findAll()).thenReturn(List.of(caSang, caChieu));
        lenient().when(lichLamViecRepository.findByNgayAndCaLamId(homNay, caSang.getId())).thenReturn(List.of());
        lenient().when(lichLamViecRepository.findByNgayAndCaLamId(homNay, caChieu.getId()))
                .thenReturn(List.of(lichLamViec(nguoiNhan, caChieu, homNay)));
        when(nhanVienRepository.findById(nguoiNhan.getId())).thenReturn(Optional.of(nguoiNhan));
        when(giaoCaRepository.calculateTienMatTrongCa(giaoCa.getId()))
                .thenReturn(new BigDecimal("13200000"));
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(giaoCa.getId()))
                .thenReturn(new BigDecimal("500000"));
        when(giaoCaRepository.save(any(GiaoCa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiaoCaResponse response = service.banGiaoCa(nguoiGiao.getId(), new BanGiaoCaRequest(
                new BigDecimal("13200000"),
                nguoiNhan.getId(),
                "",
                "Đã kiểm két"
        ));

        assertThat(response.tienMatTrongCa()).isEqualByComparingTo("13200000");
        assertThat(response.tienChuyenKhoanTrongCa()).isEqualByComparingTo("500000");
        assertThat(response.tienCuoiCaHeThong()).isEqualByComparingTo("13200000");
        assertThat(response.tienCuoiCaThucTe()).isEqualByComparingTo("13200000");
        assertThat(response.tienChenhLech()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.lyDoChenhLech()).isBlank();
    }

    @Test
    void xacNhanBanGiaoCaCuoiMoCaDauNgayKeTiepChoNguoiNhan() {
        NhanVien nguoiGiao = nhanVien(UUID.randomUUID(), "NV001", "Người giao", 2);
        NhanVien nguoiNhan = nhanVien(UUID.randomUUID(), "NV002", "Người nhận", 2);
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");
        CaLam caToi = caLam("toi", "Ca tối", "18:00", "22:00");
        LocalDate homNay = LocalDate.of(2026, 8, 24);
        GiaoCa giaoCa = giaoCaDangMo(nguoiGiao, caToi, homNay);
        giaoCa.setTrangThai(TrangThaiGiaoCa.CHO_BAN_GIAO.ma());
        giaoCa.setNhanVienNhan(nguoiNhan);
        giaoCa.setTienCuoiCaThucTe(BigDecimal.valueOf(500000));

        when(giaoCaRepository.findByIdForUpdate(giaoCa.getId())).thenReturn(Optional.of(giaoCa));
        when(nhanVienRepository.findById(nguoiNhan.getId())).thenReturn(Optional.of(nguoiNhan));
        when(caLamRepository.findAll()).thenReturn(List.of(caSang, caToi));
        when(lichLamViecRepository.existsByNhanVienIdAndNgayAndCaLamId(
                nguoiNhan.getId(), homNay.plusDays(1), caSang.getId())).thenReturn(true);
        when(giaoCaRepository.save(any(GiaoCa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(giaoCaRepository.calculateTienMatTrongCa(any())).thenReturn(BigDecimal.ZERO);
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(any())).thenReturn(BigDecimal.ZERO);

        service.xacNhanBanGiao(nguoiNhan.getId(), giaoCa.getId(), new XacNhanBanGiaoRequest(
                BigDecimal.valueOf(500000), "Đã kiểm đếm"
        ));

        ArgumentCaptor<GiaoCa> caMoiCaptor = ArgumentCaptor.forClass(GiaoCa.class);
        verify(giaoCaRepository).save(caMoiCaptor.capture());
        assertThat(caMoiCaptor.getValue().getNhanVienTrongCa()).isSameAs(nguoiNhan);
        assertThat(caMoiCaptor.getValue().getCaLam()).isSameAs(caSang);
        assertThat(caMoiCaptor.getValue().getTrangThai()).isEqualTo(TrangThaiGiaoCa.MO_CA.ma());
    }

    @Test
    void xacNhanBanGiaoChiDoiChieuTienMatKhongDoiChieuTienChuyenKhoan() {
        NhanVien nguoiGiao = nhanVien(UUID.randomUUID(), "NV001", "Người giao", 2);
        NhanVien nguoiNhan = nhanVien(UUID.randomUUID(), "NV002", "Người nhận", 2);
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");
        CaLam caChieu = caLam("chieu", "Ca chiều", "13:00", "17:00");
        LocalDate homNay = LocalDate.of(2026, 8, 28);
        GiaoCa giaoCa = giaoCaDangMo(nguoiGiao, caSang, homNay);
        giaoCa.setTienDauCa(BigDecimal.ZERO);
        giaoCa.setTrangThai(TrangThaiGiaoCa.CHO_BAN_GIAO.ma());
        giaoCa.setNhanVienNhan(nguoiNhan);
        giaoCa.setTienCuoiCaThucTe(new BigDecimal("13700000"));

        when(giaoCaRepository.findByIdForUpdate(giaoCa.getId())).thenReturn(Optional.of(giaoCa));
        when(nhanVienRepository.findById(nguoiNhan.getId())).thenReturn(Optional.of(nguoiNhan));
        when(caLamRepository.findAll()).thenReturn(List.of(caSang, caChieu));
        when(lichLamViecRepository.existsByNhanVienIdAndNgayAndCaLamId(
                nguoiNhan.getId(), homNay, caChieu.getId())).thenReturn(true);
        when(giaoCaRepository.calculateTienMatTrongCa(giaoCa.getId()))
                .thenReturn(new BigDecimal("13200000"));
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(giaoCa.getId()))
                .thenReturn(new BigDecimal("500000"));
        when(giaoCaRepository.save(any(GiaoCa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.xacNhanBanGiao(nguoiNhan.getId(), giaoCa.getId(), new XacNhanBanGiaoRequest(
                new BigDecimal("13200000"), "Đã kiểm tiền mặt"
        ));

        ArgumentCaptor<GiaoCa> caMoiCaptor = ArgumentCaptor.forClass(GiaoCa.class);
        verify(giaoCaRepository).save(caMoiCaptor.capture());
        assertThat(caMoiCaptor.getValue().getTienDauCa()).isEqualByComparingTo("13200000");
    }

    @Test
    void layCaHoatDongCuaAdminTraVeCaNhanVienDeHoTro() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin", 1);
        NhanVien nhanVien = nhanVien(UUID.randomUUID(), "NV001", "Nhân viên bán hàng", 2);
        GiaoCa caNhanVien = giaoCaDangMo(
                nhanVien,
                caLam("sang", "Ca sáng", "08:00", "12:00"),
                LocalDate.of(2026, 8, 26)
        );
        when(nhanVienRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(giaoCaRepository.findFirstByTrangThaiAndNhanVienTrongCa_VaiTroNotOrderByThoiGianVaoDesc(
                TrangThaiGiaoCa.MO_CA.ma(), 1)).thenReturn(Optional.of(caNhanVien));
        when(giaoCaRepository.calculateTienMatTrongCa(caNhanVien.getId())).thenReturn(BigDecimal.ZERO);
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(caNhanVien.getId())).thenReturn(BigDecimal.ZERO);

        GiaoCaResponse response = service.layCaHoatDong(admin.getId());

        assertThat(response).isNotNull();
        assertThat(response.nhanVienTrongCaId()).isEqualTo(nhanVien.getId());
        assertThat(response.nhanVienTrongCaVaiTro()).isNotEqualTo(1);
        verify(giaoCaRepository, never()).findFirstByNhanVienTrongCaIdAndTrangThaiInOrderByThoiGianVaoDesc(
                any(), anyList());
        verify(giaoCaRepository, never()).findFirstByTrangThaiInOrderByThoiGianVaoDesc(anyList());
    }

    @Test
    void adminKetCaTrucTiepKhongCanBanGiaoChoCaTiepTheo() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin", 1);
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");
        GiaoCa giaoCa = new GiaoCa();
        giaoCa.setMa("GC001");
        giaoCa.setNhanVienTrongCa(admin);
        giaoCa.setCaLam(caSang);
        giaoCa.setTienDauCa(BigDecimal.valueOf(500000));
        giaoCa.setTrangThai(TrangThaiGiaoCa.MO_CA.ma());
        giaoCa.setCaChuaKetThuc((byte) 1);

        when(giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(admin.getId(), TrangThaiGiaoCa.MO_CA.ma()))
                .thenReturn(Optional.of(giaoCa));
        when(nhanVienRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(giaoCaRepository.save(any(GiaoCa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiaoCaResponse response = service.ketCa(admin.getId(), new KetCaRequest(
                BigDecimal.valueOf(500000),
                "",
                "Admin kết ca trực tiếp"
        ));

        assertThat(response.trangThai()).isEqualTo(TrangThaiGiaoCa.DA_KET_THUC.ma());
        assertThat(giaoCa.getCaChuaKetThuc()).isNull();
        assertThat(giaoCa.getThoiGianRa()).isNotNull();
        verify(caLamRepository, never()).findAll();
    }

    @Test
    void tuyChonBanGiaoCuaAdminVanLaKetCaTrucTiep() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin", 1);
        CaLam caSang = caLam("sang", "Ca sáng", "08:00", "12:00");
        GiaoCa giaoCa = giaoCaDangMo(admin, caSang, LocalDate.of(2026, 8, 24));

        when(giaoCaRepository.findByNhanVienTrongCaIdAndTrangThai(
                admin.getId(), TrangThaiGiaoCa.MO_CA.ma())).thenReturn(Optional.of(giaoCa));

        GiaoCaOptionsResponse options = service.layTuyChonBanGiao(admin.getId());

        assertThat(options.coTheKetCa()).isTrue();
        assertThat(options.nhanVienNhanCa()).isEmpty();
        verify(caLamRepository, never()).findAll();
    }

    @Test
    void lichSuHoatDongCoHoaDonAdminBanKhongCanCa() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin bán hàng", 1);
        Instant ngayThanhToanLuuTrongDb = Instant.parse("2026-08-26T08:30:00Z");
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(101);
        hoaDon.setMa("HD00101");
        hoaDon.setNhanVien(admin);
        hoaDon.setNgayThanhToan(ngayThanhToanLuuTrongDb);
        hoaDon.setTongTienThanhToan(BigDecimal.valueOf(750000));

        Pageable shiftSourcePageable = PageRequest.of(0, 10);
        Pageable saleSourcePageable = PageRequest.of(0, 10);
        when(giaoCaRepository.searchHistory(
                null, null, null, null, null, false, false, false, shiftSourcePageable
        )).thenReturn(Page.empty());
        when(hoaDonRepository.searchAdminPosSalesWithoutShift(null, null, null, null, saleSourcePageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(hoaDon)));

        Page<GiaoCaResponse> result = service.layLichSuGiaoCa(
                null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        GiaoCaResponse activity = result.getContent().get(0);
        assertThat(activity.ma()).isEqualTo("HD00101");
        assertThat(activity.hoaDonId()).isEqualTo(101);
        assertThat(activity.caLamTen()).isEqualTo("Bán hàng tại quầy");
        assertThat(activity.nhanVienTrongCaId()).isEqualTo(admin.getId());
        assertThat(activity.thoiGianVao()).isEqualTo(Instant.parse("2026-08-26T01:30:00Z"));
        assertThat(activity.tienMatTrongCa()).isEqualByComparingTo("750000");
        assertThat(activity.trangThai()).isEqualTo("DA_BAN_HANG");
    }

    @Test
    void lichSuHoatDongVanCoNhanVienDangLamVaSapXepMoiNhatLenDau() {
        NhanVien nhanVien = nhanVien(UUID.randomUUID(), "NV001", "Nhân viên đang làm", 2);
        GiaoCa caDangLam = giaoCaDangMo(
                nhanVien,
                caLam("sang", "Ca sáng", "08:00", "12:00"),
                LocalDate.of(2026, 8, 26)
        );
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin bán hàng", 1);
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(102);
        hoaDon.setMa("HD00102");
        hoaDon.setNhanVien(admin);
        hoaDon.setNgayThanhToan(Instant.parse("2026-08-28T10:00:00Z"));
        hoaDon.setTongTienThanhToan(BigDecimal.valueOf(500000));

        Instant tuNgay = Instant.parse("2026-08-27T17:00:00Z");
        Instant denNgay = Instant.parse("2026-08-28T16:59:59Z");
        Instant tuNgayBanHang = Instant.parse("2026-08-28T00:00:00Z");
        Instant denNgayBanHang = Instant.parse("2026-08-28T23:59:59Z");
        Pageable sourcePageable = PageRequest.of(0, 10);
        when(giaoCaRepository.searchHistory(
                null, null, tuNgay, denNgay, null, false, false, false, sourcePageable
        )).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(caDangLam)));
        when(giaoCaRepository.calculateTienMatTrongCa(caDangLam.getId())).thenReturn(BigDecimal.ZERO);
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(caDangLam.getId())).thenReturn(BigDecimal.ZERO);
        when(hoaDonRepository.searchAdminPosSalesWithoutShift(
                null, tuNgayBanHang, denNgayBanHang, null, sourcePageable
        )).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(hoaDon)));

        Page<GiaoCaResponse> result = service.layLichSuGiaoCa(
                null, null, tuNgay, denNgay, null, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).trangThai()).isEqualTo("DA_BAN_HANG");
        assertThat(result.getContent().get(0).hoaDonId()).isEqualTo(102);
        assertThat(result.getContent().get(1).nhanVienTrongCaId()).isEqualTo(nhanVien.getId());
        assertThat(result.getContent().get(1).trangThai()).isEqualTo(TrangThaiGiaoCa.MO_CA.ma());
    }

    private List<String> trangThaiChuaKetThuc() {
        return List.of(TrangThaiGiaoCa.MO_CA.ma(), TrangThaiGiaoCa.CHO_BAN_GIAO.ma());
    }

    private NhanVien nhanVien(UUID id, String ma, String hoTen, Integer vaiTro) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setId(id);
        nhanVien.setMa(ma);
        nhanVien.setHoTen(hoTen);
        nhanVien.setVaiTro(vaiTro);
        nhanVien.setTrangThai(1);
        return nhanVien;
    }

    private CaLam caLam(String id, String ten, String gioBatDau, String gioKetThuc) {
        CaLam caLam = new CaLam();
        caLam.setId(id);
        caLam.setTen(ten);
        caLam.setGioBatDau(gioBatDau);
        caLam.setGioKetThuc(gioKetThuc);
        caLam.setTrangThai(true);
        return caLam;
    }

    private GiaoCa giaoCaDangMo(NhanVien nhanVien, CaLam caLam, LocalDate ngay) {
        GiaoCa giaoCa = new GiaoCa();
        giaoCa.setId(UUID.randomUUID());
        giaoCa.setMa("GC001");
        giaoCa.setNhanVienTrongCa(nhanVien);
        giaoCa.setCaLam(caLam);
        giaoCa.setThoiGianVao(ngay.atTime(20, 0).atZone(ZoneId.of("Asia/Bangkok")).toInstant());
        giaoCa.setTienDauCa(BigDecimal.valueOf(500000));
        giaoCa.setTrangThai(TrangThaiGiaoCa.MO_CA.ma());
        giaoCa.setCaChuaKetThuc((byte) 1);
        return giaoCa;
    }

    private LichLamViec lichLamViec(NhanVien nhanVien, CaLam caLam, LocalDate ngay) {
        LichLamViec lichLamViec = new LichLamViec();
        lichLamViec.setNhanVien(nhanVien);
        lichLamViec.setCaLam(caLam);
        lichLamViec.setNgay(ngay);
        return lichLamViec;
    }
}
