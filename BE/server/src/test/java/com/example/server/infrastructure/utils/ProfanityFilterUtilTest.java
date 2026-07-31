package com.example.server.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfanityFilterUtilTest {

    @Test
    @DisplayName("Nội dung tin nhắn bình thường không chứa từ thô tục -> Giữ nguyên")
    void testCleanMessageRemainsUnchanged() {
        String input = "Tôi muốn tìm một đôi giày chạy bộ màu trắng size 42";
        String filtered = ProfanityFilterUtil.filter(input);
        assertThat(filtered).isEqualTo(input);
    }

    @Test
    @DisplayName("Nội dung tin nhắn rỗng hoặc null -> Trả về nguyên bản")
    void testNullOrEmptyInput() {
        assertThat(ProfanityFilterUtil.filter(null)).isNull();
        assertThat(ProfanityFilterUtil.filter("   ")).isEqualTo("   ");
    }

    @Test
    @DisplayName("Phát hiện và che mờ từ thô tục tiếng Việt (đm, vkl, dkm, lồn, cặc...)")
    void testFilterVietnameseProfanity() {
        String input = "Shop làm ăn vkl, giày này đm quá kém";
        String filtered = ProfanityFilterUtil.filter(input);
        assertThat(filtered).contains("*******");
        assertThat(filtered).doesNotContain("vkl");
        assertThat(filtered).doesNotContain("đm");
    }

    @Test
    @DisplayName("Phát hiện từ thô tục không phân biệt chữ hoa hay chữ thường")
    void testCaseInsensitiveFilter() {
        String input = "Giày VKL thế này mà cũng bán";
        String filtered = ProfanityFilterUtil.filter(input);
        assertThat(filtered).isEqualTo("Giày ******* thế này mà cũng bán");
    }

    @Test
    @DisplayName("Phát hiện từ thô tục lách luật có ký tự phân tách (d.k.m, f.u.c.k)")
    void testSeparatedProfanityFilter() {
        String input = "Shop làm ăn d.k.m kém chất lượng";
        String filtered = ProfanityFilterUtil.filter(input);
        assertThat(filtered).contains("*******");
        assertThat(filtered).doesNotContain("d.k.m");
    }
}
