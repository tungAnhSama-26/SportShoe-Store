package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminQuickQueryServiceTest {

    private Function<AdminRevenueRequest, String> revenueTool;
    private Function<AdminLowStockRequest, String> lowStockTool;
    private Function<BestSellerRequest, String> bestSellerTool;
    private Function<AdminTopReviewsRequest, String> topReviewsTool;
    private Function<SearchRequest, List<ProductDto>> productSearchTool;
    private Function<AdminInvoiceSearchRequest, List<InvoiceDto>> invoiceSearchTool;
    private Function<AdminInvoiceCountRequest, Long> invoiceCountTool;
    private Function<AdminProductReviewRequest, String> productReviewsTool;
    private Function<AdminChartDataRequest, String> chartTool;
    private Function<AdminCsvExportRequest, String> csvTool;
    private Function<AdminOrderUpdateRequest, String> orderUpdateTool;
    private Function<AdminProductStockUpdateRequest, String> stockUpdateTool;
    private Function<AdminVoucherCreateRequest, String> voucherTool;
    private AdminQuickQueryService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        revenueTool = mock(Function.class);
        lowStockTool = mock(Function.class);
        bestSellerTool = mock(Function.class);
        topReviewsTool = mock(Function.class);
        productSearchTool = mock(Function.class);
        invoiceSearchTool = mock(Function.class);
        invoiceCountTool = mock(Function.class);
        productReviewsTool = mock(Function.class);
        chartTool = mock(Function.class);
        csvTool = mock(Function.class);
        orderUpdateTool = mock(Function.class);
        stockUpdateTool = mock(Function.class);
        voucherTool = mock(Function.class);
        service = new AdminQuickQueryService(revenueTool, lowStockTool, bestSellerTool, topReviewsTool,
                productSearchTool, invoiceSearchTool, invoiceCountTool, productReviewsTool, chartTool, csvTool,
                orderUpdateTool, stockUpdateTool, voucherTool);
    }

    @Test
    void routesTodayRevenueDirectlyToDatabaseTool() {
        when(revenueTool.apply(any())).thenReturn("doanh thu DB");

        assertEquals("doanh thu DB", service.answer("Thống kê doanh thu hôm nay").orElseThrow());
        verify(revenueTool).apply(new AdminRevenueRequest("today"));
        verifyNoInteractions(lowStockTool, bestSellerTool, topReviewsTool);
    }

    @Test
    void routesLowStockDirectlyToDatabaseTool() {
        when(lowStockTool.apply(any())).thenReturn("card tồn kho DB");

        assertEquals("card tồn kho DB", service.answer("Sản phẩm sắp hết hàng").orElseThrow());
        verify(lowStockTool).apply(new AdminLowStockRequest(5));
        verifyNoInteractions(revenueTool, bestSellerTool, topReviewsTool);
    }

    @Test
    void routesBestSellersDirectlyToAdminCardTool() {
        when(bestSellerTool.apply(any())).thenReturn("card bán chạy DB");

        assertEquals("card bán chạy DB", service.answer("Sản phẩm bán chạy nhất").orElseThrow());
        verify(bestSellerTool).apply(new BestSellerRequest());
        verifyNoInteractions(revenueTool, lowStockTool, topReviewsTool);
    }

    @Test
    void routesTopReviewsDirectlyToDatabaseTool() {
        when(topReviewsTool.apply(any())).thenReturn("đánh giá DB");

        assertEquals("đánh giá DB", service.answer("Lấy top 5 sản phẩm có điểm đánh giá cao nhất và thấp nhất").orElseThrow());
        verify(topReviewsTool).apply(new AdminTopReviewsRequest());
        verifyNoInteractions(revenueTool, lowStockTool, bestSellerTool);
    }

    @Test
    void keepsFreeFormAdminQuestionsForTheAiFlow() {
        assertTrue(service.answer("Hãy giải thích cách vận hành cửa hàng").isEmpty());
        verifyNoInteractions(revenueTool, lowStockTool, bestSellerTool, topReviewsTool);
    }

    @Test
    void prioritizesCsvBeforeLowStockCards() {
        when(csvTool.apply(any())).thenReturn("[Tải file](/download)");

        assertEquals("[Tải file](/download)", service.answer("Xuất CSV sản phẩm sắp hết hàng").orElseThrow());
        verify(csvTool).apply(new AdminCsvExportRequest("low_stock"));
        verifyNoInteractions(lowStockTool);
    }

    @Test
    void wrapsDatabaseChartJsonWithoutAiRewriting() {
        when(chartTool.apply(any())).thenReturn("{\"chartType\":\"line\",\"labels\":[],\"data\":[]}");

        String answer = service.answer("Vẽ biểu đồ doanh thu 7 ngày").orElseThrow();

        assertTrue(answer.contains("```chart"));
        assertTrue(answer.contains("\"chartType\":\"line\""));
        verify(chartTool).apply(new AdminChartDataRequest("revenue_7_days"));
    }

    @Test
    void formatsOnlyRealInvoiceDtos() {
        when(invoiceSearchTool.apply(any())).thenReturn(List.of(
                new InvoiceDto(12, "HD0012", "Nguyễn Văn A", "0900000000",
                        new BigDecimal("7100000"), "Hoàn thành", "21/08/2026 10:00")));

        String answer = service.answer("Tìm hóa đơn HD0012").orElseThrow();

        assertTrue(answer.contains("HD0012"));
        assertTrue(answer.contains("Nguyễn Văn A"));
        assertTrue(answer.contains("/admin/hoa-don/12"));
        verify(invoiceSearchTool).apply(new AdminInvoiceSearchRequest("HD0012", null));
    }

    @Test
    void executesStockUpdateOnlyForConfirmedFrontendCommand() {
        when(stockUpdateTool.apply(any())).thenReturn("đã cập nhật");

        assertEquals("đã cập nhật", service.answer("/execute-update-stock Hoka Clifton 8|44|Xanh dương|5").orElseThrow());
        verify(stockUpdateTool).apply(new AdminProductStockUpdateRequest("Hoka Clifton 8", 44, "Xanh dương", 5));
    }

    @Test
    void createsStockConfirmationLinkWithoutWritingData() {
        String answer = service.answer(
                "Cập nhật số lượng sản phẩm Hoka Clifton 8 size 44 màu Xanh dương thành 5").orElseThrow();

        assertTrue(answer.contains("/action/update-stock/Hoka%20Clifton%208/44/Xanh%20d%C6%B0%C6%A1ng/5"));
        verifyNoInteractions(stockUpdateTool);
    }

    @Test
    void createsVoucherConfirmationLinkWithoutWritingData() {
        String answer = service.answer("Tạo mã giảm giá SPORT20 tên Khuyến mãi hè giảm 20% "
                + "cho đơn từ 500.000 giảm tối đa 100.000 số lượng 50 trong 30 ngày").orElseThrow();

        assertTrue(answer.contains("/action/create-voucher/SPORT20/Khuy%E1%BA%BFn%20m%C3%A3i%20h%C3%A8/1/20/500000/100000/50/30"));
        verifyNoInteractions(voucherTool);
    }
}
