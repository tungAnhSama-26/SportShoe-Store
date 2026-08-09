package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.response.KhachHangTaiQuayResponse;
import com.example.server.entity.DiaChiKhachHang;
import com.example.server.repository.DiaChiKhachHangRepository;
import com.example.server.repository.KhachHangRepository;
import com.example.server.infrastructure.address.DiaChiHaiCapMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class KhachHangTaiQuayService {

    private final KhachHangRepository khachHangRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;

    public KhachHangTaiQuayService(
            KhachHangRepository khachHangRepository,
            DiaChiKhachHangRepository diaChiKhachHangRepository
    ) {
        this.khachHangRepository = khachHangRepository;
        this.diaChiKhachHangRepository = diaChiKhachHangRepository;
    }

    public List<KhachHangTaiQuayResponse> timKhachHangTheoTuKhoa(String keyword) {
        return khachHangRepository.searchByKeyword(chuanHoaTuKhoa(keyword))
                .stream()
                .limit(10)
                .map(khachHang -> {
                    DiaChiKhachHang diaChiMacDinh = diaChiKhachHangRepository
                            .findFirstByKhachHangIdAndLaMacDinhTrue(khachHang.getId())
                            .orElseGet(() -> diaChiKhachHangRepository.findByKhachHangIdOrderByLaMacDinhDesc(khachHang.getId()).stream().findFirst().orElse(null));
                    String diaChiMacDinhText = diaChiMacDinh != null
                            ? DiaChiHaiCapMapper.format(diaChiMacDinh.getDiaChi())
                            : null;
                    return new KhachHangTaiQuayResponse(
                            khachHang.getId(),
                            khachHang.getHoTen(),
                            khachHang.getSdt(),
                            khachHang.getEmail(),
                            diaChiMacDinhText
                    );
                })
                .toList();
    }

    private String chuanHoaTuKhoa(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}

