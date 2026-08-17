package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.entity.GiayChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class TonKhoTaiQuayService {

    public TonKhoTaiQuayService() {
    }

    public void validateAvailable(GiayChiTiet giayChiTiet, Integer soLuong) {
        validateAvailable(giayChiTiet, soLuong, false);
    }

    public void validateAvailable(GiayChiTiet giayChiTiet, Integer soLuong, boolean bypassActiveCheck) {
        if (!bypassActiveCheck) {
            if (giayChiTiet.getKichHoat() == null || giayChiTiet.getKichHoat() != 1 || 
                giayChiTiet.getGiay() == null || giayChiTiet.getGiay().getTrangThai() == null || giayChiTiet.getGiay().getTrangThai() != 1) {
                String tenGiay = giayChiTiet.getGiay() != null ? giayChiTiet.getGiay().getTen() : "";
                throw new BusinessException("Sản phẩm " + tenGiay + " đã ngừng hoạt động, vui lòng chọn sản phẩm khác");
            }
        }

        int tonThucTe = giayChiTiet.getSoLuong() == null ? 0 : giayChiTiet.getSoLuong();
        if (tonThucTe < soLuong) {
            throw new BusinessException("Số lượng tồn kho không đủ cho sản phẩm "
                    + giayChiTiet.getGiay().getTen() + " (còn " + tonThucTe + ")");
        }
    }

    public int soLuongKhaDung(GiayChiTiet giayChiTiet) {
        return giayChiTiet.getSoLuong() == null ? 0 : giayChiTiet.getSoLuong();
    }

    public void deductStock(GiayChiTiet giayChiTiet, Integer soLuong) {
        deductStock(giayChiTiet, soLuong, false);
    }

    public void deductStock(GiayChiTiet giayChiTiet, Integer soLuong, boolean bypassActiveCheck) {
        validateAvailable(giayChiTiet, soLuong, bypassActiveCheck);
        giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() - soLuong);
        giayChiTiet.setNgayCapNhat(Instant.now());
    }

    public void restoreStock(GiayChiTiet giayChiTiet, Integer soLuong) {
        if (giayChiTiet.getSoLuong() == null) {
            giayChiTiet.setSoLuong(0);
        }
        giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() + soLuong);
        giayChiTiet.setNgayCapNhat(Instant.now());
    }
}

