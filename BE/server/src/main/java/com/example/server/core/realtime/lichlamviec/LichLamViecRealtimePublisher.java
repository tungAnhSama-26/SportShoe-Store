package com.example.server.core.realtime.lichlamviec;

import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;

@Component
public class LichLamViecRealtimePublisher {

    public static final String TOPIC = "/topic/admin/lich-lam-viec";

    private final WebSocketNotificationService webSocketNotificationService;

    public LichLamViecRealtimePublisher(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    public void phatSauCommit(String loaiSuKien) {
        Map<String, Object> payload = Map.of(
                "loaiSuKien", loaiSuKien,
                "thoiGian", Instant.now().toString()
        );
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    phat(loaiSuKien, payload);
                }
            });
            return;
        }
        phat(loaiSuKien, payload);
    }

    private void phat(String loaiSuKien, Map<String, Object> payload) {
        try {
            webSocketNotificationService.sendToTopic(TOPIC, loaiSuKien, payload);
        } catch (Exception ignored) {
        }
    }
}
