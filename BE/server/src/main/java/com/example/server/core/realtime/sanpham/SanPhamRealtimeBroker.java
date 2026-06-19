package com.example.server.core.realtime.sanpham;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Kênh SSE CÔNG KHAI báo thay đổi catalog (ngừng bán, đổi giá, đợt giảm giá...).
 * Không cần đăng nhập nên cả khách vãng lai cũng nhận được để giỏ hàng tự đồng bộ lại.
 */
@Service
public class SanPhamRealtimeBroker {

    private static final long SSE_TIMEOUT = 0L;
    private static final long RECONNECT_DELAY_MS = 3_000L;

    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.add(emitter);

        Runnable cleanup = () -> emitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(() -> {
            cleanup.run();
            emitter.complete();
        });
        emitter.onError(error -> cleanup.run());

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .reconnectTime(RECONNECT_DELAY_MS)
                    .data(Map.of("connected", true, "at", Instant.now().toString())));
        } catch (IOException exception) {
            cleanup.run();
            emitter.completeWithError(exception);
        }

        return emitter;
    }

    public void publish(SanPhamRealtimeEvent event) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(event.eventId())
                        .name("san-pham-thay-doi")
                        .reconnectTime(RECONNECT_DELAY_MS)
                        .data(event));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(emitter);
            }
        });
    }

    @Scheduled(fixedRate = 25_000L)
    public void heartbeat() {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(emitter);
            }
        });
    }
}
