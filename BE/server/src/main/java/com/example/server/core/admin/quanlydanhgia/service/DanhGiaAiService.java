package com.example.server.core.admin.quanlydanhgia.service;

import com.example.server.core.admin.thongbao.service.ThongBaoService;
import com.example.server.entity.DanhGia;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.DanhGiaRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * AI cho phần đánh giá (dùng chung hạ tầng ChatModel với chatbot - Gemini/OpenAI fallback):
 *  - Kiểm duyệt: tự ẩn đánh giá độc hại/xúc phạm/spam/không liên quan đến sản phẩm (chạy nền sau khi khách gửi).
 *  - Tổng hợp: đọc các đánh giá của 1 sản phẩm (hoặc cả shop) rồi đưa ra nhận xét tổng thể + lời khuyên cải thiện.
 */
@Service
public class DanhGiaAiService {

    private static final int GIOI_HAN_DANH_GIA_TONG_HOP = 100;
    private static final int GIOI_HAN_KY_TU_MOI_DANH_GIA = 300;

    // Lấy lười (ObjectProvider) để việc tạo bean này KHÔNG kéo theo cả chuỗi AI/ChatModel lúc khởi động
    // (tránh vòng phụ thuộc + chỉ khởi tạo AI khi thật sự gọi).
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final DanhGiaRepository danhGiaRepository;
    private final ThongBaoService thongBaoService;

