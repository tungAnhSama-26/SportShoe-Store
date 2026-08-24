package com.example.server.core.admin.quanlyhoadon.service.impl;

import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.client.thongbao.service.ClientThongBaoService;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher;
import com.example.server.core.refund.RefundBankAccountResolver;
import com.example.server.entity.DiaChiHaiCap;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.entity.LichSuHoaDon;
import com.example.server.entity.ThanhToan;
import com.example.server.entity.VanChuyen;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.ThanhToanRepository;
import com.example.server.repository.VanChuyenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuanLyHoaDonServiceImplGiaoLaiTest {

    @Mock private HoaDonRepository hoaDonRepository;
    @Mock private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Mock private ThanhToanRepository thanhToanRepository;
    @Mock private VanChuyenRepository vanChuyenRepository;
    @Mock private HinhAnhGiayRepository hinhAnhGiayRepository;
    @Mock private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Mock private GiayChiTietRepository giayChiTietRepository;
    @Mock private NhanVienRepository nhanVienRepository;
    @Mock private GhnShippingService ghnShippingService;
    @Mock private RefundBankAccountResolver refundBankAccountResolver;
    @Mock private HoaDonRealtimePublisher hoaDonRealtimePublisher;
    @Mock private SanPhamRealtimePublisher sanPhamRealtimePublisher;
    @Mock private EmailService emailService;
    @Mock private QuanLySanPhamService quanLySanPhamService;
    @Mock private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    @Mock private ClientThongBaoService clientThongBaoService;

    private QuanLyHoaDonServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QuanLyHoaDonServiceImpl(
                hoaDonRepository,
                hoaDonChiTietRepository,
                thanhToanRepository,
                vanChuyenRepository,
                hinhAnhGiayRepository,
                lichSuHoaDonRepository,
                giayChiTietRepository,
                nhanVienRepository,
                ghnShippingService,
                refundBankAccountResolver,
                hoaDonRealtimePublisher,
                sanPhamRealtimePublisher,
                emailService,
                quanLySanPhamService,
                dotGiamGiaSanPhamRepository,
                clientThongBaoService
        );
    }

    @Test
    void giaoLaiDonHang_khoiPhucThanhToanVaTruKhoChoHangThayThe() {
        HoaDon hoaDon = taoHoaDonGiaoThatBai();
        ThanhToan thanhToan = taoThanhToan(2, 4);
        GiayChiTiet bienThe = taoBienThe(5);
        HoaDonChiTiet chiTiet = taoChiTiet(hoaDon, bienThe, 2);
        VanChuyen vanChuyen = taoVanChuyen(hoaDon);
        mockLuotGiaoThanhCong(hoaDon, thanhToan, chiTiet, bienThe, vanChuyen);

        var response = service.giaoLaiDonHang(hoaDon.getId());

        assertThat(response.trangThai()).isEqualTo("Chờ lấy hàng");
        assertThat(hoaDon.getTrangThai()).isEqualTo(2);
        assertThat(hoaDon.getDaTruKho()).isTrue();
        assertThat(bienThe.getSoLuong()).isEqualTo(3);
        assertThat(thanhToan.getTrangThai()).isEqualTo(1);
        assertThat(vanChuyen.getTrangThai()).isEqualTo(1);
        assertThat(vanChuyen.getMaVanDon()).isNull();
        assertThat(vanChuyen.getLyDoGiaoHangThatBai()).isNull();
        verify(hoaDonRealtimePublisher).publishAfterCommit(hoaDon, "GIAO_LAI");
        verify(lichSuHoaDonRepository).save(any(LichSuHoaDon.class));
    }

    @Test
    void giaoLaiDonHang_khoiPhucCodVeChoThanhToan() {
        HoaDon hoaDon = taoHoaDonGiaoThatBai();
        ThanhToan cod = taoThanhToan(4, 3);
        GiayChiTiet bienThe = taoBienThe(3);
        HoaDonChiTiet chiTiet = taoChiTiet(hoaDon, bienThe, 1);
        mockLuotGiaoThanhCong(hoaDon, cod, chiTiet, bienThe, taoVanChuyen(hoaDon));

        service.giaoLaiDonHang(hoaDon.getId());

        assertThat(cod.getTrangThai()).isZero();
        assertThat(cod.getGhiChu()).contains("Khôi phục COD chờ thanh toán");
    }

    @Test
    void giaoLaiDonHang_tuChoiKhiDaHoanTien() {
        HoaDon hoaDon = taoHoaDonGiaoThatBai();
        ThanhToan hoanTien = taoThanhToan(2, 5);
        hoanTien.setLoaiGiaoDich(2);
        when(hoaDonRepository.findDetailByIdForUpdate(hoaDon.getId())).thenReturn(Optional.of(hoaDon));
        when(thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()))
                .thenReturn(List.of(hoanTien));

        assertThatThrownBy(() -> service.giaoLaiDonHang(hoaDon.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã được hoàn tiền");

        verify(giayChiTietRepository, never()).save(any());
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void giaoLaiDonHang_tuChoiKhiKhongDuTonVaKhongDoiThanhToan() {
        HoaDon hoaDon = taoHoaDonGiaoThatBai();
        ThanhToan thanhToan = taoThanhToan(2, 4);
        GiayChiTiet bienThe = taoBienThe(1);
        HoaDonChiTiet chiTiet = taoChiTiet(hoaDon, bienThe, 2);
        when(hoaDonRepository.findDetailByIdForUpdate(hoaDon.getId())).thenReturn(Optional.of(hoaDon));
        when(thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()))
                .thenReturn(List.of(thanhToan));
        when(hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId()))
                .thenReturn(List.of(chiTiet));
        when(giayChiTietRepository.findByIdForUpdate(bienThe.getId())).thenReturn(Optional.of(bienThe));

        assertThatThrownBy(() -> service.giaoLaiDonHang(hoaDon.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không đủ số lượng");

        assertThat(thanhToan.getTrangThai()).isEqualTo(4);
        assertThat(bienThe.getSoLuong()).isEqualTo(1);
        verify(thanhToanRepository, never()).save(any());
        verify(hoaDonRepository, never()).save(any());
    }

    @Test
    void giaoLaiDonHang_tuChoiLanHaiTheoTrangThai() {
        HoaDon hoaDon = taoHoaDonGiaoThatBai();
        ThanhToan thanhToan = taoThanhToan(2, 4);
        GiayChiTiet bienThe = taoBienThe(3);
        HoaDonChiTiet chiTiet = taoChiTiet(hoaDon, bienThe, 1);
        mockLuotGiaoThanhCong(hoaDon, thanhToan, chiTiet, bienThe, taoVanChuyen(hoaDon));
        service.giaoLaiDonHang(hoaDon.getId());

        assertThatThrownBy(() -> service.giaoLaiDonHang(hoaDon.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("giao hàng thất bại");
        assertThat(bienThe.getSoLuong()).isEqualTo(2);
    }

    private void mockLuotGiaoThanhCong(
            HoaDon hoaDon,
            ThanhToan thanhToan,
            HoaDonChiTiet chiTiet,
            GiayChiTiet bienThe,
            VanChuyen vanChuyen
    ) {
        when(hoaDonRepository.findDetailByIdForUpdate(hoaDon.getId())).thenReturn(Optional.of(hoaDon));
        when(hoaDonRepository.findDetailById(hoaDon.getId())).thenReturn(Optional.of(hoaDon));
        when(thanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()))
                .thenReturn(List.of(thanhToan));
        when(hoaDonChiTietRepository.findByHoaDonIdWithProduct(hoaDon.getId()))
                .thenReturn(List.of(chiTiet), List.of());
        when(giayChiTietRepository.findByIdForUpdate(bienThe.getId())).thenReturn(Optional.of(bienThe));
        when(vanChuyenRepository.findByHoaDonId(hoaDon.getId())).thenReturn(Optional.of(vanChuyen));
        when(lichSuHoaDonRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDon.getId()))
                .thenReturn(List.of());
    }

    private HoaDon taoHoaDonGiaoThatBai() {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(101);
        hoaDon.setMa("HD101");
        hoaDon.setKenhBan(2);
        hoaDon.setTrangThai(10);
        hoaDon.setTenNguoiNhan("Nguyễn Văn A");
        hoaDon.setSdtNguoiNhan("0912345678");
        hoaDon.setDiaChiGiaoHang(taoDiaChi());
        hoaDon.setTongTienHang(BigDecimal.valueOf(500_000));
        hoaDon.setTongTienThanhToan(BigDecimal.valueOf(516_500));
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setNgayLap(Instant.now());
        hoaDon.setDaTruKho(true);
        return hoaDon;
    }

    private DiaChiHaiCap taoDiaChi() {
        DiaChiHaiCap diaChi = new DiaChiHaiCap();
        diaChi.setDiaChiCuThe("Số 1");
        diaChi.setPhuongXa("Xã Đông Anh");
        diaChi.setTinhThanh("Thành phố Hà Nội");
        return diaChi;
    }

    private ThanhToan taoThanhToan(int hinhThuc, int trangThai) {
        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setId(201);
        thanhToan.setHinhThuc(hinhThuc);
        thanhToan.setTrangThai(trangThai);
        thanhToan.setLoaiGiaoDich(1);
        thanhToan.setSoTien(BigDecimal.valueOf(516_500));
        thanhToan.setNgayTao(Instant.now());
        return thanhToan;
    }

    private GiayChiTiet taoBienThe(int tonKho) {
        Giay giay = new Giay();
        giay.setId(301);
        giay.setTen("SportShoe Test");
        GiayChiTiet bienThe = new GiayChiTiet();
        bienThe.setId(401);
        bienThe.setGiay(giay);
        bienThe.setSoLuong(tonKho);
        return bienThe;
    }

    private HoaDonChiTiet taoChiTiet(HoaDon hoaDon, GiayChiTiet bienThe, int soLuong) {
        HoaDonChiTiet chiTiet = new HoaDonChiTiet();
        chiTiet.setId(501);
        chiTiet.setHoaDon(hoaDon);
        chiTiet.setGiayChiTiet(bienThe);
        chiTiet.setSoLuong(soLuong);
        return chiTiet;
    }

    private VanChuyen taoVanChuyen(HoaDon hoaDon) {
        VanChuyen vanChuyen = new VanChuyen();
        vanChuyen.setId(601);
        vanChuyen.setHoaDon(hoaDon);
        vanChuyen.setDonViVanChuyen("GHN");
        vanChuyen.setPhiVanChuyen(BigDecimal.valueOf(16_500));
        vanChuyen.setTrangThai(4);
        vanChuyen.setMaVanDon("GHN-OLD");
        vanChuyen.setLyDoGiaoHangThatBai("Thất lạc");
        vanChuyen.setNgayTao(Instant.now());
        return vanChuyen;
    }
}
