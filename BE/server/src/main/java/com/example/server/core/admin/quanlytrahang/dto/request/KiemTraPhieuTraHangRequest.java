package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record KiemTraPhieuTraHangRequest(
        @NotEmpty(message = "Vui lòng nhập kết quả kiểm tra sản phẩm")
        List<@Valid KiemTraSanPhamTraRequest> sanPhams,

        @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
        String ghiChu
) {
}
