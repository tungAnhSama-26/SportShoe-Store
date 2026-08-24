package com.example.server.core.client.chatbot.tools;

import com.example.server.core.client.chatbot.dto.*;
import com.example.server.core.admin.quanlyhoadon.domain.TrangThaiHoaDon;
import com.example.server.core.admin.quanlydanhgia.dto.XepHangDanhGiaResponse;
import com.example.server.core.admin.quanlydanhgia.service.DanhGiaXepHangService;
import jakarta.persistence.EntityManager;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class ChatbotTools {

    private final EntityManager entityManager;
    private final com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService quanLyHoaDonService;
    private final com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService quanLySanPhamService;
    private final com.example.server.core.admin.quanlykhuyenmai.service.PhieuGiamGiaService phieuGiamGiaService;
    private final DanhGiaXepHangService danhGiaXepHangService;

    public ChatbotTools(
            EntityManager entityManager,
            com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService quanLyHoaDonService,
            com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService quanLySanPhamService,
            com.example.server.core.admin.quanlykhuyenmai.service.PhieuGiamGiaService phieuGiamGiaService,
            DanhGiaXepHangService danhGiaXepHangService
    ) {
        this.entityManager = entityManager;
        this.quanLyHoaDonService = quanLyHoaDonService;
        this.quanLySanPhamService = quanLySanPhamService;
        this.phieuGiamGiaService = phieuGiamGiaService;
        this.danhGiaXepHangService = danhGiaXepHangService;
    }

    private BigDecimal calculateActualPrice(Integer giayId, BigDecimal defaultGiaBan) {
        try {
            List<com.example.server.entity.GiayChiTiet> gcts = entityManager.createQuery(
                    "SELECT gct FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", com.example.server.entity.GiayChiTiet.class)
                    .setParameter("giayId", giayId)
                    .getResultList();

            if (gcts.isEmpty()) {
                return defaultGiaBan != null ? defaultGiaBan : BigDecimal.ZERO;
            }

            BigDecimal minPrice = null;
            java.time.LocalDate now = java.time.LocalDate.now();

            for (com.example.server.entity.GiayChiTiet gct : gcts) {
                BigDecimal price = gct.getGiaBan();
                
                List<com.example.server.entity.DotGiamGia> promos = entityManager.createQuery(
                        "SELECT dgt.dotGiamGia FROM DotGiamGiaSanPham dgt WHERE dgt.giayChiTiet.id = :gctId AND dgt.trangThai = 1 AND dgt.dotGiamGia.kichHoat = 1", com.example.server.entity.DotGiamGia.class)
                        .setParameter("gctId", gct.getId())
                        .getResultList();
                
                for (com.example.server.entity.DotGiamGia dgg : promos) {
                    if (dgg.getNgayBatDau() != null && now.isBefore(dgg.getNgayBatDau())) {
                        continue;
                    }
                    if (dgg.getNgayKetThuc() != null && now.isAfter(dgg.getNgayKetThuc())) {
                        continue;
                    }
                    
                    BigDecimal discounted = gct.getGiaBan();
                    if (dgg.getLoaiGiam() != null && dgg.getLoaiGiam() == 1) {
                        BigDecimal discountAmount = gct.getGiaBan().multiply(dgg.getGiaTriGiam())
                                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                        discounted = gct.getGiaBan().subtract(discountAmount);
                    } else if (dgg.getLoaiGiam() != null && dgg.getLoaiGiam() == 2) {
                        discounted = gct.getGiaBan().subtract(dgg.getGiaTriGiam());
                    }
                    
                    if (discounted.compareTo(BigDecimal.ZERO) < 0) {
                        discounted = BigDecimal.ZERO;
                    }
                    
                    if (discounted.compareTo(price) < 0) {
                        price = discounted;
                    }
                }
                
                if (minPrice == null || price.compareTo(minPrice) < 0) {
                    minPrice = price;
                }
            }
            
            return minPrice != null ? minPrice : (defaultGiaBan != null ? defaultGiaBan : BigDecimal.ZERO);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultGiaBan != null ? defaultGiaBan : BigDecimal.ZERO;
        }
    }

    private ProductDto buildProductDto(com.example.server.entity.Giay g, BigDecimal giaBan, List<String> mauSacs, List<String> kichCos, Long soLuongTon, Long daBan) {
        BigDecimal actualPrice = calculateActualPrice(g.getId(), giaBan);
        String moTa = g.getMoTa() != null ? g.getMoTa() : "";
        String suffix = "";
        if (giaBan != null && actualPrice.compareTo(giaBan) < 0) {
            suffix = " [ĐANG GIẢM GIÁ - Giá gốc: " + giaBan.setScale(0) + "đ, giá sau giảm: " + actualPrice.setScale(0) + "đ]";
        }
        String hinhAnh = g.getHinhAnh();
        if (hinhAnh == null || hinhAnh.isBlank()) {
            List<com.example.server.entity.HinhAnhGiay> images = entityManager.createQuery(
                    "SELECT h FROM HinhAnhGiay h WHERE h.giayChiTiet.giay.id = :giayId AND h.trangThai = 1 ORDER BY h.laHinhChinh DESC", com.example.server.entity.HinhAnhGiay.class)
                    .setParameter("giayId", g.getId())
                    .setMaxResults(1)
                    .getResultList();
            if (!images.isEmpty() && images.get(0).getUrl() != null) {
                hinhAnh = images.get(0).getUrl();
            }
        }
        return new ProductDto(
                 g.getId(), g.getMa(), g.getTen(), moTa + suffix, hinhAnh,
                 actualPrice,
                 mauSacs, kichCos, soLuongTon, daBan
        );
    }

    @Bean("search_products_tool")
    @Description("Tìm kiếm sản phẩm giày theo các thuộc tính từ khóa (keyword), màu sắc (color), loại giày (category), thương hiệu (brand), kích cỡ giày số (size, ví dụ: '38', '39', '40') hoặc trạng thái giảm giá (onSale = true). Trả về thông tin sản phẩm gồm ID, mã, tên, giá bán, hình ảnh (hinhAnh), danh sách size, màu sắc, số lượng tồn kho. Khi trả lời khách hàng BẮT BUỘC chèn link `[Tên sản phẩm](/khachhang/san-pham/ID)` để giao diện tự dựng thẻ sản phẩm có hình ảnh.")
    public Function<SearchRequest, List<ProductDto>> searchProductsTool() {
        return new Function<SearchRequest, List<ProductDto>>() {
            @Override
            public List<ProductDto> apply(SearchRequest request) {
                try {
                    StringBuilder jpql = new StringBuilder("SELECT DISTINCT g FROM Giay g ");
                    jpql.append("LEFT JOIN FETCH g.thuongHieu th ");
                    jpql.append("LEFT JOIN FETCH g.loaiGiay lg ");
                    jpql.append("WHERE g.trangThai = 1 ");

                    Map<String, Object> params = new HashMap<>();

                    if (request.keyword() != null && !request.keyword().isBlank()) {
                        jpql.append("AND (LOWER(g.ten) LIKE :keyword OR g.moTa LIKE :keyword) ");
                        params.put("keyword", "%" + request.keyword().toLowerCase().trim() + "%");
                    }
                    if (request.category() != null && !request.category().isBlank()) {
                        jpql.append("AND LOWER(lg.ten) LIKE :category ");
                        params.put("category", "%" + request.category().toLowerCase().trim() + "%");
                    }
                    if (request.brand() != null && !request.brand().isBlank()) {
                        jpql.append("AND LOWER(th.ten) LIKE :brand ");
                        params.put("brand", "%" + request.brand().toLowerCase().trim() + "%");
                    }
                    if (Boolean.TRUE.equals(request.onSale())) {
                        jpql.append("AND EXISTS (SELECT dgt.id FROM DotGiamGiaSanPham dgt JOIN dgt.giayChiTiet gct JOIN dgt.dotGiamGia dg WHERE gct.giay.id = g.id AND dg.kichHoat = 1 AND dgt.trangThai = 1) ");
                    }

                    boolean filterColor = request.color() != null && !request.color().isBlank();
                    String searchSize = request.size() == null
                            ? ""
                            : request.size().replaceAll("[^0-9]", "").trim();
                    boolean filterSize = !searchSize.isEmpty();
                    if (filterColor || filterSize) {
                        jpql.append("AND EXISTS (SELECT filterGct.id FROM GiayChiTiet filterGct ");
                        jpql.append("JOIN filterGct.mauSac filterMs JOIN filterGct.kichCo filterKc ");
                        jpql.append("WHERE filterGct.giay.id = g.id AND filterGct.kichHoat = 1 AND filterGct.soLuong > 0 ");
                        if (filterColor) {
                            jpql.append("AND LOWER(filterMs.ten) LIKE :filterColor ");
                            params.put("filterColor", "%" + request.color().toLowerCase().trim() + "%");
                        }
                        if (filterSize) {
                            jpql.append("AND filterKc.giaTri = :filterSize ");
                            params.put("filterSize", searchSize);
                        }
                        jpql.append(") ");
                    }

                    var query = entityManager.createQuery(jpql.toString(), com.example.server.entity.Giay.class);
                    params.forEach(query::setParameter);
                    query.setMaxResults(10);
                    List<com.example.server.entity.Giay> giays = query.getResultList();

                    List<ProductDto> result = new ArrayList<>();
                    for (com.example.server.entity.Giay g : giays) {
                        List<String> mauSacs = entityManager.createQuery(
                                        "SELECT DISTINCT ms.ten FROM GiayChiTiet gct JOIN gct.mauSac ms WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", String.class)
                                .setParameter("giayId", g.getId())
                                .getResultList();

                        List<String> kichCos = entityManager.createQuery(
                                        "SELECT DISTINCT kc.giaTri FROM GiayChiTiet gct JOIN gct.kichCo kc WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", String.class)
                                .setParameter("giayId", g.getId())
                                .getResultList();

                        Long soLuongTon = entityManager.createQuery(
                                        "SELECT COALESCE(SUM(gct.soLuong), 0L) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", Long.class)
                                .setParameter("giayId", g.getId())
                                .getSingleResult();

                        Long daBan = entityManager.createQuery(
                                        "SELECT COALESCE(SUM(hdct.soLuong), 0L) FROM HoaDonChiTiet hdct JOIN hdct.giayChiTiet gct WHERE gct.giay.id = :giayId", Long.class)
                                .setParameter("giayId", g.getId())
                                .getSingleResult();

                        BigDecimal giaBan = entityManager.createQuery(
                                        "SELECT MIN(gct.giaBan) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", BigDecimal.class)
                                .setParameter("giayId", g.getId())
                                .getSingleResult();

                        if (request.color() != null && !request.color().isBlank()) {
                            boolean matchColor = mauSacs.stream().anyMatch(c -> c.toLowerCase().contains(request.color().toLowerCase().trim()));
                            if (!matchColor) {
                                continue;
                            }
                        }

                        if (request.size() != null && !request.size().isBlank()) {
                            if (!searchSize.isEmpty()) {
                                boolean matchSize = kichCos.stream().anyMatch(kc -> kc.trim().equals(searchSize) || kc.trim().contains(searchSize));
                                if (!matchSize) {
                                    continue;
                                }
                            }
                        }

                        result.add(buildProductDto(g, giaBan, mauSacs, kichCos, soLuongTon, daBan));
                    }
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of();
                }
            }
        };
    }

    @Bean("get_best_selling_shoes_tool")
    @Description("Lấy danh sách các sản phẩm giày bán chạy nhất (Best Seller) của cửa hàng và trả về các thẻ sản phẩm kèm link xem chi tiết")
    public Function<BestSellerRequest, String> getBestSellingShoesTool() {
        return request -> buildBestSellingShoes(false);
    }

    @Bean("get_admin_best_selling_shoes_tool")
    @Description("Lấy top sản phẩm bán chạy từ các hóa đơn hoàn thành và trả card dành cho trang quản trị")
    public Function<BestSellerRequest, String> getAdminBestSellingShoesTool() {
        return request -> buildBestSellingShoes(true);
    }

    private String buildBestSellingShoes(boolean adminView) {
        try {
            List<Object[]> sales = entityManager.createQuery(
                            "SELECT g.id, g.ten, g.hinhAnh, SUM(hdct.soLuong) " +
                                    "FROM HoaDonChiTiet hdct " +
                                    "JOIN hdct.hoaDon hd " +
                                    "JOIN hdct.giayChiTiet soldVariant " +
                                    "JOIN soldVariant.giay g " +
                                    "WHERE hd.trangThai = 5 AND g.trangThai = 1 AND soldVariant.kichHoat = 1 " +
                                    "GROUP BY g.id, g.ten, g.hinhAnh " +
                                    "ORDER BY SUM(hdct.soLuong) DESC", Object[].class)
                    .setMaxResults(5)
                    .getResultList();

            if (sales.isEmpty()) {
                return "Hiện tại chưa có dữ liệu bán hàng hoàn thành cho các sản phẩm đang hoạt động.";
            }

            StringBuilder sb = new StringBuilder("Danh sách sản phẩm bán chạy nhất của cửa hàng:\n");
            int cardCount = 0;
            for (Object[] sale : sales) {
                Integer giayId = (Integer) sale[0];
                if (giayId == null || giayId <= 0) {
                    continue;
                }
                String ten = sale[1] != null ? (String) sale[1] : "Giày";
                String hinhAnh = sale[2] != null ? (String) sale[2] : "";
                Long daBan = sale[3] != null ? ((Number) sale[3]).longValue() : 0L;

                List<Object[]> variants = entityManager.createQuery(
                                "SELECT ms.ten, kc.giaTri, gct.giaBan, gct.soLuong " +
                                        "FROM GiayChiTiet gct " +
                                        "JOIN gct.mauSac ms " +
                                        "JOIN gct.kichCo kc " +
                                        "WHERE gct.giay.id = :giayId AND gct.kichHoat = 1 " +
                                        "ORDER BY ms.ten, kc.giaTri", Object[].class)
                        .setParameter("giayId", giayId)
                        .getResultList();
                if (variants.isEmpty()) {
                    continue;
                }

                java.util.Set<String> colors = new java.util.LinkedHashSet<>();
                java.util.Set<String> sizes = new java.util.LinkedHashSet<>();
                BigDecimal giaBan = null;
                long soLuong = 0L;
                for (Object[] variant : variants) {
                    if (variant[0] != null) colors.add((String) variant[0]);
                    if (variant[1] != null) sizes.add((String) variant[1]);
                    if (variant[2] != null) {
                        BigDecimal variantPrice = (BigDecimal) variant[2];
                        if (giaBan == null || variantPrice.compareTo(giaBan) < 0) giaBan = variantPrice;
                    }
                    if (variant[3] != null) soLuong += ((Number) variant[3]).longValue();
                }
                if (giaBan == null) giaBan = BigDecimal.ZERO;

                if (hinhAnh.isBlank()) {
                    List<String> images = entityManager.createQuery(
                                    "SELECT h.url FROM HinhAnhGiay h " +
                                            "WHERE h.giayChiTiet.giay.id = :giayId AND h.trangThai = 1 " +
                                            "ORDER BY h.laHinhChinh DESC", String.class)
                            .setParameter("giayId", giayId)
                            .setMaxResults(1)
                            .getResultList();
                    if (!images.isEmpty() && images.get(0) != null) hinhAnh = images.get(0);
                }

                BigDecimal giaThucTe = calculateActualPrice(giayId, giaBan);
                String url = adminView
                        ? "/admin/san-pham?search=" + encodeUrl(ten)
                        : "/khachhang/san-pham/" + giayId;

                sb.append(String.format("```product\n{\"name\":\"%s (Đã bán: %d)\",\"image\":\"%s\",\"price\":%s,\"originalPrice\":%s,\"color\":\"%s\",\"size\":\"%s\",\"stock\":%d,\"stockLabel\":\"Tổng số lượng\",\"url\":\"%s\"}\n```\n",
                        cleanJsonString(ten),
                        daBan,
                        cleanJsonString(hinhAnh),
                        giaThucTe.setScale(0, java.math.RoundingMode.HALF_UP),
                        giaBan.setScale(0, java.math.RoundingMode.HALF_UP),
                        cleanJsonString(String.join(", ", colors)),
                        cleanJsonString(String.join(", ", sizes)),
                        soLuong,
                        url));
                cardCount++;
            }

            return cardCount > 0
                    ? sb.toString()
                    : "Hiện tại chưa có dữ liệu bán hàng hoàn thành cho các sản phẩm đang hoạt động.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Không thể lấy thông tin sản phẩm bán chạy. Lỗi: " + e.getMessage();
        }
    }

    private String encodeUrl(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean isGenericPromotionKeyword(String kw) {
        if (kw == null) return true;
        String clean = kw.trim().toLowerCase(java.util.Locale.ROOT);
        if (clean.isBlank()) return true;
        List<String> genericTerms = List.of(
                "giảm giá", "giam gia", "đợt giảm giá", "dot giam gia", "chương trình giảm giá", "chuong trinh giam gia",
                "khuyến mãi", "khuyen mai", "chương trình", "chuong trinh", "sale", "sales", "ưu đãi", "uu dai",
                "voucher", "vouchers", "mã giảm giá", "ma giam gia", "mã giảm", "ma giam", "coupon", "coupons",
                "cửa hàng", "cua hang", "shop", "có gì", "co gi", "tất cả", "tat ca", "all", "hiện tại", "hien tai",
                "đang có", "dang co", "mới nhất", "moi nhat", "hot"
        );
        for (String term : genericTerms) {
            if (clean.equals(term) || clean.contains(term)) {
                String without = clean.replace(term, "").trim();
                if (without.length() <= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    @Bean("search_coupons_tool")
    @Description("Tìm kiếm các phiếu giảm giá (vouchers/coupons) còn hoạt động hoặc sắp diễn ra. Không truyền keyword hoặc truyền chuỗi rỗng để lấy tất cả voucher hiện có.")
    public Function<CouponSearchRequest, List<CouponDto>> searchCouponsTool() {
        return new Function<CouponSearchRequest, List<CouponDto>>() {
            @Override
            public List<CouponDto> apply(CouponSearchRequest request) {
                try {
                    String kw = request != null && request.keyword() != null ? request.keyword().trim() : null;
                    boolean isGeneric = isGenericPromotionKeyword(kw);

                    StringBuilder jpql = new StringBuilder("SELECT p FROM PhieuGiamGia p WHERE (p.trangThai IN (1, 4) OR (p.trangThai != 0 AND (p.ngayKetThuc IS NULL OR p.ngayKetThuc >= CURRENT_TIMESTAMP))) ");
                    Map<String, Object> params = new HashMap<>();

                    if (kw != null && !kw.isBlank() && !isGeneric) {
                        jpql.append("AND (LOWER(p.ma) LIKE :keyword OR LOWER(p.ten) LIKE :keyword) ");
                        params.put("keyword", "%" + kw.toLowerCase().trim() + "%");
                    }
                    jpql.append("ORDER BY p.ngayTao DESC");

                    var query = entityManager.createQuery(jpql.toString(), com.example.server.entity.PhieuGiamGia.class);
                    params.forEach(query::setParameter);
                    query.setMaxResults(10);
                    List<com.example.server.entity.PhieuGiamGia> coupons = query.getResultList();

                    // Fallback: Nếu lọc theo từ khóa cụ thể không thấy, lấy tất cả phiếu giảm giá đang hoạt động
                    if (coupons.isEmpty() && kw != null && !kw.isBlank() && !isGeneric) {
                        coupons = entityManager.createQuery("SELECT p FROM PhieuGiamGia p WHERE (p.trangThai IN (1, 4) OR (p.trangThai != 0 AND (p.ngayKetThuc IS NULL OR p.ngayKetThuc >= CURRENT_TIMESTAMP))) ORDER BY p.ngayTao DESC", com.example.server.entity.PhieuGiamGia.class)
                                .setMaxResults(10)
                                .getResultList();
                    }

                    List<CouponDto> result = new ArrayList<>();
                    for (com.example.server.entity.PhieuGiamGia p : coupons) {
                        String loaiText = (p.getLoai() != null && p.getLoai() == 1) ? "Phần trăm" : "Tiền mặt";
                        String loaiPhieuText = (p.getLoaiPhieu() != null && p.getLoaiPhieu() == 2) ? "Cá nhân" : "Công khai";
                        String trangThaiText = "Hoạt động";
                        if (p.getTrangThai() == 4) {
                            trangThaiText = "Sắp diễn ra";
                        }

                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(java.time.ZoneId.systemDefault());
                        String startStr = p.getNgayBatDau() != null ? formatter.format(p.getNgayBatDau()) : "";
                        String endStr = p.getNgayKetThuc() != null ? formatter.format(p.getNgayKetThuc()) : "";

                        result.add(new CouponDto(
                                p.getId(), p.getMa(), p.getTen(), loaiText, loaiPhieuText,
                                p.getGiaTri(), p.getGiaTriToiThieu(), p.getGiamToiDa(),
                                startStr, endStr, p.getSoLuong(), p.getSoLuongDaDung(), trangThaiText
                        ));
                    }
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of();
                }
            }
        };
    }

    @Bean("search_promotions_tool")
    @Description("Tìm kiếm các chương trình/đợt giảm giá (sales/promotions) đang diễn ra hoặc sắp diễn ra. Không truyền keyword hoặc truyền chuỗi rỗng để lấy tất cả đợt giảm giá hiện có.")
    public Function<PromotionSearchRequest, List<PromotionDto>> searchPromotionsTool() {
        return new Function<PromotionSearchRequest, List<PromotionDto>>() {
            @Override
            public List<PromotionDto> apply(PromotionSearchRequest request) {
                try {
                    String kw = request != null && request.keyword() != null ? request.keyword().trim() : null;
                    boolean isGeneric = isGenericPromotionKeyword(kw);

                    StringBuilder jpql = new StringBuilder("SELECT d FROM DotGiamGia d WHERE (d.kichHoat IN (1, 4) OR (d.kichHoat != 0 AND (d.ngayKetThuc IS NULL OR d.ngayKetThuc >= CURRENT_DATE))) ");
                    Map<String, Object> params = new HashMap<>();

                    if (kw != null && !kw.isBlank() && !isGeneric) {
                        jpql.append("AND (LOWER(d.ma) LIKE :keyword OR LOWER(d.ten) LIKE :keyword OR LOWER(d.moTa) LIKE :keyword) ");
                        params.put("keyword", "%" + kw.toLowerCase().trim() + "%");
                    }
                    jpql.append("ORDER BY d.ngayTao DESC");

                    var query = entityManager.createQuery(jpql.toString(), com.example.server.entity.DotGiamGia.class);
                    params.forEach(query::setParameter);
                    query.setMaxResults(10);
                    List<com.example.server.entity.DotGiamGia> promos = query.getResultList();

                    // Fallback: Nếu lọc theo từ khóa cụ thể không thấy, lấy tất cả đợt giảm giá đang hoạt động
                    if (promos.isEmpty() && kw != null && !kw.isBlank() && !isGeneric) {
                        promos = entityManager.createQuery("SELECT d FROM DotGiamGia d WHERE (d.kichHoat IN (1, 4) OR (d.kichHoat != 0 AND (d.ngayKetThuc IS NULL OR d.ngayKetThuc >= CURRENT_DATE))) ORDER BY d.ngayTao DESC", com.example.server.entity.DotGiamGia.class)
                                .setMaxResults(10)
                                .getResultList();
                    }

                    List<PromotionDto> result = new ArrayList<>();
                    for (com.example.server.entity.DotGiamGia d : promos) {
                        String loaiGiamText = (d.getLoaiGiam() != null && d.getLoaiGiam() == 1) ? "Phần trăm" : "Tiền mặt";
                        String trangThaiText = "Hoạt động";
                        if (d.getKichHoat() == 4 || (d.getNgayBatDau() != null && d.getNgayBatDau().isAfter(java.time.LocalDate.now()))) {
                            trangThaiText = "Sắp diễn ra";
                        }

                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        String startStr = d.getNgayBatDau() != null ? d.getNgayBatDau().format(formatter) : "";
                        String endStr = d.getNgayKetThuc() != null ? d.getNgayKetThuc().format(formatter) : "";

                        result.add(new PromotionDto(
                                d.getId(), d.getMa(), d.getTen(), d.getMoTa(), loaiGiamText,
                                d.getGiaTriGiam(), startStr, endStr, trangThaiText
                        ));
                    }
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of();
                }
            }
        };
    }

    @Bean("search_invoice_tool")
    @Description("Tìm kiếm hóa đơn theo mã hóa đơn (ví dụ: HD0001) để lấy thông tin chi tiết và trạng thái của hóa đơn")
    public Function<InvoiceSearchRequest, InvoiceDto> searchInvoiceTool() {
        return new Function<InvoiceSearchRequest, InvoiceDto>() {
            @Override
            public InvoiceDto apply(InvoiceSearchRequest request) {
                try {
                    if (request.code() == null || request.code().isBlank()) {
                        return null;
                    }
                    String cleanCode = request.code().trim();
                    if (cleanCode.startsWith("#")) {
                        cleanCode = cleanCode.substring(1);
                    }
                    cleanCode = cleanCode.toLowerCase();

                    // Tìm kiếm chính xác trước
                    List<com.example.server.entity.HoaDon> results = entityManager.createQuery(
                                    "SELECT h FROM HoaDon h WHERE LOWER(h.ma) = :code", com.example.server.entity.HoaDon.class)
                            .setParameter("code", cleanCode)
                            .getResultList();

                    if (results.isEmpty()) {
                        // Nếu không thấy, thử tìm kiếm gần đúng với LIKE
                        results = entityManager.createQuery(
                                        "SELECT h FROM HoaDon h WHERE LOWER(h.ma) LIKE :codeLike", com.example.server.entity.HoaDon.class)
                                .setParameter("codeLike", "%" + cleanCode + "%")
                                .setMaxResults(1)
                                .getResultList();
                    }

                    if (results.isEmpty()) {
                        return null;
                    }

                    com.example.server.entity.HoaDon h = results.get(0);

                    // Ánh xạ trạng thái
                    String trangThaiText = "Không xác định";
                    if (h.getTrangThai() != null) {
                        try {
                            trangThaiText = TrangThaiHoaDon.tuMa(h.getTrangThai()).getTen();
                        } catch (Exception ex) {
                            trangThaiText = "Mã trạng thái: " + h.getTrangThai();
                        }
                    }

                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(java.time.ZoneId.systemDefault());
                    String ngayLapStr = h.getNgayLap() != null ? formatter.format(h.getNgayLap()) : "";

                    return new InvoiceDto(
                            h.getId(),
                            h.getMa(),
                            h.getTenNguoiNhan(),
                            h.getSdtNguoiNhan(),
                            h.getTongTienThanhToan(),
                            trangThaiText,
                            ngayLapStr
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Bean("get_admin_revenue_stats_tool")
    @Description("Lấy thống kê doanh thu của cửa hàng theo chu kỳ (period: 'today', 'month', 'year')")
    public Function<AdminRevenueRequest, String> getAdminRevenueStatsTool() {
        return new Function<AdminRevenueRequest, String>() {
            @Override
            public String apply(AdminRevenueRequest request) {
                try {
                    String timeQuery = "";
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.time.Instant startInstant = java.time.Instant.MIN;
                    java.time.Instant endInstant = java.time.Instant.MAX;
                    
                    if ("today".equalsIgnoreCase(request.period())) {
                        startInstant = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        endInstant = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        timeQuery = "Hôm nay (" + today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                    } else if ("month".equalsIgnoreCase(request.period())) {
                        java.time.LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                        startInstant = firstDayOfMonth.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        endInstant = firstDayOfMonth.plusMonths(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        timeQuery = "Tháng này (" + today.getMonthValue() + "/" + today.getYear() + ")";
                    } else if ("year".equalsIgnoreCase(request.period())) {
                        java.time.LocalDate firstDayOfYear = today.withDayOfYear(1);
                        startInstant = firstDayOfYear.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        endInstant = firstDayOfYear.plusYears(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        timeQuery = "Năm nay (" + today.getYear() + ")";
                    } else {
                        startInstant = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        endInstant = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                        timeQuery = "Hôm nay (" + today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
                    }

                    List<com.example.server.entity.HoaDon> invoices = entityManager.createQuery(
                            "SELECT h FROM HoaDon h WHERE h.ngayLap >= :start AND h.ngayLap < :end AND h.trangThai = 5", com.example.server.entity.HoaDon.class)
                            .setParameter("start", startInstant)
                            .setParameter("end", endInstant)
                            .getResultList();

                    if (invoices.isEmpty()) {
                        return "Thống kê doanh thu cho " + timeQuery + ": Chưa có đơn hàng nào hoàn thành. Doanh thu: 0đ.";
                    }

                    BigDecimal tongDoanhThu = BigDecimal.ZERO;
                    int count = 0;
                    for (com.example.server.entity.HoaDon h : invoices) {
                        tongDoanhThu = tongDoanhThu.add(h.getTongTienThanhToan() != null ? h.getTongTienThanhToan() : BigDecimal.ZERO);
                        count++;
                    }

                    java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
                    return String.format("Thống kê doanh thu cho %s:\n- Số đơn hoàn thành: %d\n- Tổng doanh thu: %s", 
                            timeQuery, count, nf.format(tongDoanhThu));
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi thống kê doanh thu: " + e.getMessage();
                }
            }
        };
    }

    @Bean("get_admin_low_stock_tool")
    @Description("Lấy danh sách sản phẩm sắp hết hàng (số lượng tồn kho dưới ngưỡng threshold, mặc định là 5)")
    public Function<AdminLowStockRequest, String> getAdminLowStockTool() {
        return new Function<AdminLowStockRequest, String>() {
            @Override
            public String apply(AdminLowStockRequest request) {
                try {
                    int limit = request.threshold() != null ? request.threshold() : 5;
                    List<Object[]> lowStockList = entityManager.createQuery(
                            "SELECT gct.id, g.id, g.ten, g.hinhAnh, ms.ten, kc.giaTri, gct.giaBan, gct.soLuong " +
                                    "FROM GiayChiTiet gct " +
                                    "JOIN gct.giay g " +
                                    "JOIN gct.mauSac ms " +
                                    "JOIN gct.kichCo kc " +
                                    "WHERE gct.soLuong < :limit AND gct.kichHoat = 1 AND g.trangThai = 1 " +
                                    "ORDER BY gct.soLuong ASC", Object[].class)
                            .setParameter("limit", limit)
                            .setMaxResults(10)
                            .getResultList();

                    if (lowStockList.isEmpty()) {
                        return "Hiện tại không có sản phẩm nào có số lượng dưới " + limit + " chiếc.";
                    }

                    StringBuilder sb = new StringBuilder("Danh sách sản phẩm sắp hết hàng (Số lượng < " + limit + "):\n");
                    for (Object[] row : lowStockList) {
                        Integer gctId = (Integer) row[0];
                        Integer giayId = (Integer) row[1];
                        String ten = row[2] != null ? (String) row[2] : "Giày";
                        String hinhAnh = row[3] != null ? (String) row[3] : "";
                        String mau = row[4] != null ? (String) row[4] : "N/A";
                        String size = row[5] != null ? (String) row[5] : "N/A";
                        BigDecimal giaBan = row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO;
                        Integer soLuong = row[7] != null ? (Integer) row[7] : 0;
                        BigDecimal giaThucTe = calculateActualPrice(giayId, giaBan);

                        if (hinhAnh == null || hinhAnh.isBlank()) {
                            List<com.example.server.entity.HinhAnhGiay> images = entityManager.createQuery(
                                    "SELECT h FROM HinhAnhGiay h WHERE h.giayChiTiet.id = :gctId AND h.trangThai = 1 ORDER BY h.laHinhChinh DESC", com.example.server.entity.HinhAnhGiay.class)
                                    .setParameter("gctId", gctId)
                                    .setMaxResults(1)
                                    .getResultList();
                            if (!images.isEmpty() && images.get(0).getUrl() != null) {
                                hinhAnh = images.get(0).getUrl();
                            }
                        }

                        String url = "/admin/san-pham";
                        try {
                            url = "/admin/san-pham?search=" + java.net.URLEncoder.encode(ten, java.nio.charset.StandardCharsets.UTF_8.name());
                        } catch (Exception ex) {}

                        sb.append(String.format("```product\n{\"name\":\"%s\",\"image\":\"%s\",\"price\":%s,\"originalPrice\":%s,\"color\":\"%s\",\"size\":\"%s\",\"stock\":%d,\"stockLabel\":\"Còn lại ở biến thể\",\"url\":\"%s\"}\n```\n",
                                cleanJsonString(ten),
                                cleanJsonString(hinhAnh),
                                giaThucTe.setScale(0, java.math.RoundingMode.HALF_UP),
                                giaBan.setScale(0, java.math.RoundingMode.HALF_UP),
                                cleanJsonString(mau),
                                cleanJsonString(size),
                                soLuong,
                                url
                        ));
                    }
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi lấy danh sách sản phẩm hết hàng: " + e.getMessage();
                }
            }
        };
    }

    @Bean("search_admin_invoices_tool")
    @Description("Tìm kiếm danh sách hóa đơn của hệ thống theo mã hóa đơn, tên người nhận hoặc số điện thoại, trạng thái")
    public Function<AdminInvoiceSearchRequest, List<InvoiceDto>> searchAdminInvoicesTool() {
        return new Function<AdminInvoiceSearchRequest, List<InvoiceDto>>() {
            @Override
            public List<InvoiceDto> apply(AdminInvoiceSearchRequest request) {
                try {
                    String jpql = "SELECT h FROM HoaDon h WHERE 1=1";
                    Map<String, Object> params = new HashMap<>();

                    if (request.query() != null && !request.query().isBlank()) {
                        String q = "%" + request.query().trim().toLowerCase() + "%";
                        jpql += " AND (LOWER(h.ma) LIKE :query OR LOWER(h.tenNguoiNhan) LIKE :query OR LOWER(h.sdtNguoiNhan) LIKE :query)";
                        params.put("query", q);
                    }

                    if (request.status() != null && !request.status().isBlank()) {
                        try {
                            Integer statusInt = Integer.parseInt(request.status().trim());
                            jpql += " AND h.trangThai = :status";
                            params.put("status", statusInt);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }

                    jpql += " ORDER BY h.ngayLap DESC";

                    var typedQuery = entityManager.createQuery(jpql, com.example.server.entity.HoaDon.class);
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        typedQuery.setParameter(entry.getKey(), entry.getValue());
                    }

                    List<com.example.server.entity.HoaDon> results = typedQuery.setMaxResults(10).getResultList();
                    List<InvoiceDto> dtoList = new ArrayList<>();

                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(java.time.ZoneId.systemDefault());

                    for (com.example.server.entity.HoaDon h : results) {
                        String trangThaiText = "Không xác định";
                        if (h.getTrangThai() != null) {
                            try {
                                trangThaiText = TrangThaiHoaDon.tuMa(h.getTrangThai()).getTen();
                            } catch (Exception ex) {
                                trangThaiText = "Mã: " + h.getTrangThai();
                            }
                        }
                        String ngayLapStr = h.getNgayLap() != null ? formatter.format(h.getNgayLap()) : "";

                        dtoList.add(new InvoiceDto(
                                h.getId(),
                                h.getMa(),
                                h.getTenNguoiNhan(),
                                h.getSdtNguoiNhan(),
                                h.getTongTienThanhToan(),
                                trangThaiText,
                                ngayLapStr
                        ));
                    }
                    return dtoList;
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of();
                }
            }
        };
    }

    @Bean("count_admin_invoices_tool")
    @Description("Đếm chính xác số hóa đơn theo mã trạng thái")
    public Function<AdminInvoiceCountRequest, Long> countAdminInvoicesTool() {
        return request -> {
            if (request == null || request.status() == null || request.status().isBlank()) {
                return entityManager.createQuery("SELECT COUNT(h.id) FROM HoaDon h", Long.class)
                        .getSingleResult();
            }
            try {
                Integer status = Integer.parseInt(request.status().trim());
                return entityManager.createQuery(
                                "SELECT COUNT(h.id) FROM HoaDon h WHERE h.trangThai = :status", Long.class)
                        .setParameter("status", status)
                        .getSingleResult();
            } catch (NumberFormatException exception) {
                return 0L;
            }
        };
    }

    @Bean("get_admin_product_reviews_tool")
    @Description("Lấy thống kê đánh giá của khách hàng đối với một sản phẩm cụ thể theo ID hoặc Tên sản phẩm")
    public Function<AdminProductReviewRequest, String> getAdminProductReviewsTool() {
        return new Function<AdminProductReviewRequest, String>() {
            @Override
            public String apply(AdminProductReviewRequest request) {
                try {
                    List<com.example.server.entity.Giay> list = new ArrayList<>();
                    if (request.productId() != null) {
                        com.example.server.entity.Giay g = entityManager.find(com.example.server.entity.Giay.class, request.productId());
                        if (g != null) list.add(g);
                    } else if (request.productName() != null && !request.productName().isBlank()) {
                        list = entityManager.createQuery(
                                "SELECT g FROM Giay g WHERE LOWER(g.ten) LIKE :name", com.example.server.entity.Giay.class)
                                .setParameter("name", "%" + request.productName().toLowerCase().trim() + "%")
                                .setMaxResults(1)
                                .getResultList();
                    }
                    
                    if (list.isEmpty()) {
                        return "Không tìm thấy sản phẩm phù hợp.";
                    }
                    com.example.server.entity.Giay g = list.get(0);
                    
                    List<Object[]> reviews = entityManager.createQuery(
                            "SELECT d.soSao, kh.hoTen, d.noiDung FROM DanhGia d " +
                                    "LEFT JOIN d.khachHang kh " +
                                    "WHERE d.giay.id = :giayId AND d.trangThai = 1 ORDER BY d.ngayTao DESC", Object[].class)
                            .setParameter("giayId", g.getId())
                            .getResultList();
                    
                    if (reviews.isEmpty()) {
                        return "Sản phẩm **" + g.getTen() + "** chưa có lượt đánh giá nào.";
                    }
                    
                    int total = reviews.size();
                    double sum = 0;
                    int positive = 0;
                    int negative = 0;
                    
                    for (Object[] review : reviews) {
                        int soSao = review[0] != null ? (Integer) review[0] : 0;
                        sum += soSao;
                        if (soSao >= 4) positive++;
                        if (soSao <= 2) negative++;
                    }
                    
                    double avg = sum / total;
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("Thống kê đánh giá cho sản phẩm **%s**:\n", g.getTen()));
                    sb.append(formatProductBlock(g, String.format("%.1f ⭐ (%d đánh giá)", avg, total), ""));
                    sb.append(String.format("- Điểm đánh giá trung bình: **%.1f/5.0** ⭐\n", avg));
                    sb.append(String.format("- Tổng số lượt đánh giá: %d\n", total));
                    sb.append(String.format("- Số đánh giá tích cực (4-5 sao): %d (%.1f%%)\n", positive, (positive * 100.0 / total)));
                    sb.append(String.format("- Số đánh giá tiêu cực (1-2 sao): %d (%.1f%%)\n", negative, (negative * 100.0 / total)));
                    
                    sb.append("\n5 đánh giá gần nhất:\n");
                    int showCount = Math.min(5, reviews.size());
                    for (int i = 0; i < showCount; i++) {
                        Object[] review = reviews.get(i);
                        int soSao = review[0] != null ? (Integer) review[0] : 0;
                        String tenKhachHang = review[1] != null ? (String) review[1] : "Khách hàng";
                        String noiDung = review[2] != null ? (String) review[2] : "(Không có nội dung)";
                        sb.append(String.format("%d. **%d sao** | %s: \"%s\"\n", 
                                i + 1, 
                                soSao,
                                tenKhachHang,
                                noiDung));
                    }
                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi truy vấn đánh giá sản phẩm: " + e.getMessage();
                }
            }
        };
    }

    @Bean("get_admin_top_reviews_tool")
    @Description("Lấy danh sách các sản phẩm có đánh giá tích cực nhiều nhất (điểm trung bình cao nhất) và sản phẩm có nhiều đánh giá tiêu cực nhất")
    public Function<AdminTopReviewsRequest, String> getAdminTopReviewsTool() {
        return new Function<AdminTopReviewsRequest, String>() {
            @Override
            public String apply(AdminTopReviewsRequest request) {
                try {
                    var thongKe = danhGiaXepHangService.thongKeTopVaThap();
                    if (thongKe.caoNhat().isEmpty()) {
                        return "Chưa có sản phẩm nào nhận được đánh giá trong hệ thống.";
                    }

                    StringBuilder sb = new StringBuilder("Thống kê sản phẩm được đánh giá cao nhất và thấp nhất:\n\n");

                    sb.append("🏆 **Top 5 sản phẩm đánh giá cao nhất:**\n");
                    for (XepHangDanhGiaResponse item : thongKe.caoNhat()) {
                        String badge = String.format("%.1f ⭐ (%d đánh giá)", item.diemTrungBinh(), item.soDanhGia());
                        sb.append(formatProductBlock(item.giay(), badge, ""));
                    }

                    sb.append("\n⚠️ **Top 5 sản phẩm điểm đánh giá thấp nhất:**\n");
                    for (XepHangDanhGiaResponse item : thongKe.thapNhat()) {
                        String badge = String.format("%.1f ⭐ (%d đánh giá)", item.diemTrungBinh(), item.soDanhGia());
                        sb.append(formatProductBlock(item.giay(), badge, ""));
                    }

                    return sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi lấy thống kê đánh giá hệ thống: " + e.getMessage();
                }
            }
        };
    }

    @Bean("update_admin_order_status_tool")
    @Description("Cập nhật trạng thái đơn hàng (xác nhận đơn hàng hoặc hủy đơn hàng) theo mã hóa đơn và hành động (action: 'confirm' hoặc 'cancel')")
    public Function<AdminOrderUpdateRequest, String> updateAdminOrderStatusTool() {
        return new Function<AdminOrderUpdateRequest, String>() {
            @Override
            public String apply(AdminOrderUpdateRequest request) {
                try {
                    if (request.invoiceCode() == null || request.invoiceCode().isBlank()) {
                        return "Vui lòng cung cấp mã hóa đơn hợp lệ.";
                    }
                    List<com.example.server.entity.HoaDon> list = entityManager.createQuery(
                            "SELECT h FROM HoaDon h WHERE h.ma = :ma", com.example.server.entity.HoaDon.class)
                            .setParameter("ma", request.invoiceCode().trim())
                            .getResultList();
                    if (list.isEmpty()) {
                        return "Không tìm thấy hóa đơn có mã: " + request.invoiceCode();
                    }
                    com.example.server.entity.HoaDon hoaDon = list.get(0);
                    String action = request.action() != null ? request.action().toLowerCase().trim() : "";
                    
                    String targetStatus;
                    String actionText;
                    if ("confirm".equals(action)) {
                        targetStatus = "Đã xác nhận";
                        actionText = "xác nhận";
                    } else if ("cancel".equals(action)) {
                        targetStatus = "Hủy";
                        actionText = "hủy";
                    } else {
                        return "Hành động không hợp lệ. Chỉ chấp nhận 'confirm' (xác nhận) hoặc 'cancel' (hủy).";
                    }
                    
                    var updateRequest = new com.example.server.core.admin.quanlyhoadon.dto.request.CapNhatTrangThaiHoaDonRequest(
                            targetStatus, "Cập nhật qua Trợ lý AI Admin", null, null, true);
                    
                    quanLyHoaDonService.capNhatTrangThaiHoaDon(hoaDon.getId(), updateRequest);
                    return "Đã thực hiện " + actionText + " thành công đơn hàng " + request.invoiceCode() + ".";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage();
                }
            }
        };
    }

    @Bean("update_admin_product_stock_tool")
    @Description("Cập nhật số lượng tồn kho cho một biến thể sản phẩm cụ thể theo tên sản phẩm, size (kích cỡ), tên màu sắc và số lượng tồn kho mới")
    public Function<AdminProductStockUpdateRequest, String> updateAdminProductStockTool() {
        return new Function<AdminProductStockUpdateRequest, String>() {
            @Override
            public String apply(AdminProductStockUpdateRequest request) {
                try {
                    if (request.productName() == null || request.productName().isBlank()) {
                        return "Vui lòng cung cấp tên sản phẩm.";
                    }
                    if (request.sizeValue() == null) {
                        return "Vui lòng cung cấp kích cỡ (size).";
                    }
                    if (request.colorName() == null || request.colorName().isBlank()) {
                        return "Vui lòng cung cấp tên màu sắc.";
                    }
                    if (request.newStock() == null || request.newStock() < 0) {
                        return "Số lượng tồn kho mới không hợp lệ.";
                    }

                    List<Object[]> list = entityManager.createQuery(
                            "SELECT gct.id, gct.giaGoc, gct.giaBan, g.ten, ms.ten, kc.giaTri " +
                            "FROM GiayChiTiet gct " +
                            "JOIN gct.giay g " +
                            "JOIN gct.mauSac ms " +
                            "JOIN gct.kichCo kc " +
                            "WHERE LOWER(g.ten) LIKE :productName AND " +
                            "kc.giaTri = :sizeValue AND " +
                            "LOWER(ms.ten) = :colorName", Object[].class)
                            .setParameter("productName", "%" + request.productName().toLowerCase().trim() + "%")
                            .setParameter("sizeValue", String.valueOf(request.sizeValue()))
                            .setParameter("colorName", request.colorName().toLowerCase().trim())
                            .getResultList();

                    if (list.isEmpty()) {
                        return String.format("Không tìm thấy biến thể sản phẩm cho: %s (Size %d, Màu %s)", 
                                request.productName(), request.sizeValue(), request.colorName());
                    }

                    Object[] variant = list.get(0);
                    Integer variantId = (Integer) variant[0];
                    BigDecimal giaGoc = (BigDecimal) variant[1];
                    BigDecimal giaBan = (BigDecimal) variant[2];
                    String tenSanPham = (String) variant[3];
                    String tenMau = (String) variant[4];
                    String kichCo = (String) variant[5];
                    var updateRequest = new com.example.server.core.admin.quanLySanPham.dto.request.CapNhatBienTheRequest(
                            request.newStock(),
                            giaGoc,
                            giaBan,
                            request.newStock() > 0 ? 1 : 0
                    );

                    quanLySanPhamService.capNhatBienThe(variantId, updateRequest);
                    return String.format("Đã cập nhật số lượng của biến thể **%s (Màu %s, Size %s)** thành **%d** thành công.",
                            tenSanPham, tenMau, kichCo, request.newStock());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi cập nhật số lượng: " + e.getMessage();
                }
            }
        };
    }

    @Bean("create_admin_voucher_tool")
    @Description("Tạo nhanh mã giảm giá mới (voucher) trong hệ thống với các tham số tương ứng")
    public Function<AdminVoucherCreateRequest, String> createAdminVoucherTool() {
        return new Function<AdminVoucherCreateRequest, String>() {
            @Override
            public String apply(AdminVoucherCreateRequest request) {
                try {
                    if (request.code() == null || request.code().isBlank()) {
                        return "Vui lòng cung cấp mã giảm giá.";
                    }
                    if (request.name() == null || request.name().isBlank()) {
                        return "Vui lòng cung cấp tên hiển thị của mã giảm giá.";
                    }
                    if (request.value() == null || request.value().doubleValue() <= 0) {
                        return "Giá trị giảm giá không hợp lệ.";
                    }
                    if (request.quantity() == null || request.quantity() <= 0) {
                        return "Số lượng mã giảm giá phát hành phải lớn hơn 0.";
                    }

                    var voucherRequest = new com.example.server.core.admin.quanlykhuyenmai.dto.request.PhieuGiamGiaRequest();
                    voucherRequest.setMa(request.code().trim().toUpperCase());
                    voucherRequest.setTen(request.name().trim());
                    // loai: 1 là phần trăm (%), 2 là tiền mặt (VND)
                    voucherRequest.setLoai(request.type() != null ? request.type() : 1);
                    voucherRequest.setLoaiPhieu(1); // 1: Công khai (public)
                    voucherRequest.setGiaTri(request.value());
                    voucherRequest.setGiaTriToiThieu(request.minOrder() != null ? request.minOrder() : BigDecimal.ZERO);
                    voucherRequest.setGiamToiDa(request.maxDiscount() != null ? request.maxDiscount() : BigDecimal.ZERO);
                    voucherRequest.setNgayBatDau(java.time.LocalDate.now());
                    
                    int duration = request.durationDays() != null ? request.durationDays() : 30;
                    voucherRequest.setNgayKetThuc(java.time.LocalDate.now().plusDays(duration));
                    voucherRequest.setSoLuong(request.quantity());
                    voucherRequest.setTrangThai(1); // 1: Hoạt động

                    phieuGiamGiaService.add(voucherRequest);
                    
                    String typeStr = voucherRequest.getLoai() == 1 ? "%" : " VNĐ";
                    return String.format("Đã tạo thành công mã giảm giá **%s** (%s) giảm %s%s, áp dụng từ hôm nay đến hết ngày %s.",
                            voucherRequest.getMa(), 
                            voucherRequest.getTen(), 
                            voucherRequest.getGiaTri().toPlainString(), 
                            typeStr,
                            voucherRequest.getNgayKetThuc().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi tạo mã giảm giá: " + e.getMessage();
                }
            }
        };
    }

    @Bean("get_admin_chart_data_tool")
    @Description("Cung cấp dữ liệu thô dạng JSON để vẽ biểu đồ thống kê (chartType: 'revenue_7_days' - doanh thu 7 ngày qua, 'top_selling_shoes' - top 5 giày bán chạy nhất, 'order_statuses' - tỷ lệ các trạng thái hóa đơn)")
    public Function<AdminChartDataRequest, String> getAdminChartDataTool() {
        return new Function<AdminChartDataRequest, String>() {
            @Override
            public String apply(AdminChartDataRequest request) {
                try {
                    if ("revenue_7_days".equalsIgnoreCase(request.chartType())) {
                        java.time.LocalDate today = java.time.LocalDate.now();
                        List<String> labelsList = new ArrayList<>();
                        List<BigDecimal> dataList = new ArrayList<>();
                        java.time.format.DateTimeFormatter labelFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

                        for (int i = 6; i >= 0; i--) {
                            java.time.LocalDate date = today.minusDays(i);
                            labelsList.add(date.format(labelFormatter));
                            
                            java.time.Instant start = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
                            java.time.Instant end = date.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();

                            List<BigDecimal> amounts = entityManager.createQuery(
                                    "SELECT SUM(h.tongTienThanhToan) FROM HoaDon h WHERE h.trangThai = 5 AND h.ngayLap >= :start AND h.ngayLap < :end", BigDecimal.class)
                                    .setParameter("start", start)
                                    .setParameter("end", end)
                                    .getResultList();

                            BigDecimal dayTotal = (amounts.isEmpty() || amounts.get(0) == null) ? BigDecimal.ZERO : amounts.get(0);
                            dataList.add(dayTotal);
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"chartType\":\"line\",\"title\":\"Doanh thu 7 ngày gần nhất\",\"labels\":[");
                        for (int i = 0; i < labelsList.size(); i++) {
                            sb.append("\"").append(labelsList.get(i)).append("\"");
                            if (i < labelsList.size() - 1) sb.append(",");
                        }
                        sb.append("],\"data\":[");
                        for (int i = 0; i < dataList.size(); i++) {
                            sb.append(dataList.get(i).toPlainString());
                            if (i < dataList.size() - 1) sb.append(",");
                        }
                        sb.append("]}");
                        return sb.toString();
                    }
                    else if ("top_selling_shoes".equalsIgnoreCase(request.chartType())) {
                        List<Object[]> results = entityManager.createQuery(
                                "SELECT hdct.giayChiTiet.giay.ten, SUM(hdct.soLuong) FROM HoaDonChiTiet hdct " +
                                "WHERE hdct.hoaDon.trangThai = 5 GROUP BY hdct.giayChiTiet.giay.ten ORDER BY SUM(hdct.soLuong) DESC", Object[].class)
                                .setMaxResults(5)
                                .getResultList();

                        List<String> labelsList = new ArrayList<>();
                        List<Long> dataList = new ArrayList<>();
                        for (Object[] row : results) {
                            labelsList.add((String) row[0]);
                            dataList.add((Long) row[1]);
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"chartType\":\"bar\",\"title\":\"Top 5 sản phẩm bán chạy nhất\",\"labels\":[");
                        for (int i = 0; i < labelsList.size(); i++) {
                            sb.append("\"").append(labelsList.get(i).replace("\"", "\\\"")).append("\"");
                            if (i < labelsList.size() - 1) sb.append(",");
                        }
                        sb.append("],\"data\":[");
                        for (int i = 0; i < dataList.size(); i++) {
                            sb.append(dataList.get(i));
                            if (i < dataList.size() - 1) sb.append(",");
                        }
                        sb.append("]}");
                        return sb.toString();
                    }
                    else if ("order_statuses".equalsIgnoreCase(request.chartType())) {
                        List<Object[]> results = entityManager.createQuery(
                                "SELECT h.trangThai, COUNT(h.id) FROM HoaDon h GROUP BY h.trangThai", Object[].class)
                                .getResultList();

                        List<String> labelsList = new ArrayList<>();
                        List<Long> dataList = new ArrayList<>();
                        for (Object[] row : results) {
                            Integer statusInt = (Integer) row[0];
                            Long count = (Long) row[1];
                            String name = "Mã: " + statusInt;
                            try {
                                name = TrangThaiHoaDon.tuMa(statusInt).getTen();
                            } catch (Exception e) {}
                            labelsList.add(name);
                            dataList.add(count);
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"chartType\":\"doughnut\",\"title\":\"Tỷ lệ trạng thái đơn hàng\",\"labels\":[");
                        for (int i = 0; i < labelsList.size(); i++) {
                            sb.append("\"").append(labelsList.get(i)).append("\"");
                            if (i < labelsList.size() - 1) sb.append(",");
                        }
                        sb.append("],\"data\":[");
                        for (int i = 0; i < dataList.size(); i++) {
                            sb.append(dataList.get(i));
                            if (i < dataList.size() - 1) sb.append(",");
                        }
                        sb.append("]}");
                        return sb.toString();
                    }
                    else {
                        return "Loại biểu đồ không hợp lệ.";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi lấy dữ liệu biểu đồ: " + e.getMessage();
                }
            }
        };
    }

    @Bean("export_admin_data_csv_tool")
    @Description("Xuất file báo cáo Excel dạng CSV trực tiếp từ cơ sở dữ liệu (dataType: 'revenue' - doanh thu, 'cancelled_invoices' - đơn hàng bị hủy, 'low_stock' - sản phẩm tồn kho thấp)")
    public Function<AdminCsvExportRequest, String> exportAdminDataCsvTool() {
        return new Function<AdminCsvExportRequest, String>() {
            @Override
            public String apply(AdminCsvExportRequest request) {
                try {
                    String csvContent = "";
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(java.time.ZoneId.systemDefault());

                    if ("revenue".equalsIgnoreCase(request.dataType())) {
                        List<com.example.server.entity.HoaDon> results = entityManager.createQuery(
                                "SELECT h FROM HoaDon h WHERE h.trangThai = 5 ORDER BY h.ngayLap DESC", com.example.server.entity.HoaDon.class)
                                .getResultList();

                        StringBuilder sb = new StringBuilder("Mã hóa đơn,Khách hàng,Số điện thoại,Tổng thanh toán,Ngày lập\n");
                        for (com.example.server.entity.HoaDon h : results) {
                            sb.append(h.getMa()).append(",")
                              .append(h.getTenNguoiNhan() != null ? h.getTenNguoiNhan().replace(",", " ") : "").append(",")
                              .append(h.getSdtNguoiNhan() != null ? h.getSdtNguoiNhan() : "").append(",")
                              .append(h.getTongTienThanhToan() != null ? h.getTongTienThanhToan().toPlainString() : "0").append(",")
                              .append(h.getNgayLap() != null ? formatter.format(h.getNgayLap()) : "").append("\n");
                        }
                        csvContent = sb.toString();
                    }
                    else if ("cancelled_invoices".equalsIgnoreCase(request.dataType())) {
                        List<com.example.server.entity.HoaDon> results = entityManager.createQuery(
                                "SELECT h FROM HoaDon h WHERE h.trangThai = 6 ORDER BY h.ngayLap DESC", com.example.server.entity.HoaDon.class)
                                .getResultList();

                        StringBuilder sb = new StringBuilder("Mã hóa đơn,Khách hàng,Số điện thoại,Tổng tiền hàng,Tiền giảm,Tổng thanh toán,Ngày lập\n");
                        for (com.example.server.entity.HoaDon h : results) {
                            sb.append(h.getMa()).append(",")
                              .append(h.getTenNguoiNhan() != null ? h.getTenNguoiNhan().replace(",", " ") : "").append(",")
                              .append(h.getSdtNguoiNhan() != null ? h.getSdtNguoiNhan() : "").append(",")
                              .append(h.getTongTienHang() != null ? h.getTongTienHang().toPlainString() : "0").append(",")
                              .append(h.getTienGiam() != null ? h.getTienGiam().toPlainString() : "0").append(",")
                              .append(h.getTongTienThanhToan() != null ? h.getTongTienThanhToan().toPlainString() : "0").append(",")
                              .append(h.getNgayLap() != null ? formatter.format(h.getNgayLap()) : "").append("\n");
                        }
                        csvContent = sb.toString();
                    }
                    else if ("low_stock".equalsIgnoreCase(request.dataType())) {
                        List<Object[]> results = entityManager.createQuery(
                                "SELECT g.ten, ms.ten, kc.giaTri, gct.soLuong " +
                                        "FROM GiayChiTiet gct " +
                                        "JOIN gct.giay g " +
                                        "JOIN gct.mauSac ms " +
                                        "JOIN gct.kichCo kc " +
                                        "WHERE gct.soLuong < 10 AND gct.kichHoat = 1 AND g.trangThai = 1 " +
                                        "ORDER BY gct.soLuong ASC", Object[].class)
                                .getResultList();

                        StringBuilder sb = new StringBuilder("Tên sản phẩm,Màu sắc,Kích cỡ,Số lượng tồn\n");
                        for (Object[] row : results) {
                            String ten = row[0] != null ? ((String) row[0]).replace(",", " ") : "Giày";
                            String mau = row[1] != null ? (String) row[1] : "";
                            String size = row[2] != null ? (String) row[2] : "";
                            Integer soLuong = row[3] != null ? (Integer) row[3] : 0;
                            sb.append(ten).append(",")
                              .append(mau).append(",")
                              .append(size).append(",")
                              .append(soLuong).append("\n");
                        }
                        csvContent = sb.toString();
                    }
                    else {
                        return "Loại dữ liệu xuất báo cáo không hợp lệ.";
                    }

                    // Convert to UTF-8 bytes with BOM
                    byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
                    byte[] csvBytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    byte[] fileBytes = new byte[bom.length + csvBytes.length];
                    System.arraycopy(bom, 0, fileBytes, 0, bom.length);
                    System.arraycopy(csvBytes, 0, fileBytes, bom.length, csvBytes.length);

                    String token = java.util.UUID.randomUUID().toString();
                    com.example.server.core.client.chatbot.service.ChatbotService.EXPORT_CACHE.put(token, fileBytes);

                    return "[Tải file Excel báo cáo](/api/v1/admin/chatbot/download-csv?token=" + token + ")";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Lỗi khi xuất file báo cáo: " + e.getMessage();
                }
            }
        };
    }

    private static String cleanJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", " ")
                  .replace("\r", " ");
    }

    private String formatProductBlock(com.example.server.entity.Giay g, String badgeText, String nameSuffix) {
        if (g == null) return "";
        Integer giayId = g.getId();
        String ten = g.getTen() != null ? g.getTen() : "Giày";
        if (nameSuffix != null && !nameSuffix.isBlank()) {
            ten += nameSuffix;
        }

        String hinhAnh = g.getHinhAnh();
        if (hinhAnh == null || hinhAnh.isBlank()) {
            List<com.example.server.entity.HinhAnhGiay> images = entityManager.createQuery(
                    "SELECT h FROM HinhAnhGiay h WHERE h.giayChiTiet.giay.id = :giayId AND h.trangThai = 1 ORDER BY h.laHinhChinh DESC", com.example.server.entity.HinhAnhGiay.class)
                    .setParameter("giayId", giayId)
                    .setMaxResults(1)
                    .getResultList();
            if (!images.isEmpty() && images.get(0).getUrl() != null) {
                hinhAnh = images.get(0).getUrl();
            }
        }

        BigDecimal giaBan = entityManager.createQuery(
                "SELECT MIN(gct.giaBan) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", BigDecimal.class)
                .setParameter("giayId", giayId)
                .getResultList()
                .stream().findFirst().orElse(BigDecimal.ZERO);
        if (giaBan == null) giaBan = BigDecimal.ZERO;
        BigDecimal giaThucTe = calculateActualPrice(giayId, giaBan);

        Long soLuong = entityManager.createQuery(
                "SELECT COALESCE(SUM(gct.soLuong), 0L) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", Long.class)
                .setParameter("giayId", giayId)
                .getSingleResult();

        List<String> kichCos = entityManager.createQuery(
                "SELECT DISTINCT kc.giaTri FROM GiayChiTiet gct JOIN gct.kichCo kc WHERE gct.giay.id = :giayId AND gct.kichHoat = 1 ORDER BY kc.giaTri ASC", String.class)
                .setParameter("giayId", giayId)
                .getResultList();

        String sizeStr = "Nhiều size";
        if (!kichCos.isEmpty()) {
            if (kichCos.size() == 1) {
                sizeStr = kichCos.get(0);
            } else {
                sizeStr = kichCos.get(0) + " - " + kichCos.get(kichCos.size() - 1);
            }
        }

        String url = "/admin/san-pham";
        try {
            url = "/admin/san-pham?search=" + java.net.URLEncoder.encode(g.getTen() != null ? g.getTen() : "", java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception ex) {}

        return String.format("```product\n{\"name\":\"%s\",\"image\":\"%s\",\"price\":%s,\"originalPrice\":%s,\"color\":\"%s\",\"size\":\"%s\",\"stock\":%d,\"url\":\"%s\"}\n```\n",
                cleanJsonString(ten),
                cleanJsonString(hinhAnh),
                giaThucTe.setScale(0, java.math.RoundingMode.HALF_UP),
                giaBan.setScale(0, java.math.RoundingMode.HALF_UP),
                cleanJsonString(badgeText != null ? badgeText : "Sản phẩm"),
                cleanJsonString(sizeStr),
                soLuong != null ? soLuong : 0,
                url
        );
    }
}
