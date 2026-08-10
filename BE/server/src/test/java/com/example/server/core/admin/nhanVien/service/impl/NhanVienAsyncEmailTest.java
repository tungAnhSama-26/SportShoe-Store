package com.example.server.core.admin.nhanVien.service.impl;

import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.event.NhanVienAccountCreatedEvent;
import com.example.server.entity.NhanVien;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.repository.NhanVienRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NhanVienAsyncEmailTest {

    @Test
    void taoNhanVienChiPhatSuKienEmailVaTraKetQuaNgay() {
        NhanVienRepository repository = mock(NhanVienRepository.class);
        PasswordService passwordService = mock(PasswordService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        NhanVienServiceImpl service = new NhanVienServiceImpl(
                repository,
                passwordService,
                eventPublisher
        );
        when(repository.findByTenDangNhapIgnoreCase("nhanvien")).thenReturn(Optional.empty());
        when(repository.save(any(NhanVien.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordService.hash(any(String.class))).thenReturn("hashed-password");

        var response = service.taoNhanVien(new TaoNhanVienRequest(
                "Nguyen Van A",
                "nhanvien@example.com",
                "0912345678",
                "Nam",
                LocalDate.of(2000, 1, 1),
                null,
                null,
                2,
                null
        ));

        ArgumentCaptor<NhanVienAccountCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(NhanVienAccountCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        NhanVienAccountCreatedEvent event = eventCaptor.getValue();

        assertThat(event.email()).isEqualTo("nhanvien@example.com");
        assertThat(event.tenDangNhap()).isEqualTo("nhanvien");
        assertThat(event.matKhauTamThoi()).isEqualTo(response.matKhauTamThoi());
        assertThat(response.emailDaGuiThanhCong()).isNull();
        assertThat(response.canhBaoEmail()).isNull();
    }
}
