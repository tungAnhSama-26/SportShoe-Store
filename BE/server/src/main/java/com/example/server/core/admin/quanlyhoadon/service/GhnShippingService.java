package com.example.server.core.admin.quanlyhoadon.service;

import com.example.server.core.admin.quanlyhoadon.dto.request.TinhPhiVanChuyenGhnRequest;
import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.entity.Giay;
import com.example.server.entity.HoaDon;
import com.example.server.entity.HoaDonChiTiet;
import com.example.server.infrastructure.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

@Service
public class GhnShippingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_BASE_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api";
    private static final String TOKEN_HEADER = "Token";
    private static final String SHOP_ID_HEADER = "ShopId";
    private static final String PROVINCE_ENDPOINT = "/master-data/province";
    private static final String DISTRICT_ENDPOINT = "/master-data/district";
    private static final String WARD_ENDPOINT = "/master-data/ward";
    private static final String SHIPPING_FEE_ENDPOINT = "/v2/shipping-order/fee";
    private static final int MAX_INSURANCE_VALUE = 5_000_000;
    private static final int MIN_ADDRESS_NAME_MATCH_LENGTH = 5;
    private static final int TEST_PROVINCE_PENALTY = 1000;
    private static final Pattern NON_ADDRESS_CHARS = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern ADDRESS_PREFIXES = Pattern.compile("\\b(thanh pho|tp|tinh|quan|huyen|thi xa|phuong|thi tran|p|q|h)\\b");
    private static final Pattern LEADING_ADMIN_PREFIX = Pattern.compile("^(xa|phuong|thi tran)\\s+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern NUMERIC_NAME = Pattern.compile("\\d+");

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
    private final Map<Integer, List<GhnDistrict>> districtCache = new ConcurrentHashMap<>();
    private final Map<Integer, List<GhnWard>> wardCache = new ConcurrentHashMap<>();
    private volatile List<GhnProvince> provinceCache;

    public GhnShippingService(
            @Value("${ghn.base-url:https://dev-online-gateway.ghn.vn/shiip/public-api}") String baseUrl,
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
        if (token == null || token.isBlank()) {
            return new TinhPhiVanChuyenGhnResponse(
                    BigDecimal.valueOf(30000),
                    30000,
                    30000,
                    0,
                    0,
                    0,
                    request.toDistrictId() != null ? request.toDistrictId() : 0,
                    request.toWardCode() != null ? request.toWardCode() : "",
                    "Mock Province",
                    "Mock District",
                    "Mock Ward"
            );
        }

        validateConfig();
        GhnResolvedAddress resolvedAddress = resolveAddress(request);
        JsonNode data = callShippingFeeApi(hoaDon, items, request, resolvedAddress).path("data");
        Integer total = readInt(data, "total");

        return new TinhPhiVanChuyenGhnResponse(
                BigDecimal.valueOf(total != null ? total : 0),
                total,
                readInt(data, "service_fee"),
                readInt(data, "insurance_fee"),
                readInt(data, "pick_station_fee"),
                readInt(data, "coupon_value"),
                resolvedAddress.districtId(),
                resolvedAddress.wardCode(),
                resolvedAddress.provinceName(),
                resolvedAddress.districtName(),
                resolvedAddress.wardName()
        );
    }

    // ===== API địa giới GHN cho dropdown (tỉnh -> huyện -> xã) =====

    /** Một đơn vị địa giới GHN có mã số (tỉnh, huyện). */
    public record GhnDiaGioi(Integer id, String ten) {
    }

    /** Phường/xã GHN dùng mã chuỗi (WardCode). */
    public record GhnXa(String code, String ten) {
    }

    public List<GhnDiaGioi> layDanhSachTinh() {
        return getProvinces().stream()
                .map(province -> new GhnDiaGioi(province.id(), province.name()))
                .toList();
    }

    public List<GhnDiaGioi> layDanhSachHuyen(Integer tinhId) {
        if (tinhId == null) {
            throw new BusinessException("Thiếu mã tỉnh/thành GHN");
        }
        return getDistricts(tinhId).stream()
                .map(district -> new GhnDiaGioi(district.id(), district.name()))
                .toList();
    }

    public List<GhnXa> layDanhSachXa(Integer huyenId) {
        if (huyenId == null) {
            throw new BusinessException("Thiếu mã quận/huyện GHN");
        }
        return getWards(huyenId).stream()
                .map(ward -> new GhnXa(ward.code(), ward.name()))
                .toList();
    }

    private JsonNode callShippingFeeApi(
            HoaDon hoaDon,
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request,
            GhnResolvedAddress resolvedAddress
    ) {
        try {
            String responseBody = restClient.post()
                    .uri(SHIPPING_FEE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(TOKEN_HEADER, token)
                    .header(SHOP_ID_HEADER, String.valueOf(shopId))
                    .body(buildShippingFeeRequest(hoaDon, items, request, resolvedAddress))
                    .retrieve()
                    .body(String.class);
            return readSuccessfulResponse(responseBody, "GHN tinh phi that bai");
        } catch (RestClientResponseException exception) {
            throw new BusinessException(readGhnErrorMessage(exception.getResponseBodyAsString(), "GHN tính phí thất bại"));
        } catch (RestClientException exception) {
            throw new BusinessException("Không kết nối được GHN: " + exception.getMessage());
        }
    }

    private Map<String, Object> buildShippingFeeRequest(
            HoaDon hoaDon,
            List<HoaDonChiTiet> items,
            TinhPhiVanChuyenGhnRequest request,
            GhnResolvedAddress resolvedAddress
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_district_id", fromDistrictId);
        body.put("from_ward_code", fromWardCode);
        body.put("to_district_id", resolvedAddress.districtId());
        body.put("to_ward_code", resolvedAddress.wardCode());

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
        return body;
    }

    private GhnResolvedAddress resolveAddress(TinhPhiVanChuyenGhnRequest request) {
        if (hasExplicitGhnAddress(request)) {
            return new GhnResolvedAddress(request.toDistrictId(), request.toWardCode().trim(), null, null, null);
        }

        String rawAddress = request.toAddress() != null ? request.toAddress().trim() : "";
        if (rawAddress.isBlank()) {
            throw new BusinessException("Cần nhập địa chỉ người nhận hoặc mã quận/huyện + phường/xã GHN");
        }

        String normalizedAddress = normalizeAddress(rawAddress);
        GhnProvince province = findBestProvince(normalizedAddress)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tỉnh/thành GHN từ địa chỉ: " + rawAddress));

        GhnResolvedAddress resolvedAddress = resolveAddressInProvince(normalizedAddress, province);
        if (resolvedAddress == null) {
            throw new BusinessException("Cần nhập đầy đủ tỉnh/thành, quận/huyện và phường/xã theo dữ liệu GHN: " + rawAddress);
        }
        return resolvedAddress;
    }

    private boolean hasExplicitGhnAddress(TinhPhiVanChuyenGhnRequest request) {
        return request.toDistrictId() != null
                && request.toDistrictId() > 0
                && request.toWardCode() != null
                && !request.toWardCode().isBlank();
    }

    private GhnResolvedAddress resolveAddressInProvince(String normalizedAddress, GhnProvince province) {
        List<GhnDistrict> districts = getDistricts(province.id());
        GhnDistrict matchedDistrict = findBestDistrict(normalizedAddress, districts).orElse(null);
        if (matchedDistrict == null) {
            return null;
        }

        GhnWard matchedWard = findBestWard(normalizedAddress, getWards(matchedDistrict.id())).orElse(null);
        if (matchedWard == null) {
            return null;
        }
        return toResolvedAddress(province, matchedDistrict, matchedWard);
    }

    private GhnResolvedAddress toResolvedAddress(GhnProvince province, GhnDistrict district, GhnWard ward) {
        return new GhnResolvedAddress(district.id(), ward.code(), province.name(), district.name(), ward.name());
    }

    private java.util.Optional<GhnProvince> findBestProvince(String normalizedAddress) {
        return getProvinces().stream()
                .filter(province -> containsAnyName(normalizedAddress, province.name(), province.extensions()))
                .max(Comparator.comparingInt(province -> provinceMatchScore(normalizedAddress, province)));
    }

    private int provinceMatchScore(String normalizedAddress, GhnProvince province) {
        int score = bestNameLength(normalizedAddress, province.name(), province.extensions());
        String normalizedName = normalizeAddress(province.name());
        if (normalizedName.contains("test") || DIGIT.matcher(normalizedName).find()) {
            score -= TEST_PROVINCE_PENALTY;
        }
        return score;
    }

    private java.util.Optional<GhnDistrict> findBestDistrict(String normalizedAddress, List<GhnDistrict> districts) {
        return districts.stream()
                .filter(district -> containsAnyName(normalizedAddress, district.name(), district.extensions()))
                .max(Comparator.comparingInt(district -> bestNameLength(normalizedAddress, district.name(), district.extensions())));
    }

    private java.util.Optional<GhnWard> findBestWard(String normalizedAddress, List<GhnWard> wards) {
        return wards.stream()
                .filter(ward -> containsAnyName(normalizedAddress, ward.name(), ward.extensions()))
                .max(Comparator.comparingInt(ward -> bestNameLength(normalizedAddress, ward.name(), ward.extensions())));
    }

    private List<GhnProvince> getProvinces() {
        if (provinceCache != null) {
            return provinceCache;
        }
        JsonNode response = getJson(PROVINCE_ENDPOINT, "Không lấy được danh sách tỉnh/thành GHN");
        provinceCache = readArray(response.path("data")).stream()
                .map(node -> new GhnProvince(
                        node.path("ProvinceID").asInt(),
                        node.path("ProvinceName").asText(),
                        readNameExtensions(node)
                ))
                .toList();
        return provinceCache;
    }

    private List<GhnDistrict> getDistricts(Integer provinceId) {
        return districtCache.computeIfAbsent(provinceId, id -> {
            Map<String, Object> body = new HashMap<>();
            body.put("province_id", id);
            JsonNode response = postJson(DISTRICT_ENDPOINT, body, "Không lấy được danh sách quận/huyện GHN");
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
                    .map(node -> new GhnWard(
                            node.path("WardCode").asText(),
                            node.path("WardName").asText(),
                            readNameExtensions(node)
                    ))
                    .toList();
        });
    }

    private JsonNode getJson(String path, String fallbackMessage) {
        return getJson(uriBuilder -> uriBuilder.path(path).build(), fallbackMessage);
    }

    private JsonNode getJson(Function<UriBuilder, URI> uriFunction, String fallbackMessage) {
        try {
            String responseBody = restClient.get()
                    .uri(uriFunction)
                    .header(TOKEN_HEADER, token)
                    .retrieve()
                    .body(String.class);
            return readSuccessfulResponse(responseBody, fallbackMessage);
        } catch (RestClientResponseException exception) {
            throw new BusinessException(readGhnErrorMessage(exception.getResponseBodyAsString(), fallbackMessage));
        } catch (RestClientException exception) {
            throw new BusinessException(fallbackMessage + ": " + exception.getMessage());
        }
    }

    private JsonNode postJson(String path, Object body, String fallbackMessage) {
        try {
            String responseBody = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(TOKEN_HEADER, token)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return readSuccessfulResponse(responseBody, fallbackMessage);
        } catch (RestClientResponseException exception) {
            throw new BusinessException(readGhnErrorMessage(exception.getResponseBodyAsString(), fallbackMessage));
        } catch (RestClientException exception) {
            throw new BusinessException(fallbackMessage + ": " + exception.getMessage());
        }
    }

    private JsonNode readSuccessfulResponse(String responseBody, String fallbackMessage) {
        JsonNode response = readJson(responseBody, fallbackMessage);
        if (response.path("code").asInt(-1) != 200) {
            throw new BusinessException(response.path("message").asText(fallbackMessage));
        }
        return response;
    }

    private JsonNode readJson(String responseBody, String fallbackMessage) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException(fallbackMessage);
        }
        try {
            return OBJECT_MAPPER.readTree(responseBody);
        } catch (Exception exception) {
            throw new BusinessException(fallbackMessage + ": GHN trả về dữ liệu không phải JSON");
        }
    }

    private String readGhnErrorMessage(String responseBody, String fallbackMessage) {
        if (responseBody == null || responseBody.isBlank()) {
            return fallbackMessage;
        }
        try {
            String message = OBJECT_MAPPER.readTree(responseBody).path("message").asText("");
            return message.isBlank() ? fallbackMessage : message;
        } catch (Exception ignored) {
            return responseBody.length() > 250 ? responseBody.substring(0, 250) : responseBody;
        }
    }

    private boolean containsAnyName(String normalizedAddress, String name, List<String> extensions) {
        return addressNameVariants(name, extensions).stream()
                .anyMatch(variant -> containsNormalizedName(normalizedAddress, variant));
    }

    private int bestNameLength(String normalizedAddress, String name, List<String> extensions) {
        return addressNameVariants(name, extensions).stream()
                .filter(variant -> containsNormalizedName(normalizedAddress, variant))
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    private List<String> addressNameVariants(String name, List<String> extensions) {
        List<String> names = new ArrayList<>();
        names.add(name);
        names.addAll(extensions);

        return names.stream()
                .filter(Objects::nonNull)
                .flatMap(value -> normalizedNameVariants(value).stream())
                .distinct()
                .toList();
    }

    private List<String> normalizedNameVariants(String value) {
        String normalized = normalizeAddress(value);
        if (normalized.isBlank()) {
            return List.of();
        }

        List<String> variants = new ArrayList<>();
        variants.add(normalized);
        String withoutLeadingAdminPrefix = LEADING_ADMIN_PREFIX.matcher(normalized).replaceFirst("").trim();
        if (!withoutLeadingAdminPrefix.isBlank()) {
            variants.add(withoutLeadingAdminPrefix);
        }

        String withoutTrailingNumber = normalized.replaceAll("\\s+\\d+$", "").trim();
        if (!withoutTrailingNumber.isBlank()) {
            variants.add(withoutTrailingNumber);
            String compactTrailingVariant = LEADING_ADMIN_PREFIX.matcher(withoutTrailingNumber).replaceFirst("").trim();
            if (!compactTrailingVariant.isBlank()) {
                variants.add(compactTrailingVariant);
            }
        }
        return keepStrongVariants(variants);
    }

    private List<String> keepStrongVariants(List<String> variants) {
        return variants.stream()
                .distinct()
                .filter(value -> value.length() >= MIN_ADDRESS_NAME_MATCH_LENGTH || NUMERIC_NAME.matcher(value).matches())
                .toList();
    }

    private boolean containsNormalizedName(String normalizedAddress, String normalizedName) {
        return Pattern.compile("(^|\\s)" + Pattern.quote(normalizedName) + "(\\s|$)")
                .matcher(normalizedAddress)
                .find();
    }

    private List<JsonNode> readArray(JsonNode node) {
        List<JsonNode> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(result::add);
        }
        return result;
    }

    private List<String> readNameExtensions(JsonNode node) {
        JsonNode extensions = node.path("NameExtension");
        List<String> result = new ArrayList<>();
        if (extensions.isArray()) {
            extensions.forEach(value -> result.add(value.asText()));
        }
        return result;
    }

    private String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String ascii = value
                .replace('\u0110', 'D')
                .replace('\u0111', 'd');
        String normalized = Normalizer.normalize(ascii, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        normalized = NON_ADDRESS_CHARS.matcher(normalized).replaceAll(" ");
        normalized = ADDRESS_PREFIXES.matcher(normalized).replaceAll(" ");
        return WHITESPACE.matcher(normalized).replaceAll(" ").trim();
    }

    private Integer layCanNangSanPham(HoaDonChiTiet item) {
        if (item.getGiayChiTiet() != null && item.getGiayChiTiet().getGiay() != null) {
            Giay giay = item.getGiayChiTiet().getGiay();
            if (giay.getGiayThuocTinh() != null 
                    && giay.getGiayThuocTinh().getTrongLuong() != null 
                    && giay.getGiayThuocTinh().getTrongLuong().getGiaTri() != null) {
                return giay.getGiayThuocTinh().getTrongLuong().getGiaTri();
            }
        }
        return defaultWeight;
    }

    private Map<String, Object> mapItem(HoaDonChiTiet item) {
        Map<String, Object> mapped = new LinkedHashMap<>();
        mapped.put("name", item.getGiayChiTiet().getGiay().getTen());
        mapped.put("quantity", item.getSoLuong());
        mapped.put("length", defaultLength);
        mapped.put("width", defaultWidth);
        mapped.put("height", defaultHeight);
        mapped.put("weight", layCanNangSanPham(item));
        return mapped;
    }

    private Integer tinhCanNangMacDinh(List<HoaDonChiTiet> items) {
        int tongCanNang = 0;
        for (HoaDonChiTiet item : items) {
            int quantity = item.getSoLuong() != null ? item.getSoLuong() : 0;
            if (quantity <= 0) continue;
            tongCanNang += layCanNangSanPham(item) * quantity;
        }
        return tongCanNang > 0 ? tongCanNang : defaultWeight;
    }

    private Integer valueOrDefault(Integer value, Integer defaultValue) {
        return value != null && value > 0 ? value : defaultValue;
    }

    private Integer safeInsuranceValue(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return value.min(BigDecimal.valueOf(MAX_INSURANCE_VALUE)).max(BigDecimal.ZERO).intValue();
    }

    private Integer readInt(JsonNode node, String field) {
        return node.has(field) && !node.path(field).isNull() ? node.path(field).asInt() : null;
    }

    private void validateConfig() {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Chưa cấu hình ghn.token");
        }
        if (shopId == null || shopId <= 0) {
            throw new BusinessException("Chưa cấu hình ghn.shop-id");
        }
        if (fromDistrictId == null || fromDistrictId <= 0) {
            throw new BusinessException("Chưa cấu hình ghn.from-district-id");
        }
        if (fromWardCode == null || fromWardCode.isBlank()) {
            throw new BusinessException("Chưa cấu hình ghn.from-ward-code");
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record GhnProvince(Integer id, String name, List<String> extensions) {
    }

    private record GhnDistrict(Integer id, String name, List<String> extensions) {
    }

    private record GhnWard(String code, String name, List<String> extensions) {
    }

    private record GhnResolvedAddress(
            Integer districtId,
            String wardCode,
            String provinceName,
            String districtName,
            String wardName
    ) {
    }
}
