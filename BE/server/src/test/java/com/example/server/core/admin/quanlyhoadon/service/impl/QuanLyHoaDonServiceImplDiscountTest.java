package com.example.server.core.admin.quanlyhoadon.service.impl;

import com.example.server.entity.DotGiamGia;
import com.example.server.entity.DotGiamGiaSanPham;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuanLyHoaDonServiceImplDiscountTest {

    private static final LocalDate INVOICE_DATE = LocalDate.of(2026, 8, 14);

    @Test
    void selectProductDiscount_chon25PhanTramKhiRepositoryTra15PhanTramTruoc() {
        DotGiamGiaSanPham discount15 = percentageDiscount("Giảm 15%", "15");
        DotGiamGiaSanPham discount25 = percentageDiscount("Giảm 25%", "25");

        var selected = QuanLyHoaDonServiceImpl.selectProductDiscount(
                new BigDecimal("1150000"),
                new BigDecimal("862500"),
                INVOICE_DATE,
                List.of(discount15, discount25)
        );

        assertThat(selected).isNotNull();
        assertThat(selected.discount().getTen()).isEqualTo("Giảm 25%");
        assertThat(selected.discount().getGiaTriGiam()).isEqualByComparingTo("25");
        assertThat(selected.discountedPrice()).isEqualByComparingTo("862500");
    }

    @Test
    void selectProductDiscount_khongPhuThuocThuTuRepository() {
        DotGiamGiaSanPham discount15 = percentageDiscount("Giảm 15%", "15");
        DotGiamGiaSanPham discount25 = percentageDiscount("Giảm 25%", "25");

        var selected = QuanLyHoaDonServiceImpl.selectProductDiscount(
                new BigDecimal("1150000"),
                new BigDecimal("862500"),
                INVOICE_DATE,
                List.of(discount25, discount15)
        );

        assertThat(selected).isNotNull();
        assertThat(selected.discount().getGiaTriGiam()).isEqualByComparingTo("25");
    }

    @Test
    void selectProductDiscount_boQuaDotKhongNamTrongNgayTaoHoaDon() {
        DotGiamGiaSanPham expired25 = percentageDiscount("Giảm 25% hết hạn", "25");
        expired25.getDotGiamGia().setNgayKetThuc(INVOICE_DATE.minusDays(1));
        DotGiamGiaSanPham discount15 = percentageDiscount("Giảm 15%", "15");

        var selected = QuanLyHoaDonServiceImpl.selectProductDiscount(
                new BigDecimal("1000000"),
                new BigDecimal("850000"),
                INVOICE_DATE,
                List.of(expired25, discount15)
        );

        assertThat(selected).isNotNull();
        assertThat(selected.discount().getGiaTriGiam()).isEqualByComparingTo("15");
    }

    private DotGiamGiaSanPham percentageDiscount(String name, String value) {
        DotGiamGia discount = new DotGiamGia();
        discount.setTen(name);
        discount.setLoaiGiam(1);
        discount.setGiaTriGiam(new BigDecimal(value));
        discount.setNgayBatDau(INVOICE_DATE.minusDays(1));
        discount.setNgayKetThuc(INVOICE_DATE.plusDays(1));

        DotGiamGiaSanPham link = new DotGiamGiaSanPham();
        link.setDotGiamGia(discount);
        return link;
    }
}
