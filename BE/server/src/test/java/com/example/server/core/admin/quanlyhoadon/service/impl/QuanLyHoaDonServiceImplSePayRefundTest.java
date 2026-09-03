package com.example.server.core.admin.quanlyhoadon.service.impl;

import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.core.client.thongbao.service.ClientThongBaoService;
import com.example.server.core.realtime.hoadon.HoaDonRealtimePublisher;
import com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher;
import com.example.server.core.refund.RefundBankAccountResolver;
import com.example.server.entity.HoaDon;
import com.example.server.entity.ThanhToan;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuanLyHoaDonServiceImplSePayRefundTest {

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
    void xacNhanHoanTienTuDongSePay_thanhCong() {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(10);
        hoaDon.setMa("HD10023");
        hoaDon.setTrangThai(8); // Cần hoàn tiền

        ThanhToan thanhToanGoc = new ThanhToan();
        thanhToanGoc.setId(1);
        thanhToanGoc.setHoaDon(hoaDon);
        thanhToanGoc.setLoaiGiaoDich(1); // Thanh toán
        thanhToanGoc.setTrangThai(4); // Cần hoàn tiền
        thanhToanGoc.setSoTien(new BigDecimal("350000"));

        when(thanhToanRepository.findByTrangThaiAndLoaiGiaoDich(4, 1))
                .thenReturn(List.of(thanhToanGoc));
        when(thanhToanRepository.existsByGiaoDichGocIdAndLoaiGiaoDich(1, 2))
                .thenReturn(false);

        String result = service.xacNhanHoanTienTuDongSePay("HOAN TIEN DON SHOEHTHD10023", 350000L, "SPREF123");

        assertThat(result).isEqualTo("HD10023");
        assertThat(hoaDon.getTrangThai()).isEqualTo(6); // Hủy

        ArgumentCaptor<ThanhToan> captor = ArgumentCaptor.forClass(ThanhToan.class);
        verify(thanhToanRepository, times(2)).save(captor.capture());

        List<ThanhToan> saved = captor.getAllValues();
        ThanhToan giaoDichHoan = saved.get(0);
        assertThat(giaoDichHoan.getLoaiGiaoDich()).isEqualTo(2); // Hoàn tiền
        assertThat(giaoDichHoan.getTrangThai()).isEqualTo(5); // Đã hoàn tiền
        assertThat(giaoDichHoan.getSoTien()).isEqualByComparingTo("350000");
        assertThat(giaoDichHoan.getCongThanhToan()).contains("SePay Webhook Out");

        ThanhToan thanhToanCapNhat = saved.get(1);
        assertThat(thanhToanCapNhat.getTrangThai()).isEqualTo(1); // Thành công

        verify(hoaDonRealtimePublisher).publishAfterCommit(eq(hoaDon), eq("HOAN_TIEN"));
    }

    @Test
    void xacNhanHoanTienTuDongSePay_khongKhopMaDon_traVeNull() {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setId(10);
        hoaDon.setMa("HD99999");

        ThanhToan thanhToanGoc = new ThanhToan();
        thanhToanGoc.setId(1);
        thanhToanGoc.setHoaDon(hoaDon);
        thanhToanGoc.setLoaiGiaoDich(1);
        thanhToanGoc.setTrangThai(4);
        thanhToanGoc.setSoTien(new BigDecimal("350000"));

        when(thanhToanRepository.findByTrangThaiAndLoaiGiaoDich(4, 1))
                .thenReturn(List.of(thanhToanGoc));

        String result = service.xacNhanHoanTienTuDongSePay("HOAN TIEN DON SHOEHTHD10023", 350000L, "SPREF123");

        assertThat(result).isNull();
        verify(thanhToanRepository, never()).save(any());
    }
}
