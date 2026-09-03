package com.example.server.core.client.sepay.controller;

import com.example.server.core.admin.banHangTaiQuay.service.BanHangTaiQuayService;
import com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService;
import com.example.server.core.client.vnpay.service.ClientVnPayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientSePayControllerTest {

    @Mock private ClientVnPayService clientVnPayService;
    @Mock private BanHangTaiQuayService banHangTaiQuayService;
    @Mock private QuanLyHoaDonService quanLyHoaDonService;

    private ClientSePayController controller;
    private final String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        controller = new ClientSePayController(
                clientVnPayService,
                banHangTaiQuayService,
                quanLyHoaDonService,
                apiKey
        );
    }

    @Test
    void webhook_saiApiKey_traVe401() {
        ResponseEntity<Map<String, Object>> response = controller.webhook(
                "Apikey sai-key",
                Map.of("transferType", "in")
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("success", false);
    }

    @Test
    void webhook_tienRa_tuDongXacNhanHoanTien() {
        when(quanLyHoaDonService.xacNhanHoanTienTuDongSePay("HOAN TIEN DON SHOEHTHD001", 500000L, "REF12345"))
                .thenReturn("HD001");

        Map<String, Object> body = Map.of(
                "transferType", "out",
                "content", "HOAN TIEN DON SHOEHTHD001",
                "transferAmount", 500000L,
                "referenceCode", "REF12345"
        );

        ResponseEntity<Map<String, Object>> response = controller.webhook(
                "Apikey test-api-key",
                body
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("success", true);
        assertThat(response.getBody()).containsEntry("orderCode", "HD001");
        assertThat(response.getBody()).containsEntry("type", "REFUND");
        verify(quanLyHoaDonService).xacNhanHoanTienTuDongSePay("HOAN TIEN DON SHOEHTHD001", 500000L, "REF12345");
    }
}
