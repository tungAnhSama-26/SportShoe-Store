package com.example.server.core.client.chatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FaqRuleEngineTest {

    private FaqRuleEngine faqRuleEngine;

    @BeforeEach
    void setUp() {
        faqRuleEngine = new FaqRuleEngine();
    }

    @Test
    void matchFaq_SizeConsulting_ReturnsSizeGuide() {
        String query1 = "Tôi muốn được tư vấn chọn size giày";
        String answer1 = faqRuleEngine.matchFaq(query1);
        assertNotNull(answer1, "Nên khớp FAQ tư vấn size cho câu hỏi: " + query1);
        assertTrue(answer1.contains("Bảng quy đổi Size chuẩn") || answer1.contains("Hướng dẫn đo size"),
                "Nội dung phản hồi phải chứa hướng dẫn size");

        String query2 = "Tư vấn size giúp mình với";
        String answer2 = faqRuleEngine.matchFaq(query2);
        assertNotNull(answer2, "Nên khớp FAQ tư vấn size cho câu hỏi: " + query2);

        String query3 = "Hướng dẫn chọn size";
        String answer3 = faqRuleEngine.matchFaq(query3);
        assertNotNull(answer3, "Nên khớp FAQ tư vấn size cho câu hỏi: " + query3);
    }
}
