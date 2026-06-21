package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.*;
import com.example.server.entity.CuocHoiThoai;
import com.example.server.entity.TinNhan;
import com.example.server.repository.CuocHoiThoaiRepository;
import com.example.server.repository.TinNhanRepository;
import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final CuocHoiThoaiRepository cuocHoiThoaiRepository;
    private final TinNhanRepository tinNhanRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @Value("${app.debug-errors:false}")
    private boolean debugErrors;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            CuocHoiThoaiRepository cuocHoiThoaiRepository,
            TinNhanRepository tinNhanRepository,
            WebSocketNotificationService webSocketNotificationService) {
        this.chatClientBuilder = chatClientBuilder;
        this.cuocHoiThoaiRepository = cuocHoiThoaiRepository;
        this.tinNhanRepository = tinNhanRepository;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Transactional
    public ClientChatResponse handleClientMessage(ClientChatRequest request) {
        CuocHoiThoai session;
        if (request.sessionId() != null) {
            session = cuocHoiThoaiRepository.findById(request.sessionId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));
        } else {
            session = new CuocHoiThoai();
            session.setTenKhachHang(request.customerName() != null && !request.customerName().isBlank()
                    ? request.customerName() : "Khách vãng lai");
            session.setSoDienThoai(request.phoneNumber());
            session.setTrangThai(1); // Mặc định chat với AI
            session.setNgayTao(Instant.now());
            session = cuocHoiThoaiRepository.save(session);
        }

        // Lưu tin nhắn của Khách hàng
        TinNhan khachMsg = new TinNhan();
        khachMsg.setCuocHoiThoai(session);
        khachMsg.setNguoiGui("CUSTOMER");
        khachMsg.setNoiDung(request.message());
        khachMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(khachMsg);

        // Phát WebSocket cho phiên chat
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + session.getId(),
                "NEW_MESSAGE",
                new ChatbotMessageDto(khachMsg.getId(), "CUSTOMER", khachMsg.getNoiDung(), khachMsg.getNgayTao())
        );

        String botReply;
        if (session.getTrangThai() == 1) {
            // Chat với AI
            botReply = generateAiResponse(request.message());

            TinNhan aiMsg = new TinNhan();
            aiMsg.setCuocHoiThoai(session);
            aiMsg.setNguoiGui("AI");
            aiMsg.setNoiDung(botReply);
            aiMsg.setNgayTao(Instant.now());
            tinNhanRepository.save(aiMsg);

            // Phát WebSocket phản hồi từ AI
            webSocketNotificationService.sendToTopic(
                    "/topic/chatbot/session/" + session.getId(),
                    "NEW_MESSAGE",
                    new ChatbotMessageDto(aiMsg.getId(), "AI", aiMsg.getNoiDung(), aiMsg.getNgayTao())
            );
        } else {
            // Khi đang đợi hoặc đang chat với nhân viên
            botReply = "Yêu cầu của bạn đã được gửi tới nhân viên tư vấn. Vui lòng chờ nhân viên trực liên hệ hỗ trợ.";
            // Gửi thông báo đến danh sách phiên chat admin
            webSocketNotificationService.sendToTopic(
                    "/topic/chatbot/sessions",
                    "SESSION_UPDATED",
                    convertToDto(session)
            );
        }

        return new ClientChatResponse(session.getId(), botReply, session.getTrangThai());
    }

    @Transactional
    public void requestStaff(Integer sessionId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));
        session.setTrangThai(2); // Yêu cầu trợ giúp từ nhân viên
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        // Lưu tin nhắn hệ thống
        TinNhan sysMsg = new TinNhan();
        sysMsg.setCuocHoiThoai(session);
        sysMsg.setNguoiGui("AI");
        sysMsg.setNoiDung("Đã gửi yêu cầu kết nối với nhân viên tư vấn. Nhân viên trực sẽ phản hồi bạn trong giây lát!");
        sysMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(sysMsg);

        // Phát thông báo WebSocket
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "NEW_MESSAGE",
                new ChatbotMessageDto(sysMsg.getId(), "AI", sysMsg.getNoiDung(), sysMsg.getNgayTao())
        );

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "STATE_CHANGED",
                2
        );

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/sessions",
                "SESSION_UPDATED",
                convertToDto(session)
        );
    }

    @Transactional
    public void replyFromStaff(Integer sessionId, String message) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));

        session.setTrangThai(3); // Đang chat với nhân viên
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        TinNhan staffMsg = new TinNhan();
        staffMsg.setCuocHoiThoai(session);
        staffMsg.setNguoiGui("STAFF");
        staffMsg.setNoiDung(message);
        staffMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(staffMsg);

        // Phát WebSocket cho cuộc hội thoại
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "NEW_MESSAGE",
                new ChatbotMessageDto(staffMsg.getId(), "STAFF", staffMsg.getNoiDung(), staffMsg.getNgayTao())
        );

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "STATE_CHANGED",
                3
        );

        // Phát WebSocket cập nhật danh sách phiên của admin
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/sessions",
                "SESSION_UPDATED",
                convertToDto(session)
        );
    }

    @Transactional
    public void closeSession(Integer sessionId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));
        session.setTrangThai(4); // Đã kết thúc
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        // Lưu tin nhắn hệ thống báo kết thúc cuộc hội thoại
        TinNhan sysMsg = new TinNhan();
        sysMsg.setCuocHoiThoai(session);
        sysMsg.setNguoiGui("AI");
        sysMsg.setNoiDung("Cuộc hội thoại này đã kết thúc bởi nhân viên trực. Cảm ơn bạn!");
        sysMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(sysMsg);

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "NEW_MESSAGE",
                new ChatbotMessageDto(sysMsg.getId(), "AI", sysMsg.getNoiDung(), sysMsg.getNgayTao())
        );

        // Phát thông báo đóng session
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "STATE_CHANGED",
                4
        );

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/sessions",
                "SESSION_UPDATED",
                convertToDto(session)
        );
    }

    public List<ChatbotSessionDto> getActiveSessions() {
        return cuocHoiThoaiRepository.findByTrangThaiInOrderByNgayTaoDesc(List.of(2, 3))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ChatbotMessageDto> getMessagesBySession(Integer sessionId) {
        return tinNhanRepository.findByCuocHoiThoaiIdOrderByNgayTaoAsc(sessionId)
                .stream()
                .map(m -> new ChatbotMessageDto(m.getId(), m.getNguoiGui(), m.getNoiDung(), m.getNgayTao()))
                .collect(Collectors.toList());
    }

    private ChatbotSessionDto convertToDto(CuocHoiThoai session) {
        return new ChatbotSessionDto(
                session.getId(),
                session.getTenKhachHang(),
                session.getSoDienThoai(),
                session.getTrangThai(),
                session.getNgayTao(),
                session.getNgayCapNhat()
        );
    }

    private String generateAiResponse(String userMessage) {
        String systemPrompt = """
                Bạn là một trợ lý ảo hỗ trợ khách hàng mua sắm tại cửa hàng SportShoe.
                
                # PHẠM VI HỖ TRỢ
                - Bạn CHỈ được phép trả lời và truy vấn cơ sở dữ liệu cho các câu hỏi liên quan đến:
                  1. Giày và các sản phẩm giày dép (Sử dụng công cụ `search_products_tool` hoặc `get_best_selling_shoes_tool`).
                  2. Phiếu giảm giá / Vouchers / Coupons (Sử dụng công cụ `search_coupons_tool`).
                  3. Chương trình / Đợt giảm giá / Sales / Promotions (Sử dụng công cụ `search_promotions_tool`).
                - Đối với bất kỳ câu hỏi nào KHÔNG liên quan đến 3 phạm vi trên, hãy lịch sự từ chối trả lời và khuyên khách hàng chỉ hỏi các thông tin liên quan đến sản phẩm giày và khuyến mãi của cửa hàng.
                
                # HƯỚNG DẪN HIỂN THỊ SẢN PHẨM (QUAN TRỌNG)
                Khi hiển thị thông tin sản phẩm tìm thấy từ cơ sở dữ liệu, hãy luôn đính kèm một liên kết Markdown dẫn đến chi tiết sản phẩm đó theo định dạng chuẩn:
                [Xem chi tiết sản phẩm](/khachhang/san-pham/ID_SAN_PHAM)
                (Trong đó ID_SAN_PHAM là ID dạng số của sản phẩm đó lấy từ kết quả gọi hàm hệ thống).
                Ví dụ: "Bạn có thể xem chi tiết đôi **[Adidas UltraBoost]** tại đây: [Xem chi tiết giày](/khachhang/san-pham/12)".
                Hệ thống giao diện sẽ tự động chuyển đổi liên kết này thành một nút bấm đẹp mắt để khách hàng click xem chi tiết đôi giày đó.
                
                # NGUYÊN TẮC HOẠT ĐỘNG
                - Khách hàng hỏi gì thì tự động gọi các công cụ (tools) tương ứng để truy vấn cơ sở dữ liệu lấy thông tin thực tế. Không tự bịa thông tin.
                - Trả lời bằng Tiếng Việt thân thiện, tự nhiên, ngắn gọn, súc tích (Tối đa 150 từ).
                """;

        try {
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt)
                    .build();

            return chatClient.prompt()
                    .user(userMessage)
                    .functions("search_products_tool", "get_best_selling_shoes_tool", "search_coupons_tool", "search_promotions_tool")
                    .call()
                    .content();
        } catch (Exception e) {
            System.err.println("[AI CHATBOT ERROR] Lỗi khi gọi AI Chatbot:");
            e.printStackTrace();
            if (debugErrors) {
                return "Lỗi AI Chatbot (Debug): " + e.getMessage() + " | Nguyên nhân chi tiết: " + (e.getCause() != null ? e.getCause().getMessage() : "không có");
            }
            return "Hệ thống tư vấn tự động hiện đang bận hoặc đang được bảo trì. Bạn vui lòng liên hệ hotline hỗ trợ trực tiếp của cửa hàng để được hỗ trợ nhanh nhất nhé!";
        }
    }
}
