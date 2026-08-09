package com.example.server.core.admin.quanlyhoadon.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.entity.Giay;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.address.DiaChiHaiCapMapper;
import com.example.server.infrastructure.address.DiaChiHaiCapRequest;
import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
import com.example.server.infrastructure.address.VietnamAddressCatalogService;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.DiaChiDaDoiSoat;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.DonViCu;
import com.example.server.infrastructure.address.VietnamAddressCatalogService.DonViHanhChinhCu;
import com.example.server.infrastructure.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Adapter tính phí GHN. Hợp đồng công khai chỉ dùng địa chỉ 2 cấp.
 * DistrictID là chi tiết tương thích bắt buộc của GHN và không được trả về hay lưu trữ.
 */
@Service
public class GhnShippingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_BASE_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api";
    private static final String PROVINCE_ENDPOINT = "/master-data/province";
    private static final String DISTRICT_ENDPOINT = "/master-data/district";
    private static final String WARD_ENDPOINT = "/master-data/ward";
    private static final String SHIPPING_FEE_ENDPOINT = "/v2/shipping-order/fee";
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern ADMIN_PREFIX = Pattern.compile(
            "^(tinh|thanh pho|tp|phuong|xa|thi tran|dac khu|quan|huyen|thi xa)\\s+"
    );
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final RestClient restClient;
    private final VietnamAddressCatalogService addressCatalogService;
    private final String token;
    private final Integer shopId;
    private final Integer fromDistrictId;
    private final String fromWardCode;
    private final Integer defaultLength;
    private final Integer defaultWidth;
    private final Integer defaultHeight;
    private final Integer defaultWeight;
    private final Integer defaultServiceTypeId;
    private final GhnOfflineFeeService offlineFeeService;
    private final Map<Integer, List<GhnDistrict>> districtCache = new ConcurrentHashMap<>();
    private final Map<Integer, List<GhnWard>> wardCache = new ConcurrentHashMap<>();
    private volatile List<GhnProvince> provinceCache;

    @Autowired
    public GhnShippingService(
            VietnamAddressCatalogService addressCatalogService,
            @Value("${ghn.base-url:https://dev-online-gateway.ghn.vn/shiip/public-api}") String baseUrl,
            @Value("${ghn.token:}") String token,
            @Value("${ghn.shop-id:0}") Integer shopId,
            @Value("${ghn.from-district-id:0}") Integer fromDistrictId,
            @Value("${ghn.from-ward-code:}") String fromWardCode,
            @Value("${ghn.default-length:30}") Integer defaultLength,
            @Value("${ghn.default-width:20}") Integer defaultWidth,
            @Value("${ghn.default-height:12}") Integer defaultHeight,
            @Value("${ghn.default-weight:500}") Integer defaultWeight,
            @Value("${ghn.default-service-type-id:2}") Integer defaultServiceTypeId,
            GhnOfflineFeeService offlineFeeService
    ) {
        this.addressCatalogService = addressCatalogService;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(15_000);
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .requestFactory(requestFactory)
                .build();
        this.token = token;
        this.shopId = shopId;
        this.fromDistrictId = fromDistrictId;
        this.fromWardCode = fromWardCode;
        this.defaultLength = defaultLength;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.defaultWeight = defaultWeight;
        this.defaultServiceTypeId = defaultServiceTypeId;
        this.offlineFeeService = offlineFeeService;
    }

    public GhnShippingService(
            VietnamAddressCatalogService addressCatalogService,
            String baseUrl,
            String token,
            Integer shopId,
            Integer fromDistrictId,
            String fromWardCode,
            Integer defaultLength,
            Integer defaultWidth,
            Integer defaultHeight,
            Integer defaultWeight,
            Integer defaultServiceTypeId
    ) {
        this(
                addressCatalogService, baseUrl, token, shopId, fromDistrictId, fromWardCode,
                defaultLength, defaultWidth, defaultHeight, defaultWeight, defaultServiceTypeId,
                new GhnOfflineFeeService(
                        Path.of(System.getProperty("java.io.tmpdir"), "sportshoe-ghn-test-fee-cache.json").toString(),
                        30,
                        "1"
                )
        );
    }

    public TinhPhiVanChuyenGhnResponse tinhPhi(
            HoaDon hoaDon,
            List<HoaDonChiTiet> items,
        TinhPhiVanChuyenGhnRequest request
    ) {
        DiaChiHaiCapRequest input = requireAddress(request);
        DiaChiHaiCapResponse verifiedAddress = verifyAddress(input);
        GhnOfflineFeeService.FeeParameters parameters = resolveFeeParameters(items, request);
        String cacheKey = offlineFeeService.buildKey(
                fromDistrictId, fromWardCode,
                verifiedAddress.tinhThanhCode(), verifiedAddress.phuongXaCode(), parameters
        );
        try {
            TinhPhiVanChuyenGhnResponse live = tinhPhiLive(items, request, verifiedAddress);
            offlineFeeService.saveLive(cacheKey, live);
            return live;
        } catch (GhnUnavailableException unavailable) {
            return offlineFeeService.fromCache(cacheKey, verifiedAddress)
                    .orElseGet(() -> offlineFeeService.fromPublicTariff(
                            verifiedAddress.tinhThanhCode(), verifiedAddress, parameters
                    ));
        }
    }

    private TinhPhiVanChuyenGhnResponse tinhPhiLive(
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request,
            DiaChiHaiCapResponse verifiedAddress
    ) {
        validateConfig();
        List<GhnResolvedAddress> routes = resolveAddresses(verifiedAddress);
        List<FeeResult> results = new ArrayList<>();
        RuntimeException lastError = null;
        for (GhnResolvedAddress route : routes) {
            try {
                JsonNode data = callShippingFeeApi(items, request, route).path("data");
                results.add(new FeeResult(route, data));
            } catch (RuntimeException exception) {
                lastError = exception;
            }
        }
        if (results.isEmpty()) {
            if (lastError instanceof GhnUnavailableException unavailable) throw unavailable;
            if (lastError instanceof BusinessException businessException) throw businessException;
            throw new BusinessException("GHN chưa hỗ trợ tuyến giao hàng này");
        }
        FeeResult selected = results.stream()
                .max((left, right) -> Integer.compare(
                        valueOrDefault(readInt(left.data(), "total"), 0),
                        valueOrDefault(readInt(right.data(), "total"), 0)
                ))
                .orElseThrow();
        JsonNode data = selected.data();
        Integer total = readInt(data, "total");
        boolean estimated = routes.size() > 1;
        return new TinhPhiVanChuyenGhnResponse(
                BigDecimal.valueOf(total != null ? total : 0),
                total,
                readInt(data, "service_fee"),
                readInt(data, "insurance_fee"),
                readInt(data, "pick_station_fee"),
                readInt(data, "coupon_value"),
                selected.route().address(),
                estimated,
                GhnOfflineFeeService.SOURCE_LIVE,
                false,
                java.time.Instant.now(),
                offlineFeeService.tariffEffectiveDate()
        );
    }

    private DiaChiHaiCapResponse verifyAddress(DiaChiHaiCapRequest input) {
        DiaChiDaDoiSoat canonical = addressCatalogService.doiSoat(input);
        return new DiaChiHaiCapResponse(
                canonical.tinhThanh().code(), canonical.tinhThanh().ten(),
                canonical.phuongXa().code(), canonical.phuongXa().ten(), canonical.diaChiCuThe(),
                String.join(", ", canonical.diaChiCuThe(), canonical.phuongXa().ten(), canonical.tinhThanh().ten())
        );
    }

    private GhnOfflineFeeService.FeeParameters resolveFeeParameters(
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request
    ) {
        return new GhnOfflineFeeService.FeeParameters(
                request.serviceId(),
                valueOrDefault(request.serviceTypeId(), defaultServiceTypeId),
                valueOrDefault(request.length(), defaultLength),
                valueOrDefault(request.width(), defaultWidth),
                valueOrDefault(request.height(), defaultHeight),
                valueOrDefault(request.weight(), tinhCanNangMacDinh(items)),
                Math.max(0, request.insuranceValue() != null ? request.insuranceValue() : 0),
                request.coupon()
        );
    }

    private DiaChiHaiCapRequest requireAddress(TinhPhiVanChuyenGhnRequest request) {
        if (request == null || request.diaChiGiaoHang() == null) {
            throw new BusinessException("Thiếu địa chỉ giao hàng 2 cấp");
        }
        // Tái sử dụng validator nghiệp vụ tập trung kể cả khi service được gọi ngoài controller.
        DiaChiHaiCapMapper.toEntity(request.diaChiGiaoHang());
        return request.diaChiGiaoHang();
    }

    private List<GhnResolvedAddress> resolveAddresses(DiaChiHaiCapResponse verified) {
        List<DonViHanhChinhCu> legacyWards;
        try {
            legacyWards = addressCatalogService.layDonViHanhChinhCu(verified.phuongXaCode());
        } catch (BusinessException exception) {
            if (isExternalCatalogUnavailable(exception)) throw new GhnUnavailableException(exception);
            throw exception;
        }
        List<GhnResolvedAddress> resolvedRoutes = new ArrayList<>();
        for (DonViHanhChinhCu legacyWard : legacyWards) {
            try {
                resolvedRoutes.addAll(resolveLegacyRoute(legacyWard, verified));
            } catch (GhnUnavailableException unavailable) {
                throw unavailable;
            } catch (BusinessException exception) {
                if (isExternalCatalogUnavailable(exception)) throw new GhnUnavailableException(exception);
            } catch (RuntimeException ignored) {
                // Một đơn vị cũ không còn trong master-data GHN không được làm mất các tuyến hợp lệ khác.
            }
        }
        List<GhnResolvedAddress> routes = resolvedRoutes.stream().distinct().toList();
        if (routes.isEmpty()) {
            throw new BusinessException("GHN chưa hỗ trợ tuyến giao hàng này");
        }
        return routes;
    }

    private boolean isExternalCatalogUnavailable(BusinessException exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage();
        return message.startsWith("Không lấy được dữ liệu ánh xạ địa chỉ cũ")
                || message.startsWith("Không lấy được tỉnh/thành cũ")
                || message.startsWith("Không lấy được quận/huyện cũ");
    }

    private List<GhnResolvedAddress> resolveLegacyRoute(
            DonViHanhChinhCu legacyWard,
            DiaChiHaiCapResponse verified
    ) {
        DonViCu legacyProvince = addressCatalogService.layTinhThanhCu(legacyWard.provinceCode());
        DonViCu legacyDistrict = addressCatalogService.layQuanHuyenCu(legacyWard.districtCode());
        List<GhnProvince> provinces = getProvinces().stream()
                .filter(value -> matchesName(normalizeName(legacyProvince.ten()), value.name(), value.extensions()))
                .toList();
        List<GhnResolvedAddress> routes = new ArrayList<>();
        for (GhnProvince province : provinces) {
            List<GhnDistrict> districts = getDistricts(province.id()).stream()
                    .filter(value -> matchesName(
                            normalizeName(legacyDistrict.ten()),
                            value.name(),
                            value.extensions()
                    ))
                    .toList();
            for (GhnDistrict district : districts) {
                getWards(district.id()).stream()
                        .filter(value -> matchesName(
                                normalizeName(legacyWard.ten()),
                                value.name(),
                                value.extensions()
                        ))
                        .map(value -> new GhnResolvedAddress(district.id(), value.code(), verified))
                        .forEach(routes::add);
            }
        }
        return routes;
    }

    private boolean matchesName(String normalizedInput, String canonicalName, List<String> extensions) {
        if (normalizedInput.equals(normalizeName(canonicalName))) {
            return true;
        }
        return extensions.stream().anyMatch(value -> normalizedInput.equals(normalizeName(value)));
    }

    private JsonNode callShippingFeeApi(
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request,
            GhnResolvedAddress resolved
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_district_id", fromDistrictId);
        body.put("from_ward_code", fromWardCode);
        body.put("to_district_id", resolved.districtId());
        body.put("to_ward_code", resolved.wardCode());
        if (request.serviceId() != null) {
            body.put("service_id", request.serviceId());
        } else {
            body.put("service_type_id", valueOrDefault(request.serviceTypeId(), defaultServiceTypeId));
        }
        body.put("length", valueOrDefault(request.length(), defaultLength));
        body.put("width", valueOrDefault(request.width(), defaultWidth));
        body.put("height", valueOrDefault(request.height(), defaultHeight));
        body.put("weight", valueOrDefault(request.weight(), tinhCanNangMacDinh(items)));
        body.put("insurance_value", request.insuranceValue() != null ? request.insuranceValue() : 0);
        body.put("coupon", request.coupon());
        body.put("items", items.stream().map(this::mapItem).toList());
        try {
            String response = restClient.post().uri(SHIPPING_FEE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Token", token)
                    .header("ShopId", String.valueOf(shopId))
                    .body(body).retrieve().body(String.class);
            return readSuccessfulResponse(response, "GHN tính phí thất bại");
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError()) {
                throw new GhnUnavailableException(exception);
            }
            throw new BusinessException(readGhnErrorMessage(exception.getResponseBodyAsString(), "GHN tính phí thất bại"));
        } catch (RestClientException exception) {
            throw new GhnUnavailableException(exception);
        }
    }

    private List<GhnProvince> getProvinces() {
        if (provinceCache == null) {
            JsonNode response = getJson(PROVINCE_ENDPOINT, "Không lấy được danh sách tỉnh/thành GHN");
            provinceCache = readArray(response.path("data")).stream()
                    .map(node -> new GhnProvince(node.path("ProvinceID").asInt(), node.path("ProvinceName").asText(), readNameExtensions(node)))
                    .toList();
        }
        return provinceCache;
    }

    private List<GhnDistrict> getDistricts(Integer provinceId) {
        return districtCache.computeIfAbsent(provinceId, id -> {
            Map<String, Object> body = new HashMap<>();
            body.put("province_id", id);
            JsonNode response = postJson(DISTRICT_ENDPOINT, body, "Không lấy được dữ liệu trung gian GHN");
            return readArray(response.path("data")).stream()
                    .map(node -> new GhnDistrict(
                            node.path("DistrictID").asInt(),
                            node.path("DistrictName").asText(),
                            readNameExtensions(node)
                    ))
                    .toList();
        });
    }

    private List<GhnWard> getWards(Integer districtId) {
        return wardCache.computeIfAbsent(districtId, id -> {
            Map<String, Object> body = new HashMap<>();
            body.put("district_id", id);
            JsonNode response = postJson(WARD_ENDPOINT, body, "Không lấy được danh sách phường/xã GHN");
            return readArray(response.path("data")).stream()
                    .map(node -> new GhnWard(node.path("WardCode").asText(), node.path("WardName").asText(), readNameExtensions(node)))
                    .toList();
        });
    }

    private JsonNode getJson(String path, String fallback) {
        try {
            String response = restClient.get().uri(path).header("Token", token).retrieve().body(String.class);
            return readSuccessfulResponse(response, fallback);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError()) {
                throw new GhnUnavailableException(exception);
            }
            throw new BusinessException(readGhnErrorMessage(exception.getResponseBodyAsString(), fallback));
        } catch (RestClientException exception) {
            throw new GhnUnavailableException(exception);
        }
    }

    private JsonNode postJson(String path, Object body, String fallback) {
        try {
            String response = restClient.post().uri(path).contentType(MediaType.APPLICATION_JSON)
                    .header("Token", token).body(body).retrieve().body(String.class);
            return readSuccessfulResponse(response, fallback);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError()) {
                throw new GhnUnavailableException(exception);
            }
            throw new BusinessException(readGhnErrorMessage(exception.getResponseBodyAsString(), fallback));
        } catch (RestClientException exception) {
            throw new GhnUnavailableException(exception);
        }
    }

    private JsonNode readSuccessfulResponse(String responseBody, String fallback) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException(fallback);
        }
        try {
            JsonNode response = OBJECT_MAPPER.readTree(responseBody);
            if (response.path("code").asInt(-1) != 200) {
                throw new BusinessException(response.path("message").asText(fallback));
            }
            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(fallback + ": GHN trả dữ liệu không hợp lệ");
        }
    }

    private String readGhnErrorMessage(String responseBody, String fallback) {
        if (responseBody == null || responseBody.isBlank()) return fallback;
        try {
            String message = OBJECT_MAPPER.readTree(responseBody).path("message").asText("");
            return message.isBlank() ? fallback : message;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private List<JsonNode> readArray(JsonNode node) {
        List<JsonNode> result = new ArrayList<>();
        if (node != null && node.isArray()) node.forEach(result::add);
        return result;
    }

    private List<String> readNameExtensions(JsonNode node) {
        List<String> result = new ArrayList<>();
        JsonNode extensions = node.path("NameExtension");
        if (extensions.isArray()) extensions.forEach(value -> result.add(value.asText()));
        return result;
    }

    private String normalizeName(String value) {
        if (value == null) return "";
        String ascii = value.replace('Đ', 'D').replace('đ', 'd');
        String normalized = Normalizer.normalize(ascii, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll(" ");
        normalized = WHITESPACE.matcher(normalized).replaceAll(" ").trim();
        return ADMIN_PREFIX.matcher(normalized).replaceFirst("").trim();
    }

    private Map<String, Object> mapItem(HoaDonChiTiet item) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        String name = "Sản phẩm";
        if (item.getGiayChiTiet() != null && item.getGiayChiTiet().getGiay() != null) {
            name = item.getGiayChiTiet().getGiay().getTen();
        }
        mapped.put("name", name);
        mapped.put("quantity", item.getSoLuong());
        mapped.put("length", defaultLength);
        mapped.put("width", defaultWidth);
        mapped.put("height", defaultHeight);
        mapped.put("weight", layCanNangSanPham(item));
        return mapped;
    }

    private Integer layCanNangSanPham(HoaDonChiTiet item) {
        if (item.getGiayChiTiet() != null && item.getGiayChiTiet().getGiay() != null) {
            Giay giay = item.getGiayChiTiet().getGiay();
            if (giay.getGiayThuocTinh() != null && giay.getGiayThuocTinh().getTrongLuong() != null
                    && giay.getGiayThuocTinh().getTrongLuong().getGiaTri() != null) {
                return giay.getGiayThuocTinh().getTrongLuong().getGiaTri();
            }
        }
        return defaultWeight;
    }

    private Integer tinhCanNangMacDinh(List<HoaDonChiTiet> items) {
        int total = items.stream().mapToInt(item -> layCanNangSanPham(item) * Math.max(0, item.getSoLuong() != null ? item.getSoLuong() : 0)).sum();
        return total > 0 ? total : defaultWeight;
    }

    private Integer valueOrDefault(Integer value, Integer fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private Integer readInt(JsonNode node, String field) {
        return node.has(field) && !node.path(field).isNull() ? node.path(field).asInt() : null;
    }

    private void validateToken() {
        if (token == null || token.isBlank()) throw new BusinessException("Chưa cấu hình ghn.token");
    }

    private void validateConfig() {
        validateToken();
        if (shopId == null || shopId <= 0) throw new BusinessException("Chưa cấu hình ghn.shop-id");
        if (fromDistrictId == null || fromDistrictId <= 0) throw new BusinessException("Chưa cấu hình ghn.from-district-id");
        if (fromWardCode == null || fromWardCode.isBlank()) throw new BusinessException("Chưa cấu hình ghn.from-ward-code");
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) return DEFAULT_BASE_URL;
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record GhnProvince(Integer id, String name, List<String> extensions) {}
    private record GhnDistrict(Integer id, String name, List<String> extensions) {}
    private record GhnWard(String code, String name, List<String> extensions) {}
    private record GhnResolvedAddress(Integer districtId, String wardCode, DiaChiHaiCapResponse address) {}
    private record FeeResult(GhnResolvedAddress route, JsonNode data) {}
    private static final class GhnUnavailableException extends RuntimeException {
        private GhnUnavailableException(Throwable cause) {
            super(cause);
        }
    }
}
