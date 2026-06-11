package com.example.server.core.client.auth.dto.response;

import com.example.server.core.admin.khachHang.dto.responsse.KhachHangResponse;

public record CustomerLoginResponse(
        String token,
        String tokenType,
        KhachHangResponse user
) {
}
