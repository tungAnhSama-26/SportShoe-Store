package com.example.server.core.client.chatbot.service;

import com.example.server.entity.Giay;
import com.example.server.repository.GiayRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientProductResponseSanitizerTest {

    @Test
    void removesInventedProductLinkAndClaimsWhenIdDoesNotExist() {
        GiayRepository repository = mock(GiayRepository.class);
        when(repository.findById(1001)).thenReturn(Optional.empty());
        ClientProductResponseSanitizer sanitizer = new ClientProductResponseSanitizer(repository);

        String result = sanitizer.sanitize("Gợi ý cho bạn:\n"
                + "[Giày BEST SELLER](/khachhang/san-pham/1001) - Giá 999.000đ, giảm 20%, tồn 12.\n"
                + "Bạn muốn tư vấn thêm không?");

        assertFalse(result.contains("1001"));
        assertFalse(result.contains("999.000"));
        assertFalse(result.contains("tồn 12"));
        assertTrue(result.contains("Bạn muốn tư vấn thêm không?"));
    }

    @Test
    void rebuildsExistingProductLinkUsingDatabaseNameOnly() {
        GiayRepository repository = mock(GiayRepository.class);
        Giay product = mock(Giay.class);
        when(product.getId()).thenReturn(15);
        when(product.getTen()).thenReturn("Dior B22 Sneaker");
        when(product.getTrangThai()).thenReturn(1);
        when(repository.findById(15)).thenReturn(Optional.of(product));
        ClientProductResponseSanitizer sanitizer = new ClientProductResponseSanitizer(repository);

        String result = sanitizer.sanitize(
                "[Tên do AI bịa](/khachhang/san-pham/15) - Giá 1đ, tồn 999.");

        assertTrue(result.contains("[Dior B22 Sneaker](/khachhang/san-pham/15)"));
        assertFalse(result.startsWith("- "));
        assertFalse(result.contains("Giá 1đ"));
        assertFalse(result.contains("tồn 999"));
    }

    @Test
    void removesUntrustedProductCodeBlocks() {
        ClientProductResponseSanitizer sanitizer =
                new ClientProductResponseSanitizer(mock(GiayRepository.class));

        String result = sanitizer.sanitize("Sản phẩm:\n```product\n"
                + "{\"name\":\"Bịa\",\"price\":999000}\n```\nHết.");

        assertFalse(result.contains("999000"));
        assertTrue(result.contains("Hết."));
    }
}
