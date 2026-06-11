package com.example.server.core.admin.nhanVien.service;

import com.example.server.core.admin.nhanVien.dto.request.PhanCaRequest;
import com.example.server.core.admin.nhanVien.dto.responsse.LichLamViecResponse;

import java.time.LocalDate;
import java.util.List;

public interface LichLamViecService {
    List<LichLamViecResponse> layLichLamViecTheoTuan(LocalDate tuNgay, LocalDate denNgay);
    LichLamViecResponse phanCa(PhanCaRequest request);
    void xepCaTuDong(LocalDate tuNgay, LocalDate denNgay);
}
