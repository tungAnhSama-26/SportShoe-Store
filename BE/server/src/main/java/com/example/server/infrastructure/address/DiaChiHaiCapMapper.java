package com.example.server.infrastructure.address;

import com.example.server.entity.DiaChiHaiCap;
import com.example.server.infrastructure.exception.BusinessException;
import java.util.Objects;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DiaChiHaiCapMapper {

    private DiaChiHaiCapMapper() {
    }

    public static DiaChiHaiCap toEntity(DiaChiHaiCapRequest request) {
        if (request == null) {
            throw new BusinessException("Địa chỉ không được để trống");
        }
        DiaChiHaiCap entity = new DiaChiHaiCap();
        update(entity, request);
        return entity;
    }

    public static void update(DiaChiHaiCap entity, DiaChiHaiCapRequest request) {
        if (entity == null || request == null) {
            throw new BusinessException("Địa chỉ không được để trống");
        }
        entity.setTinhThanh(requireText(request.tinhThanh(), "Tỉnh/Thành phố"));
        entity.setPhuongXa(requireText(request.phuongXa(), "Phường/Xã"));
        entity.setDiaChiCuThe(requireText(request.diaChiCuThe(), "Địa chỉ cụ thể"));
    }

    public static DiaChiHaiCapResponse toResponse(DiaChiHaiCap entity) {
        if (entity == null) {
            return null;
        }
        return new DiaChiHaiCapResponse(
                null,
                entity.getTinhThanh(),
                null,
                entity.getPhuongXa(),
                entity.getDiaChiCuThe(),
                format(entity)
        );
    }

    public static String format(DiaChiHaiCap entity) {
        if (entity == null) {
            return "";
        }
        return Stream.of(entity.getDiaChiCuThe(), entity.getPhuongXa(), entity.getTinhThanh())
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    public static boolean same(DiaChiHaiCap left, DiaChiHaiCap right) {
        return Objects.equals(normalize(left != null ? left.getTinhThanh() : null), normalize(right != null ? right.getTinhThanh() : null))
                && Objects.equals(normalize(left != null ? left.getPhuongXa() : null), normalize(right != null ? right.getPhuongXa() : null))
                && Objects.equals(normalize(left != null ? left.getDiaChiCuThe() : null), normalize(right != null ? right.getDiaChiCuThe() : null));
    }

    private static String requireText(String value, String field) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(field + " không được để trống");
        }
        return normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
