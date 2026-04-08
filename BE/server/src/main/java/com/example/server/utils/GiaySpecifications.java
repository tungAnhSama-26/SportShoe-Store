package com.example.server.utils;

import com.example.server.entity.Giay;
import com.example.server.entity.GiayChiTiet;
import com.example.server.entity.enums.Gender;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class GiaySpecifications {

    private GiaySpecifications() {
    }

    public static Specification<Giay> activeCatalog(
            String keyword,
            UUID brandId,
            UUID categoryId,
            UUID materialId,
            Gender gender,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return Specification.allOf(
                notDeleted(),
                keywordContains(keyword),
                hasBrand(brandId),
                hasCategory(categoryId),
                hasMaterial(materialId),
                hasGender(gender),
                priceFrom(minPrice),
                priceTo(maxPrice)
        );
    }

    private static Specification<Giay> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    private static Specification<Giay> keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String normalized = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), normalized),
                cb.like(cb.lower(root.get("code")), normalized)
        );
    }

    private static Specification<Giay> hasBrand(UUID brandId) {
        return brandId == null ? null : (root, query, cb) -> cb.equal(root.join("thuongHieu").get("id"), brandId);
    }

    private static Specification<Giay> hasCategory(UUID categoryId) {
        return categoryId == null ? null : (root, query, cb) -> cb.equal(root.join("loaiGiay", JoinType.LEFT).get("id"), categoryId);
    }

    private static Specification<Giay> hasMaterial(UUID materialId) {
        return materialId == null ? null : (root, query, cb) -> cb.equal(root.join("chatLieu", JoinType.LEFT).get("id"), materialId);
    }

    private static Specification<Giay> hasGender(Gender gender) {
        return gender == null ? null : (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }

    private static Specification<Giay> priceFrom(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Giay, GiayChiTiet> variants = root.join("bienThe", JoinType.LEFT);
            return cb.and(
                    cb.isFalse(variants.get("deleted")),
                    cb.greaterThanOrEqualTo(variants.get("salePrice"), minPrice)
            );
        };
    }

    private static Specification<Giay> priceTo(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Giay, GiayChiTiet> variants = root.join("bienThe", JoinType.LEFT);
            return cb.and(
                    cb.isFalse(variants.get("deleted")),
                    cb.lessThanOrEqualTo(variants.get("salePrice"), maxPrice)
            );
        };
    }
}
