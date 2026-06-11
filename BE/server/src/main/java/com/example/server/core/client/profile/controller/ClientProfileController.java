package com.example.server.core.client.profile.controller;

import com.example.server.core.client.profile.dto.ClientDoiMatKhauRequest;
import com.example.server.core.client.profile.dto.ClientProfileRequest;
import com.example.server.core.client.profile.dto.ClientProfileResponse;
import com.example.server.core.client.profile.service.ClientProfileService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.ClientResourceAuthorization;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client/khach-hang/{khachHangId}")
public class ClientProfileController {

    private final ClientProfileService service;
    private final ClientResourceAuthorization authorization;

    public ClientProfileController(
            ClientProfileService service,
            ClientResourceAuthorization authorization
    ) {
        this.service = service;
        this.authorization = authorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ClientProfileResponse>> layThongTin(
            @PathVariable UUID khachHangId,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin tài khoản thành công",
                service.layThongTin(khachHangId)
        ));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ClientProfileResponse>> capNhatThongTin(
            @PathVariable UUID khachHangId,
            @Valid @RequestBody ClientProfileRequest request,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật thông tin tài khoản thành công",
                service.capNhatThongTin(khachHangId, request)
        ));
    }

    @PutMapping("/doi-mat-khau")
    public ResponseEntity<ApiResponse<Void>> doiMatKhau(
            @PathVariable UUID khachHangId,
            @Valid @RequestBody ClientDoiMatKhauRequest request,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        service.doiMatKhau(khachHangId, request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }
}
