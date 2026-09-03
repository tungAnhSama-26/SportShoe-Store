package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.request.ThanhToanTaiQuayRequest;
import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.entity.HoaDon;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.GiaoCaRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThucThiThanhToanTaiQuayServiceTest {

    @Mock private HoaDonRepository hoaDonRepository;
    @Mock private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Mock private ThanhToanRepository thanhToanRepository;
    @Mock private VanChuyenRepository vanChuyenRepository;
    @Mock private EmailService emailService;
    @Mock private ThanhToanTaiQuayService paymentUseCase;
    @Mock private TrangThaiHoaDonTaiQuayService invoiceStateUseCase;
    @Mock private HoaDonTaiQuayService invoiceUseCase;
    @Mock private PhieuGiamGiaTaiQuayService voucherUseCase;
    @Mock private GiaoCaRepository giaoCaRepository;
    @Mock private TonKhoTaiQuayService inventoryUseCase;
    @Mock private GiayChiTietRepository giayChiTietRepository;
    @Mock private SanPhamTaiQuayService productUseCase;
    @Mock private com.example.server.repository.HinhAnhGiayRepository hinhAnhGiayRepository;
    @Mock private com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher sanPhamRealtimePublisher;

    private ThucThiThanhToanTaiQuayService service;

    @BeforeEach
    void setUp() {
        service = new ThucThiThanhToanTaiQuayService(
                hoaDonRepository,
                hoaDonChiTietRepository,
                thanhToanRepository,
                vanChuyenRepository,
                emailService,
                paymentUseCase,
                invoiceStateUseCase,
                invoiceUseCase,
                voucherUseCase,
                giaoCaRepository,
                inventoryUseCase,
                giayChiTietRepository,
                productUseCase,
                hinhAnhGiayRepository,
                sanPhamRealtimePublisher
        );
    }

    @Test
    void adminThanhToanTaiQuayKhongGanVaoCaLamViec() {
        NhanVien admin = new NhanVien();
        admin.setId(UUID.randomUUID());
        admin.setMa("AD001");
        admin.setHoTen("Admin bán hàng");
        admin.setVaiTro(1);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(101);
        hoaDon.setMa("HD101");
        hoaDon.setNhanVien(admin);
        hoaDon.setTongTienHang(BigDecimal.valueOf(250000));
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(BigDecimal.valueOf(250000));

        ThanhToanTaiQuayRequest request = new ThanhToanTaiQuayRequest(
                null,
                null,
                "Khách vãng lai",
                "",
                null,
                null,
                1,
                BigDecimal.valueOf(250000),
                null,
                null,
                "Admin bán hàng tại quầy",
                List.of(new TaoHoaDonChoItemRequest(1, 1, BigDecimal.valueOf(250000)))
        );

        when(invoiceUseCase.resolveNhanVienDangDangNhap()).thenReturn(admin);
        when(invoiceStateUseCase.xacDinhTrangThaiSauThanhToan(null)).thenReturn(3);
        when(invoiceUseCase.taoHoaDon(
                eq(null),
                eq("Khách vãng lai"),
                eq(""),
                eq(null),
                eq(null),
                anyList(),
                eq(3),
                eq("Admin bán hàng tại quầy")
        )).thenReturn(hoaDon);
        when(paymentUseCase.xacDinhTienKhachDua(eq(1), any(), eq(BigDecimal.valueOf(250000)), eq(null), eq(null)))
                .thenReturn(BigDecimal.valueOf(250000));
        when(paymentUseCase.tinhTienThua(eq(1), eq(BigDecimal.valueOf(250000)), eq(BigDecimal.valueOf(250000)), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);
        when(paymentUseCase.mapHinhThucThanhToan(1)).thenReturn(1);
        when(vanChuyenRepository.findByHoaDonId(101)).thenReturn(Optional.empty());
        when(invoiceUseCase.resolveTenKhachHangHoaDon(hoaDon)).thenReturn("Khách vãng lai");
        when(invoiceUseCase.resolveSoDienThoaiKhachHangHoaDon(hoaDon)).thenReturn("");
        when(invoiceUseCase.mapHoaDonChiTiet(eq(hoaDon), anyList(), any()))
                .thenReturn(new HoaDonChoChiTietResponse(
                        101, "HD101", null, "Khách vãng lai", "", null,
                        BigDecimal.valueOf(250000), BigDecimal.ZERO, BigDecimal.valueOf(250000),
                        null, null, List.of()
                ));

        service.thanhToanTaiQuay(request);

        ArgumentCaptor<HoaDon> hoaDonCaptor = ArgumentCaptor.forClass(HoaDon.class);
        verify(hoaDonRepository).save(hoaDonCaptor.capture());
        assertThat(hoaDonCaptor.getValue().getGiaoCa()).isNull();
        assertThat(hoaDonCaptor.getValue().getNhanVien()).isSameAs(admin);

        verify(invoiceUseCase).luuLichSuHoaDon(hoaDon, 3, "Admin bán hàng tại quầy");
    }

    @Test
    void adminThanhToanTaiQuayKhongCanMoCaNeuCuaHangChuaCoCaDangMo() {
        NhanVien admin = new NhanVien();
        admin.setId(UUID.randomUUID());
        admin.setMa("AD001");
        admin.setHoTen("Admin bán hàng");
        admin.setVaiTro(1);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(102);
        hoaDon.setMa("HD102");
        hoaDon.setNhanVien(admin);
        hoaDon.setTongTienHang(BigDecimal.valueOf(180000));
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(BigDecimal.valueOf(180000));

        ThanhToanTaiQuayRequest request = new ThanhToanTaiQuayRequest(
                null,
                null,
                "Khách vãng lai",
                "",
                null,
                null,
                1,
                BigDecimal.valueOf(180000),
                null,
                null,
                "Admin bán không cần mở ca",
                List.of(new TaoHoaDonChoItemRequest(2, 1, BigDecimal.valueOf(180000)))
        );

        when(invoiceUseCase.resolveNhanVienDangDangNhap()).thenReturn(admin);
        when(invoiceStateUseCase.xacDinhTrangThaiSauThanhToan(null)).thenReturn(3);
        when(invoiceUseCase.taoHoaDon(
                eq(null),
                eq("Khách vãng lai"),
                eq(""),
                eq(null),
                eq(null),
                anyList(),
                eq(3),
                eq("Admin bán không cần mở ca")
        )).thenReturn(hoaDon);
        when(paymentUseCase.xacDinhTienKhachDua(eq(1), any(), eq(BigDecimal.valueOf(180000)), eq(null), eq(null)))
                .thenReturn(BigDecimal.valueOf(180000));
        when(paymentUseCase.tinhTienThua(eq(1), eq(BigDecimal.valueOf(180000)), eq(BigDecimal.valueOf(180000)), eq(null), eq(null)))
                .thenReturn(BigDecimal.ZERO);
        when(paymentUseCase.mapHinhThucThanhToan(1)).thenReturn(1);
        when(vanChuyenRepository.findByHoaDonId(102)).thenReturn(Optional.empty());
        when(invoiceUseCase.resolveTenKhachHangHoaDon(hoaDon)).thenReturn("Khách vãng lai");
        when(invoiceUseCase.resolveSoDienThoaiKhachHangHoaDon(hoaDon)).thenReturn("");
        when(invoiceUseCase.mapHoaDonChiTiet(eq(hoaDon), anyList(), any()))
                .thenReturn(new HoaDonChoChiTietResponse(
                        102, "HD102", null, "Khách vãng lai", "", null,
                        BigDecimal.valueOf(180000), BigDecimal.ZERO, BigDecimal.valueOf(180000),
                        null, null, List.of()
                ));

        service.thanhToanTaiQuay(request);

        ArgumentCaptor<HoaDon> hoaDonCaptor = ArgumentCaptor.forClass(HoaDon.class);
        verify(hoaDonRepository).save(hoaDonCaptor.capture());
        assertThat(hoaDonCaptor.getValue().getGiaoCa()).isNull();
        assertThat(hoaDonCaptor.getValue().getNhanVien()).isSameAs(admin);

        verify(invoiceUseCase).luuLichSuHoaDon(hoaDon, 3, "Admin bán không cần mở ca");
    }
}
