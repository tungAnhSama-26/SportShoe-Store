package com.example.server.core.client.profile.service;

import com.example.server.core.client.profile.dto.ClientProfileRequest;
import com.example.server.core.client.profile.dto.ClientProfileResponse;
import com.example.server.core.client.profile.dto.ClientDoiMatKhauRequest;
import com.example.server.core.client.profile.dto.ClientDiaChiRequest;
import com.example.server.core.client.profile.dto.ClientDiaChiResponse;

import java.util.List;
import java.util.UUID;

public interface ClientProfileService {

    ClientProfileResponse layThongTin(UUID khachHangId);

    ClientProfileResponse capNhatThongTin(UUID khachHangId, ClientProfileRequest request);

    void doiMatKhau(UUID khachHangId, ClientDoiMatKhauRequest request);

    List<ClientDiaChiResponse> layDanhSachDiaChi(UUID khachHangId);

    ClientDiaChiResponse themDiaChi(UUID khachHangId, ClientDiaChiRequest request);

    ClientDiaChiResponse capNhatDiaChi(UUID khachHangId, Integer diaChiId, ClientDiaChiRequest request);

    void xoaDiaChi(UUID khachHangId, Integer diaChiId);

    void datMacDinhDiaChi(UUID khachHangId, Integer diaChiId);
}
