package com.example.server.core.admin.nhanVien.event;

import com.example.server.infrastructure.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NhanVienAccountEmailListenerTest {

    @Test
    void listenerChiChayBatDongBoSauKhiTransactionCommit() throws Exception {
        Method method = NhanVienAccountEmailListener.class.getMethod(
                "guiEmailSauKhiTaoNhanVien",
                NhanVienAccountCreatedEvent.class
        );

        assertThat(method.getAnnotation(Async.class)).isNotNull();
        assertThat(method.getAnnotation(TransactionalEventListener.class).phase())
                .isEqualTo(TransactionPhase.AFTER_COMMIT);
    }

    @Test
    void listenerGuiDungThongTinTaiKhoanNhanVien() {
        EmailService emailService = mock(EmailService.class);
        NhanVienAccountEmailListener listener = new NhanVienAccountEmailListener(emailService);
        NhanVienAccountCreatedEvent event = new NhanVienAccountCreatedEvent(
                "nhanvien@example.com",
                "Nguyen Van A",
                "nhanvien",
                "MatKhauTamThoi"
        );

        listener.guiEmailSauKhiTaoNhanVien(event);

        verify(emailService).trySendRegistrationEmail(
                "nhanvien@example.com",
                "Nguyen Van A",
                "nhanvien",
                "MatKhauTamThoi"
        );
        assertThat(event.toString()).doesNotContain("MatKhauTamThoi", "nhanvien@example.com");
    }
}
