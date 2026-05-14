package com.example.server.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secretBytes;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret:sport-shoe-local-dev-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(AdminPrincipal principal) {
        try {
            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", principal.id().toString());
            claims.put("ma", principal.ma());
            claims.put("tenDangNhap", principal.tenDangNhap());
            claims.put("hoTen", principal.hoTen());
            claims.put("vaiTro", principal.vaiTro());
            claims.put("role", principal.role());
            claims.put("iat", Instant.now().getEpochSecond());
            claims.put("exp", Instant.now().plusSeconds(expirationSeconds).getEpochSecond());

            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(claims);
            String signaturePart = sign(headerPart + "." + payloadPart);
            return headerPart + "." + payloadPart + "." + signaturePart;
        } catch (Exception exception) {
            throw new IllegalStateException("Khong the tao JWT", exception);
        }
    }

    public AdminPrincipal parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT khong dung dinh dang");
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new IllegalArgumentException("JWT khong hop le");
            }

            Map<String, Object> claims = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    }
            );

            long exp = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("JWT da het han");
            }

            return new AdminPrincipal(
                    UUID.fromString(String.valueOf(claims.get("sub"))),
                    String.valueOf(claims.get("ma")),
                    String.valueOf(claims.get("tenDangNhap")),
                    String.valueOf(claims.get("hoTen")),
                    ((Number) claims.get("vaiTro")).intValue(),
                    String.valueOf(claims.get("role"))
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT khong hop le", exception);
        }
    }

    private String encodeJson(Object value) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secretBytes, HMAC_ALGORITHM));
        return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }
}
