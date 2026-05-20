package com.example.server.core.admin.banHangTaiQuay.service.usecase;

import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.infrastructure.exception.BusinessException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BanHangTaiQuayValidationUseCase {

    public void validateDuplicateItems(List<TaoHoaDonChoItemRequest> items) {
        long distinctCount = items.stream()
                .map(TaoHoaDonChoItemRequest::chiTietId)
                .distinct()
                .count();
        if (distinctCount != items.size()) {
            throw new BusinessException("Moi san pham chi duoc xuat hien mot lan trong hoa don");
        }
    }
}
