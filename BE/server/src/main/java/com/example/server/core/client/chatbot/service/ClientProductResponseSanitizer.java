package com.example.server.core.client.chatbot.service;

import com.example.server.entity.Giay;
import com.example.server.repository.GiayRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientProductResponseSanitizer {

    private static final Pattern PRODUCT_BLOCK = Pattern.compile(
            "(?is)```product\\s*.*?```");
    private static final Pattern PRODUCT_LINK = Pattern.compile(
            "(?i)\\[[^\\]]*]\\(/khachhang/san-pham/(\\d+)\\)");

    private final GiayRepository giayRepository;

    public ClientProductResponseSanitizer(GiayRepository giayRepository) {
        this.giayRepository = giayRepository;
    }

    public String sanitize(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }

        String withoutUntrustedBlocks = PRODUCT_BLOCK.matcher(response).replaceAll("");
        StringBuilder safe = new StringBuilder();
        for (String line : withoutUntrustedBlocks.split("\\R", -1)) {
            Matcher matcher = PRODUCT_LINK.matcher(line);
            Set<Integer> productIds = new LinkedHashSet<>();
            while (matcher.find()) {
                productIds.add(Integer.parseInt(matcher.group(1)));
            }

            if (productIds.isEmpty()) {
                safe.append(line).append('\n');
                continue;
            }

            for (Integer productId : productIds) {
                giayRepository.findById(productId)
                        .filter(product -> Integer.valueOf(1).equals(product.getTrangThai()))
                        .ifPresent(product -> appendVerifiedLink(safe, product));
            }
        }

        return safe.toString()
                .replaceAll("(?m)[ \\t]+$", "")
                .replaceAll("\\n{3,}", "\\n\\n")
                .trim();
    }

    private void appendVerifiedLink(StringBuilder output, Giay product) {
        String safeName = product.getTen() == null
                ? "Sản phẩm #" + product.getId()
                : product.getTen().replace("[", "").replace("]", "");
        output.append("[")
                .append(safeName)
                .append("](/khachhang/san-pham/")
                .append(product.getId())
                .append(")\n");
    }
}