    public DanhGiaAiService(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            DanhGiaRepository danhGiaRepository,
            ThongBaoService thongBaoService) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.danhGiaRepository = danhGiaRepository;
        this.thongBaoService = thongBaoService;
    }

    /** ChatClient dựng mới mỗi lần gọi (builder được lấy lười lúc dùng, không lúc khởi động). */
    private ChatClient.Builder chatClient() {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new BusinessException("Chưa cấu hình AI (thiếu API key), vui lòng thử lại sau");
        }
        return builder;
    }

    // ─── 1. Kiểm duyệt tự động ──────────────────────────────────────────────

    /**
     * Đăng ký kiểm duyệt 1 đánh giá SAU khi transaction tạo đánh giá commit
     * (chạy nền, không làm chậm việc gửi đánh giá; AI lỗi thì đánh giá vẫn hiển thị).
     */
    public void kiemDuyetSauCommit(Integer danhGiaId, Integer soSao, String noiDung) {
        if (noiDung == null || noiDung.isBlank()) {
            return; // chỉ chấm sao, không có chữ -> không có gì để kiểm duyệt
        }
        Runnable chayNen = () -> CompletableFuture.runAsync(() -> kiemDuyet(danhGiaId, soSao, noiDung));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    chayNen.run();
                }
            });
        } else {
            chayNen.run();
        }
    }

    private void kiemDuyet(Integer danhGiaId, Integer soSao, String noiDung) {
        try {
            String traLoi = chatClient().build().prompt()
                    .system("""
                            Bạn là bộ lọc kiểm duyệt đánh giá cho một cửa hàng giày thể thao online.
                            Nhiệm vụ: quyết định 1 đánh giá có được HIỂN THỊ công khai hay phải ẨN.
                            PHẢI ẨN nếu đánh giá: chửi bới, tục tĩu, xúc phạm, công kích cá nhân;
                            hoặc là spam/quảng cáo/chèn link; hoặc nội dung KHÔNG liên quan gì đến
                            giày/sản phẩm/dịch vụ mua hàng.
                            KHÔNG ẨN nếu chỉ là lời chê, góp ý, phàn nàn bình thường về sản phẩm hay dịch vụ
                            (kể cả đánh giá 1 sao) - khách có quyền chê.
                            Trả lời ĐÚNG 1 dòng, không giải thích thêm:
                            - "OK" nếu được hiển thị.
                            - "AN|<lý do rất ngắn gọn bằng tiếng Việt>" nếu phải ẩn.
                            """)
                    .user("Đánh giá (" + (soSao == null ? "?" : soSao) + "/5 sao): \"" + noiDung + "\"")
                    .call()
                    .content();

            String kq = traLoi == null ? "" : traLoi.trim();
            System.out.println("[AI DANH GIA] Kiểm duyệt #" + danhGiaId + " -> " + kq);
            if (!kq.toUpperCase(java.util.Locale.ROOT).startsWith("AN")) {
                guiThongBaoDanhGiaMoi(danhGiaId);
                return; // OK -> giữ nguyên
            }
            String lyDo = kq.contains("|") ? kq.substring(kq.indexOf('|') + 1).trim() : "Nội dung không phù hợp";
            danhGiaRepository.findById(danhGiaId).ifPresent(dg -> {
                dg.setTrangThai(0);
                dg.setLyDoAn("AI tự ẩn: " + (lyDo.length() > 400 ? lyDo.substring(0, 400) : lyDo));
                dg.setNgayCapNhat(Instant.now());
                danhGiaRepository.save(dg);
            });
        } catch (Exception e) {
            // AI lỗi/hết hạn mức -> bỏ qua, đánh giá vẫn hiển thị bình thường.
            System.err.println("[AI DANH GIA] Lỗi kiểm duyệt #" + danhGiaId + ": " + e.getMessage());
            guiThongBaoDanhGiaMoi(danhGiaId);
        }
    }

    private void guiThongBaoDanhGiaMoi(Integer danhGiaId) {
        try {
            danhGiaRepository.findById(danhGiaId).ifPresent(dg -> {
                String noiDung = dg.getNoiDung() != null ? dg.getNoiDung() : "(Chỉ chấm sao)";
                if (noiDung.length() > 80) {
                    noiDung = noiDung.substring(0, 77) + "...";
                }
                thongBaoService.taoThongBao(
                        "Đánh giá sản phẩm mới",
                        "Khách " + dg.getKhachHang().getHoTen() + " đánh giá " + dg.getSoSao() + " sao: " + noiDung,
                        "REVIEW",
                        "/admin/danh-gia"
                );
            });
        } catch (Exception ex) {
            System.err.println("[AI DANH GIA] Lỗi gửi thông báo đánh giá: " + ex.getMessage());
        }
    }

    // ─── 2. Tổng hợp đánh giá ───────────────────────────────────────────────

    /**
     * Tổng hợp đánh giá bằng AI: nhận xét tổng thể + điểm khách khen/chê + lời khuyên cải thiện.
     *
     * @param giayId       null = tổng hợp toàn shop; khác null = tổng hợp 1 sản phẩm.
     * @param tenSanPham   tên sản phẩm (chỉ để đưa vào prompt khi tổng hợp 1 sản phẩm).
     */
    public String tongHop(Integer giayId, String tenSanPham) {
        List<DanhGia> ds = giayId == null
                ? danhGiaRepository.findTatCaCongKhai()
                : danhGiaRepository.findByGiayId(giayId);
        if (ds.isEmpty()) {
            throw new BusinessException("Chưa có đánh giá nào để tổng hợp");
        }

        StringBuilder duLieu = new StringBuilder();
        ds.stream().limit(GIOI_HAN_DANH_GIA_TONG_HOP).forEach(dg -> {
            String nd = dg.getNoiDung() == null ? "(chỉ chấm sao)" : dg.getNoiDung().trim();
            if (nd.length() > GIOI_HAN_KY_TU_MOI_DANH_GIA) {
                nd = nd.substring(0, GIOI_HAN_KY_TU_MOI_DANH_GIA) + "...";
            }
            duLieu.append("- (").append(dg.getSoSao()).append("/5 sao) ").append(nd).append('\n');
        });
        double diemTb = ds.stream().mapToInt(DanhGia::getSoSao).average().orElse(0);

        String doiTuong = giayId == null
                ? "toàn bộ cửa hàng giày thể thao"
                : "sản phẩm \"" + (tenSanPham == null ? ("#" + giayId) : tenSanPham) + "\"";
        try {
            return chatClient().build().prompt()
                    .system("""
                            Bạn là trợ lý phân tích đánh giá khách hàng cho cửa hàng giày thể thao online.
                            Trả lời bằng tiếng Việt, ngắn gọn, đúng cấu trúc sau (dùng đúng các tiêu đề này):
                            NHẬN XÉT TỔNG THỂ: 2-3 câu về cảm nhận chung của khách.
                            ĐIỂM KHÁCH KHEN: liệt kê 2-4 gạch đầu dòng.
                            ĐIỂM KHÁCH CHÊ: liệt kê 2-4 gạch đầu dòng (nếu không có thì ghi "Chưa ghi nhận").
                            KHUYẾN NGHỊ CẢI THIỆN: 2-4 gạch đầu dòng hành động cụ thể shop nên làm.
                            Chỉ dựa trên dữ liệu được cung cấp, không bịa thêm.
                            """)
                    .user("Tổng hợp " + ds.size() + " đánh giá (điểm trung bình "
                            + Math.round(diemTb * 10.0) / 10.0 + "/5) của " + doiTuong + ":\n" + duLieu)
                    .call()
                    .content();
        } catch (Exception e) {
            System.err.println("[AI DANH GIA] Lỗi tổng hợp: " + e.getMessage());
            throw new BusinessException("AI đang bận hoặc chưa cấu hình API key, vui lòng thử lại sau");
        }
    }
}
