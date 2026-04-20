package com.example.server.core.admin.thongKe.dto.response;

import java.time.LocalDate;

public record ThongKeBoLocDaApDungResponse(
        String kyThongKe,
        LocalDate tuNgay,
        LocalDate denNgay,
        Integer thuongHieuId,
        String keyword
) {
}
