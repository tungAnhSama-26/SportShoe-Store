package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.ProductDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.SearchRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientProductQueryGuardTest {

    @Test
    void reportsNoProductFromDatabaseForBlackSize43() {
        AtomicReference<SearchRequest> captured = new AtomicReference<>();
        Function<SearchRequest, List<ProductDto>> search = request -> {
            captured.set(request);
            return List.of();
        };

        String answer = new ClientProductQueryGuard(search)
                .answerFromDatabase("Tôi muốn mua một đôi giày màu đen size 43")
                .orElseThrow();

        assertEquals("đen", captured.get().color());
        assertEquals("43", captured.get().size());
        assertTrue(answer.contains("chưa có sản phẩm phù hợp màu đen size 43"));
    }

    @Test
    void returnsOnlyVerifiedProductLinks() {
        ProductDto product = new ProductDto(
                15, "SP15", "Giày đen thật", "", null, BigDecimal.valueOf(900000),
                List.of("Đen"), List.of("43"), 2L, 0L);

        String answer = new ClientProductQueryGuard(request -> List.of(product))
                .answerFromDatabase("Tìm giày size 43 màu đen")
                .orElseThrow();

        assertTrue(answer.contains("[Giày đen thật](/khachhang/san-pham/15)"));
    }
}
