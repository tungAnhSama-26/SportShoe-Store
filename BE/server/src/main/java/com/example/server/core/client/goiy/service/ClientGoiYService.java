package com.example.server.core.client.goiy.service;

import com.example.server.core.client.goiy.dto.GoiYDtos.CauHoiResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.GoiYRequest;
import com.example.server.core.client.goiy.dto.GoiYDtos.GoiYResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.SanPhamGoiYResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.TraLoiRequest;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.HinhAnhGiayRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * Gợi ý giày cho khách bằng AI: khách trả lời 5 câu trắc nghiệm (chọn nhiều đáp án),
 * kèm ảnh outfit không bắt buộc. AI chỉ được chọn trong danh sách sản phẩm đang bán
 * do BE cung cấp nên không thể bịa ra giày không có thật.
 */
@Service
public class ClientGoiYService {

    /** Hạn mức token phải để rộng: gemini-2.5-flash tiêu token cho phần "suy nghĩ" ngầm,
     *  để thấp sẽ trả về nội dung RỖNG (finish_reason=length). */
    private static final int GIOI_HAN_TOKEN = 2500;
    private static final int SO_SAN_PHAM_UNG_VIEN = 45;
    private static final int SO_SAN_PHAM_GOI_Y = 4;

    /** Mã câu hỏi kích cỡ - xử lý riêng: dùng để LỌC sản phẩm chứ không chỉ đưa cho AI đọc. */
    private static final String MA_KICH_CO = "kich-co";

    private static final List<CauHoiResponse> CAC_CAU_HOI = List.of(
            new CauHoiResponse("muc-dich", "Bạn mua giày để dùng vào việc gì?",
                    "Chọn được nhiều đáp án",
                    List.of("Đi học, đi làm hằng ngày", "Chơi thể thao, chạy bộ",
                            "Đi chơi, dạo phố, cà phê", "Dự tiệc, sự kiện")),
            new CauHoiResponse("phong-cach", "Phong cách bạn thích là gì?",
                    "Chọn được nhiều đáp án",
                    List.of("Năng động, thể thao", "Đơn giản, tối giản",
                            "Cá tính, nổi bật", "Cổ điển, retro")),
            new CauHoiResponse("mau-sac", "Bạn hay mặc tông màu nào?",
                    "Chọn được nhiều đáp án",
                    List.of("Trắng, kem, be", "Đen, xám, navy",
                            "Màu nổi (đỏ, xanh, vàng)", "Pastel nhẹ nhàng")),
            new CauHoiResponse("uu-tien", "Bạn ưu tiên điều gì ở đôi giày?",
                    "Chọn được nhiều đáp án",
                    List.of("Êm chân, đi lâu không mỏi", "Nhẹ, thoáng khí",
                            "Bền, dễ vệ sinh", "Kiểu dáng đẹp là chính")),
            new CauHoiResponse("ngan-sach", "Tầm giá bạn muốn?",
                    "Chọn được nhiều đáp án",
                    List.of("Dưới 1 triệu", "1 - 2 triệu", "2 - 3 triệu", "Trên 3 triệu"))
    );

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final EntityManager entityManager;
    private final HinhAnhGiayRepository hinhAnhGiayRepository;

