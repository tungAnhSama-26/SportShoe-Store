package com.example.server.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClientResourceAuthorization {

    public void assertCanAccess(UUID customerId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Vui lòng đăng nhập để tiếp tục");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AdminPrincipal) {
            return;
        }
        if (principal instanceof CustomerPrincipal customerPrincipal
                && customerPrincipal.id().equals(customerId)) {
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền truy cập dữ liệu của khách hàng khác");
    }
}
