package com.example.server.infrastructure.security.ratelimit;

/**
 * Triển khai thuật toán Token Bucket (Xô thẻ) luồng an toàn (Thread-safe).
 */
public class TokenBucket {
    private final long capacity;
    private final long refillIntervalNanos;
    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(long capacity, long durationInSeconds) {
        this.capacity = capacity;
        // refillIntervalNanos = tổng thời gian (ns) / số thẻ tối đa
        this.refillIntervalNanos = (durationInSeconds * 1_000_000_000L) / capacity;
        this.tokens = capacity;
        this.lastRefillNanos = System.nanoTime();
    }

    /**
     * Thử tiêu thụ 1 token. Trả về true nếu thành công, false nếu hết thẻ.
     */
    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    /**
     * Nạp lại thẻ dựa trên khoảng thời gian đã trôi qua kể từ lần nạp trước.
     */
    private void refill() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos >= refillIntervalNanos) {
            double tokensToAdd = (double) elapsedNanos / refillIntervalNanos;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillNanos = now;
        }
    }
}
