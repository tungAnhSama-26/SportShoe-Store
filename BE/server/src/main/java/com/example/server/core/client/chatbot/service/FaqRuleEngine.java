package com.example.server.core.client.chatbot.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class FaqRuleEngine {

    public String matchFaq(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        String query = message.toLowerCase(Locale.ROOT).trim();

        // 0. Chào hỏi / Khởi động hội thoại
        if (query.equals("xin chào") || query.equals("chào") || query.equals("chào shop") || query.equals("hello") ||
            query.equals("hi") || query.equals("chào bạn") || query.equals("shop ơi") || query.equals("ad ơi") ||
            query.startsWith("xin chào") || query.startsWith("chào shop")) {
            return "Xin chào bạn! Mình là trợ lý ảo của SportShoe. Mình có thể giúp bạn tìm kiếm các mẫu giày hot trend, tư vấn chọn size chuẩn, xem các đợt giảm giá khuyến mãi hoặc tra cứu đơn hàng. Bạn cần mình hỗ trợ gì ạ?";
        }

        // 1. Địa chỉ / Vị trí cửa hàng
        if (query.contains("địa chỉ") || query.contains("shop ở đâu") || query.contains("cửa hàng ở đâu") || query.contains("vị trí shop")) {
            return "Cửa hàng SportShoe hân hạnh đón tiếp bạn tại địa chỉ: **Số 123 Đường Cầu Giấy, Quận Cầu Giấy, Hà Nội**. Cửa hàng có chỗ đỗ xe ô tô và xe máy miễn phí cho khách hàng nhé!";
        }

        // 2. Giờ mở cửa / Thời gian làm việc
        if (query.contains("giờ mở cửa") || query.contains("mở cửa lúc mấy giờ") || query.contains("thời gian làm việc") || query.contains("mấy giờ đóng cửa")) {
            return "SportShoe mở cửa đón khách tất cả các ngày trong tuần (kể cả Thứ 7, Chủ Nhật & Ngày lễ) từ **08:00 sáng đến 22:00 tối** nhé!";
        }

        // 3. Chính sách đổi trả / Bảo hành
        if (query.contains("đổi trả") || query.contains("chính sách đổi") || query.contains("đổi hàng") || query.contains("bảo hành")) {
            return "Để được hỗ trợ chi tiết và chính xác nhất về quy định đổi trả hoặc bảo hành sản phẩm, bạn vui lòng bấm nút **Gặp nhân viên** bên dưới hoặc liên hệ Hotline **0965852782** để được nhân viên tư vấn hỗ trợ trực tiếp nhé!";
        }

        // 4. Hotline / Liên hệ
        if (query.contains("hotline") || query.contains("số điện thoại") || query.contains("liên hệ nhân viên") || query.contains("tổng đài")) {
            return "Bạn có thể liên hệ trực tiếp với bộ phận chăm sóc khách hàng của SportShoe qua:\n" +
                   "- Hotline: **0965852782** (8:00 - 22:00)\n" +
                   "- Email: cskh@sportshoe.vn\n" +
                   "- Hoặc bấm nút **Gặp nhân viên** bên dưới để được nhân viên tư vấn trực tiếp qua chat nhé!";
        }

        // 5. Bảng size / Cách đo chân tại nhà / Tư vấn chọn size
        if (query.contains("bảng size") || query.contains("đo size") || query.contains("hướng dẫn đo chân") || query.contains("cách đo chân") ||
            query.contains("tư vấn chọn size") || query.contains("tư vấn size") || query.contains("chọn size") || query.contains("tư vấn kích cỡ") ||
            query.contains("hướng dẫn chọn size") || query.contains("tư vấn chọn size giày")) {
            return "Hướng dẫn đo size chân chuẩn tại nhà:\n" +
                   "1. Đặt bàn chân vuông góc lên tờ giấy A4.\n" +
                   "2. Dùng bút vạch 2 điểm ở gót chân và đầu ngón chân dài nhất.\n" +
                   "3. Dùng thước đo khoảng cách chiều dài (cm) giữa 2 vạch đó.\n\n" +
                   "**Bảng quy đổi Size chuẩn VN/EU:**\n" +
                   "- 21.1 - 21.5 cm: Size 35\n" +
                   "- 21.6 - 22.0 cm: Size 36\n" +
                   "- 22.1 - 22.5 cm: Size 37\n" +
                   "- 22.6 - 23.5 cm: Size 38\n" +
                   "- 23.6 - 24.2 cm: Size 39\n" +
                   "- 24.3 - 25.0 cm: Size 40\n" +
                   "- 25.1 - 25.7 cm: Size 41\n" +
                   "- 25.8 - 26.5 cm: Size 42\n" +
                   "- 26.6 - 27.2 cm: Size 43\n" +
                   "- 27.3 - 28.0 cm: Size 44\n" +
                   "- > 28.0 cm: Size 45\n\n" +
                   "*(Lưu ý: Nếu chân bạn thuộc dạng bè ngang hoặc mập, nên nhích lên 0.5 - 1 size để mang thoải mái hơn).* \n\n" +
                   "Bạn có thể cho shop biết chiều dài bàn chân (cm) hoặc mẫu giày bạn quan tâm để shop hỗ trợ tư vấn chuẩn nhất nhé!";
        }

        return null;
    }
}
