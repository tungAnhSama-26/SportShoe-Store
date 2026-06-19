package com.example.server.core.admin.banHangTaiQuay.service.usecase;

import com.example.server.core.admin.banHangTaiQuay.dto.request.TaoHoaDonChoItemRequest;
import com.example.server.infrastructure.exception.BusinessException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BanHangTaiQuayValidationUseCase {

    public void validateDuplicateItems(List<TaoHoaDonChoItemRequest> items) {
        // Removed validation to allow same chiTietId with different prices
    }
}
