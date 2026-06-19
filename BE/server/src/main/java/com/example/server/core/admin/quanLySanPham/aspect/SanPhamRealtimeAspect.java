package com.example.server.core.admin.quanLySanPham.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SanPhamRealtimeAspect {

    private final SimpMessagingTemplate messagingTemplate;

    public SanPhamRealtimeAspect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @AfterReturning("execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.tao*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.capNhat*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.doiTrangThai*(..)) || " +
                    "execution(* com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService.xoa*(..))")
    public void afterSanPhamModified() {
        messagingTemplate.convertAndSend("/topic/admin/san-pham", "Cập nhật sản phẩm");
    }
}
