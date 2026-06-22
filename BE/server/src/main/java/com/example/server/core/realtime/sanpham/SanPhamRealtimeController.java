package com.example.server.core.realtime.sanpham;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Kênh SSE công khai cho thay đổi catalog (giỏ hàng theo dõi để tự đồng bộ giá/tồn/ngừng bán). */
@RestController
@RequestMapping("/api/v1/realtime/san-pham")
public class SanPhamRealtimeController {

    private final SanPhamRealtimeBroker broker;

    public SanPhamRealtimeController(SanPhamRealtimeBroker broker) {
        this.broker = broker;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no")
                .body(broker.subscribe());
    }
}
