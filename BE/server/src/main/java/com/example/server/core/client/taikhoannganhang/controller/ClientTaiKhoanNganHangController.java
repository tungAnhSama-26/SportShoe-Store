package com.example.server.core.client.taikhoannganhang.controller;

import com.example.server.core.client.taikhoannganhang.dto.TaiKhoanNganHangRequest;
import com.example.server.core.client.taikhoannganhang.dto.TaiKhoanNganHangResponse;
import com.example.server.core.client.taikhoannganhang.service.ClientTaiKhoanNganHangService;
import com.example.server.infrastructure.api.ApiResponse;
import com.example.server.infrastructure.security.ClientResourceAuthorization;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client/khach-hang/{khachHangId}/tai-khoan-ngan-hang")
public class ClientTaiKhoanNganHangController {

    private final ClientTaiKhoanNganHangService service;
    private final ClientResourceAuthorization authorization;

    public ClientTaiKhoanNganHangController(
            ClientTaiKhoanNganHangService service,
            ClientResourceAuthorization authorization
    ) {
        this.service = service;
        this.authorization = authorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaiKhoanNganHangResponse>>> layDanhSach(
            @PathVariable UUID khachHangId,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tài khoản ngân hàng thành công",
                service.layDanhSach(khachHangId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaiKhoanNganHangResponse>> themMoi(
            @PathVariable UUID khachHangId,
            @Valid @RequestBody TaiKhoanNganHangRequest request,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Thêm tài khoản ngân hàng thành công",
                service.themMoi(khachHangId, request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaiKhoanNganHangResponse>> capNhat(
            @PathVariable UUID khachHangId,
            @PathVariable Integer id,
            @Valid @RequestBody TaiKhoanNganHangRequest request,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật tài khoản ngân hàng thành công",
                service.capNhat(khachHangId, id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> xoa(
            @PathVariable UUID khachHangId,
            @PathVariable Integer id,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        service.xoa(khachHangId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản ngân hàng thành công", null));
    }

    @PostMapping("/{id}/mac-dinh")
    public ResponseEntity<ApiResponse<TaiKhoanNganHangResponse>> datMacDinh(
            @PathVariable UUID khachHangId,
            @PathVariable Integer id,
            Authentication authentication
    ) {
        authorization.assertCanAccess(khachHangId, authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Đặt tài khoản mặc định thành công",
                service.datMacDinh(khachHangId, id)
        ));
    }
}
