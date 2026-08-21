package com.example.server.core.client.chatbot.tools;

import com.example.server.core.admin.quanLySanPham.service.QuanLySanPhamService;
import com.example.server.core.admin.quanlydanhgia.service.DanhGiaXepHangService;
import com.example.server.core.admin.quanlyhoadon.service.QuanLyHoaDonService;
import com.example.server.core.admin.quanlykhuyenmai.service.PhieuGiamGiaService;
import com.example.server.entity.GiayChiTiet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatbotToolsAdminTest {

    @Test
    void lowStockToolBuildsProductFromProjectionWithoutLazyEntities() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Object[]> lowStockQuery = objectArrayQuery(entityManager, "SELECT gct.id");
        when(lowStockQuery.setParameter("limit", 5)).thenReturn(lowStockQuery);
        when(lowStockQuery.setMaxResults(10)).thenReturn(lowStockQuery);
        when(lowStockQuery.getResultList()).thenReturn(List.<Object[]>of(new Object[]{
                92,
                15,
                "Dior B22 Sneaker",
                "/products/dior-b22.jpg",
                "Đen",
                "43",
                new BigDecimal("3250000"),
                2
        }));

        TypedQuery<GiayChiTiet> priceQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(
                argThat(jpql -> jpql != null && jpql.contains("SELECT gct FROM GiayChiTiet gct")
                        && jpql.contains("gct.giay.id = :giayId")),
                eq(GiayChiTiet.class)))
                .thenReturn(priceQuery);
        when(priceQuery.setParameter("giayId", 15)).thenReturn(priceQuery);
        when(priceQuery.getResultList()).thenReturn(List.of());

        Function<ChatbotTools.AdminLowStockRequest, String> tool = tools(entityManager).getAdminLowStockTool();
        String result = tool.apply(new ChatbotTools.AdminLowStockRequest(5));

        assertTrue(result.contains("Dior B22 Sneaker"));
        assertTrue(result.contains("\"color\":\"Đen\""));
        assertTrue(result.contains("\"size\":\"43\""));
        assertTrue(result.contains("\"stock\":2"));
        assertFalse(result.contains("LazyInitializationException"));
        verify(lowStockQuery).setParameter("limit", 5);
    }

    @Test
    void lowStockToolReturnsClearMessageWhenProjectionIsEmpty() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Object[]> lowStockQuery = objectArrayQuery(entityManager, "SELECT gct.id");
        when(lowStockQuery.setParameter("limit", 8)).thenReturn(lowStockQuery);
        when(lowStockQuery.setMaxResults(10)).thenReturn(lowStockQuery);
        when(lowStockQuery.getResultList()).thenReturn(List.of());

        String result = tools(entityManager)
                .getAdminLowStockTool()
                .apply(new ChatbotTools.AdminLowStockRequest(8));

        assertTrue(result.contains("không có sản phẩm nào"));
        assertTrue(result.contains("dưới 8"));
    }

    @Test
    void adminBestSellerToolBuildsDatabaseProductCards() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Object[]> salesQuery = objectArrayQuery(entityManager, "SUM(hdct.soLuong)");
        when(salesQuery.setMaxResults(5)).thenReturn(salesQuery);
        when(salesQuery.getResultList()).thenReturn(List.<Object[]>of(new Object[]{
                15, "Dior B22 Sneaker", "/products/dior-b22.jpg", 7L
        }));

        TypedQuery<Object[]> variantsQuery = objectArrayQuery(entityManager, "SELECT ms.ten, kc.giaTri");
        when(variantsQuery.setParameter("giayId", 15)).thenReturn(variantsQuery);
        when(variantsQuery.getResultList()).thenReturn(List.of(
                new Object[]{"Đen", "42", new BigDecimal("3250000"), 3},
                new Object[]{"Đen", "43", new BigDecimal("3250000"), 2}
        ));

        TypedQuery<GiayChiTiet> priceQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(
                argThat(jpql -> jpql != null && jpql.contains("SELECT gct FROM GiayChiTiet gct")
                        && jpql.contains("gct.giay.id = :giayId")),
                eq(GiayChiTiet.class)))
                .thenReturn(priceQuery);
        when(priceQuery.setParameter("giayId", 15)).thenReturn(priceQuery);
        when(priceQuery.getResultList()).thenReturn(List.of());

        String result = tools(entityManager)
                .getAdminBestSellingShoesTool()
                .apply(new ChatbotTools.BestSellerRequest());

        assertTrue(result.contains("```product"));
        assertTrue(result.contains("Dior B22 Sneaker (Đã bán: 7)"));
        assertTrue(result.contains("\"color\":\"Đen\""));
        assertTrue(result.contains("\"size\":\"42, 43\""));
        assertTrue(result.contains("\"stock\":5"));
        assertTrue(result.contains("/admin/san-pham?search=Dior+B22+Sneaker"));
    }

    @Test
    void adminBestSellerToolDoesNotReturnAnEmptyHeading() {
        EntityManager entityManager = mock(EntityManager.class);
        TypedQuery<Object[]> salesQuery = objectArrayQuery(entityManager, "SUM(hdct.soLuong)");
        when(salesQuery.setMaxResults(5)).thenReturn(salesQuery);
        when(salesQuery.getResultList()).thenReturn(List.of());

        String result = tools(entityManager)
                .getAdminBestSellingShoesTool()
                .apply(new ChatbotTools.BestSellerRequest());

        assertTrue(result.contains("chưa có dữ liệu bán hàng hoàn thành"));
        assertFalse(result.contains("Danh sách sản phẩm bán chạy nhất"));
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<Object[]> objectArrayQuery(EntityManager entityManager, String jpqlFragment) {
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(entityManager.createQuery(
                argThat(jpql -> jpql != null && jpql.contains(jpqlFragment)),
                eq(Object[].class)))
                .thenReturn(query);
        return query;
    }

    private ChatbotTools tools(EntityManager entityManager) {
        return new ChatbotTools(
                entityManager,
                mock(QuanLyHoaDonService.class),
                mock(QuanLySanPhamService.class),
                mock(PhieuGiamGiaService.class),
                mock(DanhGiaXepHangService.class)
        );
    }
}
