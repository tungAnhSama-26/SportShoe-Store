package com.example.server.core.admin.banHangTaiQuay.service;

import com.example.server.core.admin.banHangTaiQuay.dto.response.HoaDonChoChiTietResponse;
import com.example.server.core.admin.quanlyhoadon.service.GhnShippingService;
import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BanHangTaiQuayServiceRealtimeTest {

    @Mock private KhachHangTaiQuayService customerUseCase;
    @Mock private SanPhamTaiQuayService productUseCase;
    @Mock private PhieuGiamGiaTaiQuayService voucherUseCase;
    @Mock private HoaDonTaiQuayService invoiceUseCase;
    @Mock private HoaDonChoTaiQuayService pendingInvoiceUseCase;
    @Mock private ThucThiThanhToanTaiQuayService paymentExecutionUseCase;
    @Mock private GhnShippingService ghnShippingService;
    @Mock private WebSocketNotificationService webSocketNotificationService;

    private BanHangTaiQuayService service;

    @BeforeEach
    void setUp() {
        service = new BanHangTaiQuayService(
                customerUseCase,
                productUseCase,
                voucherUseCase,
                invoiceUseCase,
                ghnShippingService,
                webSocketNotificationService,
                pendingInvoiceUseCase,
                paymentExecutionUseCase
        );
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void capNhatHoaDonChoChiPhatRealtimeSauKhiCommit() {
        int invoiceId = 25;
        HoaDonChoChiTietResponse response = new HoaDonChoChiTietResponse(
                invoiceId, "HD25", null, null, null, null,
                null, null, null, null, null, List.of()
        );
        when(pendingInvoiceUseCase.capNhatHoaDonCho(invoiceId, null)).thenReturn(response);

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        service.capNhatHoaDonCho(invoiceId, null);

        verify(webSocketNotificationService, never()).sendToTopic(anyString(), anyString(), any());

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        verify(webSocketNotificationService).sendToTopic(
                "/topic/admin/pos-sync",
                "POS_INVOICE_CHANGED",
                java.util.Map.of("action", "UPDATED", "invoiceId", invoiceId)
        );
    }
}
