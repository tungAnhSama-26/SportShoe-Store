package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.ProductDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.CouponDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.InvoiceDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.PromotionDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.SearchRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientQuickQueryServiceTest {

    @Test
    void bestSellerUsesDatabaseFunctionDirectly() {
        AtomicInteger calls = new AtomicInteger();
        ClientQuickQueryService service = service(
                request -> {
                    calls.incrementAndGet();
                    return "Danh sách thật\n[Hoka Clifton 8](/khachhang/san-pham/18)";
                });

        String answer = service.answerFromDatabase("Xem các mẫu giày bán chạy và hot nhất")
                .orElseThrow();

        assertEquals(1, calls.get());
        assertTrue(answer.contains("/khachhang/san-pham/18"));
    }

    @Test
    void promotionAndCouponAreFormattedFromDatabaseDtos() {
        ClientQuickQueryService service = new ClientQuickQueryService(
                request -> "unused",
                request -> List.of(new CouponDto(
                        1, "GIAM50", "Voucher tháng 8", "Tiền mặt", "Công khai",
                        BigDecimal.valueOf(50000), BigDecimal.valueOf(500000), BigDecimal.valueOf(50000),
                        "01/08/2026", "31/08/2026", 20, 2, "Hoạt động")),
                request -> List.of(new PromotionDto(
                        2, "SALE20", "Sale giày chạy", "", "Phần trăm",
                        BigDecimal.valueOf(20), "01/08/2026", "31/08/2026", "Hoạt động")),
                request -> null,
                request -> List.of());

        String answer = service.answerFromDatabase("Cửa hàng đang có đợt giảm giá nào không?")
                .orElseThrow();

        assertTrue(answer.contains("SALE20"));
        assertTrue(answer.contains("Giảm 20%"));
        assertTrue(answer.contains("GIAM50"));
        assertEquals(2, answer.split("```offer", -1).length - 1);
        assertTrue(answer.contains("\"type\":\"promotion\""));
        assertTrue(answer.contains("\"type\":\"coupon\""));
    }

    @Test
    void privateCouponIsNotRenderedInCustomerOfferCards() {
        ClientQuickQueryService service = new ClientQuickQueryService(
                request -> "unused",
                request -> List.of(new CouponDto(
                        3, "PRIVATE50", "Voucher cá nhân", "Phần trăm", "Cá nhân",
                        BigDecimal.valueOf(50), BigDecimal.ZERO, BigDecimal.valueOf(100000),
                        "01/08/2026", "31/08/2026", 1, 0, "Hoạt động")),
                request -> List.of(new PromotionDto(
                        2, "SALE10", "Sale công khai", "", "Phần trăm",
                        BigDecimal.TEN, "01/08/2026", "31/08/2026", "Hoạt động")),
                request -> null,
                request -> List.of());

        String answer = service.answerFromDatabase("Có voucher nào không?").orElseThrow();

        assertFalse(answer.contains("PRIVATE50"));
        assertTrue(answer.contains("SALE10"));
    }

    @Test
    void productSearchPassesRealFiltersAndReturnsVerifiedLink() {
        AtomicReference<SearchRequest> captured = new AtomicReference<>();
        ProductDto product = new ProductDto(
                18, "SP18", "Hoka Clifton 8", "", null, BigDecimal.valueOf(3700000),
                List.of("Đen"), List.of("43"), 4L, 10L);
        ClientQuickQueryService service = new ClientQuickQueryService(
                request -> "unused", request -> List.of(), request -> List.of(), request -> null,
                request -> {
                    captured.set(request);
                    return List.of(product);
                });

        String answer = service.answerFromDatabase("Tôi muốn mua giày Hoka màu đen size 43")
                .orElseThrow();

        assertEquals("Hoka", captured.get().brand());
        assertEquals("đen", captured.get().color());
        assertEquals("43", captured.get().size());
        assertTrue(answer.contains("[Hoka Clifton 8](/khachhang/san-pham/18)"));
    }

    @Test
    void missingInvoiceNeverFallsThroughToAi() {
        ClientQuickQueryService service = service(request -> "unused");

        String answer = service.answerFromDatabase("Kiểm tra đơn hàng HD99999")
                .orElseThrow();

        assertTrue(answer.contains("không tìm thấy"));
        assertTrue(answer.contains("HD99999"));
    }

    @Test
    void freeSizeAdviceIsNotClaimedAsDatabaseQuery() {
        ClientQuickQueryService service = service(request -> "unused");

        assertTrue(service.answerFromDatabase("Tôi muốn được tư vấn chọn size giày").isEmpty());
    }

    private ClientQuickQueryService service(
            java.util.function.Function<com.example.server.core.client.chatbot.tools.ChatbotTools.BestSellerRequest, String> bestSeller) {
        return new ClientQuickQueryService(
                bestSeller, request -> List.of(), request -> List.of(), request -> null, request -> List.of());
    }
}
