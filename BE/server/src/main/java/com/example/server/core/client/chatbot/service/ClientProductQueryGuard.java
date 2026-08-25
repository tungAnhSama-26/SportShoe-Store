package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.ProductDto;
import com.example.server.core.client.chatbot.dto.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientProductQueryGuard {

    private static final Pattern SIZE_PATTERN = Pattern.compile(
            "(?iu)\\b(?:size|cỡ)\\s*[:#-]?\\s*(\\d{2})\\b");
    private static final Pattern COLOR_PATTERN = Pattern.compile(
            "(?iu)\\bmàu\\s+([\\p{L}-]+(?:\\s+[\\p{L}-]+){0,2}?)(?=\\s+(?:size|cỡ)\\b|[,.!?]|$)");

    private final Function<SearchRequest, List<ProductDto>> searchProducts;

    public ClientProductQueryGuard(
            @Qualifier("search_products_tool") Function<SearchRequest, List<ProductDto>> searchProducts) {
        this.searchProducts = searchProducts;
    }

    public Optional<String> answerFromDatabase(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        boolean productIntent = normalized.contains("giày")
                || normalized.contains("sản phẩm")
                || normalized.contains("đôi")
                || normalized.contains("mua");
        if (!productIntent) {
            return Optional.empty();
        }

        String size = extract(SIZE_PATTERN, message);
        String color = extract(COLOR_PATTERN, message);
        if (size == null && color == null) {
            return Optional.empty();
        }

        List<ProductDto> products = searchProducts.apply(
                new SearchRequest(null, color, null, null, size, false));
        String filterDescription = describeFilters(color, size);
        if (products == null || products.isEmpty()) {
            return Optional.of("Hiện cửa hàng chưa có sản phẩm " + filterDescription
                    + ". Bạn có muốn chọn màu hoặc size khác không ạ?");
        }

        StringBuilder reply = new StringBuilder("Mình tìm thấy các sản phẩm ")
                .append(filterDescription)
                .append(" đang có tại cửa hàng:\n");
        products.stream().limit(5).forEach(product -> reply
                .append("- [")
                .append(product.ten())
                .append("](/khachhang/san-pham/")
                .append(product.id())
                .append(")\n"));
        reply.append("Bạn có thể bấm vào sản phẩm để xem giá và biến thể còn hàng nhé!");
        return Optional.of(reply.toString());
    }

    private String extract(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String describeFilters(String color, String size) {
        StringBuilder result = new StringBuilder("phù hợp");
        if (color != null) {
            result.append(" màu ").append(color);
        }
        if (size != null) {
            result.append(" size ").append(size);
        }
        return result.toString();
    }
}
