package com.example.server.core.admin.quanLyDanhMuc.aspect;

import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DanhMucRealtimeAspect {

    private final WebSocketNotificationService webSocketNotificationService;

    public DanhMucRealtimeAspect(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @AfterReturning("execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.tao*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.capNhat*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.doiTrangThai*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.xoa*(..))")
    public void afterDanhMucModified() {
        webSocketNotificationService.sendToTopic("/topic/admin/thuoc-tinh", "ATTRIBUTE_UPDATED", "Cập nhật thuộc tính");
    }
}
