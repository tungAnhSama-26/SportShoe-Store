package com.example.server.core.admin.nhanVien.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CapNhatFaceIdRequest(
    @NotBlank(message = "Face descriptor không được để trống")
    String faceDescriptor
) {}
