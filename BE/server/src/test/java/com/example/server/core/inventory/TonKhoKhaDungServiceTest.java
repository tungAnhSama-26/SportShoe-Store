package com.example.server.core.inventory;

import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.KichCo;
import com.example.server.entity.MauSac;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TonKhoKhaDungServiceTest {

    private TonKhoKhaDungService service;

    @BeforeEach
    void setUp() {
        service = new TonKhoKhaDungService();
    }

    @Test
    void laySoLuongKhaDung_traVeTonKhoThucTe() {
        GiayChiTiet bienThe = bienThe(5, 10);
        assertThat(service.laySoLuongKhaDung(List.of(bienThe))).containsEntry(5, 10);
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
