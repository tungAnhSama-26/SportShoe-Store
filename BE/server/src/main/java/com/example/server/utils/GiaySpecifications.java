package com.example.server.utils;

import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class GiaySpecifications {

    private GiaySpecifications() {
    }

    public static Specification<Giay> filter(
            String keyword,
            Integer gioiTinh,
            Integer trangThai,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return Specification.where(keywordContains(keyword))
                .and(hasGender(gioiTinh))
                .and(hasStatus(trangThai))
                .and(priceFrom(minPrice))
                .and(priceTo(maxPrice));
    }

    public static Specification<Giay> activeProducts() {
        return hasStatus(1);
    }

    private static Specification<Giay> keywordContains(String keyword) {
        if (!SpecificationUtils.hasText(keyword)) {
            return null;
        }

        String normalized = SpecificationUtils.containsPattern(keyword);
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("ma")), normalized),
                cb.like(cb.lower(root.get("ten")), normalized),
                cb.like(cb.lower(cb.coalesce(root.get("chatLieu"), "")), normalized)
        );
    }

    private static Specification<Giay> hasGender(Integer gioiTinh) {
        return gioiTinh == null ? null : (root, query, cb) -> cb.equal(root.get("gioiTinh"), gioiTinh);
    }

    private static Specification<Giay> hasStatus(Integer trangThai) {
        return trangThai == null ? null : (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
    }

    private static Specification<Giay> priceFrom(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }

        return (root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            var chiTietRoot = subquery.from(GiayChiTiet.class);
            subquery.select(chiTietRoot.get("id"))
                    .where(
                            cb.equal(chiTietRoot.get("giay"), root),
                            cb.greaterThanOrEqualTo(chiTietRoot.get("giaBan"), minPrice)
                    );
            return cb.exists(subquery);
        };
    }

    private static Specification<Giay> priceTo(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }

        return (root, query, cb) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            var chiTietRoot = subquery.from(GiayChiTiet.class);
            subquery.select(chiTietRoot.get("id"))
                    .where(
                            cb.equal(chiTietRoot.get("giay"), root),
                            cb.lessThanOrEqualTo(chiTietRoot.get("giaBan"), maxPrice)
                    );
            return cb.exists(subquery);
        };
    }
}
