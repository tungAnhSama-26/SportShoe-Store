package com.example.server.core.admin.nhanVien.service;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.LichLamViecResponse;
import com.example.server.core.admin.nhanVien.dto.responsse.CapNhatLichLamViecResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LichLamViecService {
    List<LichLamViecResponse> layLichLamViecTheoTuan(LocalDate tuNgay, LocalDate denNgay);
    LichLamViecResponse phanCa(PhanCaRequest request);
    void xoaLich(UUID id);
    CapNhatLichLamViecResponse xepCaTuDong(LocalDate tuNgay, LocalDate denNgay);
    CapNhatLichLamViecResponse datLaiLich(LocalDate tuNgay, LocalDate denNgay);
}
