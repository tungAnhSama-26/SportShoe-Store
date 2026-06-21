package org.springframework.ai.openai.metadata.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.openai.metadata.OpenAiRateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.time.Duration;
import java.util.List;

public class OpenAiResponseHeaderExtractor {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiResponseHeaderExtractor.class);

    public static RateLimit extractAiResponseHeaders(ResponseEntity<?> responseEntity) {
        Long requestsLimit = getHeaderAsLong(responseEntity, OpenAiApiResponseHeaders.REQUESTS_LIMIT_HEADER.getName());
        Long requestsRemaining = getHeaderAsLong(responseEntity, OpenAiApiResponseHeaders.REQUESTS_REMAINING_HEADER.getName());
        Long tokensLimit = getHeaderAsLong(responseEntity, OpenAiApiResponseHeaders.TOKENS_LIMIT_HEADER.getName());
        Long tokensRemaining = getHeaderAsLong(responseEntity, OpenAiApiResponseHeaders.TOKENS_REMAINING_HEADER.getName());
        Duration requestsReset = getHeaderAsDuration(responseEntity, OpenAiApiResponseHeaders.REQUESTS_RESET_HEADER.getName());
        Duration tokensReset = getHeaderAsDuration(responseEntity, OpenAiApiResponseHeaders.TOKENS_RESET_HEADER.getName());
        return new OpenAiRateLimit(requestsLimit, requestsRemaining, requestsReset, tokensLimit, tokensRemaining, tokensReset);
    }

    private static Duration getHeaderAsDuration(ResponseEntity<?> responseEntity, String headerName) {
        var headers = responseEntity.getHeaders();
        if (headers.asMultiValueMap().containsKey(headerName)) {
            List<String> values = headers.get(headerName);
            if (!CollectionUtils.isEmpty(values)) {
                return DurationFormatter.TIME_UNIT.parse(values.get(0));
            }
        }
        return null;
    }

    private static Long getHeaderAsLong(ResponseEntity<?> responseEntity, String headerName) {
        var headers = responseEntity.getHeaders();
        if (headers.asMultiValueMap().containsKey(headerName)) {
            List<String> values = headers.get(headerName);
            if (!CollectionUtils.isEmpty(values)) {
                return parseLong(headerName, values.get(0));
            }
        }
        return null;
    }

    private static Long parseLong(String headerName, String value) {
        if (StringUtils.hasText(value)) {
            try {
                return Long.valueOf(Long.parseLong(value.trim()));
            } catch (NumberFormatException e) {
                logger.warn("Value [{}] for HTTP header [{}] is not valid: {}", value, headerName, e.getMessage());
            }
        }
        return null;
    }

    // Mirror nested enum of original class to prevent verification issues
    public enum DurationFormatter {
        TIME_UNIT;
        public Duration parse(String value) {
            if (!StringUtils.hasText(value)) return null;
            try {
                if (value.endsWith("ms")) return Duration.ofMillis(Long.parseLong(value.substring(0, value.length() - 2).trim()));
                if (value.endsWith("s")) return Duration.ofSeconds(Long.parseLong(value.substring(0, value.length() - 1).trim()));
                if (value.endsWith("m")) return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1).trim()));
                if (value.endsWith("h")) return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1).trim()));
                return Duration.ofSeconds(Long.parseLong(value.trim()));
            } catch (Exception e) {
                return null;
            }
        }
    }
}
