package com.example.server.core.realtime.sanpham;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Phát sự kiện thay đổi catalog. Phát SAU khi commit để giỏ đồng bộ đúng dữ liệu mới. */
@Component
public class SanPhamRealtimePublisher {

    private final SanPhamRealtimeBroker broker;

    public SanPhamRealtimePublisher(SanPhamRealtimeBroker broker) {
        this.broker = broker;
    }

    public void phatSauCommit(String loaiSuKien) {
        SanPhamRealtimeEvent event = new SanPhamRealtimeEvent(
                UUID.randomUUID().toString(), loaiSuKien, Instant.now());

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    broker.publish(event);
                }
            });
            return;
        }

        broker.publish(event);
    }
}
