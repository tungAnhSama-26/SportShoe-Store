package com.example.server.core.admin.quanlytrahang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CapNhatVanChuyenTraHangRequest(
        @NotBlank(message = "Vui lòng nhập đơn vị vận chuyển")
        @Size(max = 100, message = "Đơn vị vận chuyển không được vượt quá 100 ký tự")
        String donViVanChuyen,

        @NotBlank(message = "Vui lòng nhập mã vận đơn hoàn")
        @Size(max = 150, message = "Mã vận đơn không được vượt quá 150 ký tự")
        String maVanDonHoan,

        @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
        String ghiChu
) {
}
