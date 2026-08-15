package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import com.example.server.repository.HoaDonRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.repository.LichSuHoaDonRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.repository.VanChuyenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoaDonTaiQuayServiceTest {

    @Mock private HoaDonRepository hoaDonRepository;
    @Mock private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Mock private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Mock private VanChuyenRepository vanChuyenRepository;
    @Mock private NhanVienRepository nhanVienRepository;
    @Mock private KhachHangRepository khachHangRepository;
    @Mock private GiayChiTietRepository giayChiTietRepository;
    @Mock private HinhAnhGiayRepository hinhAnhGiayRepository;
    @Mock private XacThucTaiQuayService validationUseCase;
    @Mock private TrangThaiHoaDonTaiQuayService invoiceStateUseCase;
    @Mock private GiaCaTaiQuayService pricingUseCase;
    @Mock private GiaoHangTaiQuayService shippingUseCase;
    @Mock private PhieuGiamGiaTaiQuayService voucherUseCase;
    @Mock private SanPhamTaiQuayService productUseCase;
    @Mock private TonKhoTaiQuayService inventoryUseCase;

    @InjectMocks
    private HoaDonTaiQuayService service;

    @Test
    void taoDongHoaDonPhaiDungGiaHienHanhTuBackend() {
        GiayChiTiet bienThe = new GiayChiTiet();
        bienThe.setId(15);
        bienThe.setGiaBan(new BigDecimal("1000000"));
        bienThe.setSoLuong(20);
        bienThe.setKichHoat(1);

        when(giayChiTietRepository.findByIdForUpdate(15)).thenReturn(Optional.of(bienThe));
        when(productUseCase.layGiaBanThucTe(bienThe)).thenReturn(new BigDecimal("1000000"));

        TaoHoaDonChoItemRequest request = new TaoHoaDonChoItemRequest(
                15,
                2,
                new BigDecimal("700000")
        );

        HoaDonChiTiet result = service.taoDongHoaDon(request);

        assertThat(result.getGiaDonVi()).isEqualByComparingTo("1000000");
        assertThat(result.getThanhTien()).isEqualByComparingTo("2000000");
    }
}
