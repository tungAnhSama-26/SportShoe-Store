package com.example.server.core.admin.quanLySanPham.aspect;

import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SanPhamRealtimeAspect {

    private final WebSocketNotificationService webSocketNotificationService;

    public SanPhamRealtimeAspect(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @AfterReturning("execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.tao*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.capNhat*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.doiTrangThai*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.xoa*(..))")
    public void afterSanPhamModified() {
        webSocketNotificationService.sendToTopic("/topic/admin/san-pham", "PRODUCT_UPDATED", "Cập nhật sản phẩm");
    }
}
