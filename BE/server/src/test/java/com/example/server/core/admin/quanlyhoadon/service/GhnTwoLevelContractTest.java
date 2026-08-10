package com.example.server.core.admin.quanlyhoadon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.entity.HoaDon;
import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import com.example.server.infrastructure.address.VietnamAddressCatalogService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

class GhnTwoLevelContractTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    @EnabledIfSystemProperty(named = "ghn.contract", matches = "true")
    void tinhPhiChoPhuongMoiGomNhieuTuyenCu() throws Exception {
        Properties properties = new Properties();
        Path localConfig = Path.of("src/main/resources/application-local.properties");
        try (var reader = Files.newBufferedReader(localConfig, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        VietnamAddressCatalogService catalog = new VietnamAddressCatalogService(
                "https://provinces.open-api.vn/api",
                temporaryDirectory.resolve("catalog.json").toString()
        );
        GhnShippingService service = new GhnShippingService(
                catalog,
                required(properties, "ghn.base-url"),
                required(properties, "ghn.token"),
                Integer.valueOf(required(properties, "ghn.shop-id")),
                Integer.valueOf(required(properties, "ghn.from-district-id")),
                required(properties, "ghn.from-ward-code"),
                30,
                20,
                12,
                500,
                2
        );
        var address = new DiaChiHaiCapRequest(
                "1",
                "Thành phố Hà Nội",
                "166",
                "Phường Cầu Giấy",
                "Số 1 đường Trần Thái Tông"
        );
        var request = new TinhPhiVanChuyenGhnRequest(
                address,
                null,
                2,
                30,
                20,
                12,
                500,
                0,
                null
        );

        var result = service.tinhPhi(new HoaDon(), List.of(), request);

        assertThat(result.phiVanChuyen()).isPositive();
        assertThat(result.diaChiDaDoiSoat().phuongXaCode()).isEqualTo("166");
        assertThat(result.uocTinh()).isTrue();
        assertThat(result.nguonTinhPhi()).isEqualTo(GhnOfflineFeeService.SOURCE_LIVE);
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key, "").trim();
        if (value.isBlank()) throw new IllegalStateException("Thiếu cấu hình local: " + key);
        return value;
    }
}
