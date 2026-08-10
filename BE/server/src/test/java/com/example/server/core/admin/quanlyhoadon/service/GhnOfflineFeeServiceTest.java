package com.example.server.core.admin.quanlyhoadon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.entity.HoaDon;
import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
import com.example.server.infrastructure.address.VietnamAddressCatalogService;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.DiaChiDaDoiSoat;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.PhuongXa;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.TinhThanh;
import com.example.server.infrastructure.exception.BusinessException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GhnOfflineFeeServiceTest {

    @TempDir
    java.nio.file.Path temporaryDirectory;

    @Test
    void usesPublicTariffWhenLegacyMappingSourceIsOffline() {
        VietnamAddressCatalogService catalog = mock(VietnamAddressCatalogService.class);
        when(catalog.doiSoat(any())).thenReturn(new DiaChiDaDoiSoat(
                new TinhThanh(92, "92", "Thành phố Cần Thơ", "thành phố trung ương"),
                new PhuongXa("31282", "Xã Đông Thuận", "xã"),
                "Số 1"
        ));
        when(catalog.layDonViHanhChinhCu("31282"))
                .thenThrow(new BusinessException("Không lấy được dữ liệu ánh xạ địa chỉ cũ"));
        GhnOfflineFeeService offline = new GhnOfflineFeeService(
                temporaryDirectory.resolve("fee-cache.json").toString(), 30, "1"
        );
        GhnShippingService service = new GhnShippingService(
                catalog, "https://invalid.local", "token", 1, 1486, "1A0406",
                30, 20, 12, 500, 2, offline
        );

        TinhPhiVanChuyenGhnResponse result = service.tinhPhi(
                new HoaDon(),
                List.of(),
                new TinhPhiVanChuyenGhnRequest(
                        new DiaChiHaiCapRequest("92", "Thành phố Cần Thơ", "31282", "Xã Đông Thuận", "Số 1"),
                        null, 2, 30, 20, 12, 500, 0, null
                )
        );

        assertThat(result.nguonTinhPhi()).isEqualTo(GhnOfflineFeeService.SOURCE_PUBLIC_TARIFF);
        assertThat(result.uocTinh()).isTrue();
        assertThat(result.phiVanChuyen()).isPositive();
        assertThat(result.diaChiDaDoiSoat().phuongXaCode()).isEqualTo("31282");
    }

    @Test
    void persistsAndReloadsExactLiveQuote() {
        java.nio.file.Path cacheFile = temporaryDirectory.resolve("fee-cache.json");
        GhnOfflineFeeService first = new GhnOfflineFeeService(cacheFile.toString(), 30, "1");
        GhnOfflineFeeService.FeeParameters parameters = new GhnOfflineFeeService.FeeParameters(
                null, 2, 30, 20, 12, 500, 0, null
        );
        String key = first.buildKey(1486, "1A0406", "92", "31282", parameters);
        DiaChiHaiCapResponse address = new DiaChiHaiCapResponse(
                "92", "Thành phố Cần Thơ", "31282", "Xã Đông Thuận", "Số 1",
                "Số 1, Xã Đông Thuận, Thành phố Cần Thơ"
        );
        first.saveLive(key, new TinhPhiVanChuyenGhnResponse(
                BigDecimal.valueOf(31_000), 31_000, 31_000, 0, 0, 0, address,
                true, GhnOfflineFeeService.SOURCE_LIVE, false, Instant.now(), LocalDate.of(2026, 3, 20)
        ));

        GhnOfflineFeeService reloaded = new GhnOfflineFeeService(cacheFile.toString(), 30, "1");
        TinhPhiVanChuyenGhnResponse cached = reloaded.fromCache(key, address).orElseThrow();

        assertThat(cached.phiVanChuyen()).isEqualByComparingTo("31000");
        assertThat(cached.nguonTinhPhi()).isEqualTo(GhnOfflineFeeService.SOURCE_CACHE);
        assertThat(cached.giaCu()).isFalse();
    }
}
