package com.example.server.core.realtime.hoadon;

import com.example.server.entity.HoaDon;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class HoaDonRealtimePublisher {

    private final HoaDonRealtimeBroker broker;

    public HoaDonRealtimePublisher(HoaDonRealtimeBroker broker) {
        this.broker = broker;
    }

    public void publishAfterCommit(HoaDon hoaDon, String loaiSuKien) {
        HoaDonRealtimeEvent event = new HoaDonRealtimeEvent(
                UUID.randomUUID().toString(),
                hoaDon.getId(),
                hoaDon.getMa(),
                hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                hoaDon.getTrangThai(),
                loaiSuKien,
                Instant.now()
        );

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
