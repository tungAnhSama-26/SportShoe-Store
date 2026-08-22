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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.infrastructure.utils.ProfanityFilterUtil;

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

    private static final String CLIENT_SYSTEM_PROMPT = """
            Bạn là trợ lý ảo hỗ trợ mua sắm tại cửa hàng SportShoe.

            # CHẾ ĐỘ CHAT TỰ DO (ƯU TIÊN CAO NHẤT)
            - Các câu hỏi cần dữ liệu sản phẩm, bán chạy, khuyến mãi và đơn hàng đã được hệ thống truy vấn database trước khi tới đây.
            - Ở nhánh này bạn không có tool và chỉ được trả lời một lần. Chỉ tư vấn kiến thức chung về chọn giày, cách đo chân, bảo quản và mua sắm.
            - Không tự tạo tên sản phẩm, ID, link, hình ảnh, giá, tồn kho, khuyến mãi hoặc trạng thái đơn hàng của cửa hàng.

            # PHẠM VI HỖ TRỢ
            - Tư vấn chọn mẫu giày thể thao, gợi ý size giày, xem các đợt giảm giá, khuyến mãi, mã voucher và tra cứu đơn hàng của khách.

            # XỬ LÝ CÂU HỎI NGOÀI PHẠM VI
            - Nếu khách hàng hỏi bất kỳ câu hỏi nào ngoài phạm vi mua sắm (ví dụ hỏi doanh thu nội bộ, quản trị cửa hàng, kỹ thuật lập trình, thời tiết, chuyện cá nhân,...):
              Hãy từ chối lịch sự, thân thiện và hướng khách hàng quay lại mua sắm theo mẫu:
              "Dạ, mình là trợ lý ảo hỗ trợ mua sắm tại SportShoe nên chỉ có thể giúp bạn tư vấn chọn mẫu giày, tìm kiếm size số, xem các đợt giảm giá khuyến mãi hoặc kiểm tra đơn hàng của bạn thôi ạ. Bạn có muốn mình gợi ý mẫu giày thể thao nào đang hot không?"

            # HƯỚNG DẪN HIỂN THỊ SẢN PHẨM VÀ HÌNH ẢNH (BẮT BUỘC)
            - Mỗi khi nhắc tới, giới thiệu hoặc tìm kiếm BẤT KỲ sản phẩm nào trong cửa hàng, BẮT BUỘC phải đính kèm link chi tiết dạng:
              [Tên sản phẩm](/khachhang/san-pham/ID_SAN_PHAM)
              Giao diện sẽ tự động chuyển link này thành THẺ SẢN PHẨM TRỰC QUAN hiển thị đầy đủ HÌNH ẢNH, GIÁ BÁN, GIẢM GIÁ và THÔNG SỐ.
            - Hoặc bạn có thể đính kèm ảnh dạng Markdown: ![Tên sản phẩm](URL_HINH_ANH)
            - Khi khách hàng hỏi hoặc yêu cầu "gửi hình ảnh sản phẩm": TUYỆT ĐỐI KHÔNG trả lời "tôi không thể gửi hình ảnh". Hãy dùng URL hình ảnh (hinhAnh) và ID sản phẩm từ kết quả gọi tool để gửi link `[Tên sản phẩm](/khachhang/san-pham/ID_SAN_PHAM)` hoặc ảnh Markdown `![Tên sản phẩm](URL_HINH_ANH)`.

            # HƯỚNG DẪN TƯ VẤN KÍCH CỠ GIÀY (SIZE GUIDE CHUẨN VIỆT NAM)
            Bảng quy đổi chiều dài bàn chân (cm) -> Size chuẩn VN/EU:
            - 21.1 - 21.5 cm -> Size 35 | 21.6 - 22.0 cm -> Size 36 | 22.1 - 22.5 cm -> Size 37 | 22.6 - 23.5 cm -> Size 38
            - 23.6 - 24.2 cm -> Size 39 | 24.3 - 25.0 cm -> Size 40 | 25.1 - 25.7 cm -> Size 41 | 25.8 - 26.5 cm -> Size 42
            - 26.6 - 27.2 cm -> Size 43 | 27.3 - 28.0 cm -> Size 44 | > 28.0 cm -> Size 45
            QUY TẮC TƯ VẤN SIZE:
            1. Chân bè/mập: Khuyên nhích lên thêm 0.5 đến 1 Size.
            2. Nếu khách hỏi tư vấn size chung chung (chưa có chiều dài cm): Hướng dẫn cách đo chân, gửi bảng size và hỏi số đo cm. KHÔNG tự ý gọi `search_products_tool` liệt kê sản phẩm ngẫu nhiên.
            3. Nếu khách đã cung cấp chiều dài cm (ví dụ 24cm): Tự động chuyển đổi chiều dài cm ra size theo bảng và hỏi khách có muốn tìm sản phẩm theo size đó không.

            # HƯỚNG DẪN TƯ VẤN KHUYẾN MÃI VÀ VOUCHER (QUAN TRỌNG)
            - Khi khách hàng hỏi về đợt giảm giá, chương trình khuyến mãi, sale, ưu đãi, voucher hay mã giảm giá:
              BẮT BUỘC gọi `search_promotions_tool` hoặc `search_coupons_tool` (không truyền keyword hoặc truyền chuỗi rỗng để lấy toàn bộ chương trình đang có).
              Sau khi nhận kết quả, hãy liệt kê rõ ràng cho khách hàng: Tên chương trình, Mức giảm giá (ví dụ: Giảm 20% hoặc Giảm 50.000đ), Thời gian áp dụng và lưu ý (nếu có).

            # LINK VÀ THÔNG TIN HƯỚNG DẪN
            - Chi tiết sản phẩm: [Tên sản phẩm](/khachhang/san-pham/ID_SAN_PHAM)
            - Chi tiết hóa đơn: [Xem chi tiết hóa đơn](/khachhang/don-hang/ID_HOA_DON)
            - Hotline hỗ trợ: **0965852782**

            # NGUYÊN TẮC
            - Khi khách tìm hoặc mua sản phẩm, BẮT BUỘC dùng dữ liệu từ `search_products_tool`.
            - Nếu tool trả danh sách rỗng, phải nói cửa hàng chưa có sản phẩm phù hợp. TUYỆT ĐỐI KHÔNG tự tạo ID, tên sản phẩm, giá, giảm giá, tồn kho, link hoặc khối `product`.
            - Chỉ được nêu thông tin sản phẩm xuất hiện nguyên vẹn trong kết quả tool/database.
            - Trả lời bằng Tiếng Việt thân thiện, tự nhiên, ngắn gọn (Tối đa 120 từ).
            """;

    private static final String ADMIN_SYSTEM_PROMPT = """
            Bạn là trợ lý ảo hỗ trợ quản trị và vận hành cửa hàng SportShoe dành cho Admin.

            # PHẠM VI HỖ TRỢ & HÀNH ĐỘNG
            - Thống kê doanh thu, tồn kho, tra cứu hóa đơn, đánh giá, tạo voucher, vẽ biểu đồ.
            - Hướng dẫn sinh hành động cần xác nhận:
              + Xác nhận đơn: [Đồng ý xác nhận](/action/confirm-order/MÃ_HĐ)
              + Hủy đơn: [Đồng ý hủy](/action/cancel-order/MÃ_HĐ)
              + Cập nhật tồn kho: [Đồng ý cập nhật tồn kho](/action/update-stock/TÊN_SẢN_PHẨM/SIZE/MÀU/SỐ_LƯỢNG)
              + Tạo mã: [Đồng ý tạo mã](/action/create-voucher/MÃ/TÊN/LOẠI/GIÁ_TRỊ/ĐƠN_TỐI_THIỂU/GIẢM_TỐI_ĐA/SỐ_LƯỢNG/SỐ_NGÀY)

            # HIỂN THỊ LINK & THẺ SẢN PHẨM
            - Chỉ dùng từ "Số lượng" (không dùng từ "tồn kho").
            - Giữ nguyên khối thẻ sản phẩm ```product ... ``` trong kết quả trả về.
            - Link hóa đơn: [Xem chi tiết hóa đơn](/admin/hoa-don/ID_HOA_DON)

            # NGUYÊN TẮC
            - Mọi số liệu doanh thu, hóa đơn, sản phẩm, số lượng và đánh giá phải lấy từ tool/database.
            - Nếu tool không có dữ liệu, phải trả đúng thông báo không có dữ liệu. Tuyệt đối không tự tạo tên sản phẩm, số liệu, nhận xét, link hoặc khối `product`.
            - Không diễn giải lại hoặc thay đổi nội dung bên trong khối ```product ... ``` do tool trả về.
            - Trả lời bằng Tiếng Việt chuyên nghiệp, ngắn gọn, đi thẳng vào vấn đề.
            """;

    private static final String CLIENT_FREE_CHAT_PROMPT = """
            Bạn là trợ lý mua sắm của SportShoe. Trả lời bằng tiếng Việt thân thiện, rõ ràng, tối đa 70 từ.

            Chỉ tư vấn kiến thức chung về chọn loại giày, đo chân, chọn size, sử dụng và bảo quản giày.
            Các câu hỏi về sản phẩm đang bán, giá, màu, size còn hàng, bán chạy, khuyến mãi và đơn hàng
            đã được hệ thống truy vấn database trước khi tới nhánh này.

            Không tự tạo tên sản phẩm, ID, link, ảnh, giá, số lượng, khuyến mãi hoặc trạng thái đơn hàng.
            Nếu thiếu số đo để chọn size, hướng dẫn khách đo chiều dài bàn chân và hỏi lại số cm.
            Nếu câu hỏi ngoài phạm vi mua sắm, từ chối ngắn gọn và hướng khách quay lại nội dung về giày.
            """;

    private final ChatClient clientChatClient;
    private final ChatClient adminChatClient;

    private final CuocHoiThoaiRepository cuocHoiThoaiRepository;
    private final TinNhanRepository tinNhanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ThongBaoService thongBaoService;
    private final FaqRuleEngine faqRuleEngine;
    private final ChatbotIntentRouter intentRouter;
    private final ClientProductQueryGuard productQueryGuard;
    private final ClientProductResponseSanitizer productResponseSanitizer;
    private final ClientQuickQueryService clientQuickQueryService;
    private final AdminQuickQueryService adminQuickQueryService;

    @Value("${app.debug-errors:false}")
    private boolean debugErrors;

    public ChatbotService(
            org.springframework.ai.chat.model.ChatModel chatModel,
            CuocHoiThoaiRepository cuocHoiThoaiRepository,
            TinNhanRepository tinNhanRepository,
            NhanVienRepository nhanVienRepository,
            WebSocketNotificationService webSocketNotificationService,
            ThongBaoService thongBaoService,
            FaqRuleEngine faqRuleEngine,
            ChatbotIntentRouter intentRouter,
            ClientProductQueryGuard productQueryGuard,
            ClientProductResponseSanitizer productResponseSanitizer,
            ClientQuickQueryService clientQuickQueryService,
            AdminQuickQueryService adminQuickQueryService) {
        this.clientChatClient = ChatClient.builder(chatModel).defaultSystem(CLIENT_FREE_CHAT_PROMPT).build();
        this.adminChatClient = ChatClient.builder(chatModel).defaultSystem(ADMIN_SYSTEM_PROMPT).build();

        this.cuocHoiThoaiRepository = cuocHoiThoaiRepository;
        this.tinNhanRepository = tinNhanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.webSocketNotificationService = webSocketNotificationService;
        this.thongBaoService = thongBaoService;
        this.faqRuleEngine = faqRuleEngine;
        this.intentRouter = intentRouter;
        this.productQueryGuard = productQueryGuard;
        this.productResponseSanitizer = productResponseSanitizer;
        this.clientQuickQueryService = clientQuickQueryService;
        this.adminQuickQueryService = adminQuickQueryService;
    }

    // --- BƯỚC 1: LƯU TIN NHẮN KHÁCH VÀO DB (TRANSACTION NGẮN) ---
    @Transactional
    public CuocHoiThoai prepareClientSessionAndSaveUserMsg(ClientChatRequest request) {
        CuocHoiThoai session = null;
        if (request.sessionId() != null) {
            session = cuocHoiThoaiRepository.findById(request.sessionId()).orElse(null);
            if (session != null && Integer.valueOf(4).equals(session.getTrangThai())) {
                session = null;
            }
        }

        if (session == null) {
            session = new CuocHoiThoai();
            session.setTenKhachHang(request.customerName() != null && !request.customerName().isBlank()
                    ? request.customerName() : "Khách vãng lai");
            session.setSoDienThoai(request.phoneNumber());
            session.setTrangThai(1); // Chat với AI
            session.setNgayTao(Instant.now());
            session = cuocHoiThoaiRepository.save(session);
        }

        String filteredContent = ProfanityFilterUtil.filter(request.message());

        TinNhan khachMsg = new TinNhan();
        khachMsg.setCuocHoiThoai(session);
        khachMsg.setNguoiGui("CUSTOMER");
        khachMsg.setNoiDung(filteredContent);
        khachMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(khachMsg);

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + session.getId(),
                "NEW_MESSAGE",
                new ChatbotMessageDto(khachMsg.getId(), "CUSTOMER", khachMsg.getNoiDung(), khachMsg.getNgayTao(), null)
        );

        return session;
    }

    // --- BƯỚC 2: LƯU PHẢN HỒI AI VÀO DB (TRANSACTION NGẮN) ---
    @Transactional
    public void saveAiMessage(Integer sessionId, String botReply) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId).orElse(null);
        if (session == null || botReply == null) return;

        TinNhan aiMsg = new TinNhan();
        aiMsg.setCuocHoiThoai(session);
        aiMsg.setNguoiGui("AI");
        aiMsg.setNoiDung(botReply);
        aiMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(aiMsg);

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + session.getId(),
                "NEW_MESSAGE",
                new ChatbotMessageDto(aiMsg.getId(), "AI", aiMsg.getNoiDung(), aiMsg.getNgayTao(), null)
        );
    }

    @Transactional
    public void updateSessionToStaffRequest(Integer sessionId, String userMessage) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId).orElse(null);
        if (session == null) return;

        session.setTrangThai(2); // Yêu cầu trợ giúp từ nhân viên
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        String botReply = "Đã gửi yêu cầu kết nối với nhân viên tư vấn. Nhân viên trực sẽ phản hồi bạn trong giây lát!";

        TinNhan aiMsg = new TinNhan();
        aiMsg.setCuocHoiThoai(session);
        aiMsg.setNguoiGui("AI");
        aiMsg.setNoiDung(botReply);
        aiMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(aiMsg);

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

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/sessions",
                "SESSION_UPDATED",
                convertToDto(session)
        );

        guiThongBaoChatMoi(session, userMessage);
    }

    // --- LUỒNG XỬ LÝ CLIENT CHATBOT (KHÔNG KHÓA DB TRANSACTION KHI GỌI AI) ---
    public ClientChatResponse handleClientMessage(ClientChatRequest request) {
        // 1. Lưu tin nhắn người dùng (Tx ngắn)
        CuocHoiThoai session = prepareClientSessionAndSaveUserMsg(request);

        String botReply = null;
        if (session.getTrangThai() == 1) {
            // Check yêu cầu gặp nhân viên
            if ("Tôi muốn gặp nhân viên trực tiếp hỗ trợ".equals(request.message()) ||
                "Liên hệ trực tiếp với nhân viên".equals(request.message())) {
                updateSessionToStaffRequest(session.getId(), request.message());
                botReply = "Đã gửi yêu cầu kết nối với nhân viên tư vấn. Nhân viên trực sẽ phản hồi bạn trong giây lát!";
                return new ClientChatResponse(session.getId(), botReply, 2);
            }

            // Check FAQ Rule Engine (0 token, 0ms AI call)
            String faqAnswer = faqRuleEngine.matchFaq(request.message());
            if (faqAnswer != null) {
                botReply = faqAnswer;
                saveAiMessage(session.getId(), botReply);
                return new ClientChatResponse(session.getId(), botReply, session.getTrangThai());
            }

            // Các truy vấn nghiệp vụ phổ biến lấy dữ liệu trực tiếp từ database.
            // Không đưa qua LLM để tránh tool-call hai lượt và dữ liệu tự suy diễn.
            java.util.Optional<String> quickAnswer =
                    clientQuickQueryService.answerFromDatabase(request.message());
            if (quickAnswer.isPresent()) {
                botReply = quickAnswer.get();
                saveAiMessage(session.getId(), botReply);
                return new ClientChatResponse(session.getId(), botReply, session.getTrangThai());
            }

            // Yêu cầu tìm mua có màu/size rõ ràng được trả lời trực tiếp từ database.
            // Không giao trường hợp "không có sản phẩm" cho LLM để tránh bịa dữ liệu.
            java.util.Optional<String> verifiedProductAnswer =
                    productQueryGuard.answerFromDatabase(request.message());
            if (verifiedProductAnswer.isPresent()) {
                botReply = verifiedProductAnswer.get();
                saveAiMessage(session.getId(), botReply);
                return new ClientChatResponse(session.getId(), botReply, session.getTrangThai());
            }

            // Gọi AI (HTTP REST Call - KHÔNG GIỮ DB TRANSACTION)
            botReply = generateAiResponse(request.message());

            // Lưu tin nhắn AI vào DB (Tx ngắn)
            saveAiMessage(session.getId(), botReply);
        } else {
            notifyStaffNewMessage(session.getId(), request.message());
        }

        return new ClientChatResponse(session.getId(), botReply, session.getTrangThai());
    }

    @Transactional
    public void notifyStaffNewMessage(Integer sessionId, String message) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId).orElse(null);
        if (session != null) {
            webSocketNotificationService.sendToTopic(
                    "/topic/chatbot/sessions",
                    "SESSION_UPDATED",
                    convertToDto(session)
            );
            guiThongBaoChatMoi(session, message);
        }
    }

    @Transactional
    public void requestStaff(Integer sessionId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));
        session.setTrangThai(2);
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        TinNhan sysMsg = new TinNhan();
        sysMsg.setCuocHoiThoai(session);
        sysMsg.setNguoiGui("AI");
        sysMsg.setNoiDung("Khách hàng đã yêu cầu hỗ trợ từ nhân viên.");
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
                2
        );

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/sessions",
                "SESSION_UPDATED",
                convertToDto(session)
        );

        guiThongBaoChatMoi(session, "Khách hàng yêu cầu hỗ trợ trực tiếp từ nhân viên.");
    }

    @Transactional
    public void closeSessionDueToInactivity(Integer sessionId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId).orElse(null);
        if (session == null || Integer.valueOf(4).equals(session.getTrangThai())) {
            return;
        }

        session.setTrangThai(4);
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        TinNhan sysMsg = new TinNhan();
        sysMsg.setCuocHoiThoai(session);
        sysMsg.setNguoiGui("AI");
        sysMsg.setNoiDung("Phiên trò chuyện đã được tự động kết thúc do không có tương tác mới.");
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

    @Transactional
    public ChatbotMessageDto replyFromStaff(Integer sessionId, String message, UUID nhanVienId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));

        if (Integer.valueOf(4).equals(session.getTrangThai())) {
            throw new IllegalArgumentException("Cuộc hội thoại này đã kết thúc, không thể gửi thêm tin nhắn");
        }
        
        NhanVien nv = null;
        if (nhanVienId != null) {
            nv = nhanVienRepository.findById(nhanVienId).orElse(null);
        }

        if (session.getTrangThai() == 1 || session.getTrangThai() == 2) {
            session.setTrangThai(3);
        }
        if (nv != null) {
            session.setNhanVien(nv);
        }
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        String filteredMessage = ProfanityFilterUtil.filter(message);

        TinNhan staffMsg = new TinNhan();
        staffMsg.setCuocHoiThoai(session);
        staffMsg.setNhanVien(nv);
        staffMsg.setNguoiGui("STAFF");
        staffMsg.setNoiDung(filteredMessage);
        staffMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(staffMsg);

        ChatbotMessageDto dto = new ChatbotMessageDto(
                staffMsg.getId(),
                "STAFF",
                staffMsg.getNoiDung(),
                staffMsg.getNgayTao(),
                nv != null ? nv.getMa() : null
        );

        webSocketNotificationService.sendToTopic("/topic/chatbot/session/" + sessionId, "NEW_MESSAGE", dto);
        webSocketNotificationService.sendToTopic("/topic/chatbot/session/" + sessionId, "STATE_CHANGED", session.getTrangThai());
        webSocketNotificationService.sendToTopic("/topic/chatbot/sessions", "SESSION_UPDATED", convertToDto(session));

        return dto;
    }

    @Transactional
    public void closeSession(Integer sessionId) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên chat"));
        session.setTrangThai(4);
        session.setNgayCapNhat(Instant.now());
        cuocHoiThoaiRepository.save(session);

        TinNhan sysMsg = new TinNhan();
        sysMsg.setCuocHoiThoai(session);
        sysMsg.setNguoiGui("AI");
        sysMsg.setNoiDung("Nhân viên tư vấn đã kết thúc phiên hỗ trợ. Cảm ơn bạn!");
        sysMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(sysMsg);

        webSocketNotificationService.sendToTopic(
                "/topic/chatbot/session/" + sessionId,
                "NEW_MESSAGE",
                new ChatbotMessageDto(sysMsg.getId(), "AI", sysMsg.getNoiDung(), sysMsg.getNgayTao(), null)
        );
        webSocketNotificationService.sendToTopic("/topic/chatbot/session/" + sessionId, "STATE_CHANGED", 4);
        webSocketNotificationService.sendToTopic("/topic/chatbot/sessions", "SESSION_UPDATED", convertToDto(session));
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
        return cuocHoiThoaiRepository.findByTrangThaiOrderByNgayTaoDesc(4)
                .stream()
                .map(this::convertToDto)
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

    // --- GỌI CLIENT LLM CHO HỘI THOẠI TỰ DO, KHÔNG ĐĂNG KÝ TOOL ---
    private String generateAiResponse(String userMessage) {
        try {
            String response = clientChatClient.prompt()
                    .system(CLIENT_FREE_CHAT_PROMPT)
                    .user(userMessage)
                    .options(org.springframework.ai.openai.OpenAiChatOptions.builder()
                            .withTemperature(0.3f)
                            .withMaxTokens(100)
                            .build())
                    .call()
                    .content();
            return productResponseSanitizer.sanitize(response);
        } catch (Exception e) {
            System.err.println("[AI CHATBOT ERROR] Lỗi khi gọi AI Chatbot:");
            e.printStackTrace();
            if (debugErrors) {
                return "Lỗi AI Chatbot (Debug): " + e.getMessage() + " | Nguyên nhân chi tiết: " + (e.getCause() != null ? e.getCause().getMessage() : "không có");
            }
            return "Hệ thống tư vấn tự động đang quá tải tạm thời. Bạn có thể đợi vài phút rồi gửi lại tin nhắn, hoặc kết nối ngay với nhân viên hỗ trợ nhé!";
        }
    }

    // --- HELPER LƯU TIN NHẮN ADMIN TRONG TRANSACTION NGẮN ---
    @Transactional
    public CuocHoiThoai prepareAdminSessionAndSaveMsg(UUID nhanVienId, String userMessage) {
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
            session.setNgayTao(Instant.now());
            session = cuocHoiThoaiRepository.save(session);
        }

        TinNhan adminMsg = new TinNhan();
        adminMsg.setCuocHoiThoai(session);
        adminMsg.setNhanVien(nv);
        adminMsg.setNguoiGui("STAFF");
        adminMsg.setNoiDung(userMessage);
        adminMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(adminMsg);

        return session;
    }

    @Transactional(readOnly = true)
    public List<org.springframework.ai.chat.messages.Message> getRecentAdminChatHistory(Integer sessionId, int maxMessages) {
        List<TinNhan> historyMsgs = tinNhanRepository.findByCuocHoiThoaiIdOrderByNgayTaoAsc(sessionId);
        if (historyMsgs.size() > maxMessages) {
            historyMsgs = historyMsgs.subList(historyMsgs.size() - maxMessages, historyMsgs.size());
        }

        List<org.springframework.ai.chat.messages.Message> springAiMsgs = new java.util.ArrayList<>();
        for (TinNhan m : historyMsgs) {
            if ("STAFF".equals(m.getNguoiGui())) {
                springAiMsgs.add(new org.springframework.ai.chat.messages.UserMessage(m.getNoiDung()));
            } else if ("AI".equals(m.getNguoiGui())) {
                springAiMsgs.add(new org.springframework.ai.chat.messages.AssistantMessage(m.getNoiDung()));
            }
        }
        return springAiMsgs;
    }

    @Transactional
    public void saveAdminAiReply(Integer sessionId, String reply) {
        CuocHoiThoai session = cuocHoiThoaiRepository.findById(sessionId).orElse(null);
        if (session == null || reply == null) return;

        TinNhan aiMsg = new TinNhan();
        aiMsg.setCuocHoiThoai(session);
        aiMsg.setNguoiGui("AI");
        aiMsg.setNoiDung(reply);
        aiMsg.setNgayTao(Instant.now());
        tinNhanRepository.save(aiMsg);
    }

    // --- GOI ADMIN LLM (HTTP CALL - KHÔNG GIỮ DB TRANSACTION, CHATCLIENT DUY NHẤT & PROMPT CACHING) ---
    public String generateAdminAiResponse(UUID nhanVienId, String userMessage) {
        // 1. Check FAQ Rule Engine trước
        String faqMatch = faqRuleEngine.matchFaq(userMessage);
        if (faqMatch != null) {
            CuocHoiThoai session = prepareAdminSessionAndSaveMsg(nhanVienId, userMessage);
            saveAdminAiReply(session.getId(), faqMatch);
            return faqMatch;
        }

        // 2. Lưu tin nhắn Admin (Tx ngắn)
        CuocHoiThoai session = prepareAdminSessionAndSaveMsg(nhanVienId, userMessage);

        // Các thống kê nhanh chỉ-đọc phải trả nguyên dữ liệu từ DB, không để LLM diễn giải hoặc bịa thêm.
        java.util.Optional<String> quickReply = adminQuickQueryService.answer(userMessage);
        if (quickReply.isPresent()) {
            saveAdminAiReply(session.getId(), quickReply.get());
            return quickReply.get();
        }

        // 3. Load 6 tin nhắn gần nhất để tiết kiệm token (Tx read-only ngắn)
        List<org.springframework.ai.chat.messages.Message> historyMsgs = getRecentAdminChatHistory(session.getId(), 6);

        try {
            // Intent Router: Chọn đúng tập Tool liên quan tới câu hỏi
            String[] activeTools = intentRouter.resolveAdminTools(userMessage);

            String reply = adminChatClient.prompt()
                    .system(ADMIN_SYSTEM_PROMPT)
                    .messages(historyMsgs)
                    .functions(activeTools)
                    .call()
                    .content();

            // 4. Lưu tin nhắn AI (Tx ngắn)
            saveAdminAiReply(session.getId(), reply);

            return reply;
        } catch (Exception e) {
            System.err.println("[ADMIN CHATBOT ERROR] Lỗi khi gọi Admin AI: " + e.getMessage());
            e.printStackTrace();
            String errReply = "Hiện không thể kết nối cả AI cloud lẫn AI local. Vui lòng kiểm tra API key, trạng thái Ollama hoặc thử lại sau.";
            saveAdminAiReply(session.getId(), errReply);
            return errReply;
        }
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageDto> getAdminChatHistory(UUID nhanVienId) {
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
    public void closeAdminAiSession(UUID nhanVienId) {
        List<CuocHoiThoai> activeSessions = cuocHoiThoaiRepository.findByNhanVienIdAndTrangThai(nhanVienId, 1);
        for (CuocHoiThoai session : activeSessions) {
            session.setTrangThai(4);
            session.setNgayCapNhat(Instant.now());
            cuocHoiThoaiRepository.save(session);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatbotSessionDto> getAdminAiSessions(UUID nhanVienId) {
        return cuocHoiThoaiRepository.findByNhanVienIdOrderByNgayTaoDesc(nhanVienId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageDto> getAdminAiSessionMessages(UUID nhanVienId, Integer sessionId) {
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
