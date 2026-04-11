package com.example.server.core.admin.thongKe;

import com.example.server.infrastructure.api.ApiResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/admin/thong-ke")
public class ThongKeController {

    private final ThongKeService thongKeService;

    public ThongKeController(ThongKeService thongKeService) {
        this.thongKeService = thongKeService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ThongKeDashboardResponse>> layDashboardThongKe(
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(name = "brandId", required = false) Integer brandId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "periodType", required = false) String periodType
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lay du lieu thong ke thanh cong",
                thongKeService.layDuLieuThongKe(fromDate, toDate, brandId, keyword, periodType)
        ));
    }
}
