package com.example.server.infrastructure.security.ratelimit;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dịch vụ quản lý các Token Bucket tương ứng với từng địa chỉ IP của Client.
 */
@Service
public class RateLimiterService {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Kiểm tra xem request có được phép tiếp tục hay không.
     */
    public boolean isAllowed(String key, int limit, int durationInSeconds) {
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(limit, durationInSeconds));
        return bucket.tryConsume();
    }

    /**
     * Dọn dẹp toàn bộ bộ nhớ đệm (dùng khi cần reset hệ thống).
     */
    public void clear() {
        buckets.clear();
    }
}