    public ClientGoiYService(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            EntityManager entityManager,
            HinhAnhGiayRepository hinhAnhGiayRepository
    ) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.entityManager = entityManager;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
    }

    /**
     * Bộ câu hỏi = 5 câu cố định + 1 câu kích cỡ dựng động từ các size ĐANG CÒN HÀNG,
     * để khách không chọn phải size mà cửa hàng không có.
     */
    @Transactional(readOnly = true)
    public List<CauHoiResponse> layCauHoi() {
        List<CauHoiResponse> ds = new ArrayList<>(CAC_CAU_HOI);
        List<String> sizes = laySizeConHang();
        if (!sizes.isEmpty()) {
            ds.add(new CauHoiResponse(MA_KICH_CO, "Bạn đi giày size bao nhiêu?",
                    "Chọn được nhiều size nếu bạn nằm giữa hai cỡ", sizes));
        }
        return ds;
    }

    /** Các size còn hàng thật (biến thể đang bán + tồn > 0), sắp xếp tăng dần. */
    @SuppressWarnings("unchecked")
    private List<String> laySizeConHang() {
        List<String> sizes = entityManager.createQuery("""
                select distinct kc.giaTri
                from GiayChiTiet gct
                  join gct.kichCo kc
                  join gct.giay g
                where g.trangThai = 1 and gct.kichHoat = 1 and gct.soLuong > 0
                """).getResultList();
        List<String> kq = new ArrayList<>(sizes);
        // Size là chuỗi ("40", "41") -> sắp theo số cho đúng thứ tự hiển thị.
        kq.sort((a, b) -> {
            try {
                return Double.compare(Double.parseDouble(a.trim()), Double.parseDouble(b.trim()));
            } catch (NumberFormatException e) {
                return a.compareToIgnoreCase(b);
            }
        });
        return kq;
    }

    /** Gom biến thể đang bán thành danh sách sản phẩm ứng viên cho AI chọn. */
    private record UngVien(Integer id, String ma, String ten, String thuongHieu,
                           String loai, BigDecimal giaThapNhat, List<String> mauSac) {}

    @Transactional(readOnly = true)
    public GoiYResponse goiY(GoiYRequest request) {
        if (request == null || request.traLoi() == null || request.traLoi().isEmpty()) {
            throw new BusinessException("Vui lòng trả lời ít nhất một câu hỏi");
        }

        // Size khách chọn -> lọc cứng danh sách, chỉ gợi ý giày còn đúng size đó.
        List<String> sizeChon = request.traLoi().stream()
                .filter(t -> MA_KICH_CO.equals(t.ma()) && t.daChon() != null)
                .flatMap(t -> t.daChon().stream())
                .distinct()
                .toList();

        List<UngVien> ungVien = layUngVien(sizeChon);
        if (ungVien.isEmpty()) {
            throw new BusinessException(sizeChon.isEmpty()
                    ? "Cửa hàng chưa có sản phẩm nào đang bán để gợi ý"
                    : "Không còn giày nào cỡ " + String.join(", ", sizeChon) + ". Bạn thử chọn size khác nhé.");
        }

        String traLoi = motTaTraLoi(request.traLoi());
        String danhSach = motTaUngVien(ungVien);

        String noiDung = goiAi(traLoi, danhSach, request.anhOutfit());
        return phanTichKetQua(noiDung, ungVien);
    }

    // ─── Lấy sản phẩm đang bán ───────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<UngVien> layUngVien(List<String> sizeChon) {
        boolean locSize = sizeChon != null && !sizeChon.isEmpty();
        String jpql = """
                select g.id, g.ma, g.ten, th.ten, lg.ten, gct.giaBan, ms.ten
                from GiayChiTiet gct
                  join gct.giay g
                  join g.thuongHieu th
                  join g.loaiGiay lg
                  join gct.mauSac ms
                  join gct.kichCo kc
                where g.trangThai = 1 and gct.kichHoat = 1 and gct.soLuong > 0
                """ + (locSize ? " and kc.giaTri in :sizes" : "");

        var query = entityManager.createQuery(jpql).setMaxResults(500);
        if (locSize) {
            query.setParameter("sizes", sizeChon);
        }
        List<Object[]> rows = query.getResultList();

        Map<Integer, UngVien> gom = new LinkedHashMap<>();
        for (Object[] r : rows) {
            Integer id = (Integer) r[0];
            BigDecimal gia = (BigDecimal) r[5];
            String mau = (String) r[6];
            UngVien cu = gom.get(id);
            if (cu == null) {
                List<String> mauSac = new ArrayList<>();
                if (mau != null) {
                    mauSac.add(mau);
                }
                gom.put(id, new UngVien(id, (String) r[1], (String) r[2],
                        (String) r[3], (String) r[4], gia, mauSac));
            } else {
                if (mau != null && !cu.mauSac().contains(mau)) {
                    cu.mauSac().add(mau);
                }
                if (gia != null && (cu.giaThapNhat() == null || gia.compareTo(cu.giaThapNhat()) < 0)) {
                    gom.put(id, new UngVien(cu.id(), cu.ma(), cu.ten(), cu.thuongHieu(),
                            cu.loai(), gia, cu.mauSac()));
                }
            }
            if (gom.size() >= SO_SAN_PHAM_UNG_VIEN) {
                break;
            }
        }
        return new ArrayList<>(gom.values());
    }

    // ─── Dựng prompt ─────────────────────────────────────────────────────────

    private String motTaTraLoi(List<TraLoiRequest> traLoi) {
        Map<String, CauHoiResponse> theoMa = new LinkedHashMap<>();
        CAC_CAU_HOI.forEach(c -> theoMa.put(c.ma(), c));

        StringBuilder sb = new StringBuilder();
        for (TraLoiRequest t : traLoi) {
            if (t.daChon() == null || t.daChon().isEmpty()) {
                continue;
            }
            CauHoiResponse ch = theoMa.get(t.ma());
            sb.append("- ").append(ch != null ? ch.cauHoi() : t.ma())
              .append(" -> ").append(String.join(", ", t.daChon())).append('\n');
        }
        return sb.length() == 0 ? "(khách chưa chọn gì)" : sb.toString();
    }

    private String motTaUngVien(List<UngVien> ds) {
        StringBuilder sb = new StringBuilder();
        for (UngVien u : ds) {
            sb.append("[id=").append(u.id()).append("] ").append(u.ten())
              .append(" | Hãng: ").append(u.thuongHieu())
              .append(" | Loại: ").append(u.loai())
              .append(" | Giá từ: ").append(u.giaThapNhat() == null ? "?" : u.giaThapNhat().toPlainString())
              .append("đ | Màu: ").append(String.join(", ", u.mauSac()))
              .append('\n');
        }
        return sb.toString();
    }

    // ─── Gọi AI ──────────────────────────────────────────────────────────────

    private String goiAi(String traLoi, String danhSach, String anhOutfit) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new BusinessException("Chưa cấu hình AI (thiếu API key), vui lòng thử lại sau");
        }

        String heThong = """
                Bạn là chuyên viên tư vấn giày thể thao của một cửa hàng online tại Việt Nam.
                Dựa vào câu trả lời của khách (và ảnh outfit nếu có), hãy chọn ra %d đôi giày
                PHÙ HỢP NHẤT trong DANH SÁCH SẢN PHẨM được cung cấp.

                RÀNG BUỘC:
                - CHỈ được chọn giày có trong danh sách, dùng đúng id đã cho. Tuyệt đối không bịa.
                - XẾP THEO ĐỘ PHÙ HỢP GIẢM DẦN: dòng SP đầu tiên là đôi hợp nhất với khách.
                - Trả lời bằng tiếng Việt, thân thiện, ngắn gọn.

                Trả về ĐÚNG định dạng sau, không thêm gì khác:
                NHAN_XET: <1-2 câu nhận xét về outfit trong ảnh; nếu không có ảnh thì ghi đúng chữ: khong>
                LOI_KHUYEN: <2-3 câu tư vấn chung dựa trên lựa chọn của khách>
                SP: <id>|<lý do hợp với khách; dòng ĐẦU viết kỹ hơn 1-2 câu vì là đôi hợp nhất>
                SP: <id>|<lý do hợp với khách, 1 câu ngắn>
                """.formatted(SO_SAN_PHAM_GOI_Y);

        String cauHoiNguoiDung = "LỰA CHỌN CỦA KHÁCH:\n" + traLoi
                + "\nDANH SÁCH SẢN PHẨM ĐANG BÁN:\n" + danhSach;

        OpenAiChatOptions tuyChon = OpenAiChatOptions.builder()
                .withMaxTokens(GIOI_HAN_TOKEN)
                .build();

        try {
            var spec = builder.build().prompt()
                    .system(heThong)
                    .options(tuyChon);

            byte[] anh = giaiMaAnh(anhOutfit);
            String kq;
            if (anh != null) {
                // Spring AI 1.0.0-M1: UserSpec.media(MimeType, Resource) -> gửi ảnh kèm câu hỏi.
                MimeType kieu = doanKieuAnh(anhOutfit);
                kq = spec.user(u -> u.text(cauHoiNguoiDung + "\n(Khách có gửi kèm ảnh outfit ở dưới)")
                                .media(kieu, new ByteArrayResource(anh)))
                        .call().content();
            } else {
                kq = spec.user(cauHoiNguoiDung).call().content();
            }
            if (kq == null || kq.isBlank()) {
                throw new BusinessException("AI không trả về nội dung, vui lòng thử lại");
            }
            return kq;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[AI GOI Y] Lỗi gọi AI: " + e.getMessage());
            e.printStackTrace();
            throw new BusinessException("AI đang bận hoặc ảnh tải lên không được hỗ trợ (vi phạm chính sách nội dung). Vui lòng thử lại ảnh khác hoặc bỏ qua bước tải ảnh!");
        }
    }

    /** Ảnh FE gửi lên dạng data URI base64; trả null nếu khách không gửi ảnh. */
    private byte[] giaiMaAnh(String anhOutfit) {
        if (anhOutfit == null || anhOutfit.isBlank()) {
            return null;
        }
        try {
            String base64 = anhOutfit.contains(",")
                    ? anhOutfit.substring(anhOutfit.indexOf(',') + 1)
                    : anhOutfit;
            byte[] du = Base64.getDecoder().decode(base64.trim());
            return du.length == 0 ? null : du;
        } catch (Exception e) {
            System.err.println("[AI GOI Y] Ảnh outfit không hợp lệ, bỏ qua: " + e.getMessage());
            return null;
        }
    }

    private MimeType doanKieuAnh(String dataUri) {
        String d = dataUri == null ? "" : dataUri.toLowerCase(java.util.Locale.ROOT);
        if (d.startsWith("data:image/png")) {
            return MimeTypeUtils.IMAGE_PNG;
        }
        if (d.startsWith("data:image/webp")) {
            return MimeType.valueOf("image/webp");
        }
        return MimeTypeUtils.IMAGE_JPEG;
    }

    // ─── Đọc kết quả AI ──────────────────────────────────────────────────────

    private GoiYResponse phanTichKetQua(String noiDung, List<UngVien> ungVien) {
        Map<Integer, UngVien> theoId = new LinkedHashMap<>();
        ungVien.forEach(u -> theoId.put(u.id(), u));

        String nhanXet = null;
        String loiKhuyen = null;
        List<Integer> ids = new ArrayList<>();
        Map<Integer, String> lyDoTheoId = new LinkedHashMap<>();

        for (String dong : noiDung.split("\\r?\\n")) {
            String d = dong.trim();
            if (d.regionMatches(true, 0, "NHAN_XET:", 0, 9)) {
                String v = d.substring(9).trim();
                nhanXet = v.equalsIgnoreCase("khong") || v.isBlank() ? null : v;
            } else if (d.regionMatches(true, 0, "LOI_KHUYEN:", 0, 11)) {
                loiKhuyen = d.substring(11).trim();
            } else if (d.regionMatches(true, 0, "SP:", 0, 3)) {
                String v = d.substring(3).trim();
                String phanId = v.contains("|") ? v.substring(0, v.indexOf('|')) : v;
                String lyDo = v.contains("|") ? v.substring(v.indexOf('|') + 1).trim() : "";
                try {
                    Integer id = Integer.valueOf(phanId.replaceAll("[^0-9]", "").trim());
                    // Chỉ nhận id có thật trong danh sách ứng viên -> chặn AI bịa sản phẩm.
                    if (theoId.containsKey(id) && !ids.contains(id)) {
                        ids.add(id);
                        lyDoTheoId.put(id, lyDo);
                    }
                } catch (NumberFormatException ignored) {
                    // dòng SP hỏng -> bỏ qua
                }
            }
        }

        if (ids.isEmpty()) {
            throw new BusinessException("AI chưa chọn được sản phẩm phù hợp, bạn thử chọn lại đáp án nhé");
        }

        // Ảnh sản phẩm lấy từ biến thể (giay.hinhAnh là field cũ, hầu như luôn null).
        Map<Integer, String> anhChinh = new LinkedHashMap<>();
        for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayIds(ids)) {
            anhChinh.putIfAbsent((Integer) row[0], (String) row[1]);
        }

        List<SanPhamGoiYResponse> sanPhams = new ArrayList<>();
        for (Integer id : ids) {
            UngVien u = theoId.get(id);
            sanPhams.add(new SanPhamGoiYResponse(
                    u.id(), u.ma(), u.ten(), u.giaThapNhat(),
                    anhChinh.get(id), lyDoTheoId.getOrDefault(id, "")));
        }

        return new GoiYResponse(
                loiKhuyen == null || loiKhuyen.isBlank()
                        ? "Dưới đây là những đôi mình thấy hợp với bạn nhất."
                        : loiKhuyen,
                nhanXet,
                sanPhams);
    }
}
