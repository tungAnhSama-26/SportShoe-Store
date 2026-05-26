package com.example.server.core.admin.banHangTaiQuay.service.usecase;

import com.example.server.entity.GiayChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class BanHangTaiQuayInventoryUseCase {

    public void validateAvailable(GiayChiTiet giayChiTiet, Integer soLuong) {
        if (giayChiTiet.getKichHoat() == null || giayChiTiet.getKichHoat() != 1) {
            throw new BusinessException("San pham da ngung kinh doanh");
        }

        if (giayChiTiet.getSoLuong() == null || giayChiTiet.getSoLuong() < soLuong) {
            throw new BusinessException("Số lượng tồn không đủ cho sản phẩm " + giayChiTiet.getGiay().getTen());
        }
    }

    public void deductStock(GiayChiTiet giayChiTiet, Integer soLuong) {
        validateAvailable(giayChiTiet, soLuong);
        giayChiTiet.setSoLuong(giayChiTiet.getSoLuong() - soLuong);
        giayChiTiet.setNgayCapNhat(Instant.now());
    }
}
