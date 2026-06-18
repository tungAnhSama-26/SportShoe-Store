package com.example.server.core.admin.quanLyDanhMuc.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DanhMucRealtimeAspect {

    private final SimpMessagingTemplate messagingTemplate;

    public DanhMucRealtimeAspect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @AfterReturning("execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.tao*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.capNhat*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.doiTrangThai*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLyDanhMuc.*.service.*Service.xoa*(..))")
    public void afterDanhMucModified() {
        messagingTemplate.convertAndSend("/topic/admin/thuoc-tinh", "Cập nhật thuộc tính");
    }
}
