package com.example.server.core.client.chatbot.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ChatbotIntentRouter {

    public String[] resolveClientTools(String message) {
        if (message == null || message.isBlank()) {
            return new String[]{"search_products_tool", "get_best_selling_shoes_tool"};
        }

        String query = message.toLowerCase(Locale.ROOT).trim();
        List<String> tools = new ArrayList<>();

        boolean isProduct = query.contains("giày") || query.contains("sản phẩm") || query.contains("bán chạy") ||
                            query.contains("size") || query.contains("màu") || query.contains("giá") || query.contains("tìm");
        boolean isDiscount = query.contains("voucher") || query.contains("mã giảm") || query.contains("khuyến mãi") ||
                             query.contains("giảm giá") || query.contains("sale") || query.contains("ưu đãi");
        boolean isInvoice = query.contains("hóa đơn") || query.contains("đơn hàng") || query.contains("đơn mua") ||
                            query.contains("trạng thái") || query.contains("tra cứu");

        if (isProduct) {
            tools.add("search_products_tool");
            tools.add("get_best_selling_shoes_tool");
        }
        if (isDiscount) {
            tools.add("search_coupons_tool");
            tools.add("search_promotions_tool");
        }
        if (isInvoice) {
            tools.add("search_invoice_tool");
        }

        if (tools.isEmpty()) {
            tools.add("search_products_tool");
            tools.add("get_best_selling_shoes_tool");
            tools.add("search_coupons_tool");
            tools.add("search_promotions_tool");
            tools.add("search_invoice_tool");
        }

        return tools.toArray(new String[0]);
    }

    public String[] resolveAdminTools(String message) {
        if (message == null || message.isBlank()) {
            return new String[]{
                "get_admin_revenue_stats_tool", "get_admin_low_stock_tool",
                "search_admin_invoices_tool", "search_products_tool", "get_admin_best_selling_shoes_tool"
            };
        }

        String query = message.toLowerCase(Locale.ROOT).trim();
        List<String> tools = new ArrayList<>();

        boolean isRevenue = query.contains("doanh thu") || query.contains("doanh số") || query.contains("tiền") ||
                            query.contains("báo cáo bán hàng") || query.contains("csv");
        boolean isInventory = query.contains("tồn kho") || query.contains("hết hàng") || query.contains("cảnh báo") ||
                             query.contains("kho") || query.contains("số lượng") || query.contains("cập nhật tồn");
        boolean isInvoice = query.contains("hóa đơn") || query.contains("đơn hàng") || query.contains("xác nhận") ||
                            query.contains("hủy đơn") || query.contains("trạng thái");
        boolean isReview = query.contains("đánh giá") || query.contains("nhận xét") || query.contains("sao") || query.contains("review");
        boolean isProduct = query.contains("giày") || query.contains("bán chạy") || query.contains("best seller") ||
                            (query.contains("sản phẩm") && !isReview);
        boolean isVoucher = query.contains("voucher") || query.contains("mã giảm") || query.contains("tạo mã");
        boolean isChart = query.contains("vẽ") || query.contains("biểu đồ") || query.contains("chart");

        if (isRevenue) {
            tools.add("get_admin_revenue_stats_tool");
            tools.add("export_admin_data_csv_tool");
        }
        if (isInventory) {
            tools.add("get_admin_low_stock_tool");
            tools.add("update_admin_product_stock_tool");
            tools.add("export_admin_data_csv_tool");
        }
        if (isInvoice) {
            tools.add("search_admin_invoices_tool");
            tools.add("update_admin_order_status_tool");
            tools.add("export_admin_data_csv_tool");
        }
        if (isProduct) {
            tools.add("search_products_tool");
            tools.add("get_admin_best_selling_shoes_tool");
        }
        if (isReview) {
            tools.add("get_admin_product_reviews_tool");
            tools.add("get_admin_top_reviews_tool");
        }
        if (isVoucher) {
            tools.add("create_admin_voucher_tool");
        }
        if (isChart || tools.isEmpty()) {
            tools.add("get_admin_chart_data_tool");
        }

        if (tools.isEmpty()) {
            tools.add("get_admin_revenue_stats_tool");
            tools.add("get_admin_low_stock_tool");
            tools.add("search_admin_invoices_tool");
            tools.add("search_products_tool");
            tools.add("get_admin_best_selling_shoes_tool");
            tools.add("get_admin_chart_data_tool");
        }

        return tools.toArray(new String[0]);
    }
}
