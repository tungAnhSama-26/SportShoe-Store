package com.example.server.core.client.chatbot.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ChatbotIntentRouterTest {

    private final ChatbotIntentRouter router = new ChatbotIntentRouter();

    @Test
    void reviewRankingPromptOnlyReceivesReviewTools() {
        assertArrayEquals(
                new String[]{"get_admin_product_reviews_tool", "get_admin_top_reviews_tool"},
                router.resolveAdminTools("Thống kê những sản phẩm được đánh giá tốt nhất và tệ nhất")
        );
    }

    @Test
    void revenuePromptStillReceivesRevenueTools() {
        var tools = Arrays.asList(router.resolveAdminTools("Báo cáo doanh thu hôm nay"));
        assertTrue(tools.contains("get_admin_revenue_stats_tool"));
        assertTrue(tools.contains("export_admin_data_csv_tool"));
        assertFalse(tools.contains("get_admin_top_reviews_tool"));
    }

    @Test
    void bestSellerPromptStillReceivesProductTools() {
        var tools = Arrays.asList(router.resolveAdminTools("Sản phẩm bán chạy nhất"));
        assertTrue(tools.contains("search_products_tool"));
        assertTrue(tools.contains("get_best_selling_shoes_tool"));
    }

    @Test
    void explicitCombinedPromptReceivesBothRelevantGroups() {
        var tools = Arrays.asList(router.resolveAdminTools("So sánh doanh thu và đánh giá khách hàng"));
        assertTrue(tools.contains("get_admin_revenue_stats_tool"));
        assertTrue(tools.contains("get_admin_top_reviews_tool"));
    }
}
