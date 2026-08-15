package com.example.server.core.inventory;

import com.example.server.core.client.dathang.dto.DatHangItemRequest;
import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.KichCo;
import com.example.server.entity.MauSac;
import com.example.server.infrastructure.exception.InventoryConflictException;
import com.example.server.repository.GiayChiTietRepository;
import com.example.server.repository.HoaDonChiTietRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TonKhoKhaDungServiceTest {

    @Mock private GiayChiTietRepository giayChiTietRepository;
    @Mock private HoaDonChiTietRepository hoaDonChiTietRepository;
    private TonKhoKhaDungService service;

    @BeforeEach
    void setUp() {
        service = new TonKhoKhaDungService(giayChiTietRepository, hoaDonChiTietRepository);
    }

    @Test
    void khoaVaKiemTra_khoaIdTangDanVaGomDongTrungBienThe() {
        GiayChiTiet bienThe2 = bienThe(2, 10);
        GiayChiTiet bienThe7 = bienThe(7, 10);
        when(giayChiTietRepository.findByIdForUpdate(2)).thenReturn(Optional.of(bienThe2));
        when(giayChiTietRepository.findByIdForUpdate(7)).thenReturn(Optional.of(bienThe7));
        when(hoaDonChiTietRepository.tongSoLuongDangGiuTheoBienThe(any(), any(), eq(null)))
                .thenReturn(List.of());

        Map<Integer, GiayChiTiet> result = service.khoaVaKiemTra(List.of(
                new DatHangItemRequest(7, 2),
                new DatHangItemRequest(2, 1),
                new DatHangItemRequest(7, 3)
        ), null);

        InOrder order = inOrder(giayChiTietRepository);
        order.verify(giayChiTietRepository).findByIdForUpdate(2);
        order.verify(giayChiTietRepository).findByIdForUpdate(7);
        assertThat(result.keySet()).containsExactly(2, 7);
    }

    @Test
    void khoaVaKiemTra_tuChoiKhiKhachTruocDaGiuHet() {
        GiayChiTiet bienThe = bienThe(5, 10);
        when(giayChiTietRepository.findByIdForUpdate(5)).thenReturn(Optional.of(bienThe));
        when(hoaDonChiTietRepository.tongSoLuongDangGiuTheoBienThe(any(), any(), eq(null)))
                .thenReturn(List.<Object[]>of(new Object[]{5, 10L}));

        assertThatThrownBy(() -> service.khoaVaKiemTra(
                List.of(new DatHangItemRequest(5, 1)), null))
                .isInstanceOf(InventoryConflictException.class)
                .hasMessageContaining("còn 0")
                .hasMessageContaining("yêu cầu 1");
    }

    @Test
    void khoaVaKiemTra_choPhepDungPhanConLai() {
        GiayChiTiet bienThe = bienThe(5, 10);
        when(giayChiTietRepository.findByIdForUpdate(5)).thenReturn(Optional.of(bienThe));
        when(hoaDonChiTietRepository.tongSoLuongDangGiuTheoBienThe(any(), any(), eq(null)))
                .thenReturn(List.<Object[]>of(new Object[]{5, 6L}));

        Map<Integer, GiayChiTiet> result = service.khoaVaKiemTra(
                List.of(new DatHangItemRequest(5, 4)), null);

        assertThat(result).containsEntry(5, bienThe);
    }

    @Test
    void laySoLuongKhaDung_khongBaoGioTraSoAm() {
        GiayChiTiet bienThe = bienThe(5, 3);
        when(hoaDonChiTietRepository.tongSoLuongDangGiuTheoBienThe(any(), any(), eq(null)))
                .thenReturn(List.<Object[]>of(new Object[]{5, 9L}));

        assertThat(service.laySoLuongKhaDung(List.of(bienThe))).containsEntry(5, 0);
    }

    private GiayChiTiet bienThe(int id, int soLuong) {
        Giay giay = new Giay();
        giay.setTen("Giày test");
        MauSac mauSac = new MauSac();
        mauSac.setTen("Đen");
        KichCo kichCo = new KichCo();
        kichCo.setGiaTri("42");
        GiayChiTiet result = new GiayChiTiet();
        result.setId(id);
        result.setSoLuong(soLuong);
        result.setGiay(giay);
        result.setMauSac(mauSac);
        result.setKichCo(kichCo);
        return result;
    }
}
