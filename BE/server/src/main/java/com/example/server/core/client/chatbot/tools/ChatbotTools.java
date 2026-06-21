package com.example.server.core.client.chatbot.tools;

import com.example.server.core.client.chatbot.dto.ProductDto;
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

    public ChatbotTools(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public record SearchRequest(
            String keyword,
            String color,
            String category,
            String brand
    ) {}

    public record BestSellerRequest() {}

    @Bean("search_products_tool")
    @Description("Tìm kiếm sản phẩm giày theo các thuộc tính từ khóa, màu sắc, loại giày (category), hoặc thương hiệu (brand)")
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

                        result.add(new ProductDto(
                                 g.getId(), g.getMa(), g.getTen(), g.getMoTa(), g.getHinhAnh(),
                                 giaBan != null ? giaBan : BigDecimal.ZERO,
                                 mauSacs, kichCos, soLuongTon, daBan
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

    @Bean("get_best_selling_shoes_tool")
    @Description("Lấy danh sách các sản phẩm giày bán chạy nhất (Best Seller) của cửa hàng")
    public Function<BestSellerRequest, List<ProductDto>> getBestSellingShoesTool() {
        return new Function<BestSellerRequest, List<ProductDto>>() {
            @Override
            public List<ProductDto> apply(BestSellerRequest request) {
                try {
                    List<Object[]> sales = entityManager.createQuery(
                                    "SELECT gct.giay.id, SUM(hdct.soLuong) as totalSales FROM HoaDonChiTiet hdct " +
                                            "JOIN hdct.giayChiTiet gct " +
                                            "GROUP BY gct.giay.id " +
                                            "ORDER BY totalSales DESC", Object[].class)
                            .setMaxResults(5)
                            .getResultList();

                    List<ProductDto> result = new ArrayList<>();
                    for (Object[] row : sales) {
                        Integer giayId = (Integer) row[0];
                        com.example.server.entity.Giay g = entityManager.find(com.example.server.entity.Giay.class, giayId);
                        if (g == null || g.getTrangThai() != 1) continue;

                        List<String> mauSacs = entityManager.createQuery(
                                        "SELECT DISTINCT ms.ten FROM GiayChiTiet gct JOIN gct.mauSac ms WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", String.class)
                                .setParameter("giayId", giayId)
                                .getResultList();

                        List<String> kichCos = entityManager.createQuery(
                                        "SELECT DISTINCT kc.giaTri FROM GiayChiTiet gct JOIN gct.kichCo kc WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", String.class)
                                .setParameter("giayId", giayId)
                                .getResultList();

                        Long soLuongTon = entityManager.createQuery(
                                        "SELECT COALESCE(SUM(gct.soLuong), 0L) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", Long.class)
                                .setParameter("giayId", giayId)
                                .getSingleResult();

                        BigDecimal giaBan = entityManager.createQuery(
                                        "SELECT MIN(gct.giaBan) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", BigDecimal.class)
                                .setParameter("giayId", giayId)
                                .getSingleResult();

                        Long daBan = (Long) row[1];

                        result.add(new ProductDto(
                                g.getId(), g.getMa(), g.getTen(), g.getMoTa(), g.getHinhAnh(),
                                giaBan != null ? giaBan : BigDecimal.ZERO,
                                mauSacs, kichCos, soLuongTon, daBan
                        ));
                    }

                    if (result.isEmpty()) {
                        List<com.example.server.entity.Giay> latest = entityManager.createQuery(
                                        "SELECT g FROM Giay g WHERE g.trangThai = 1 ORDER BY g.id DESC", com.example.server.entity.Giay.class)
                                .setMaxResults(5)
                                .getResultList();
                        for (com.example.server.entity.Giay g : latest) {
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

                            BigDecimal giaBan = entityManager.createQuery(
                                            "SELECT MIN(gct.giaBan) FROM GiayChiTiet gct WHERE gct.giay.id = :giayId AND gct.kichHoat = 1", BigDecimal.class)
                                    .setParameter("giayId", g.getId())
                                    .getSingleResult();

                            result.add(new ProductDto(
                                    g.getId(), g.getMa(), g.getTen(), g.getMoTa(), g.getHinhAnh(),
                                    giaBan != null ? giaBan : BigDecimal.ZERO,
                                    mauSacs, kichCos, soLuongTon, 0L
                            ));
                        }
                    }
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of();
                }
            }
        };
    }

    public record CouponSearchRequest(
            String keyword
    ) {}

    public record CouponDto(
            Integer id,
            String ma,
            String ten,
            String loaiText,
            String loaiPhieuText,
            BigDecimal giaTri,
            BigDecimal giaTriToiThieu,
            BigDecimal giamToiDa,
            String ngayBatDau,
            String ngayKetThuc,
            Integer soLuong,
            Integer soLuongDaDung,
            String trangThaiText
    ) {}

    public record PromotionSearchRequest(
            String keyword
    ) {}

    public record PromotionDto(
            Integer id,
            String ma,
            String ten,
            String moTa,
            String loaiGiamText,
            BigDecimal giaTriGiam,
            String ngayBatDau,
            String ngayKetThuc,
            String trangThaiText
    ) {}

    @Bean("search_coupons_tool")
    @Description("Tìm kiếm các phiếu giảm giá (vouchers/coupons) còn hoạt động hoặc sắp diễn ra dựa trên từ khóa hoặc lấy tất cả nếu không truyền từ khóa")
    public Function<CouponSearchRequest, List<CouponDto>> searchCouponsTool() {
        return new Function<CouponSearchRequest, List<CouponDto>>() {
            @Override
            public List<CouponDto> apply(CouponSearchRequest request) {
                try {
                    StringBuilder jpql = new StringBuilder("SELECT p FROM PhieuGiamGia p WHERE p.trangThai IN (1, 4) ");
                    Map<String, Object> params = new HashMap<>();

                    if (request.keyword() != null && !request.keyword().isBlank()) {
                        jpql.append("AND (LOWER(p.ma) LIKE :keyword OR LOWER(p.ten) LIKE :keyword) ");
                        params.put("keyword", "%" + request.keyword().toLowerCase().trim() + "%");
                    }
                    jpql.append("ORDER BY p.ngayTao DESC");

                    var query = entityManager.createQuery(jpql.toString(), com.example.server.entity.PhieuGiamGia.class);
                    params.forEach(query::setParameter);
                    query.setMaxResults(10);
                    List<com.example.server.entity.PhieuGiamGia> coupons = query.getResultList();

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
    @Description("Tìm kiếm các chương trình/đợt giảm giá (sales/promotions) đang diễn ra hoặc sắp diễn ra dựa trên từ khóa hoặc lấy tất cả nếu không truyền từ khóa")
    public Function<PromotionSearchRequest, List<PromotionDto>> searchPromotionsTool() {
        return new Function<PromotionSearchRequest, List<PromotionDto>>() {
            @Override
            public List<PromotionDto> apply(PromotionSearchRequest request) {
                try {
                    StringBuilder jpql = new StringBuilder("SELECT d FROM DotGiamGia d WHERE d.kichHoat IN (1, 4) ");
                    Map<String, Object> params = new HashMap<>();

                    if (request.keyword() != null && !request.keyword().isBlank()) {
                        jpql.append("AND (LOWER(d.ma) LIKE :keyword OR LOWER(d.ten) LIKE :keyword OR LOWER(d.moTa) LIKE :keyword) ");
                        params.put("keyword", "%" + request.keyword().toLowerCase().trim() + "%");
                    }
                    jpql.append("ORDER BY d.ngayTao DESC");

                    var query = entityManager.createQuery(jpql.toString(), com.example.server.entity.DotGiamGia.class);
                    params.forEach(query::setParameter);
                    query.setMaxResults(10);
                    List<com.example.server.entity.DotGiamGia> promos = query.getResultList();

                    List<PromotionDto> result = new ArrayList<>();
                    for (com.example.server.entity.DotGiamGia d : promos) {
                        String loaiGiamText = (d.getLoaiGiam() != null && d.getLoaiGiam() == 1) ? "Phần trăm" : "Tiền mặt";
                        String trangThaiText = "Hoạt động";
                        if (d.getKichHoat() == 4) {
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
}
