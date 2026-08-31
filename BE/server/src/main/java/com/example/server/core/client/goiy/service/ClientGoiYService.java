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
            String m = lg.getNhomMucDich();
            String p = lg.getNhomPhongCach();
            // Nếu DB đã có mã chuẩn (di-lam, the-thao, dao-pho, du-tiec) thì dùng ngay
            if (m != null && !m.isBlank() && (m.contains("di-lam") || m.contains("the-thao") || m.contains("dao-pho") || m.contains("du-tiec"))) {
                kq.put(loaiId, new String[]{m, p == null ? "" : p});
                continue;
            }
            // Phân loại siêu nhanh bằng Rule-based từ khóa (chống treo nghẽn do AI Ollama/Cloud)
            kq.put(loaiId, phanLoaiNhanhBangRule(lg));
        }
        return kq;
    }

    /** Phân loại cực nhanh bằng Rule-based theo tên & mô tả + map mã legacy trong DB (< 1ms). */
    private String[] phanLoaiNhanhBangRule(LoaiGiay lg) {
        String ten = (lg.getTen() == null ? "" : lg.getTen()).toLowerCase(Locale.ROOT);
        String moTa = (lg.getMoTa() == null ? "" : lg.getMoTa()).toLowerCase(Locale.ROOT);
        String curM = lg.getNhomMucDich() == null ? "" : lg.getNhomMucDich();
        String curP = lg.getNhomPhongCach() == null ? "" : lg.getNhomPhongCach();

        Set<String> mucDichSet = new HashSet<>();
        Set<String> phongCachSet = new HashSet<>();

        // Map legacy constants (PURPOSE_..., STYLE_...) từ DB 02_data.sql
        if (curM.contains("PURPOSE_RUNNING") || curM.contains("PURPOSE_TRAINING") 
                || curM.contains("PURPOSE_BASKETBALL") || curM.contains("PURPOSE_TENNIS") 
                || curM.contains("PURPOSE_FOOTBALL") || curM.contains("PURPOSE_VOLLEYBALL")) {
            mucDichSet.add("the-thao");
            mucDichSet.add("di-lam");
        }
        if (curM.contains("PURPOSE_CASUAL") || curM.contains("PURPOSE_WALKING")) {
            mucDichSet.add("di-lam");
            mucDichSet.add("dao-pho");
        }
        if (curM.contains("PURPOSE_SKATE") || curM.contains("PURPOSE_DANCE")) {
            mucDichSet.add("dao-pho");
            mucDichSet.add("di-lam");
        }
        if (curM.contains("PURPOSE_GOLF") || curM.contains("PURPOSE_HIKING")) {
            mucDichSet.add("the-thao");
            mucDichSet.add("dao-pho");
        }
        if (curP.contains("STYLE_SPORT") || curP.contains("STYLE_OUTDOOR")) {
            phongCachSet.add("nang-dong");
        }
        if (curP.contains("STYLE_CASUAL")) {
            phongCachSet.add("toi-gian");
        }
        if (curP.contains("STYLE_STREET")) {
            phongCachSet.add("ca-tinh");
        }

        // Match theo từ khóa tiếng Việt / tiếng Anh trong tên loại giày & mô tả
        if (ten.contains("run") || ten.contains("chạy") || ten.contains("sport") || ten.contains("thể thao")
                || ten.contains("basket") || ten.contains("bóng") || ten.contains("train") || ten.contains("tennis")
                || ten.contains("foot") || ten.contains("đá bóng") || ten.contains("volley") || ten.contains("gym")) {
            mucDichSet.add("the-thao");
            mucDichSet.add("di-lam");
            phongCachSet.add("nang-dong");
        }
        if (ten.contains("sneak") || ten.contains("casual") || ten.contains("walk") || ten.contains("đi bộ")
                || ten.contains("slip-on") || ten.contains("lười") || ten.contains("phố") || ten.contains("đời thường")) {
            mucDichSet.add("di-lam");
            mucDichSet.add("dao-pho");
            phongCachSet.add("toi-gian");
        }
        if (ten.contains("skate") || ten.contains("dance") || ten.contains("retro") || ten.contains("vintage")
                || ten.contains("classic") || ten.contains("cổ điển")) {
            mucDichSet.add("dao-pho");
            phongCachSet.add("ca-tinh");
            phongCachSet.add("co-dien");
        }
        if (ten.contains("golf") || ten.contains("tiệc") || ten.contains("tây") || ten.contains("da") || ten.contains("lịch sự")) {
            mucDichSet.add("du-tiec");
            mucDichSet.add("di-lam");
            phongCachSet.add("co-dien");
            phongCachSet.add("toi-gian");
        }

        // Default fallback nếu không khớp từ khóa nào
        if (mucDichSet.isEmpty()) {
            mucDichSet.add("di-lam");
            mucDichSet.add("dao-pho");
        }
        if (phongCachSet.isEmpty()) {
            phongCachSet.add("nang-dong");
            phongCachSet.add("toi-gian");
        }

        String resM = String.join(",", mucDichSet);
        String resP = String.join(",", phongCachSet);

        // Lưu vào DB để lần sau sử dụng ngay
        if (!resM.equals(lg.getNhomMucDich()) || !resP.equals(lg.getNhomPhongCach())) {
            lg.setNhomMucDich(resM);
            lg.setNhomPhongCach(resP);
            try {
                loaiGiayRepository.save(lg);
            } catch (Exception ignored) {}
        }

        return new String[]{resM, resP};
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
