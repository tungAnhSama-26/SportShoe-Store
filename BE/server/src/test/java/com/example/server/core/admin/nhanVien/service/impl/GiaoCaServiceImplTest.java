package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.XacNhanBanGiaoRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaOptionsResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.service.TrangThaiGiaoCa;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.CaLam;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.LichLamViec;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.CaLamRepository;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.LichLamViecRepository;
import com.example.server.repository.NhanVienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    private GiaoCaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GiaoCaServiceImpl(
                giaoCaRepository,
                nhanVienRepository,
                thongBaoService,
                lichLamViecRepository,
                caLamRepository
        );
    }

    @Test
    void adminMoCaDocLapKhongBiCaNhanVienDangMoChanVaKhongCanLyDoNgoaiGio() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin", 1);
        CaLam caToi = caLam("toi", "Ca tối", "18:00", "22:00");

        when(nhanVienRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(giaoCaRepository.existsByNhanVienTrongCaIdAndTrangThaiIn(admin.getId(), trangThaiChuaKetThuc()))
                .thenReturn(false);
        when(caLamRepository.findById("toi")).thenReturn(Optional.of(caToi));
        when(giaoCaRepository.save(any(GiaoCa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(giaoCaRepository.calculateTienMatTrongCa(any())).thenReturn(BigDecimal.ZERO);
        when(giaoCaRepository.calculateTienChuyenKhoanTrongCa(any())).thenReturn(BigDecimal.ZERO);

        GiaoCaResponse response = service.moCa(admin.getId(), new MoCaRequest(
                BigDecimal.valueOf(500000),
                "",
                "toi",
                ""
        ));

        assertThat(response.caLamId()).isEqualTo("toi");
        assertThat(response.nhanVienTrongCaId()).isEqualTo(admin.getId());
        assertThat(response.trangThai()).isEqualTo(TrangThaiGiaoCa.MO_CA.ma());

        ArgumentCaptor<GiaoCa> captor = ArgumentCaptor.forClass(GiaoCa.class);
        verify(giaoCaRepository).save(captor.capture());
        assertThat(captor.getValue().getNhanVienTrongCa()).isSameAs(admin);
        assertThat(captor.getValue().getCaLam()).isSameAs(caToi);
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
                "Xác nhận vào ca hiện tại"
        ));

        assertThat(response.caLamId()).isEqualTo(caHienTai.getId());
        assertThat(response.nhanVienTrongCaId()).isEqualTo(nhanVien.getId());
        assertThat(response.trangThai()).isEqualTo(TrangThaiGiaoCa.MO_CA.ma());
        verify(giaoCaRepository).existsByNhanVienTrongCaIdAndTrangThaiIn(
                nhanVien.getId(), trangThaiChuaKetThuc());
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
        when(lichLamViecRepository.findByNgayAndCaLamId(homNay.plusDays(1), caSang.getId()))
                .thenReturn(List.of(lichLamViec(nguoiNhan, caSang, homNay.plusDays(1))));

        GiaoCaOptionsResponse options = service.layTuyChonBanGiao(nguoiGiao.getId());

        assertThat(options.coTheKetCa()).isFalse();
        assertThat(options.caKeTiep()).isEqualTo(caSang.getId());
        assertThat(options.nhanVienNhanCa())
                .extracting(GiaoCaOptionsResponse.NhanVienNhanCaResponse::id)
                .containsExactly(nguoiNhan.getId());
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
    void layCaHoatDongCuaAdminKhongLayCaDangMoCuaNhanVienKhac() {
        NhanVien admin = nhanVien(UUID.randomUUID(), "AD001", "Admin", 1);
        when(nhanVienRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(giaoCaRepository.findFirstByNhanVienTrongCaIdAndTrangThaiInOrderByThoiGianVaoDesc(
                admin.getId(), trangThaiChuaKetThuc())).thenReturn(Optional.empty());

        GiaoCaResponse response = service.layCaHoatDong(admin.getId());

        assertThat(response).isNull();
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
        giaoCa.setCaChuaKetThuc(1);

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
        giaoCa.setCaChuaKetThuc(1);
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
