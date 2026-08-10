package com.example.server.infrastructure.address;

import com.example.server.infrastructure.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Danh mục hành chính Việt Nam hai cấp sau 01/07/2025.
 *
 * <p>Dữ liệu trực tuyến lấy từ Province Open API v2. Một snapshot thành công gần nhất
 * được giữ trên đĩa để dropdown địa chỉ vẫn hoạt động khi nguồn bên ngoài tạm lỗi.</p>
 */
@Service
public class VietnamAddressCatalogService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_BASE_URL = "https://provinces.open-api.vn/api";
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern ADMIN_PREFIX = Pattern.compile(
            "^(tinh|thanh pho|tp|phuong|xa|thi tran|dac khu|quan|huyen|thi xa)\\s+"
    );
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final RestClient restClient;
    private final Path snapshotPath;
    private final Map<String, List<DonViHanhChinhCu>> legacyWardCache = new ConcurrentHashMap<>();
    private final Map<String, DonViCu> legacyDistrictCache = new ConcurrentHashMap<>();
    private final Map<String, DonViCu> legacyProvinceCache = new ConcurrentHashMap<>();
    private volatile CatalogSnapshot snapshot;

    public VietnamAddressCatalogService(
            @Value("${address.catalog.base-url:https://provinces.open-api.vn/api}") String baseUrl,
            @Value("${address.catalog.cache-file:${java.io.tmpdir}/sportshoe/vietnam-address-v2.json}") String cacheFile
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(5_000);
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(baseUrl))
                .requestFactory(requestFactory)
                .build();
        this.snapshotPath = Path.of(cacheFile).toAbsolutePath().normalize();
    }

    public record TinhThanh(Integer id, String code, String ten, String loai) {}
    public record PhuongXa(String code, String ten, String loai) {}
    public record DiaChiDaDoiSoat(TinhThanh tinhThanh, PhuongXa phuongXa, String diaChiCuThe) {}
    public record DonViHanhChinhCu(
            String code,
            String ten,
            String loai,
            String districtCode,
            String provinceCode
    ) {}
    public record DonViCu(String code, String ten, String loai) {}

    public List<TinhThanh> layDanhSachTinhThanh() {
        return getSnapshot().provinces();
    }

    public List<PhuongXa> layDanhSachPhuongXa(String tinhThanhCode) {
        String code = requireCode(tinhThanhCode, "Thiếu mã tỉnh/thành");
        CatalogSnapshot current = getSnapshot();
        if (!current.provincesByCode().containsKey(code)) {
            throw new BusinessException("Mã tỉnh/thành không hợp lệ");
        }
        return current.wardsByProvince().getOrDefault(code, List.of());
    }

    public DiaChiDaDoiSoat doiSoat(DiaChiHaiCapRequest input) {
        if (input == null) {
            throw new BusinessException("Thiếu địa chỉ giao hàng 2 cấp");
        }
        CatalogSnapshot current = getSnapshot();
        TinhThanh province = resolveProvince(current, input);
        PhuongXa ward = resolveWard(current, province, input);
        String detail = input.diaChiCuThe() == null ? "" : input.diaChiCuThe().trim();
        if (detail.isBlank()) {
            throw new BusinessException("Địa chỉ cụ thể không được để trống");
        }
        return new DiaChiDaDoiSoat(province, ward, detail);
    }

    public List<DonViHanhChinhCu> layDonViHanhChinhCu(String phuongXaCode) {
        String code = requireCode(phuongXaCode, "Thiếu mã phường/xã");
        return legacyWardCache.computeIfAbsent(code, this::fetchLegacyWards);
    }

    public DonViCu layTinhThanhCu(String code) {
        return legacyProvinceCache.computeIfAbsent(
                requireCode(code, "Thiếu mã tỉnh/thành cũ"),
                value -> fetchLegacyDivision("/v1/p/" + value, "Không lấy được tỉnh/thành cũ")
        );
    }

    public DonViCu layQuanHuyenCu(String code) {
        return legacyDistrictCache.computeIfAbsent(
                requireCode(code, "Thiếu mã quận/huyện cũ"),
                value -> fetchLegacyDivision("/v1/d/" + value, "Không lấy được quận/huyện cũ")
        );
    }

    private CatalogSnapshot getSnapshot() {
        CatalogSnapshot current = snapshot;
        if (current != null) return current;
        synchronized (this) {
            if (snapshot != null) return snapshot;
            try {
                snapshot = fetchOnlineSnapshot();
                saveSnapshot(snapshot);
                return snapshot;
            } catch (RuntimeException onlineError) {
                CatalogSnapshot disk = readDiskSnapshot();
                if (disk != null) {
                    snapshot = disk;
                    return snapshot;
                }
                CatalogSnapshot bundled = readBundledSnapshot();
                if (bundled != null) {
                    snapshot = bundled;
                    return snapshot;
                }
                throw new BusinessException(
                        "Không tải được danh mục địa chỉ Việt Nam và chưa có dữ liệu dự phòng"
                );
            }
        }
    }

    private CatalogSnapshot fetchOnlineSnapshot() {
        JsonNode root = getJson("/v2/?depth=2", "Không tải được danh mục địa chỉ Việt Nam");
        return parseSnapshot(root);
    }

    private CatalogSnapshot parseSnapshot(JsonNode root) {
        if (root == null || !root.isArray()) {
            throw new BusinessException("Danh mục địa chỉ Việt Nam trả dữ liệu không hợp lệ");
        }
        List<TinhThanh> provinces = new ArrayList<>();
        Map<String, List<PhuongXa>> wardsByProvince = new LinkedHashMap<>();
        for (JsonNode provinceNode : root) {
            String code = provinceNode.path("code").asText("").trim();
            if (code.isBlank()) continue;
            TinhThanh province = new TinhThanh(
                    parseInteger(code),
                    code,
                    provinceNode.path("name").asText("").trim(),
                    provinceNode.path("division_type").asText("").trim()
            );
            provinces.add(province);
            Map<String, PhuongXa> uniqueWards = new LinkedHashMap<>();
            JsonNode wardsNode = provinceNode.path("wards");
            if (wardsNode.isArray()) {
                for (JsonNode wardNode : wardsNode) {
                    String wardCode = wardNode.path("code").asText("").trim();
                    if (wardCode.isBlank()) continue;
                    uniqueWards.putIfAbsent(wardCode, new PhuongXa(
                            wardCode,
                            wardNode.path("name").asText("").trim(),
                            wardNode.path("division_type").asText("").trim()
                    ));
                }
            }
            wardsByProvince.put(code, sortWards(uniqueWards.values().stream().toList()));
        }
        provinces.sort(Comparator.comparing(TinhThanh::ten, String.CASE_INSENSITIVE_ORDER));
        int totalWards = wardsByProvince.values().stream().mapToInt(List::size).sum();
        if (provinces.size() != 34 || totalWards != 3321) {
            throw new BusinessException(
                    "Danh mục địa chỉ Việt Nam không đủ 34 tỉnh/thành và 3.321 đơn vị cấp xã"
            );
        }
        return createSnapshot(provinces, wardsByProvince);
    }

    private CatalogSnapshot createSnapshot(
            List<TinhThanh> provinces,
            Map<String, List<PhuongXa>> wardsByProvince
    ) {
        Map<String, TinhThanh> provincesByCode = new LinkedHashMap<>();
        provinces.forEach(value -> provincesByCode.put(value.code(), value));
        return new CatalogSnapshot(
                List.copyOf(provinces),
                Map.copyOf(provincesByCode),
                Map.copyOf(wardsByProvince)
        );
    }

    private void saveSnapshot(CatalogSnapshot value) {
        try {
            Path parent = snapshotPath.getParent();
            if (parent != null) Files.createDirectories(parent);
            Path temporary = snapshotPath.resolveSibling(snapshotPath.getFileName() + ".tmp");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("provinces", value.provinces());
            payload.put("wardsByProvince", value.wardsByProvince());
            OBJECT_MAPPER.writeValue(temporary.toFile(), payload);
            try {
                Files.move(temporary, snapshotPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, snapshotPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
            // Cache đĩa là dự phòng; lỗi ghi cache không làm hỏng request đang có dữ liệu online.
        }
    }

    private CatalogSnapshot readDiskSnapshot() {
        if (!Files.isRegularFile(snapshotPath)) return null;
        try {
            JsonNode root = OBJECT_MAPPER.readTree(snapshotPath.toFile());
            List<TinhThanh> provinces = new ArrayList<>();
            root.path("provinces").forEach(node -> provinces.add(new TinhThanh(
                    node.path("id").isNull() ? null : node.path("id").asInt(),
                    node.path("code").asText(),
                    node.path("ten").asText(),
                    node.path("loai").asText()
            )));
            Map<String, List<PhuongXa>> wardsByProvince = new LinkedHashMap<>();
            root.path("wardsByProvince").fields().forEachRemaining(entry -> {
                List<PhuongXa> wards = new ArrayList<>();
                entry.getValue().forEach(node -> wards.add(new PhuongXa(
                        node.path("code").asText(),
                        node.path("ten").asText(),
                        node.path("loai").asText()
                )));
                wardsByProvince.put(entry.getKey(), List.copyOf(wards));
            });
            int totalWards = wardsByProvince.values().stream().mapToInt(List::size).sum();
            if (provinces.size() != 34 || totalWards != 3321) return null;
            return createSnapshot(provinces, wardsByProvince);
        } catch (Exception ignored) {
            return null;
        }
    }

    private CatalogSnapshot readBundledSnapshot() {
        try {
            ClassPathResource resource = new ClassPathResource("address/vietnam-address-v2.json");
            if (!resource.exists()) return null;
            try (var input = resource.getInputStream()) {
                return parseSnapshot(OBJECT_MAPPER.readTree(input));
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<DonViHanhChinhCu> fetchLegacyWards(String wardCode) {
        JsonNode root = getJson(
                "/v2/w/" + wardCode + "/to-legacies/",
                "Không lấy được dữ liệu ánh xạ địa chỉ cũ"
        );
        if (!root.isArray()) return List.of();
        List<DonViHanhChinhCu> result = new ArrayList<>();
        root.forEach(node -> result.add(new DonViHanhChinhCu(
                node.path("code").asText("").trim(),
                node.path("name").asText("").trim(),
                node.path("division_type").asText("").trim(),
                node.path("district_code").asText("").trim(),
                node.path("province_code").asText("").trim()
        )));
        return result.stream()
                .filter(value -> !value.code().isBlank()
                        && !value.districtCode().isBlank()
                        && !value.provinceCode().isBlank())
                .toList();
    }

    private DonViCu fetchLegacyDivision(String path, String message) {
        JsonNode node = getJson(path, message);
        String code = node.path("code").asText("").trim();
        String name = node.path("name").asText("").trim();
        if (code.isBlank() || name.isBlank()) throw new BusinessException(message);
        return new DonViCu(code, name, node.path("division_type").asText("").trim());
    }

    private JsonNode getJson(String path, String fallback) {
        try {
            String response = restClient.get().uri(path).retrieve().body(String.class);
            if (response == null || response.isBlank()) throw new BusinessException(fallback);
            return OBJECT_MAPPER.readTree(response);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(fallback);
        } catch (Exception exception) {
            throw new BusinessException(fallback + ": dữ liệu không hợp lệ");
        }
    }

    private TinhThanh resolveProvince(CatalogSnapshot current, DiaChiHaiCapRequest input) {
        String code = input.tinhThanhCode() == null ? "" : input.tinhThanhCode().trim();
        if (!code.isBlank()) {
            TinhThanh match = current.provincesByCode().get(code);
            if (match == null) throw new BusinessException("Mã tỉnh/thành không hợp lệ");
            return match;
        }
        String normalizedName = normalizeName(input.tinhThanh());
        List<TinhThanh> matches = current.provinces().stream()
                .filter(value -> normalizeName(value.ten()).equals(normalizedName))
                .toList();
        if (matches.size() != 1) throw new BusinessException("Không xác định được tỉnh/thành hiện hành");
        return matches.get(0);
    }

    private PhuongXa resolveWard(
            CatalogSnapshot current,
            TinhThanh province,
            DiaChiHaiCapRequest input
    ) {
        List<PhuongXa> wards = current.wardsByProvince().getOrDefault(province.code(), List.of());
        String code = input.phuongXaCode() == null ? "" : input.phuongXaCode().trim();
        if (!code.isBlank()) {
            return wards.stream().filter(value -> value.code().equals(code)).findFirst()
                    .orElseThrow(() -> new BusinessException("Phường/xã không thuộc tỉnh/thành đã chọn"));
        }
        String normalizedName = normalizeName(input.phuongXa());
        List<PhuongXa> matches = wards.stream()
                .filter(value -> normalizeName(value.ten()).equals(normalizedName))
                .toList();
        if (matches.size() != 1) throw new BusinessException("Không xác định được phường/xã hiện hành");
        return matches.get(0);
    }

    private List<PhuongXa> sortWards(List<PhuongXa> wards) {
        return wards.stream()
                .sorted(Comparator.comparing(PhuongXa::ten, String.CASE_INSENSITIVE_ORDER))
                .toList();
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

    private String requireCode(String value, String message) {
        if (value == null || value.isBlank()) throw new BusinessException(message);
        return value.trim();
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) return DEFAULT_BASE_URL;
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record CatalogSnapshot(
            List<TinhThanh> provinces,
            Map<String, TinhThanh> provincesByCode,
            Map<String, List<PhuongXa>> wardsByProvince
    ) {}
}
