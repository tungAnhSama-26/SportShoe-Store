package com.example.server.core.admin.chamCong.controller;

import com.example.server.core.admin.chamCong.dto.request.CheckInRequest;
import com.example.server.core.admin.chamCong.dto.request.CheckOutRequest;
import com.example.server.core.admin.chamCong.dto.response.ChamCongResponse;
import com.example.server.core.admin.chamCong.service.ChamCongService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cham-cong")
public class ChamCongController {

    private final ChamCongService chamCongService;

    public ChamCongController(ChamCongService chamCongService) {
        this.chamCongService = chamCongService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<ChamCongResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(chamCongService.checkIn(request));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ChamCongResponse> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(chamCongService.checkOut(request));
    }

    @GetMapping
    public ResponseEntity<List<ChamCongResponse>> layDanhSachChamCong(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay
    ) {
        if (tuNgay == null) tuNgay = LocalDate.now().minusDays(30);
        if (denNgay == null) denNgay = LocalDate.now();
        return ResponseEntity.ok(chamCongService.layDanhSachChamCong(tuNgay, denNgay));
    }

    @GetMapping("/server-time")
    public ResponseEntity<java.util.Map<String, String>> getServerTime() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("serverTime", now.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return ResponseEntity.ok(response);
    }
}
