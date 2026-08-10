package com.example.server.core.admin.quanlyhoadon.service;

import com.example.server.core.admin.quanlyhoadon.dto.responsse.TinhPhiVanChuyenGhnResponse;
import com.example.server.infrastructure.address.DiaChiHaiCapResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class GhnOfflineFeeService {

    public static final String SOURCE_LIVE = "GHN_LIVE";
    public static final String SOURCE_CACHE = "GHN_CACHE";
    public static final String SOURCE_PUBLIC_TARIFF = "GHN_PUBLIC_TARIFF";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Path cachePath;
    private final int cacheTtlDays;
    private final String fromProvinceCode;
    private final PublicTariff tariff;
    private final Map<String, CachedFee> cache = new LinkedHashMap<>();

    public GhnOfflineFeeService(
            @Value("${ghn.fee-cache-file:data/ghn-fee-cache.json}") String cacheFile,
            @Value("${ghn.fee-cache-ttl-days:30}") int cacheTtlDays,
            @Value("${ghn.from-province-code:1}") String fromProvinceCode
    ) {
        this.cachePath = Path.of(cacheFile).toAbsolutePath().normalize();
        this.cacheTtlDays = Math.max(1, cacheTtlDays);
        this.fromProvinceCode = normalizeCode(fromProvinceCode);
        this.tariff = readTariff();
        readCache();
    }

    public String buildKey(
            Integer fromDistrictId,
            String fromWardCode,
            String destinationProvinceCode,
            String destinationWardCode,
            FeeParameters parameters
    ) {
        String raw = String.join("|",
                tariff.version(),
                String.valueOf(fromDistrictId),
                safe(fromWardCode),
                normalizeCode(destinationProvinceCode),
                normalizeCode(destinationWardCode),
                String.valueOf(parameters.serviceId()),
                String.valueOf(parameters.serviceTypeId()),
                String.valueOf(parameters.length()),
                String.valueOf(parameters.width()),
                String.valueOf(parameters.height()),
                String.valueOf(parameters.weight()),
                String.valueOf(parameters.insuranceValue()),
                safe(parameters.coupon())
        );
        return sha256(raw);
    }

    public synchronized void saveLive(String key, TinhPhiVanChuyenGhnResponse response) {
        cache.put(key, new CachedFee(
                response.total(), response.serviceFee(), response.insuranceFee(),
                response.pickStationFee(), response.couponValue(), Instant.now()
        ));
        writeCache();
    }

    public synchronized Optional<TinhPhiVanChuyenGhnResponse> fromCache(
            String key,
            DiaChiHaiCapResponse address
    ) {
        CachedFee value = cache.get(key);
        if (value == null || value.total() == null) return Optional.empty();
        boolean stale = value.quotedAt() == null
                || Duration.between(value.quotedAt(), Instant.now()).toDays() > cacheTtlDays;
        return Optional.of(new TinhPhiVanChuyenGhnResponse(
                BigDecimal.valueOf(value.total()),
                value.total(), value.serviceFee(), value.insuranceFee(), value.pickStationFee(), value.couponValue(),
                address, true, SOURCE_CACHE, stale, value.quotedAt(), tariff.effectiveDate()
        ));
    }

    public TinhPhiVanChuyenGhnResponse fromPublicTariff(
            String destinationProvinceCode,
            DiaChiHaiCapResponse address,
            FeeParameters parameters
    ) {
        TariffBand band = normalizeCode(destinationProvinceCode).equals(fromProvinceCode)
                ? tariff.sameProvinceConservative()
                : tariff.differentProvinceConservative();
        int volumetricGrams = (int) Math.ceil(
                (parameters.length() * parameters.width() * parameters.height() * 1000.0)
                        / tariff.volumetricDivisor()
        );
        int chargeableWeight = Math.max(parameters.weight(), volumetricGrams);
        int increments = chargeableWeight <= band.baseWeightGrams()
                ? 0
                : (int) Math.ceil((chargeableWeight - band.baseWeightGrams()) / (double) band.incrementWeightGrams());
        int transportFee = band.baseFee() + increments * band.incrementFee();
        int fuelSurcharge = BigDecimal.valueOf(transportFee)
                .multiply(tariff.fuelSurchargeRate())
                .setScale(0, RoundingMode.CEILING)
                .intValue();
        int insuranceFee = parameters.insuranceValue() > tariff.insuranceFreeThreshold()
                ? BigDecimal.valueOf(parameters.insuranceValue())
                        .multiply(tariff.insuranceRate()).setScale(0, RoundingMode.CEILING).intValue()
                : 0;
        int serviceFee = transportFee + fuelSurcharge;
        int total = serviceFee + insuranceFee;
        return new TinhPhiVanChuyenGhnResponse(
                BigDecimal.valueOf(total), total, serviceFee, insuranceFee, 0, 0,
                address, true, SOURCE_PUBLIC_TARIFF, false, Instant.now(), tariff.effectiveDate()
        );
    }

    public LocalDate tariffEffectiveDate() {
        return tariff.effectiveDate();
    }

    private PublicTariff readTariff() {
        try {
            ClassPathResource resource = new ClassPathResource("shipping/ghn-public-tariff-2026-03-20.json");
            try (var input = resource.getInputStream()) {
                return OBJECT_MAPPER.readValue(input, PublicTariff.class);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Không đọc được snapshot bảng giá GHN", exception);
        }
    }

    private synchronized void readCache() {
        if (!Files.isRegularFile(cachePath)) return;
        try {
            CacheFile file = OBJECT_MAPPER.readValue(cachePath.toFile(), CacheFile.class);
            if (file.entries() != null) cache.putAll(file.entries());
        } catch (Exception ignored) {
            cache.clear();
        }
    }

    private synchronized void writeCache() {
        try {
            Path parent = cachePath.getParent();
            if (parent != null) Files.createDirectories(parent);
            Path temporary = cachePath.resolveSibling(cachePath.getFileName() + ".tmp");
            OBJECT_MAPPER.writeValue(temporary.toFile(), new CacheFile(1, cache));
            try {
                Files.move(temporary, cachePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, cachePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) {
            // Cache là lớp dự phòng; lỗi ghi đĩa không được làm hỏng báo giá live.
        }
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Không tạo được khóa cache phí GHN", exception);
        }
    }

    private String normalizeCode(String value) {
        String normalized = safe(value).replaceFirst("^0+(?!$)", "");
        return normalized.isBlank() ? "0" : normalized;
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public record FeeParameters(
            Integer serviceId,
            int serviceTypeId,
            int length,
            int width,
            int height,
            int weight,
            int insuranceValue,
            String coupon
    ) {}

    private record CacheFile(int schemaVersion, Map<String, CachedFee> entries) {}
    private record CachedFee(
            Integer total,
            Integer serviceFee,
            Integer insuranceFee,
            Integer pickStationFee,
            Integer couponValue,
            Instant quotedAt
    ) {}
    private record PublicTariff(
            String version,
            LocalDate effectiveDate,
            String sourceUrl,
            int volumetricDivisor,
            BigDecimal fuelSurchargeRate,
            int insuranceFreeThreshold,
            BigDecimal insuranceRate,
            TariffBand sameProvinceConservative,
            TariffBand differentProvinceConservative
    ) {}
    private record TariffBand(
            int baseWeightGrams,
            int baseFee,
            int incrementWeightGrams,
            int incrementFee
    ) {}
}
