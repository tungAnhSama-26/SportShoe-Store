package com.example.server.infrastructure.security.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation cấu hình giới hạn lượt truy cập (Rate Limiting).
 * Có thể gắn trên Method của Controller hoặc trên toàn bộ Class Controller.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * Số lượng request tối đa được phép thực hiện trong khoảng thời gian quy định.
     */
    int limit() default 10;

    /**
     * Khoảng thời gian (tính bằng giây) để làm đầy lại lượng request tối đa.
     */
    int durationInSeconds() default 60;
}
