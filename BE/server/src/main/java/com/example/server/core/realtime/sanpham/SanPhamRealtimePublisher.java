package com.example.server.core.realtime.sanpham;

import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Phát sự kiện thay đổi catalog. Phát SAU khi commit để giỏ đồng bộ đúng dữ liệu mới. */
@Component
public class SanPhamRealtimePublisher {

    private final SanPhamRealtimeBroker broker;
    private final WebSocketNotificationService webSocketNotificationService;

    public SanPhamRealtimePublisher(
            SanPhamRealtimeBroker broker,
            WebSocketNotificationService webSocketNotificationService
    ) {
        this.broker = broker;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    public void phatSauCommit(String loaiSuKien) {
        SanPhamRealtimeEvent event = new SanPhamRealtimeEvent(
                UUID.randomUUID().toString(), loaiSuKien, Instant.now());

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    phatEvent(event, loaiSuKien);
                }
            });
            return;
        }

        phatEvent(event, loaiSuKien);
    }

    private void phatEvent(SanPhamRealtimeEvent event, String loaiSuKien) {
        broker.publish(event);
        try {
            webSocketNotificationService.sendToTopic("/topic/admin/san-pham", loaiSuKien, event);
            webSocketNotificationService.sendToTopic("/topic/admin/pos-sync", "PRODUCT_CHANGED", event);
        } catch (Exception ignored) {
        }
    }
}
