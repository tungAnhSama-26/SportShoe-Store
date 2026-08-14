package com.example.server.core.admin.quanlykhuyenmai.service;

import com.example.server.core.admin.quanlykhuyenmai.dto.request.DotGiamGiaRequest;
import com.example.server.core.client.thongbao.service.ClientThongBaoService;
import com.example.server.core.realtime.sanpham.SanPhamRealtimePublisher;
import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import com.example.server.entity.GiayChiTiet;
import com.example.server.repository.DotGiamGiaRepository;
import com.example.server.repository.DotGiamGiaSanPhamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DotGiamGiaServiceRealtimeTest {

    @Mock
    private DotGiamGiaRepository dotGiamGiaRepository;
    @Mock
    private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;
    @Mock
    private DotGiamGiaSanPhamService dotGiamGiaSanPhamService;
    @Mock
    private ClientThongBaoService clientThongBaoService;
    @Mock
    private SanPhamRealtimePublisher sanPhamRealtimePublisher;

    private DotGiamGiaService service;

    @BeforeEach
    void setUp() {
        service = new DotGiamGiaService(
                dotGiamGiaRepository,
                dotGiamGiaSanPhamRepository,
                dotGiamGiaSanPhamService,
                clientThongBaoService,
                sanPhamRealtimePublisher
        );
    }

    @Test
    void updateNhieuBienTheChiPhatMotSuKienRealtime() {
        int campaignId = 7;
        DotGiamGia campaign = campaign(campaignId);
        when(dotGiamGiaRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(dotGiamGiaRepository.save(any(DotGiamGia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dotGiamGiaSanPhamRepository.findByDotGiamGiaId(campaignId))
                .thenReturn(List.of(link(101), link(102), link(103)));

        service.update(campaignId, request(0));

        verify(dotGiamGiaSanPhamService).updateGiaBanForGiayChiTiet(101, false);
        verify(dotGiamGiaSanPhamService).updateGiaBanForGiayChiTiet(102, false);
        verify(dotGiamGiaSanPhamService).updateGiaBanForGiayChiTiet(103, false);
        verify(sanPhamRealtimePublisher).phatSauCommit("DOT_GIAM_GIA");
    }

    @Test
    void batTatLapLaiNhieuLanMoiLanChiPhatMotSuKien() {
        int campaignId = 8;
        int repeatCount = 12;
        DotGiamGia campaign = campaign(campaignId);
        when(dotGiamGiaRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(dotGiamGiaRepository.save(any(DotGiamGia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dotGiamGiaSanPhamRepository.findByDotGiamGiaId(campaignId))
                .thenReturn(List.of(link(201), link(202)));

        for (int index = 0; index < repeatCount; index++) {
            service.update(campaignId, request(index % 2));
        }

        verify(dotGiamGiaSanPhamService, times(repeatCount))
                .updateGiaBanForGiayChiTiet(201, false);
        verify(dotGiamGiaSanPhamService, times(repeatCount))
                .updateGiaBanForGiayChiTiet(202, false);
        verify(sanPhamRealtimePublisher, times(repeatCount)).phatSauCommit("DOT_GIAM_GIA");
        verify(dotGiamGiaSanPhamService, never()).updateGiaBanForGiayChiTiet(201);
        verify(dotGiamGiaSanPhamService, never()).updateGiaBanForGiayChiTiet(202);
    }

    private DotGiamGia campaign(Integer id) {
        DotGiamGia campaign = new DotGiamGia();
        campaign.setId(id);
        campaign.setMa("DGG-REALTIME");
        campaign.setTen("Giảm giá realtime");
        campaign.setNgayTao(LocalDate.now());
        return campaign;
    }

    private DotGiamGiaRequest request(Integer active) {
        return DotGiamGiaRequest.builder()
                .ma("DGG-REALTIME")
                .ten("Giảm giá realtime")
                .moTa("Kiểm thử bật tắt liên tục")
                .loaiGiam(1)
                .giaTriGiam(BigDecimal.valueOf(25))
                .ngayBatDau(LocalDate.now().minusDays(1))
                .ngayKetThuc(LocalDate.now().plusDays(1))
                .kichHoat(active)
                .build();
    }

    private DotGiamGiaSanPham link(Integer variantId) {
        GiayChiTiet variant = new GiayChiTiet();
        variant.setId(variantId);
        DotGiamGiaSanPham link = new DotGiamGiaSanPham();
        link.setGiayChiTiet(variant);
        return link;
    }
}
