package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.ProductDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminChartDataRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminCsvExportRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminInvoiceCountRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminInvoiceSearchRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminLowStockRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminOrderUpdateRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminProductReviewRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminProductStockUpdateRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminRevenueRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminTopReviewsRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.AdminVoucherCreateRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.BestSellerRequest;
import com.example.server.core.client.chatbot.tools.ChatbotTools.InvoiceDto;
import com.example.server.core.client.chatbot.tools.ChatbotTools.SearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdminQuickQueryService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern INVOICE_CODE = Pattern.compile("(?i)\\b(?:HD|INV)[A-Z0-9-]*\\d[A-Z0-9-]*\\b");
    private static final Pattern STOCK_PREVIEW = Pattern.compile(
            "(?iu)^.*?(?:sản\\s*phẩm|giày)\\s+(.+?)\\s+(?:size|kích\\s*cỡ)\\s*(\\d+)\\s+"
                    + "(?:màu)\\s+(.+?)\\s+(?:thành|là|còn)\\s*(\\d+).*$");

    private final Function<AdminRevenueRequest, String> revenueTool;
    private final Function<AdminLowStockRequest, String> lowStockTool;
    private final Function<BestSellerRequest, String> bestSellerTool;
    private final Function<AdminTopReviewsRequest, String> topReviewsTool;
    private final Function<SearchRequest, List<ProductDto>> productSearchTool;
    private final Function<AdminInvoiceSearchRequest, List<InvoiceDto>> invoiceSearchTool;
    private final Function<AdminInvoiceCountRequest, Long> invoiceCountTool;
    private final Function<AdminProductReviewRequest, String> productReviewsTool;
    private final Function<AdminChartDataRequest, String> chartTool;
    private final Function<AdminCsvExportRequest, String> csvTool;
    private final Function<AdminOrderUpdateRequest, String> orderUpdateTool;
    private final Function<AdminProductStockUpdateRequest, String> stockUpdateTool;
    private final Function<AdminVoucherCreateRequest, String> voucherTool;

    public AdminQuickQueryService(
            @Qualifier("get_admin_revenue_stats_tool") Function<AdminRevenueRequest, String> revenueTool,
            @Qualifier("get_admin_low_stock_tool") Function<AdminLowStockRequest, String> lowStockTool,
            @Qualifier("get_admin_best_selling_shoes_tool") Function<BestSellerRequest, String> bestSellerTool,
            @Qualifier("get_admin_top_reviews_tool") Function<AdminTopReviewsRequest, String> topReviewsTool,
            @Qualifier("search_products_tool") Function<SearchRequest, List<ProductDto>> productSearchTool,
            @Qualifier("search_admin_invoices_tool") Function<AdminInvoiceSearchRequest, List<InvoiceDto>> invoiceSearchTool,
            @Qualifier("count_admin_invoices_tool") Function<AdminInvoiceCountRequest, Long> invoiceCountTool,
            @Qualifier("get_admin_product_reviews_tool") Function<AdminProductReviewRequest, String> productReviewsTool,
            @Qualifier("get_admin_chart_data_tool") Function<AdminChartDataRequest, String> chartTool,
            @Qualifier("export_admin_data_csv_tool") Function<AdminCsvExportRequest, String> csvTool,
            @Qualifier("update_admin_order_status_tool") Function<AdminOrderUpdateRequest, String> orderUpdateTool,
            @Qualifier("update_admin_product_stock_tool") Function<AdminProductStockUpdateRequest, String> stockUpdateTool,
            @Qualifier("create_admin_voucher_tool") Function<AdminVoucherCreateRequest, String> voucherTool) {
        this.revenueTool = revenueTool;
        this.lowStockTool = lowStockTool;
        this.bestSellerTool = bestSellerTool;
        this.topReviewsTool = topReviewsTool;
        this.productSearchTool = productSearchTool;
        this.invoiceSearchTool = invoiceSearchTool;
        this.invoiceCountTool = invoiceCountTool;
        this.productReviewsTool = productReviewsTool;
        this.chartTool = chartTool;
        this.csvTool = csvTool;
        this.orderUpdateTool = orderUpdateTool;
        this.stockUpdateTool = stockUpdateTool;
        this.voucherTool = voucherTool;
    }

    public Optional<String> answer(String message) {
        String original = message == null ? "" : message.trim();
        String query = normalize(original);
        if (query.isBlank()) return Optional.empty();

        Optional<String> execution = executeConfirmedCommand(original);
        if (execution.isPresent()) return execution;
        if (isCsvRequest(query)) return Optional.of(csvTool.apply(new AdminCsvExportRequest(csvType(query))));
        if (isChartRequest(query)) return Optional.of(buildChart(chartType(query)));

        Optional<String> stockPreview = buildStockPreview(original, query);
        if (stockPreview.isPresent()) return stockPreview;
        Optional<String> voucherPreview = buildVoucherPreview(original, query);
        if (voucherPreview.isPresent()) return voucherPreview;

        Optional<String> orderPreview = buildOrderPreview(original, query);
        if (orderPreview.isPresent()) return orderPreview;
        if (isInvoiceRequest(query)) return Optional.of(searchInvoices(original, query));

        if (query.contains("danh gia")) {
            if (query.contains("cao nhat") || query.contains("thap nhat") || query.contains("tot nhat")) {
                return Optional.of(topReviewsTool.apply(new AdminTopReviewsRequest()));
            }
            String productName = extractProductKeyword(original);
            if (productName.isBlank()) return Optional.of("Vui lòng nhập tên sản phẩm cần xem đánh giá.");
            return Optional.of(productReviewsTool.apply(new AdminProductReviewRequest(null, productName)));
        }

        if (query.contains("san pham") && (query.contains("sap het hang") || query.contains("ton kho thap"))) {
            return Optional.of(lowStockTool.apply(new AdminLowStockRequest(5)));
        }
        if ((query.contains("san pham") || query.contains("giay")) && query.contains("ban chay")) {
            return Optional.of(bestSellerTool.apply(new BestSellerRequest()));
        }
        if (isProductSearch(query)) return Optional.of(searchProducts(extractProductKeyword(original)));

        if (query.contains("doanh thu")) {
            String period = query.contains("nam nay") ? "year" : query.contains("thang nay") ? "month" : "today";
            return Optional.of(revenueTool.apply(new AdminRevenueRequest(period)));
        }
        return Optional.empty();
    }

    private Optional<String> executeConfirmedCommand(String message) {
        if (message.startsWith("/execute-confirm-order ")) {
            String code = message.substring("/execute-confirm-order ".length()).trim();
            return Optional.of(code.isBlank() ? "Mã hóa đơn không hợp lệ."
                    : orderUpdateTool.apply(new AdminOrderUpdateRequest(code, "confirm")));
        }
        if (message.startsWith("/execute-cancel-order ")) {
            String code = message.substring("/execute-cancel-order ".length()).trim();
            return Optional.of(code.isBlank() ? "Mã hóa đơn không hợp lệ."
                    : orderUpdateTool.apply(new AdminOrderUpdateRequest(code, "cancel")));
        }
        if (message.startsWith("/execute-update-stock ")) {
            String[] parts = message.substring("/execute-update-stock ".length()).split("\\|", -1);
            if (parts.length != 4) return Optional.of("Thông tin cập nhật số lượng không hợp lệ.");
            try {
                int size = Integer.parseInt(parts[1].trim());
                int stock = Integer.parseInt(parts[3].trim());
                if (parts[0].isBlank() || parts[2].isBlank() || stock < 0) throw new IllegalArgumentException();
                return Optional.of(stockUpdateTool.apply(new AdminProductStockUpdateRequest(
                        parts[0].trim(), size, parts[2].trim(), stock)));
            } catch (RuntimeException exception) {
                return Optional.of("Thông tin cập nhật số lượng không hợp lệ.");
            }
        }
        if (message.startsWith("/execute-create-voucher ")) {
            String[] parts = message.substring("/execute-create-voucher ".length()).split("\\|", -1);
            if (parts.length != 8) return Optional.of("Thông tin mã giảm giá không hợp lệ.");
            try {
                AdminVoucherCreateRequest request = new AdminVoucherCreateRequest(
                        parts[0].trim(), parts[1].trim(), Integer.valueOf(parts[2].trim()),
                        new BigDecimal(parts[3].trim()), new BigDecimal(parts[4].trim()),
                        new BigDecimal(parts[5].trim()), Integer.valueOf(parts[6].trim()),
                        Integer.valueOf(parts[7].trim()));
                if (request.code().isBlank() || request.name().isBlank() || request.value().signum() <= 0
                        || request.quantity() <= 0 || request.durationDays() <= 0) throw new IllegalArgumentException();
                return Optional.of(voucherTool.apply(request));
            } catch (RuntimeException exception) {
                return Optional.of("Thông tin mã giảm giá không hợp lệ.");
            }
        }
        return Optional.empty();
    }

    private Optional<String> buildOrderPreview(String original, String query) {
        if (!isInvoiceRequest(query)) return Optional.empty();
        String code = extractInvoiceCode(original);
        if (code == null) return Optional.empty();
        if (query.contains("xac nhan") && !query.contains("da xac nhan")) {
            return Optional.of("Bạn muốn xác nhận đơn hàng **" + code + "**? "
                    + "[Đồng ý xác nhận](/action/confirm-order/" + url(code) + ")");
        }
        if (query.contains("huy") && !query.contains("da bi huy") && !query.contains("da huy")
                && !query.contains("bao nhieu")) {
            return Optional.of("Bạn muốn hủy đơn hàng **" + code + "**? "
                    + "[Đồng ý hủy](/action/cancel-order/" + url(code) + ")");
        }
        return Optional.empty();
    }

    private Optional<String> buildStockPreview(String original, String query) {
        if (!(query.contains("cap nhat") || query.contains("thay doi"))
                || !(query.contains("so luong") || query.contains("ton kho"))) return Optional.empty();
        Matcher matcher = STOCK_PREVIEW.matcher(original);
        if (!matcher.matches()) {
            return Optional.of("Vui lòng nhập theo mẫu: Cập nhật số lượng sản phẩm [tên] size [size] màu [màu] thành [số lượng].");
        }
        String product = matcher.group(1).trim();
        String size = matcher.group(2).trim();
        String color = matcher.group(3).trim();
        int stock;
        try {
            stock = Integer.parseInt(matcher.group(4));
        } catch (NumberFormatException exception) {
            return Optional.of("Số lượng mới không hợp lệ.");
        }
        if (stock < 0) return Optional.of("Số lượng mới không được nhỏ hơn 0.");
        String action = "/action/update-stock/" + path(product) + "/" + size + "/" + path(color) + "/" + stock;
        return Optional.of("Bạn muốn cập nhật **" + product + "** (Size **" + size + "**, Màu **" + color
                + "**) thành **" + stock + " đôi**? [Đồng ý cập nhật số lượng](" + action + ")");
    }

    private Optional<String> buildVoucherPreview(String original, String query) {
        if (!(query.contains("tao") && (query.contains("voucher") || query.contains("ma giam gia")))) {
            return Optional.empty();
        }
        String code = group(original, "(?iu)(?:mã\\s+giảm\\s+giá|voucher|code)\\s+([A-Z0-9_-]+)");
        String name = group(original, "(?iu)tên\\s+(.+?)\\s+(?:giảm|giá trị)");
        String valueText = group(original, "(?iu)giảm\\s+([0-9.,]+\\s*%?)");
        String minText = group(original, "(?iu)đơn\\s+(?:từ|tối\\s*thiểu)\\s+([0-9.,]+)");
        String maxText = group(original, "(?iu)(?:giảm\\s+)?tối\\s*đa\\s+([0-9.,]+)");
        String quantityText = group(original, "(?iu)số\\s*lượng\\s+([0-9]+)");
        String daysText = group(original, "(?iu)([0-9]+)\\s*ngày");
        if (code == null || name == null || valueText == null || minText == null
                || quantityText == null || daysText == null) {
            return Optional.of("Vui lòng nhập đủ: mã, tên, mức giảm, đơn tối thiểu, giảm tối đa (nếu giảm %), số lượng và số ngày hiệu lực.");
        }
        try {
            boolean percent = valueText.contains("%");
            BigDecimal value = money(valueText.replace("%", ""));
            BigDecimal minOrder = money(minText);
            BigDecimal maxDiscount = maxText == null ? BigDecimal.ZERO : money(maxText);
            int quantity = Integer.parseInt(quantityText);
            int days = Integer.parseInt(daysText);
            if (value.signum() <= 0 || minOrder.signum() < 0 || maxDiscount.signum() < 0 || quantity <= 0 || days <= 0) {
                throw new IllegalArgumentException();
            }
            int type = percent ? 1 : 2;
            String action = "/action/create-voucher/" + code.toUpperCase(Locale.ROOT) + "/" + path(name) + "/"
                    + type + "/" + number(value) + "/" + number(minOrder) + "/" + number(maxDiscount)
                    + "/" + quantity + "/" + days;
            return Optional.of("Bạn muốn tạo mã **" + code.toUpperCase(Locale.ROOT) + "** (" + name
                    + ")? [Đồng ý tạo mã](" + action + ")");
        } catch (RuntimeException exception) {
            return Optional.of("Thông tin mã giảm giá không hợp lệ; vui lòng kiểm tra lại các giá trị số.");
        }
    }

    private String searchInvoices(String original, String query) {
        String status = invoiceStatus(query);
        if (query.contains("bao nhieu") || query.startsWith("dem ")) {
            long count = invoiceCountTool.apply(new AdminInvoiceCountRequest(status));
            String label = "6".equals(status) ? "đã hủy" : "phù hợp";
            return "Hệ thống có **" + count + " hóa đơn " + label + "**.";
        }
        String code = extractInvoiceCode(original);
        List<InvoiceDto> invoices = invoiceSearchTool.apply(new AdminInvoiceSearchRequest(code, status));
        if (invoices == null || invoices.isEmpty()) return "Không tìm thấy hóa đơn phù hợp trong hệ thống.";
        StringBuilder result = new StringBuilder("Danh sách hóa đơn từ hệ thống:\n");
        for (InvoiceDto invoice : invoices) {
            result.append("\n**").append(safe(invoice.ma())).append("**")
                    .append("\n- Người nhận: ").append(safe(invoice.tenNguoiNhan()))
                    .append("\n- Số điện thoại: ").append(safe(invoice.sdtNguoiNhan()))
                    .append("\n- Tổng thanh toán: ").append(formatMoney(invoice.tongTienThanhToan()))
                    .append("\n- Trạng thái: ").append(safe(invoice.trangThaiText()))
                    .append("\n- Ngày lập: ").append(safe(invoice.ngayLap()));
            if (invoice.id() != null) result.append("\n[Xem chi tiết hóa đơn](/admin/hoa-don/").append(invoice.id()).append(")");
            result.append("\n");
        }
        return result.toString().trim();
    }

    private String searchProducts(String keyword) {
        if (keyword.isBlank()) return "Vui lòng nhập tên sản phẩm cần tìm.";
        List<ProductDto> products = productSearchTool.apply(new SearchRequest(keyword, null, null, null, null, null));
        if (products == null || products.isEmpty()) return "Không tìm thấy sản phẩm phù hợp trong cửa hàng.";
        StringBuilder result = new StringBuilder("Sản phẩm tìm thấy trong hệ thống:\n");
        for (ProductDto product : products) {
            result.append("```product\n{")
                    .append("\"name\":\"").append(json(product.ten())).append("\",")
                    .append("\"image\":\"").append(json(product.hinhAnh())).append("\",")
                    .append("\"price\":").append(number(product.giaBan())).append(",")
                    .append("\"originalPrice\":").append(number(product.giaBan())).append(",")
                    .append("\"color\":\"").append(json(join(product.mauSacs()))).append("\",")
                    .append("\"size\":\"").append(json(join(product.kichCos()))).append("\",")
                    .append("\"stock\":").append(product.soLuongTon() == null ? 0 : product.soLuongTon()).append(",")
                    .append("\"stockLabel\":\"Tổng số lượng\",")
                    .append("\"url\":\"/admin/san-pham?search=").append(json(url(product.ten()))).append("\"")
                    .append("}\n```\n");
        }
        return result.toString();
    }

    private String buildChart(String type) {
        String raw = chartTool.apply(new AdminChartDataRequest(type));
        try {
            JSON.readTree(raw);
            return "Dữ liệu biểu đồ từ hệ thống:\n```chart\n" + raw + "\n```";
        } catch (Exception exception) {
            return raw == null || raw.isBlank() ? "Không có dữ liệu để vẽ biểu đồ." : raw;
        }
    }

    private boolean isCsvRequest(String query) {
        return query.contains("csv") || query.contains("xuat file") || query.contains("tai file")
                || (query.contains("xuat") && query.contains("bao cao"));
    }

    private String csvType(String query) {
        if (query.contains("sap het") || query.contains("ton kho") || query.contains("so luong")) return "low_stock";
        if (query.contains("huy")) return "cancelled_invoices";
        return "revenue";
    }

    private boolean isChartRequest(String query) {
        return query.contains("bieu do") || query.contains("chart") || query.startsWith("ve bieu");
    }

    private String chartType(String query) {
        if (query.contains("ban chay") || query.contains("san pham")) return "top_selling_shoes";
        if (query.contains("trang thai") || query.contains("hoa don") || query.contains("don hang")) return "order_statuses";
        return "revenue_7_days";
    }

    private boolean isInvoiceRequest(String query) {
        return query.contains("hoa don") || query.contains("don hang") || query.matches(".*\\b(?:hd|inv)[a-z0-9-]*\\d.*");
    }

    private String invoiceStatus(String query) {
        if (query.contains("huy")) return "6";
        if (query.contains("hoan thanh")) return "5";
        return null;
    }

    private boolean isProductSearch(String query) {
        return query.contains("tim san pham") || query.contains("tim giay") || query.contains("tra cuu san pham")
                || query.startsWith("san pham ");
    }

    private String extractProductKeyword(String original) {
        String value = original.replaceFirst("(?iu)^.*?(?:đánh\\s*giá|tìm|tra\\s*cứu|xem)\\s+(?:của\\s+)?(?:sản\\s*phẩm|giày)\\s*", "");
        value = value.replaceFirst("(?iu)^(?:sản\\s*phẩm|giày)\\s+", "");
        return value.replaceAll("[?.!]+$", "").trim();
    }

    private String extractInvoiceCode(String original) {
        Matcher matcher = INVOICE_CODE.matcher(original);
        return matcher.find() ? matcher.group().toUpperCase(Locale.ROOT) : null;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D');
        return withoutAccents.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0 đ";
        return java.text.NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(value);
    }

    private String group(String value, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(value);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value.replace(".", "").replace(",", "").replaceAll("\\s+", ""));
    }

    private String number(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private String join(List<String> values) {
        return values == null ? "" : String.join(", ", values);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Chưa có" : value;
    }

    private String json(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", " ").replace("\n", " ");
    }

    private String url(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String path(String value) {
        return url(value).replace("+", "%20");
    }
}
