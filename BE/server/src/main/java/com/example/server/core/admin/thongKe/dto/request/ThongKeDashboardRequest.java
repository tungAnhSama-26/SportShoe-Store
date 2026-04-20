package com.example.server.core.admin.thongKe.dto.request;

import java.time.LocalDate;

public record ThongKeDashboardRequest(
        LocalDate fromDate,
        LocalDate toDate,
        Integer brandId,
        String keyword,
        String periodType
) {
}
