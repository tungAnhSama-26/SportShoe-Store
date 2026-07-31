package com.example.server.core.client.chatbot;

import com.example.server.core.client.chatbot.dto.ClientChatRequest;
import com.example.server.infrastructure.utils.ProfanityFilterUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClientChatbotValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Báo lỗi khi gửi tin nhắn rỗng hoặc chỉ có khoảng trắng")
    void testBlankMessageValidation() {
        ClientChatRequest request = new ClientChatRequest(null, "   ", "Nguyen Van A", "0912345678");
        Set<ConstraintViolation<ClientChatRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Nội dung tin nhắn không được để trống");
    }

    @Test
    @DisplayName("Báo lỗi khi gửi số điện thoại sai định dạng")
    void testInvalidPhoneNumberValidation() {
        ClientChatRequest request = new ClientChatRequest(null, "Xin chào", "Nguyen Van A", "12345");
        Set<ConstraintViolation<ClientChatRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Số điện thoại không đúng định dạng Việt Nam");
    }

    @Test
    @DisplayName("Chấp nhận tin nhắn hợp lệ và số điện thoại chuẩn Việt Nam")
    void testValidRequest() {
        ClientChatRequest request = new ClientChatRequest(1, "Tôi muốn mua giày", "Nguyen Van A", "0987654321");
        Set<ConstraintViolation<ClientChatRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Kiểm tra lọc từ ngữ xúc phạm tự động chuyển thành *******")
    void testProfanityFiltering() {
        String rawMessage = "Giày này vkl thật, đm shop";
        String filtered = ProfanityFilterUtil.filter(rawMessage);

        assertThat(filtered).isEqualTo("Giày này ******* thật, ******* shop");
    }
}
