package com.example.server.core.client.profile.service;

import com.example.server.core.client.profile.dto.ClientProfileRequest;
import com.example.server.core.client.profile.dto.ClientProfileResponse;
import com.example.server.core.client.profile.dto.ClientDoiMatKhauRequest;

import java.util.UUID;

public interface ClientProfileService {

    ClientProfileResponse layThongTin(UUID khachHangId);

    ClientProfileResponse capNhatThongTin(UUID khachHangId, ClientProfileRequest request);

    void doiMatKhau(UUID khachHangId, ClientDoiMatKhauRequest request);
}
