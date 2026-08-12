package com.example.server.core.admin.quanlydanhgia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.server.entity.Giay;
import com.example.server.repository.DanhGiaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class DanhGiaXepHangServiceTest {

    private final DanhGiaRepository repository = mock(DanhGiaRepository.class);
    private final DanhGiaXepHangService service = new DanhGiaXepHangService(repository);

    @Test
    void sortsStablyAndExcludesInactiveProducts() {
        Giay mot = giay(1, 1);
        Giay hai = giay(2, 1);
        Giay ba = giay(3, 1);
        Giay ngungHoatDong = giay(4, 0);
        when(repository.thongKeXepHangDanhGia()).thenReturn(List.of(
                new Object[]{mot, 4.5, 10L},
                new Object[]{hai, 4.5, 20L},
                new Object[]{ba, 2.0, 5L},
                new Object[]{ngungHoatDong, 5.0, 100L}
        ));

        var result = service.thongKeTopVaThap();

        assertEquals(List.of(2, 1, 3), result.caoNhat().stream().map(item -> item.giay().getId()).toList());
        assertEquals(List.of(3, 2, 1), result.thapNhat().stream().map(item -> item.giay().getId()).toList());
    }

    @Test
    void returnsEmptyListsWhenThereAreNoVisibleReviews() {
        when(repository.thongKeXepHangDanhGia()).thenReturn(List.of());

        var result = service.thongKeTopVaThap();

        assertTrue(result.caoNhat().isEmpty());
        assertTrue(result.thapNhat().isEmpty());
    }

    @Test
    void limitsEachRankingToFiveUniqueProducts() {
        when(repository.thongKeXepHangDanhGia()).thenReturn(List.of(
                new Object[]{giay(1, 1), 1.0, 1L},
                new Object[]{giay(2, 1), 2.0, 1L},
                new Object[]{giay(3, 1), 3.0, 1L},
                new Object[]{giay(4, 1), 4.0, 1L},
                new Object[]{giay(5, 1), 5.0, 1L},
                new Object[]{giay(6, 1), 4.5, 1L}
        ));

        var result = service.thongKeTopVaThap();

        assertEquals(5, result.caoNhat().size());
        assertEquals(5, result.thapNhat().size());
        assertEquals(5, result.caoNhat().stream().map(item -> item.giay().getId()).distinct().count());
        assertEquals(5, result.thapNhat().stream().map(item -> item.giay().getId()).distinct().count());
    }

    private Giay giay(int id, int trangThai) {
        Giay giay = new Giay();
        giay.setId(id);
        giay.setTrangThai(trangThai);
        giay.setTen("Giày " + id);
        return giay;
    }
}
