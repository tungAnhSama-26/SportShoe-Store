package com.example.server.infrastructure.address;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Đối chiếu chuỗi địa chỉ hành chính cũ trên QR CCCD sang danh mục hai cấp hiện hành. */
@Service
public class LegacyAddressMappingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern ADMIN_PREFIX = Pattern.compile(
            "^(tinh|thanh pho|tp|t p|quan|q|huyen|h|thi xa|tx|phuong|p|xa|x|thi tran|tt)\\s+"
    );
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final String MANUAL_MESSAGE =
            "Không xác định chắc chắn được tỉnh/thành và phường/xã mới. Vui lòng chọn thủ công.";

    private final VietnamAddressCatalogService addressCatalogService;
    private final RestClient restClient;
    private final Map<String, List<LegacyMapping>> mappingsByLegacyAddress;
    private final Map<String, List<ResolvedUnit>> resolvedCache = new ConcurrentHashMap<>();

    public LegacyAddressMappingService(
            VietnamAddressCatalogService addressCatalogService,
            @Value("${address.catalog.base-url:https://provinces.open-api.vn/api}") String baseUrl
    ) {
        this.addressCatalogService = addressCatalogService;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(5_000);
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .requestFactory(requestFactory)
                .build();
        this.mappingsByLegacyAddress = readBundledMappings();
    }

    public DiaChiCuResponse doiChieu(String rawAddress) {
        ParsedAddress parsed = parse(rawAddress);
        if (parsed == null) {
            return manual(rawAddress == null ? "" : rawAddress.trim());
        }

        String key = legacyKey(parsed.province(), parsed.district(), parsed.ward());
        List<ResolvedUnit> matches = resolvedCache.computeIfAbsent(
                key,
                ignored -> resolveUnits(key, parsed.ward())
        );
        if (matches.size() != 1) {
            return manual(parsed.detail());
        }

        ResolvedUnit match = matches.get(0);
        return new DiaChiCuResponse(
                true,
                match.provinceCode(),
                match.provinceName(),
                match.wardCode(),
                match.wardName(),
                parsed.detail(),
                "Đã chuyển địa chỉ CCCD sang địa chỉ hành chính 2 cấp"
        );
    }

    ParsedAddress parse(String rawAddress) {
        if (rawAddress == null || rawAddress.isBlank()) return null;
        List<String> parts = Arrays.stream(rawAddress.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        if (parts.size() < 3) return null;

        int size = parts.size();
        String province = parts.get(size - 1);
        String district = parts.get(size - 2);
        String ward = parts.get(size - 3);
        String detail = size > 3
                ? String.join(", ", parts.subList(0, size - 3))
                : "";
        return new ParsedAddress(province, district, ward, detail);
    }

    private List<ResolvedUnit> resolveUnits(String key, String legacyWardName) {
        List<LegacyMapping> bundled = mappingsByLegacyAddress.getOrDefault(key, List.of());
        if (bundled.isEmpty()) return List.of();

        List<ResolvedUnit> offlineMatches = validateAndDeduplicate(bundled);
        if (!offlineMatches.isEmpty()) return offlineMatches;

        return validateAndDeduplicate(resolveOnline(legacyWardName, bundled));
    }

    private List<ResolvedUnit> validateAndDeduplicate(List<LegacyMapping> candidates) {
        Map<String, ResolvedUnit> unique = new LinkedHashMap<>();
        for (LegacyMapping candidate : candidates) {
            ResolvedUnit resolved = validateCurrentUnit(candidate);
            if (resolved != null) {
                unique.putIfAbsent(resolved.provinceCode() + "|" + resolved.wardCode(), resolved);
            }
        }
        return List.copyOf(unique.values());
    }

    private List<LegacyMapping> resolveOnline(String legacyWardName, List<LegacyMapping> bundled) {
        Set<String> allowedSourceCodes = new LinkedHashSet<>();
        bundled.forEach(value -> allowedSourceCodes.add(value.oldWardCode()));
        try {
            String response = restClient.get()
                    .uri(builder -> builder.path("/v2/w/from-legacy/")
                            .queryParam("legacy_name", legacyWardName)
                            .build())
                    .retrieve()
                    .body(String.class);
            JsonNode root = response == null ? null : OBJECT_MAPPER.readTree(response);
            if (root == null || !root.isArray()) return List.of();

            List<LegacyMapping> result = new ArrayList<>();
            for (JsonNode node : root) {
                String sourceCode = node.path("source_code").asText("").trim();
                if (!allowedSourceCodes.contains(sourceCode)) continue;
                JsonNode ward = node.path("ward");
                String wardCode = ward.path("code").asText("").trim();
                String provinceCode = ward.path("province_code").asText("").trim();
                if (!wardCode.isBlank() && !provinceCode.isBlank()) {
                    LegacyMapping source = bundled.stream()
                            .filter(value -> value.oldWardCode().equals(sourceCode))
                            .findFirst()
                            .orElse(null);
                    if (source != null) {
                        result.add(source.withCurrentCodes(wardCode, provinceCode));
                    }
                }
            }
            return result;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private ResolvedUnit validateCurrentUnit(LegacyMapping mapping) {
        var province = addressCatalogService.layDanhSachTinhThanh().stream()
                .filter(value -> value.code().equals(mapping.newProvinceCode()))
                .findFirst()
                .orElse(null);
        if (province == null) return null;
        var ward = addressCatalogService.layDanhSachPhuongXa(province.code()).stream()
                .filter(value -> value.code().equals(mapping.newWardCode()))
                .findFirst()
                .orElse(null);
        if (ward == null) return null;
        return new ResolvedUnit(province.code(), province.ten(), ward.code(), ward.ten());
    }

    private Map<String, List<LegacyMapping>> readBundledMappings() {
        ClassPathResource resource = new ClassPathResource("address/vietnam-address-legacy-map.json");
        if (!resource.exists()) return Map.of();
        Map<String, List<LegacyMapping>> index = new LinkedHashMap<>();
        try (InputStream input = resource.getInputStream()) {
            JsonNode root = OBJECT_MAPPER.readTree(input).path("mappings");
            if (!root.isArray()) return Map.of();
            for (JsonNode node : root) {
                LegacyMapping value = new LegacyMapping(
                        node.path("oldWardCode").asText("").trim(),
                        node.path("oldWardName").asText("").trim(),
                        node.path("oldDistrictName").asText("").trim(),
                        node.path("oldProvinceName").asText("").trim(),
                        node.path("newWardCode").asText("").trim(),
                        node.path("newProvinceCode").asText("").trim()
                );
                if (value.isValid()) {
                    String key = legacyKey(value.oldProvinceName(), value.oldDistrictName(), value.oldWardName());
                    index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
                }
            }
        } catch (Exception ignored) {
            return Map.of();
        }
        Map<String, List<LegacyMapping>> immutable = new LinkedHashMap<>();
        index.forEach((key, value) -> immutable.put(key, List.copyOf(value)));
        return Map.copyOf(immutable);
    }

    private DiaChiCuResponse manual(String detail) {
        return new DiaChiCuResponse(false, "", "", "", "", detail, MANUAL_MESSAGE);
    }

    private String legacyKey(String province, String district, String ward) {
        return normalizeName(province) + "|" + normalizeName(district) + "|" + normalizeName(ward);
    }

    private String normalizeName(String value) {
        if (value == null) return "";
        String ascii = value.replace('Đ', 'D').replace('đ', 'd');
        String normalized = Normalizer.normalize(ascii, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll(" ");
        normalized = WHITESPACE.matcher(normalized).replaceAll(" ").trim();
        String withoutPrefix = ADMIN_PREFIX.matcher(normalized).replaceFirst("").trim();
        return ADMIN_PREFIX.matcher(withoutPrefix).replaceFirst("").trim();
    }

    private String trimTrailingSlash(String value) {
        String fallback = "https://provinces.open-api.vn/api";
        if (value == null || value.isBlank()) return fallback;
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    record ParsedAddress(String province, String district, String ward, String detail) {}

    private record ResolvedUnit(
            String provinceCode,
            String provinceName,
            String wardCode,
            String wardName
    ) {}

    private record LegacyMapping(
            String oldWardCode,
            String oldWardName,
            String oldDistrictName,
            String oldProvinceName,
            String newWardCode,
            String newProvinceCode
    ) {
        boolean isValid() {
            return !oldWardCode.isBlank() && !oldWardName.isBlank() && !oldDistrictName.isBlank()
                    && !oldProvinceName.isBlank() && !newWardCode.isBlank() && !newProvinceCode.isBlank();
        }

        LegacyMapping withCurrentCodes(String wardCode, String provinceCode) {
            return new LegacyMapping(
                    oldWardCode,
                    oldWardName,
                    oldDistrictName,
                    oldProvinceName,
                    wardCode,
                    provinceCode
            );
        }
    }
}
