package com.example.server.core.admin.nhanVien.event;

import com.example.server.infrastructure.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NhanVienAccountEmailListener {

    private final EmailService emailService;

    public NhanVienAccountEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void guiEmailSauKhiTaoNhanVien(NhanVienAccountCreatedEvent event) {
        emailService.trySendRegistrationEmail(
                event.email(),
                event.hoTen(),
                event.tenDangNhap(),
                event.matKhauTamThoi()
        );
    }
}
