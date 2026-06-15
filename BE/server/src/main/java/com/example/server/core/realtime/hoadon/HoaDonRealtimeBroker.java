package com.example.server.core.realtime.hoadon;

import com.example.server.infrastructure.security.AdminPrincipal;
import com.example.server.infrastructure.security.CustomerPrincipal;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class HoaDonRealtimeBroker {

    private static final long SSE_TIMEOUT = 0L;
    private static final long RECONNECT_DELAY_MS = 3_000L;

    private final Set<SseEmitter> adminEmitters = new CopyOnWriteArraySet<>();
    private final Map<UUID, Set<SseEmitter>> customerEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Vui long dang nhap de theo doi hoa don");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AdminPrincipal) {
            return register(adminEmitters);
        }
        if (principal instanceof CustomerPrincipal customerPrincipal) {
            Set<SseEmitter> emitters = customerEmitters.computeIfAbsent(
                    customerPrincipal.id(),
                    ignored -> new CopyOnWriteArraySet<>()
            );
            return register(emitters);
        }

        throw new AccessDeniedException("Tai khoan khong duoc phep theo doi hoa don");
    }

    public void publish(HoaDonRealtimeEvent event) {
        sendEvent(adminEmitters, event);
        if (event.khachHangId() == null) {
            return;
        }

        Set<SseEmitter> emitters = customerEmitters.get(event.khachHangId());
        if (emitters != null) {
            sendEvent(emitters, event);
            removeEmptyCustomerGroup(event.khachHangId(), emitters);
        }
    }

    @Scheduled(fixedRate = 25_000L)
    public void heartbeat() {
        sendHeartbeat(adminEmitters);
        customerEmitters.forEach((customerId, emitters) -> {
            sendHeartbeat(emitters);
            removeEmptyCustomerGroup(customerId, emitters);
        });
    }

    private SseEmitter register(Set<SseEmitter> emitters) {
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

    private void sendEvent(Set<SseEmitter> emitters, HoaDonRealtimeEvent event) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(event.eventId())
                        .name("hoa-don-thay-doi")
                        .reconnectTime(RECONNECT_DELAY_MS)
                        .data(event));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(emitter);
            }
        });
    }

    private void sendHeartbeat(Set<SseEmitter> emitters) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException | IllegalStateException exception) {
                emitters.remove(emitter);
            }
        });
    }

    private void removeEmptyCustomerGroup(UUID customerId, Set<SseEmitter> emitters) {
        if (emitters.isEmpty()) {
            customerEmitters.remove(customerId, emitters);
        }
    }
}
