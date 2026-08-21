package com.example.server.core.client.goiy.service;

import com.example.server.core.client.goiy.dto.GoiYDtos.CauHoiResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.GoiYRequest;
import com.example.server.core.client.goiy.dto.GoiYDtos.GoiYResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.SanPhamGoiYResponse;
import com.example.server.core.client.goiy.dto.GoiYDtos.TraLoiRequest;
import com.example.server.entity.LoaiGiay;
import com.example.server.infrastructure.exception.BusinessException;
import com.example.server.repository.HinhAnhGiayRepository;
import com.example.server.repository.LoaiGiayRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gợi ý giày (HYBRID, KẾT QUẢ ỔN ĐỊNH): CODE chấm điểm các tiêu chí khách quan (mục đích,
 * phong cách, màu, ưu tiên êm/nhẹ/bền, ngân sách) dựa trên thuộc tính thật, rồi CODE tự lấy
 * top sản phẩm theo điểm -> cùng câu trả lời luôn cho ra CÙNG sản phẩm, cùng thứ tự (không
 * để AI chọn nên không bị ngẫu nhiên). Loại giày được AI phân loại 1 lần rồi cache ở DB.
 */
@Service
public class ClientGoiYService {

    /** Token phải để rộng: gemini-2.5-flash tốn token cho phần "suy nghĩ" ngầm; thấp quá -> nội dung RỖNG. */
    private static final int GIOI_HAN_TOKEN = 2500;
    private static final int SO_UNG_VIEN = 60;
    private static final int SO_SAN_PHAM_GOI_Y = 4;   // số đôi gợi ý cuối cùng

    private static final int NGUONG_NHE_GAM = 310;    // <= 310g coi là nhẹ
    private static final String DEM_CO_BAN = "Standard EVA"; // đệm thường; khác cái này coi là đệm êm

    // ─── Bảng map hằng số (dùng giá trị thật trong DB) ──────────────────────

    private static final Map<String, String> MA_MUC_DICH = Map.of(
            "Đi học, đi làm hằng ngày", "di-lam",
            "Chơi thể thao, chạy bộ", "the-thao",
            "Đi chơi, dạo phố, cà phê", "dao-pho",
            "Dự tiệc, sự kiện", "du-tiec");

    private static final Map<String, String> MA_PHONG_CACH = Map.of(
            "Năng động, thể thao", "nang-dong",
            "Đơn giản, tối giản", "toi-gian",
            "Cá tính, nổi bật", "ca-tinh",
            "Cổ điển, retro", "co-dien");

    private static final Map<String, Set<String>> MAU_THEO_NHOM = Map.of(
            "Trắng, kem, be", Set.of("Trắng", "Kem"),
            "Đen, xám, navy", Set.of("Đen", "Xám", "Xanh Navy"),
            "Màu nổi (đỏ, xanh, vàng)", Set.of("Đỏ", "Cam", "Xanh Lá"),
            "Pastel nhẹ nhàng", Set.of("Hồng", "Kem"));

    private static final Set<String> CHAT_LIEU_BEN =
            Set.of("Da thật", "Da tổng hợp", "TPU", "Rubber Upper", "Suede");
    private static final Set<String> CHAT_LIEU_THOANG =
            Set.of("Mesh", "Knit", "Vải dệt", "Nylon", "Canvas");

    private static final Set<String> MA_MUC_DICH_HOP_LE = Set.of("di-lam", "the-thao", "dao-pho", "du-tiec");
    private static final Set<String> MA_PHONG_CACH_HOP_LE = Set.of("nang-dong", "toi-gian", "ca-tinh", "co-dien");

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
    private final LoaiGiayRepository loaiGiayRepository;

