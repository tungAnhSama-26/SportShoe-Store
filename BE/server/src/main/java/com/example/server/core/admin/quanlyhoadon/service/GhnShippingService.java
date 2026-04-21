package com.example.server.core.admin.quanlyhoadon.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GhnShippingService {

    private final RestClient restClient;
    private final String token;
    private final Integer shopId;
    private final Integer fromDistrictId;
    private final String fromWardCode;
    private final Integer defaultLength;
    private final Integer defaultWidth;
    private final Integer defaultHeight;
    private final Integer defaultWeight;
    private final Integer defaultServiceTypeId;

    public GhnShippingService(
            @Value("${ghn.base-url:https://api-dev.ghn.vn/shiip/public-api}") String baseUrl,
            @Value("${ghn.token:}") String token,
            @Value("${ghn.shop-id:0}") Integer shopId,
            @Value("${ghn.from-district-id:0}") Integer fromDistrictId,
            @Value("${ghn.from-ward-code:}") String fromWardCode,
            @Value("${ghn.default-length:30}") Integer defaultLength,
            @Value("${ghn.default-width:20}") Integer defaultWidth,
            @Value("${ghn.default-height:12}") Integer defaultHeight,
            @Value("${ghn.default-weight:500}") Integer defaultWeight,
            @Value("${ghn.default-service-type-id:2}") Integer defaultServiceTypeId
    ) {
        this.restClient = RestClient.builder().baseUrl(trimTrailingSlash(baseUrl)).build();
        this.token = token;
        this.shopId = shopId;
        this.fromDistrictId = fromDistrictId;
        this.fromWardCode = fromWardCode;
        this.defaultLength = defaultLength;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.defaultWeight = defaultWeight;
        this.defaultServiceTypeId = defaultServiceTypeId;
    }

    public TinhPhiVanChuyenGhnResponse tinhPhi(
            HoaDon hoaDon,
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request
    ) {
        validateConfig();

        JsonNode response = restClient.post()
                .uri("/v2/shipping-order/fee")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", token)
                .header("ShopId", String.valueOf(shopId))
                .body(buildRequestBody(hoaDon, items, request))
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.path("code").asInt(-1) != 200) {
            String message = response != null ? response.path("message").asText("GHN tinh phi that bai") : "GHN khong tra ve du lieu";
            throw new BusinessException(message);
        }

        JsonNode data = response.path("data");
        Integer total = data.path("total").isMissingNode() ? null : data.path("total").asInt();
        return new TinhPhiVanChuyenGhnResponse(
                BigDecimal.valueOf(total != null ? total : 0),
                total,
                readInt(data, "service_fee"),
                readInt(data, "insurance_fee"),
                readInt(data, "pick_station_fee"),
                readInt(data, "coupon_value")
        );
    }

    private Map<String, Object> buildRequestBody(
            HoaDon hoaDon,
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_district_id", fromDistrictId);
        body.put("from_ward_code", fromWardCode);
        body.put("to_district_id", request.toDistrictId());
        body.put("to_ward_code", request.toWardCode());

        if (request.serviceId() != null) {
            body.put("service_id", request.serviceId());
        } else {
            body.put("service_type_id", request.serviceTypeId() != null ? request.serviceTypeId() : defaultServiceTypeId);
        }

        body.put("length", valueOrDefault(request.length(), defaultLength));
        body.put("width", valueOrDefault(request.width(), defaultWidth));
        body.put("height", valueOrDefault(request.height(), defaultHeight));
        body.put("weight", valueOrDefault(request.weight(), tinhCanNangMacDinh(items)));
        body.put("insurance_value", request.insuranceValue() != null ? request.insuranceValue() : safeMoney(hoaDon.getTongTienHang()));
        body.put("coupon", request.coupon());
        body.put("items", items.stream().map(this::mapItem).toList());
        return body;
    }

    private Map<String, Object> mapItem(HoaDonChiTiet item) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("name", item.getGiayChiTiet().getGiay().getTen());
        mapped.put("quantity", item.getSoLuong());
        mapped.put("length", defaultLength);
        mapped.put("width", defaultWidth);
        mapped.put("height", defaultHeight);
        mapped.put("weight", defaultWeight);
        return mapped;
    }

    private Integer tinhCanNangMacDinh(List<HoaDonChiTiet> items) {
        int tongSoLuong = items.stream().mapToInt(item -> item.getSoLuong() != null ? item.getSoLuong() : 0).sum();
        return Math.max(defaultWeight, defaultWeight * Math.max(tongSoLuong, 1));
    }

    private Integer valueOrDefault(Integer value, Integer defaultValue) {
        return value != null && value > 0 ? value : defaultValue;
    }

    private Integer safeMoney(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return value.min(BigDecimal.valueOf(5_000_000)).max(BigDecimal.ZERO).intValue();
    }

    private Integer readInt(JsonNode node, String field) {
        return node.has(field) && !node.path(field).isNull() ? node.path(field).asInt() : null;
    }

    private void validateConfig() {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Chua cau hinh ghn.token");
        }
        if (shopId == null || shopId <= 0) {
            throw new BusinessException("Chua cau hinh ghn.shop-id");
        }
        if (fromDistrictId == null || fromDistrictId <= 0) {
            throw new BusinessException("Chua cau hinh ghn.from-district-id");
        }
        if (fromWardCode == null || fromWardCode.isBlank()) {
            throw new BusinessException("Chua cau hinh ghn.from-ward-code");
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://api-dev.ghn.vn/shiip/public-api";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
