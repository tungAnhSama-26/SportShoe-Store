package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ClientQuickQueryService {

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private static final Pattern INVOICE_CODE = Pattern.compile(
            "(?iu)(?:^|\\s|#)((?:HD|HĐ)[A-Z0-9-]{2,})(?=$|\\s|[.,!?])");
    private static final Pattern SIZE = Pattern.compile("(?iu)\\b(?:size|cỡ)\\s*[:#-]?\\s*(\\d{2})\\b");
    private static final Pattern COLOR = Pattern.compile(
            "(?iu)\\bmàu\\s+([\\p{L}-]+(?:\\s+[\\p{L}-]+){0,2}?)(?=\\s+(?:size|cỡ)\\b|[,.!?]|$)");
    private static final List<String> COMMON_BRANDS = List.of(
            "Nike", "Adidas", "Hoka", "Brooks", "Puma", "Converse", "Vans", "Asics", "New Balance");

    private final Function<BestSellerRequest, String> bestSellers;
    private final Function<CouponSearchRequest, List<CouponDto>> coupons;
    private final Function<PromotionSearchRequest, List<PromotionDto>> promotions;
    private final Function<InvoiceSearchRequest, InvoiceDto> invoices;
    private final Function<SearchRequest, List<ProductDto>> products;

    public ClientQuickQueryService(
            @Qualifier("get_best_selling_shoes_tool") Function<BestSellerRequest, String> bestSellers,
            @Qualifier("search_coupons_tool") Function<CouponSearchRequest, List<CouponDto>> coupons,
            @Qualifier("search_promotions_tool") Function<PromotionSearchRequest, List<PromotionDto>> promotions,
            @Qualifier("search_invoice_tool") Function<InvoiceSearchRequest, InvoiceDto> invoices,
            @Qualifier("search_products_tool") Function<SearchRequest, List<ProductDto>> products) {
        this.bestSellers = bestSellers;
        this.coupons = coupons;
        this.promotions = promotions;
        this.invoices = invoices;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public Optional<String> answerFromDatabase(String message) {
        if (message == null || message.isBlank()) return Optional.empty();

        String query = message.toLowerCase(Locale.ROOT).trim();
        if (isBestSellerQuery(query)) {
            return Optional.of(normalizeBestSellerAnswer(bestSellers.apply(new BestSellerRequest())));
        }
        if (isDiscountQuery(query)) {
            return Optional.of(formatDiscounts(
                    coupons.apply(new CouponSearchRequest("")),
                    promotions.apply(new PromotionSearchRequest(""))));
        }
        if (isInvoiceQuery(query)) return Optional.of(formatInvoiceQuery(message));
        if (isProductQuery(query)) return Optional.of(formatProducts(message));
        return Optional.empty();
    }

    private boolean isBestSellerQuery(String query) {
        return query.contains("bán chạy") || query.contains("ban chay")
                || query.contains("best seller") || query.contains("bestseller")
                || (query.contains("giày") && query.contains("hot"));
    }

    private boolean isDiscountQuery(String query) {
        return query.contains("voucher") || query.contains("coupon")
                || query.contains("mã giảm") || query.contains("ma giam")
                || query.contains("khuyến mãi") || query.contains("khuyen mai")
                || query.contains("giảm giá") || query.contains("giam gia")
                || query.contains("ưu đãi") || query.contains("uu dai")
                || query.matches(".*\\bsale\\b.*");
    }

    private boolean isInvoiceQuery(String query) {
        return query.contains("hóa đơn") || query.contains("hoa don")
                || query.contains("đơn hàng") || query.contains("don hang")
                || query.contains("tra cứu đơn") || query.contains("tra cuu don")
                || INVOICE_CODE.matcher(query).find();
    }

    private boolean isProductQuery(String query) {
        boolean mentionsProduct = query.contains("giày") || query.contains("sản phẩm")
                || query.contains("đôi giày") || query.matches(".*\\b(?:nike|adidas|hoka|brooks|puma|converse|vans|asics)\\b.*");
        boolean asksToFind = query.contains("tìm") || query.contains("mua") || query.contains("xem")
                || query.contains("có ") || query.contains("còn ") || query.contains("gợi ý")
                || query.contains("giới thiệu") || SIZE.matcher(query).find() || COLOR.matcher(query).find();
        boolean onlySizeAdvice = (query.contains("tư vấn") || query.contains("hướng dẫn") || query.contains("đo chân"))
                && query.contains("size") && !query.contains("mua") && !query.contains("tìm");
        return mentionsProduct && asksToFind && !onlySizeAdvice;
    }

    private String normalizeBestSellerAnswer(String answer) {
        if (answer == null || answer.isBlank() || answer.startsWith("Không thể")) {
            return "Hiện cửa hàng chưa có dữ liệu sản phẩm bán chạy. Bạn có muốn xem các sản phẩm đang còn hàng không ạ?";
        }
        return answer;
    }

    private String formatDiscounts(List<CouponDto> couponList, List<PromotionDto> promotionList) {
        List<CouponDto> safeCoupons = couponList == null ? List.of() : couponList.stream()
                .filter(coupon -> coupon != null && "Công khai".equalsIgnoreCase(coupon.loaiPhieuText()))
                .toList();
        List<PromotionDto> safePromotions = promotionList == null ? List.of() : promotionList;
        if (safeCoupons.isEmpty() && safePromotions.isEmpty()) {
            return "Hiện cửa hàng chưa có voucher hoặc chương trình giảm giá đang áp dụng.";
        }

        StringBuilder reply = new StringBuilder("Ưu đãi hiện có tại SportShoe:\n");
        safePromotions.stream().limit(5).forEach(promotion -> appendOfferBlock(reply, new OfferCard(
                "promotion",
                "Đợt giảm giá",
                safe(promotion.ten()),
                safe(promotion.ma()),
                formatDiscount(promotion.loaiGiamText(), promotion.giaTriGiam()),
                "",
                "",
                optional(promotion.ngayBatDau()),
                optional(promotion.ngayKetThuc()),
                safe(promotion.trangThaiText())
        )));
        safeCoupons.stream().limit(5).forEach(coupon -> appendOfferBlock(reply, new OfferCard(
                "coupon",
                "Voucher",
                safe(coupon.ten()),
                safe(coupon.ma()),
                formatDiscount(coupon.loaiText(), coupon.giaTri()),
                coupon.giaTriToiThieu() == null ? "" : formatMoney(coupon.giaTriToiThieu()),
                coupon.giamToiDa() == null ? "" : formatMoney(coupon.giamToiDa()),
                optional(coupon.ngayBatDau()),
                optional(coupon.ngayKetThuc()),
                safe(coupon.trangThaiText())
        )));
        return reply.toString();
    }

    private void appendOfferBlock(StringBuilder reply, OfferCard offer) {
        try {
            reply.append("```offer\n")
                    .append(JSON_MAPPER.writeValueAsString(offer))
                    .append("\n```\n");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Không thể định dạng dữ liệu ưu đãi", e);
        }
    }

    private String formatInvoiceQuery(String message) {
        Matcher matcher = INVOICE_CODE.matcher(message);
        if (!matcher.find()) {
            return "Bạn vui lòng gửi mã đơn hàng hoặc mã hóa đơn (ví dụ: HD12345) để mình tra cứu trực tiếp nhé.";
        }

        String code = matcher.group(1);
        InvoiceDto invoice = invoices.apply(new InvoiceSearchRequest(code));
        if (invoice == null || invoice.id() == null) {
            return "Mình không tìm thấy đơn hàng có mã **" + safe(code) + "** trong cửa hàng. Bạn kiểm tra lại mã giúp mình nhé.";
        }

        return "Thông tin đơn hàng **" + safe(invoice.ma()) + "**:\n"
                + "- Trạng thái: **" + safe(invoice.trangThaiText()) + "**\n"
                + "- Người nhận: " + safe(invoice.tenNguoiNhan()) + "\n"
                + "- Tổng thanh toán: " + formatMoney(invoice.tongTienThanhToan()) + "\n"
                + "- Ngày lập: " + safe(invoice.ngayLap()) + "\n"
                + "[Xem chi tiết hóa đơn](/khachhang/don-hang/" + invoice.id() + ")";
    }

    private String formatProducts(String message) {
        String size = extract(SIZE, message);
        String color = extract(COLOR, message);
        String brand = COMMON_BRANDS.stream()
                .filter(value -> message.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
        List<ProductDto> matches = products.apply(new SearchRequest(null, color, null, brand, size, false));
        if (matches == null || matches.isEmpty()) {
            String filters = describeProductFilters(brand, color, size);
            return "Hiện cửa hàng không có sản phẩm" + filters
                    + " phù hợp. Bạn có muốn thử thương hiệu, màu hoặc size khác không ạ?";
        }

        StringBuilder reply = new StringBuilder("Mình tìm thấy các sản phẩm đang có tại cửa hàng:\n");
        matches.stream()
                .filter(product -> product != null && product.id() != null && product.id() > 0)
                .limit(5)
                .forEach(product -> reply.append("- [")
                        .append(safe(product.ten()))
                        .append("](/khachhang/san-pham/")
                        .append(product.id())
                        .append(")\n"));
        if (reply.toString().equals("Mình tìm thấy các sản phẩm đang có tại cửa hàng:\n")) {
            return "Hiện cửa hàng không có sản phẩm phù hợp.";
        }
        reply.append("Bạn bấm vào từng sản phẩm để xem giá, màu, size và số lượng còn lại nhé!");
        return reply.toString();
    }

    private String extract(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String describeProductFilters(String brand, String color, String size) {
        StringBuilder filters = new StringBuilder();
        if (brand != null) filters.append(" thương hiệu ").append(brand);
        if (color != null) filters.append(" màu ").append(color);
        if (size != null) filters.append(" size ").append(size);
        return filters.toString();
    }

    private String formatDiscount(String type, BigDecimal value) {
        if (value == null) return "Chưa cập nhật mức giảm";
        return type != null && type.toLowerCase(Locale.ROOT).contains("phần trăm")
                ? "Giảm " + value.stripTrailingZeros().toPlainString() + "%"
                : "Giảm " + formatMoney(value);
    }

    private String formatMoney(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
                .format(value == null ? BigDecimal.ZERO : value);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value.trim();
    }

    private String optional(String value) {
        return value == null ? "" : value.trim();
    }

    private record OfferCard(
            String type,
            String label,
            String name,
            String code,
            String discount,
            String minimumOrder,
            String maximumDiscount,
            String startDate,
            String endDate,
            String status
    ) {}
}