    public ClientGoiYService(
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
            EntityManager entityManager,
            HinhAnhGiayRepository hinhAnhGiayRepository,
            LoaiGiayRepository loaiGiayRepository
    ) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.entityManager = entityManager;
        this.hinhAnhGiayRepository = hinhAnhGiayRepository;
        this.loaiGiayRepository = loaiGiayRepository;
    }

    // ─── Câu hỏi ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CauHoiResponse> layCauHoi() {
        return CAC_CAU_HOI;
    }

    // ─── Ứng viên + thuộc tính ───────────────────────────────────────────────

    private record UngVien(Integer id, String ma, String ten, String thuongHieu,
                           Integer loaiId, String loai, BigDecimal giaThapNhat, List<String> mauSac,
                           String congNgheDem, Integer trongLuong, String chatLieu) {}

    private record KetQuaCham(int diem, List<String> tieuChiKhop) {}

    @Transactional
    public GoiYResponse goiY(GoiYRequest request) {
        if (request == null || request.traLoi() == null || request.traLoi().isEmpty()) {
            throw new BusinessException("Vui lòng trả lời ít nhất một câu hỏi");
        }

        Map<String, Set<String>> traLoiTheoCau = gomTraLoi(request.traLoi());

        List<UngVien> ungVien = layUngVien(List.of());
        if (ungVien.isEmpty()) {
            throw new BusinessException("Cửa hàng chưa có sản phẩm nào đang bán để gợi ý");
        }

        Map<Integer, String[]> phanLoai = boDamPhanLoai(ungVien);

        // Chấm điểm bằng CODE -> xếp giảm dần (điểm cao trước, id nhỏ trước) => THỨ TỰ CỐ ĐỊNH.
        Map<Integer, KetQuaCham> cham = new HashMap<>();
        for (UngVien u : ungVien) {
            String[] nl = phanLoai.getOrDefault(u.loaiId(), new String[]{"", ""});
            cham.put(u.id(), chamDiem(u, traLoiTheoCau, nl[0], nl[1]));
        }
        List<UngVien> xepHang = new ArrayList<>(ungVien);
        xepHang.sort(Comparator.comparingInt((UngVien u) -> cham.get(u.id()).diem()).reversed()
                .thenComparingInt(UngVien::id));

        // CODE tự lấy top N theo điểm -> KHÔNG để AI chọn, nên cùng câu trả lời luôn ra CÙNG sản phẩm.
        List<UngVien> chon = xepHang.subList(0, Math.min(SO_SAN_PHAM_GOI_Y, xepHang.size()));

        List<Integer> ids = chon.stream().map(UngVien::id).toList();
        Map<Integer, String> anhChinh = new LinkedHashMap<>();
        for (Object[] row : hinhAnhGiayRepository.findMainImageUrlsByGiayIds(ids)) {
            anhChinh.putIfAbsent((Integer) row[0], (String) row[1]);
        }
        List<SanPhamGoiYResponse> sanPhams = new ArrayList<>();
        for (UngVien u : chon) {
            sanPhams.add(new SanPhamGoiYResponse(u.id(), u.ma(), u.ten(), u.giaThapNhat(),
                    anhChinh.get(u.id()), lyDoTuTieuChi(cham.get(u.id()))));
        }

        return new GoiYResponse(
                "Đây là những đôi khớp nhiều tiêu chí bạn chọn nhất, xếp từ phù hợp nhất xuống.",
                sanPhams);
    }

    private Map<String, Set<String>> gomTraLoi(List<TraLoiRequest> traLoi) {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        for (TraLoiRequest t : traLoi) {
            if (t.ma() == null || t.daChon() == null) {
                continue;
            }
            map.computeIfAbsent(t.ma(), k -> new HashSet<>()).addAll(t.daChon());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<UngVien> layUngVien(List<String> sizeChon) {
        boolean locSize = sizeChon != null && !sizeChon.isEmpty();
        String jpql = """
                select g.id, g.ma, g.ten, th.ten, lg.id, lg.ten, gct.giaBan, ms.ten,
                       cnd.ten, tl.giaTri, clg.ten
                from GiayChiTiet gct
                  join gct.giay g
                  join g.thuongHieu th
                  join g.loaiGiay lg
                  join gct.mauSac ms
                  join gct.kichCo kc
                  left join g.giayThuocTinh gtt
                  left join gtt.congNgheDem cnd
                  left join gtt.trongLuong tl
                  left join gtt.chatLieuGiay clg
                where g.trangThai = 1 and gct.kichHoat = 1 and gct.soLuong > 0
                """ + (locSize ? " and kc.giaTri in :sizes" : "")
                + " order by g.id asc";  // thứ tự cố định để chấm điểm/xếp hạng ổn định

        var query = entityManager.createQuery(jpql).setMaxResults(1000);
        if (locSize) {
            query.setParameter("sizes", sizeChon);
        }
        List<Object[]> rows = query.getResultList();

        Map<Integer, UngVien> gom = new LinkedHashMap<>();
        for (Object[] r : rows) {
            Integer id = (Integer) r[0];
            BigDecimal gia = (BigDecimal) r[6];
            String mau = (String) r[7];
            UngVien cu = gom.get(id);
            if (cu == null) {
                List<String> mauSac = new ArrayList<>();
                if (mau != null) {
                    mauSac.add(mau);
                }
                gom.put(id, new UngVien(id, (String) r[1], (String) r[2], (String) r[3],
                        (Integer) r[4], (String) r[5], gia, mauSac,
                        (String) r[8], (Integer) r[9], (String) r[10]));
            } else {
                if (mau != null && !cu.mauSac().contains(mau)) {
                    cu.mauSac().add(mau);
                }
                if (gia != null && (cu.giaThapNhat() == null || gia.compareTo(cu.giaThapNhat()) < 0)) {
                    gom.put(id, new UngVien(cu.id(), cu.ma(), cu.ten(), cu.thuongHieu(),
                            cu.loaiId(), cu.loai(), gia, cu.mauSac(),
                            cu.congNgheDem(), cu.trongLuong(), cu.chatLieu()));
                }
            }
            if (gom.size() >= SO_UNG_VIEN) {
                break;
            }
        }
        return new ArrayList<>(gom.values());
    }

    // ─── Phân loại loại giày (AI, cache DB) ──────────────────────────────────

    private Map<Integer, String[]> boDamPhanLoai(List<UngVien> ungVien) {
        Map<Integer, String[]> kq = new HashMap<>();
        Set<Integer> loaiIds = ungVien.stream().map(UngVien::loaiId).filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        for (Integer loaiId : loaiIds) {
            LoaiGiay lg = loaiGiayRepository.findById(loaiId).orElse(null);
            if (lg == null) {
                kq.put(loaiId, new String[]{"", ""});
                continue;
            }
            if (lg.getNhomMucDich() != null) {
                kq.put(loaiId, new String[]{
                        lg.getNhomMucDich(), lg.getNhomPhongCach() == null ? "" : lg.getNhomPhongCach()});
                continue;
            }
            kq.put(loaiId, phanLoaiBangAi(lg));
        }
        return kq;
    }

    /** Gọi AI phân loại 1 loại giày -> [mucDichCsv, phongCachCsv], lưu vào DB. Lỗi -> trả rỗng, không lưu. */
    private String[] phanLoaiBangAi(LoaiGiay lg) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return new String[]{"", ""};
        }
        String moTa = lg.getMoTa() == null || lg.getMoTa().isBlank() ? "" : " (" + lg.getMoTa().trim() + ")";
        String prompt = """
                Phân loại loại giày sau vào các nhóm. Chỉ trả về ĐÚNG 2 dòng, không thêm gì.
                Loại giày: "%s"%s

                MỤC ĐÍCH sử dụng (chọn 1 hoặc nhiều mã, cách nhau bởi dấu phẩy):
                - di-lam   : đi học, đi làm, mang hằng ngày
                - the-thao : chơi thể thao, chạy bộ, tập luyện
                - dao-pho  : đi chơi, dạo phố, cà phê
                - du-tiec  : dự tiệc, sự kiện, cần lịch sự

                PHONG CÁCH (chọn 1 hoặc nhiều mã):
                - nang-dong : năng động, thể thao
                - toi-gian  : đơn giản, tối giản
                - ca-tinh   : cá tính, nổi bật
                - co-dien   : cổ điển, retro

                Định dạng:
                MUC_DICH: <mã,mã>
                PHONG_CACH: <mã,mã>
                """.formatted(lg.getTen(), moTa);
        try {
            String kq = builder.build().prompt()
                    .options(OpenAiChatOptions.builder().withMaxTokens(GIOI_HAN_TOKEN).withTemperature(0.0f).build())
                    .user(prompt)
                    .call().content();
            String mucDich = "";
            String phongCach = "";
            for (String dong : (kq == null ? "" : kq).split("\\r?\\n")) {
                String d = dong.trim();
                if (d.regionMatches(true, 0, "MUC_DICH:", 0, 9)) {
                    mucDich = locMa(d.substring(9), MA_MUC_DICH_HOP_LE);
                } else if (d.regionMatches(true, 0, "PHONG_CACH:", 0, 11)) {
                    phongCach = locMa(d.substring(11), MA_PHONG_CACH_HOP_LE);
                }
            }
            lg.setNhomMucDich(mucDich);
            lg.setNhomPhongCach(phongCach);
            loaiGiayRepository.save(lg);
            System.out.println("[AI GOI Y] Đã phân loại loại '" + lg.getTen() + "' -> mục đích=["
                    + mucDich + "] phong cách=[" + phongCach + "]");
            return new String[]{mucDich, phongCach};
        } catch (Exception e) {
            System.err.println("[AI GOI Y] Lỗi phân loại loại '" + lg.getTen() + "': " + e.getMessage());
            return new String[]{"", ""};
        }
    }

    private String locMa(String csv, Set<String> hopLe) {
        return Arrays.stream(csv.split("[,;]"))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(hopLe::contains)
                .distinct()
                .collect(Collectors.joining(","));
    }

    // ─── Chấm điểm bằng CODE ─────────────────────────────────────────────────

    private KetQuaCham chamDiem(UngVien u, Map<String, Set<String>> traLoi,
                                String nhomMucDichLoai, String nhomPhongCachLoai) {
        int diem = 0;
        List<String> khop = new ArrayList<>();

        Set<String> chonMucDich = maTheoDapAn(traLoi.get("muc-dich"), MA_MUC_DICH);
        if (!chonMucDich.isEmpty() && !Collections.disjoint(chonMucDich, tachCsv(nhomMucDichLoai))) {
            diem++;
            khop.add("đúng mục đích");
        }
        Set<String> chonPhongCach = maTheoDapAn(traLoi.get("phong-cach"), MA_PHONG_CACH);
        if (!chonPhongCach.isEmpty() && !Collections.disjoint(chonPhongCach, tachCsv(nhomPhongCachLoai))) {
            diem++;
            khop.add("đúng phong cách");
        }
        Set<String> mauMongMuon = (traLoi.getOrDefault("mau-sac", Set.of())).stream()
                .flatMap(g -> MAU_THEO_NHOM.getOrDefault(g, Set.of()).stream())
                .collect(Collectors.toSet());
        if (!mauMongMuon.isEmpty() && u.mauSac().stream()
                .anyMatch(m -> mauMongMuon.stream().anyMatch(x -> x.equalsIgnoreCase(m)))) {
            diem++;
            khop.add("đúng màu");
        }
        Set<String> uuTien = traLoi.getOrDefault("uu-tien", Set.of());
        if (uuTien.contains("Êm chân, đi lâu không mỏi")
                && u.congNgheDem() != null && !u.congNgheDem().equalsIgnoreCase(DEM_CO_BAN)) {
            diem++;
            khop.add("êm chân");
        }
        if (uuTien.contains("Nhẹ, thoáng khí")
                && ((u.trongLuong() != null && u.trongLuong() <= NGUONG_NHE_GAM)
                    || (u.chatLieu() != null && CHAT_LIEU_THOANG.contains(u.chatLieu())))) {
            diem++;
            khop.add("nhẹ/thoáng");
        }
        if (uuTien.contains("Bền, dễ vệ sinh")
                && u.chatLieu() != null && CHAT_LIEU_BEN.contains(u.chatLieu())) {
            diem++;
            khop.add("bền, dễ vệ sinh");
        }
        if ((traLoi.getOrDefault("ngan-sach", Set.of())).stream()
                .anyMatch(ns -> giaTrongKhoang(u.giaThapNhat(), ns))) {
            diem++;
            khop.add("đúng tầm giá");
        }
        return new KetQuaCham(diem, khop);
    }

    private Set<String> maTheoDapAn(Set<String> dapAn, Map<String, String> bangMa) {
        if (dapAn == null) {
            return Set.of();
        }
        return dapAn.stream().map(bangMa::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<String> tachCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean giaTrongKhoang(BigDecimal gia, String khoang) {
        if (gia == null) {
            return false;
        }
        double g = gia.doubleValue();
        return switch (khoang) {
            case "Dưới 1 triệu" -> g < 1_000_000;
            case "1 - 2 triệu" -> g >= 1_000_000 && g <= 2_000_000;
            case "2 - 3 triệu" -> g >= 2_000_000 && g <= 3_000_000;
            case "Trên 3 triệu" -> g > 3_000_000;
            default -> false;
        };
    }

    /** Lý do sinh từ các tiêu chí đã khớp (CODE, nên cùng input luôn ra cùng lý do). */
    private String lyDoTuTieuChi(KetQuaCham kq) {
        if (kq == null || kq.tieuChiKhop().isEmpty()) {
            return "Một gợi ý bạn có thể tham khảo thêm.";
        }
        return "Phù hợp vì: " + String.join(", ", kq.tieuChiKhop()) + ".";
    }

}
