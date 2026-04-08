package com.example.server.infrastructure;

import com.example.server.infrastructure.api.ApiResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/system")
public class HeThongController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.success("Server is healthy", Map.of(
                "status", "UP",
                "timestamp", Instant.now()
        )));
    }
}
