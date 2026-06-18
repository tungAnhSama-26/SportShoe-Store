package com.example.server.core.admin.chamCong.service;

import com.example.server.core.admin.chamCong.dto.request.CheckInRequest;
import com.example.server.core.admin.chamCong.dto.request.CheckOutRequest;
import com.example.server.core.admin.chamCong.dto.response.ChamCongResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ChamCongService {
    ChamCongResponse checkIn(CheckInRequest request);
    ChamCongResponse checkOut(CheckOutRequest request);
    List<ChamCongResponse> layDanhSachChamCong(LocalDate tuNgay, LocalDate denNgay);
}
