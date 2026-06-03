package com.example.server.core.admin.nhanVien.service;

import com.example.server.core.admin.nhanVien.dto.request.CapNhatNhanVienRequest;
import com.example.server.core.admin.nhanVien.dto.request.TaoNhanVienRequest;
import com.example.server.core.admin.nhanVien.service.impl.NhanVienServiceImpl;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.infrastructure.security.PasswordService;
import com.example.server.infrastructure.service.EmailService;
import com.example.server.repository.NhanVienRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class NhanVienServiceRoleTest {

    private final NhanVienRepository nhanVienRepository = mock(NhanVienRepository.class);
    private final EmailService emailService = mock(EmailService.class);
    private final PasswordService passwordService = new PasswordService();
    private final NhanVienServiceImpl service = new NhanVienServiceImpl(nhanVienRepository, emailService, passwordService);

    @Test
    void cannotCreateEmployeeWithWarehouseRole() {
        assertThrows(BusinessException.class, () -> service.taoNhanVien(createRequest(3)));
    }

    @Test
    void cannotUpdateEmployeeWithWarehouseRole() {
        assertThrows(BusinessException.class, () -> service.capNhatNhanVien(UUID.randomUUID(), updateRequest(3)));
    }

    private TaoNhanVienRequest createRequest(Integer vaiTro) {
        return new TaoNhanVienRequest(
                "Nhan Vien",
                "staff@example.com",
                "0987654321",
                "001086000001",
                "Nam",
                LocalDate.of(2000, 1, 1),
                "Ha Noi",
                null,
                vaiTro
        );
    }

    private CapNhatNhanVienRequest updateRequest(Integer vaiTro) {
        return new CapNhatNhanVienRequest(
                "Nhan Vien",
                "staff",
                "staff@example.com",
                "0987654321",
                "001086000001",
                "Nam",
                LocalDate.of(2000, 1, 1),
                "Ha Noi",
                null,
                vaiTro
        );
    }
}
