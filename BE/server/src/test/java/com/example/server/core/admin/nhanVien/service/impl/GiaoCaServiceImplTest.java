package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.MoCaRequest;
import com.example.server.core.admin.nhanVien.dto.request.KetCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.GiaoCaResponse;
import com.example.server.core.admin.nhanVien.service.TrangThaiGiaoCa;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.CaLam;
import com.example.server.entity.GiaoCa;
import com.example.server.entity.NhanVien;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        verify(giaoCaRepository, never()).existsByCaChuaKetThuc(1);
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
}
