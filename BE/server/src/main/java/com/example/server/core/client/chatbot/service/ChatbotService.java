package com.example.server.core.client.chatbot.service;

import com.example.server.core.client.chatbot.dto.*;
import com.example.server.entity.CuocHoiThoai;
import com.example.server.entity.TinNhan;
import com.example.server.entity.NhanVien;
import com.example.server.repository.CuocHoiThoaiRepository;
import com.example.server.repository.TinNhanRepository;
import com.example.server.repository.NhanVienRepository;
import com.example.server.infrastructure.websocket.WebSocketNotificationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.server.core.admin.thongbao.service.ThongBaoService;

import java.util.UUID;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    public static final java.util.Map<String, byte[]> EXPORT_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public byte[] getExportedFile(String token) {
        return EXPORT_CACHE.get(token);
    }

    private final ChatClient.Builder chatClientBuilder;
    private final CuocHoiThoaiRepository cuocHoiThoaiRepository;
    private final TinNhanRepository tinNhanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ThongBaoService thongBaoService;

    @Value("${app.debug-errors:false}")
    private boolean debugErrors;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            CuocHoiThoaiRepository cuocHoiThoaiRepository,
            TinNhanRepository tinNhanRepository,
            NhanVienRepository nhanVienRepository,
            WebSocketNotificationService webSocketNotificationService,
            ThongBaoService thongBaoService) {
        this.chatClientBuilder = chatClientBuilder;
        this.cuocHoiThoaiRepository = cuocHoiThoaiRepository;
        this.tinNhanRepository = tinNhanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.webSocketNotificationService = webSocketNotificationService;
        this.thongBaoService = thongBaoService;
    }

    @Transactional
    public ClientChatResponse handleClientMessage(ClientChatRequest request) {
        CuocHoiThoai session = null;
        if (request.sessionId() != null) {
            session = cuocHoiThoaiRepository.findById(request.sessionId()).orElse(null);
            // Nếu phiên đã đóng (trạng thái = 4), ta sẽ bắt đầu phiên mới
            if (session != null && Integer.valueOf(4).equals(session.getTrangThai())) {
                session = null;
            }
        }

        if (session == null) {
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
                new ChatbotMessageDto(khachMsg.getId(), "CUSTOMER", khachMsg.getNoiDung(), khachMsg.getNgayTao(), null)
        );

        String botReply;
        if (session.getTrangThai() == 1) {
            // Kiểm tra xem tin nhắn khách gửi có phải là yêu cầu gặp nhân viên không
            if ("Tôi muốn gặp nhân viên trực tiếp hỗ trợ".equals(request.message()) ||
                "Liên hệ trực tiếp với nhân viên".equals(request.message())) {

                session.setTrangThai(2); // Yêu cầu trợ giúp từ nhân viên
                session.setNgayCapNhat(Instant.now());
                session = cuocHoiThoaiRepository.save(session);

                botReply = "Đã gửi yêu cầu kết nối với nhân viên tư vấn. Nhân viên trực sẽ phản hồi bạn trong giây lát!";

                TinNhan aiMsg = new TinNhan();
                aiMsg.setCuocHoiThoai(session);
                aiMsg.setNguoiGui("AI");
                aiMsg.setNoiDung(botReply);
                aiMsg.setNgayTao(Instant.now());
                tinNhanRepository.save(aiMsg);

                // Phát WebSocket phản hồi từ AI/Hệ thống
                webSocketNotificationService.sendToTopic(
                        "/topic/chatbot/session/" + session.getId(),
                        "NEW_MESSAGE",
                        new ChatbotMessageDto(aiMsg.getId(), "AI", aiMsg.getNoiDung(), aiMsg.getNgayTao(), null)
                );

                webSocketNotificationService.sendToTopic(
                        "/topic/chatbot/session/" + session.getId(),
                        "STATE_CHANGED",
                        2
                );

                // Gửi thông báo đến danh sách phiên chat admin
                webSocketNotificationService.sendToTopic(
                        "/topic/chatbot/sessions",
                        "SESSION_UPDATED",
                        convertToDto(session)
                );

                // Trigger chat notification
                guiThongBaoChatMoi(session, request.message());
            } else {
                // Chat với AI thông thường
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
                        new ChatbotMessageDto(aiMsg.getId(), "AI", aiMsg.getNoiDung(), aiMsg.getNgayTao(), null)
                );
            }
        } else {
            // Khi đang đợi hoặc đang chat với nhân viên, hệ thống/AI không tự động phản hồi nữa
            botReply = null;
            // Gửi thông báo đến danh sách phiên chat admin
            webSocketNotificationService.sendToTopic(
                    "/topic/chatbot/sessions",
                    "SESSION_UPDATED",
                    convertToDto(session)
            );

            // Trigger chat notification
            guiThongBaoChatMoi(session, request.message());
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
                new ChatbotMessageDto(sysMsg.getId(), "AI", sysMsg.getNoiDung(), sysMsg.getNgayTao(), null)
        );

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "STATE_CHANGED",
                2
        );

        // Trigger chat notification
        guiThongBaoChatMoi(session, null);
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/sessions",
                "SESSION_UPDATED",
                convertToDto(session)
        );
    }

    @Transactional
    public void replyFromStaff(Integer sessionId, String message, UUID staffId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));

        session.setTrangThai(3); // Đang chat với nhân viên
        session.setNgayCapNhat(Instant.now());

        NhanVien currentStaff = null;
        if (staffId != null) {
            currentStaff = nhanVienRepository.findById(staffId).orElse(null);
            if (currentStaff != null) {
                session.setNhanVien(currentStaff);
            }
        }
        cuocHoiThoaiRepository.save(session);

        TinNhan staffMsg = new TinNhan();
        staffMsg.setCuocHoiThoai(session);
        staffMsg.setNguoiGui("STAFF");
        staffMsg.setNoiDung(message);
        staffMsg.setNgayTao(Instant.now());
        if (currentStaff != null) {
            staffMsg.setNhanVien(currentStaff);
        }
        tinNhanRepository.save(staffMsg);

        String maNhanVien = currentStaff != null ? currentStaff.getMa() : null;

        // Phát WebSocket cho cuộc hội thoại
        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "NEW_MESSAGE",
                new ChatbotMessageDto(staffMsg.getId(), "STAFF", staffMsg.getNoiDung(), staffMsg.getNgayTao(), maNhanVien)
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
                new ChatbotMessageDto(sysMsg.getId(), "AI", sysMsg.getNoiDung(), sysMsg.getNgayTao(), null)
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

    @Transactional
    public void closeSessionDueToInactivity(Integer sessionId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElse(null);
        if (session == null || Integer.valueOf(4).equals(session.getTrangThai())) {
            return;
        }
        session.setTrangThai(4);
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        TinNhan sysMsg = new TinNhan();
        sysMsg.setCuocHoiThoai(session);
        sysMsg.setNguoiGui("AI");
        sysMsg.setNoiDung("Phiên trò chuyện đã tự động đóng do bạn đã quá thời gian không hoạt động. Cảm ơn bạn!");
        sysMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(sysMsg);

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "NEW_MESSAGE",
                new ChatbotMessageDto(sysMsg.getId(), "AI", sysMsg.getNoiDung(), sysMsg.getNgayTao(), null)
        );

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

    @Transactional(readOnly = true)
    public List<ChatbotSessionDto> getActiveSessions() {
        return cuocHoiThoaiRepository.findByTrangThaiInOrderByNgayTaoDesc(List.of(2, 3))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatbotSessionDto> getClosedSessions() {
        return cuocHoiThoaiRepository.findByTrangThaiInOrderByNgayTaoDesc(List.of(4))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageDto> getMessagesBySession(Integer sessionId) {
        return tinNhanRepository.findByCuocHoiThoaiIdOrderByNgayTaoAsc(sessionId)
                .stream()
                .map(m -> new ChatbotMessageDto(
                        m.getId(),
                        m.getNguoiGui(),
                        m.getNoiDung(),
                        m.getNgayTao(),
                        m.getNhanVien() != null ? m.getNhanVien().getMa() : null
                ))
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
                  1. Giày và các sản phẩm giày (Sử dụng công cụ `search_products_tool` hoặc `get_best_selling_shoes_tool`).
                  2. Phiếu giảm giá / Vouchers / Coupons (Sử dụng công cụ `search_coupons_tool`).
                  3. Chương trình / Đợt giảm giá / Sales / Promotions (Sử dụng công cụ `search_promotions_tool`).
                  4. Hóa đơn / Tra cứu đơn hàng / Đơn mua (Sử dụng công cụ `search_invoice_tool`).
                - Đối với bất kỳ câu hỏi nào KHÔNG liên quan đến 4 phạm vi trên, hãy lịch sự từ chối trả lời và khuyên khách hàng chỉ hỏi các thông tin liên quan đến sản phẩm, khuyến mãi hoặc hóa đơn của cửa hàng.

                # HƯỚNG DẪN HIỂN THỊ SẢN PHẨM & HÓA ĐƠN (QUAN TRỌNG)
                - Khi hiển thị thông tin sản phẩm tìm thấy từ cơ sở dữ liệu, hãy luôn đính kèm một liên kết Markdown dẫn đến chi tiết sản phẩm đó theo định dạng chuẩn:
                  [Xem chi tiết sản phẩm](/khachhang/san-pham/ID_SAN_PHAM)
                  (Trong đó ID_SAN_PHAM là ID dạng số của sản phẩm đó lấy từ kết quả gọi hàm hệ thống).
                  Ví dụ: "Bạn có thể xem chi tiết đôi **[Adidas UltraBoost]** tại đây: [Xem chi tiết giày](/khachhang/san-pham/12)".
                - Khi hiển thị thông tin hóa đơn tìm thấy từ cơ sở dữ liệu, hãy đính kèm một liên kết Markdown dẫn đến chi tiết hóa đơn đó theo định dạng chuẩn:
                  [Xem chi tiết hóa đơn](/khachhang/don-hang/ID_HOA_DON)
                  (Trong đó ID_HOA_DON là ID dạng số của hóa đơn lấy từ kết quả gọi hàm hệ thống).
                  Ví dụ: "Tôi tìm thấy hóa đơn **[HD0001]** của bạn. Hãy click vào đây để xem chi tiết: [Xem chi tiết hóa đơn](/khachhang/don-hang/10)".
                - Hệ thống giao diện sẽ tự động chuyển đổi liên kết này thành một nút bấm đẹp mắt để khách hàng click xem chi tiết.

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
                    .functions("search_products_tool", "get_best_selling_shoes_tool", "search_coupons_tool", "search_promotions_tool", "search_invoice_tool")
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

    @Transactional(noRollbackFor = Exception.class)
    public String generateAdminAiResponse(java.util.UUID nhanVienId, String userMessage) {
        String systemPrompt = """
                Bạn là một trợ lý ảo hỗ trợ quản trị và vận hành cửa hàng SportShoe dành riêng cho Admin/Quản lý.

                # PHẠM VI HỖ TRỢ
                - Bạn được phép truy cập và tra cứu thông tin quản trị hệ thống:
                  1. Doanh thu cửa hàng: Sử dụng công cụ `get_admin_revenue_stats_tool` để thống kê theo ngày (today), tháng (month) hoặc năm (year).
                  2. Hàng tồn kho và cảnh báo hết hàng: Sử dụng công cụ `get_admin_low_stock_tool` để tra cứu sản phẩm sắp hết hàng.
                  3. Tra cứu nhanh danh sách hóa đơn admin: Sử dụng công cụ `search_admin_invoices_tool` để tìm kiếm hóa đơn theo từ khóa hoặc trạng thái.
                  4. Tra cứu thông tin sản phẩm công khai: Sử dụng công cụ `search_products_tool` hoặc `get_best_selling_shoes_tool`.
                  5. Tra cứu đánh giá khách hàng: Sử dụng công cụ `get_admin_product_reviews_tool` (đánh giá của sản phẩm cụ thể) hoặc `get_admin_top_reviews_tool` (sản phẩm đánh giá tốt nhất / tệ nhất).
                  6. Cập nhật trạng thái đơn hàng (xác nhận hoặc hủy): Sử dụng công cụ `update_admin_order_status_tool`.
                  7. Cập nhật tồn kho sản phẩm: Sử dụng công cụ `update_admin_product_stock_tool`.
                  8. Tạo mã giảm giá nhanh: Sử dụng công cụ `create_admin_voucher_tool`.
                  9. Vẽ biểu đồ thống kê (doanh thu, giày bán chạy, trạng thái đơn): Sử dụng công cụ `get_admin_chart_data_tool`.
                  10. Xuất báo cáo Excel (CSV): Sử dụng công cụ `export_admin_data_csv_tool`.
                - Bạn CHỈ hỗ trợ các câu hỏi liên quan đến quản lý cửa hàng, thống kê, kiểm kho và tra cứu vận hành. Từ chối trả lời lịch sự cho các câu hỏi cá nhân hoặc ngoài phạm vi.

                # HƯỚNG DẪN HIỂN THỊ BIỂU ĐỒ (QUAN TRỌNG)
                Khi người dùng yêu cầu vẽ hoặc xem biểu đồ, hãy gọi công cụ `get_admin_chart_data_tool` để nhận chuỗi JSON dữ liệu thô. Sau đó, hiển thị nội dung đó nguyên bản bên trong khối code Markdown có tag là `chart` (ví dụ: bọc toàn bộ chuỗi JSON nhận được từ tool trong cặp dấu nháy ```chart và ```). Không thay đổi cấu trúc JSON bên trong tag chart này, và không chèn thêm ký tự lạ ngoài khối chart.

                # HƯỚNG DẪN SINH HÀNH ĐỘNG CẦN XÁC NHẬN (QUAN TRỌNG)
                Khi người dùng yêu cầu thực hiện hành động thay đổi dữ liệu nhạy cảm (như xác nhận đơn hàng, hủy đơn hàng, cập nhật tồn kho, tạo mã giảm giá), bạn TUYỆT ĐỐI không được gọi các tool thay đổi trực tiếp ngay. Thay vào đó, hãy phân tích tham số và sinh ra liên kết hành động dưới dạng Markdown như sau để yêu cầu xác nhận từ Admin:
                - Xác nhận đơn hàng: [Đồng ý xác nhận](/action/confirm-order/MÃ_HĐ)
                - Hủy đơn hàng: [Đồng ý hủy](/action/cancel-order/MÃ_HĐ)
                - Cập nhật tồn kho: [Đồng ý cập nhật tồn kho](/action/update-stock/TÊN_SẢN_PHẨM/SIZE/MÀU/SỐ_LƯỢNG)
                - Tạo mã giảm giá: [Đồng ý tạo mã](/action/create-voucher/MÃ/TÊN/LOẠI/GIÁ_TRỊ/ĐƠN_TỐI_THIỂU/GIẢM_TỐI_ĐA/SỐ_LƯỢNG/SỐ_NGÀY)
                  (Trong đó: LOẠI = 1 cho %, 2 cho tiền mặt. ĐƠN_TỐI_THIỂU và GIẢM_TỐI_ĐA mặc định là 0 nếu không yêu cầu. SỐ_LƯỢNG mặc định là 100. SỐ_NGÀY mặc định là 30).

                Ví dụ: "Tôi có thể giúp bạn xác nhận hóa đơn HD0001. Bạn có muốn thực hiện không? [Đồng ý xác nhận](/action/confirm-order/HD0001)".

                *Chú ý:* Chỉ khi người dùng gửi tin nhắn bắt đầu bằng lệnh "/execute-..." (Ví dụ: "/execute-confirm-order HD0001", "/execute-update-stock Ananas|41|đen|20", "/execute-create-voucher GIAMGIA|Tên|1|10|0|0|100|30"), bạn mới được phép gọi ngay lập tức tool tương ứng để thực hiện thao tác trực tiếp vào DB và trả về kết quả thành công cho họ.

                # HƯỚNG DẪN HIỂN THỊ LINK & THẺ SẢN PHẨM (CỰC KỲ QUAN TRỌNG)
                - BẮT BUỘC: Tuyệt đối KHÔNG dùng từ "tồn kho" hay "số lượng tồn kho". Chỉ được sử dụng duy nhất từ "Số lượng" (Ví dụ: "Số lượng: 3", "Số lượng còn lại: 5").
                - KHI CÔNG CỤ (TOOLS) TRẢ VỀ DỮ LIỆU CÓ CÁC KHỐI THẺ SẢN PHẨM ```product ... ``` (như sản phẩm bán chạy, sản phẩm sắp hết hàng, tìm kiếm sản phẩm), BẮT BUỘC PHẢI GIỮ NGUYÊN TẤT CẢ CÁC KHỐI ```product ... ``` NÀY TRONG CÂU TRẢ LỜI CHO NGUỜI DÙNG! TUYỆT ĐỐI KHÔNG ĐƯỢC TÓM TẮT HOẶC VIẾT LẠI THÀNH DẠNG VĂN BẢN CHỮ THƯỜNG (như "1. Nike... 2. Asics...").
                - Giao diện Admin Vue.js bắt buộc cần các khối ```product ... ``` này để tự động dựng thành các thẻ sản phẩm đẹp mắt có HÌNH ẢNH SẢN PHẨM, GIÁ BÁN, SỐ LƯỢNG và khi click vào ảnh sẽ tự động điều hướng Admin tới đúng sản phẩm đó.
                - Khi hiển thị hóa đơn, hãy đính kèm link dạng:
                  [Xem chi tiết hóa đơn](/admin/hoa-don/ID_HOA_DON)
                  (Trong đó ID_HOA_DON là ID dạng số của hóa đơn lấy từ kết quả gọi hàm).
                  Ví dụ: "Hóa đơn **[HD0001]** của khách hàng đã hoàn thành: [Xem chi tiết hóa đơn](/admin/hoa-don/10)".
                - Hệ thống giao diện admin sẽ tự động nhận diện thẻ và link này để hiển thị ảnh và nút bấm chuyển hướng.

                # NGUYÊN TẮC HOẠT ĐỘNG
                - Luôn sử dụng thông tin thực tế từ công cụ, không tự bịa số liệu tài chính hoặc số lượng.
                - Trả lời bằng Tiếng Việt chuyên nghiệp, ngắn gọn, đi thẳng vào vấn đề.
                """;

        try {
            NhanVien nv = nhanVienRepository.findById(nhanVienId)
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

            CuocHoiThoai session = null;
            List<CuocHoiThoai> sessions = cuocHoiThoaiRepository.findByNhanVienIdAndTrangThai(nhanVienId, 1);
            if (!sessions.isEmpty()) {
                session = sessions.get(0);
            } else {
                session = new CuocHoiThoai();
                session.setNhanVien(nv);
                session.setTrangThai(1);
                session.setNgayTao(java.time.Instant.now());
                session = cuocHoiThoaiRepository.save(session);
            }

            // Lưu tin nhắn Admin
            TinNhan adminMsg = new TinNhan();
            adminMsg.setCuocHoiThoai(session);
            adminMsg.setNhanVien(nv);
            adminMsg.setNguoiGui("STAFF");
            adminMsg.setNoiDung(userMessage);
            adminMsg.setNgayTao(java.time.Instant.now());
            tinNhanRepository.save(adminMsg);

            // Load lịch sử hội thoại (15 tin nhắn gần nhất)
            List<TinNhan> historyMsgs = tinNhanRepository.findByCuocHoiThoaiIdOrderByNgayTaoAsc(session.getId());
            if (historyMsgs.size() > 15) {
                historyMsgs = historyMsgs.subList(historyMsgs.size() - 15, historyMsgs.size());
            }

            List<org.springframework.ai.chat.messages.Message> springAiMsgs = new java.util.ArrayList<>();
            for (TinNhan m : historyMsgs) {
                if ("STAFF".equals(m.getNguoiGui())) {
                    springAiMsgs.add(new org.springframework.ai.chat.messages.UserMessage(m.getNoiDung()));
                } else if ("AI".equals(m.getNguoiGui())) {
                    springAiMsgs.add(new org.springframework.ai.chat.messages.AssistantMessage(m.getNoiDung()));
                }
            }

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem(systemPrompt)
                    .build();

            String reply = chatClient.prompt()
                    .messages(springAiMsgs)
                    .functions(
                            "get_admin_revenue_stats_tool", 
                            "get_admin_low_stock_tool", 
                            "search_admin_invoices_tool", 
                            "search_products_tool", 
                            "get_best_selling_shoes_tool",
                            "get_admin_product_reviews_tool",
                            "get_admin_top_reviews_tool",
                            "update_admin_order_status_tool",
                            "update_admin_product_stock_tool",
                            "create_admin_voucher_tool",
                            "get_admin_chart_data_tool",
                            "export_admin_data_csv_tool"
                    )
                    .call()
                    .content();

            // Lưu tin nhắn AI
            TinNhan aiMsg = new TinNhan();
            aiMsg.setCuocHoiThoai(session);
            aiMsg.setNguoiGui("AI");
            aiMsg.setNoiDung(reply);
            aiMsg.setNgayTao(java.time.Instant.now());
            tinNhanRepository.save(aiMsg);

            return reply;
        } catch (Exception e) {
            System.err.println("[ADMIN CHATBOT ERROR] Lỗi khi gọi Admin AI: " + e.getMessage());
            e.printStackTrace();
            String errReply = "Hệ thống AI hiện đang không phản hồi do API Key hết hạn ngạch (Quota Exceeded / Insufficient Balance). Vui lòng cập nhật API Key mới tại file chatbot-keys.json.";
            if (e.getMessage() != null && e.getMessage().contains("insufficient_quota")) {
                errReply = "Tài khoản ChatGPT (OpenAI API Key) hiện chưa có số dư (0$). OpenAI bắt buộc phải nạp tiền ở platform.openai.com để dùng API. Bạn vui lòng đổi sang Gemini API Key miễn phí nhé!";
            }
            try {
                List<CuocHoiThoai> sessions = cuocHoiThoaiRepository.findByNhanVienIdAndTrangThai(nhanVienId, 1);
                if (!sessions.isEmpty()) {
                    TinNhan aiErr = new TinNhan();
                    aiErr.setCuocHoiThoai(sessions.get(0));
                    aiErr.setNguoiGui("AI");
                    aiErr.setNoiDung(errReply);
                    aiErr.setNgayTao(java.time.Instant.now());
                    tinNhanRepository.save(aiErr);
                }
            } catch (Exception ex) {
                // Ignore DB error on error fallback
            }
            return errReply;
        }
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageDto> getAdminChatHistory(java.util.UUID nhanVienId) {
        List<CuocHoiThoai> sessions = cuocHoiThoaiRepository.findByNhanVienIdAndTrangThai(nhanVienId, 1);
        if (sessions.isEmpty()) {
            return List.of();
        }
        return tinNhanRepository.findByCuocHoiThoaiIdOrderByNgayTaoAsc(sessions.get(0).getId())
                .stream()
                .map(m -> new ChatbotMessageDto(
                        m.getId(),
                        m.getNguoiGui(),
                        m.getNoiDung(),
                        m.getNgayTao(),
                        m.getNhanVien() != null ? m.getNhanVien().getMa() : null
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void closeAdminAiSession(java.util.UUID nhanVienId) {
        List<CuocHoiThoai> activeSessions = cuocHoiThoaiRepository.findByNhanVienIdAndTrangThai(nhanVienId, 1);
        for (CuocHoiThoai session : activeSessions) {
            session.setTrangThai(4);
            session.setNgayCapNhat(java.time.Instant.now());
            cuocHoiThoaiRepository.save(session);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatbotSessionDto> getAdminAiSessions(java.util.UUID nhanVienId) {
        return cuocHoiThoaiRepository.findByNhanVienIdOrderByNgayTaoDesc(nhanVienId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageDto> getAdminAiSessionMessages(java.util.UUID nhanVienId, Integer sessionId) {
        return tinNhanRepository.findByCuocHoiThoaiIdOrderByNgayTaoAsc(sessionId)
                .stream()
                .map(m -> new ChatbotMessageDto(
                        m.getId(),
                        m.getNguoiGui(),
                        m.getNoiDung(),
                        m.getNgayTao(),
                        m.getNhanVien() != null ? m.getNhanVien().getMa() : null
                ))
                .collect(Collectors.toList());
    }

    private void guiThongBaoChatMoi(CuocHoiThoai session, String message) {
        try {
            String name = session.getTenKhachHang() != null ? session.getTenKhachHang() : "Khách hàng";
            String nd = message != null ? message : "Yêu cầu hỗ trợ trực tiếp từ nhân viên tư vấn.";
            if (nd.length() > 80) {
                nd = nd.substring(0, 77) + "...";
            }
            thongBaoService.taoThongBao(
                    "Tin nhắn hỗ trợ mới",
                    "Khách \"" + name + "\": " + nd,
                    "CHAT",
                    "/admin/chat"
            );
        } catch (Exception e) {
            System.err.println("[ChatbotService] Lỗi tạo thông báo chat: " + e.getMessage());
        }
    }
}
