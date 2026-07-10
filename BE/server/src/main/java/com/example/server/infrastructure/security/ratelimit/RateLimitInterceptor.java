package com.example.server.infrastructure.security.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Interceptor đánh chặn các request để kiểm soát tần suất truy cập API.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            // Lấy annotation từ method trước, nếu không có thì lấy từ class controller
            RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
            if (rateLimit == null) {
                rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
            }

            if (rateLimit != null) {
                String ip = getClientIp(request);
                String uri = request.getRequestURI();
                String key = ip + ":" + uri;

                boolean allowed = rateLimiterService.isAllowed(key, rateLimit.limit(), rateLimit.durationInSeconds());
                if (!allowed) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.setHeader("Retry-After", String.valueOf(rateLimit.durationInSeconds()));

                    Map<String, Object> errorDetails = new java.util.HashMap<>();
                    errorDetails.put("success", false);
                    errorDetails.put("message", "Thao tác quá nhanh! Vui lòng thử lại sau " + rateLimit.durationInSeconds() + " giây.");
                    errorDetails.put("data", null);
                    response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
                    return false; // Chặn request không cho vào controller
                }
            }
        }
        return true;
    }

    /**
     * Lấy IP thực tế của Client, xử lý trường hợp đi qua proxy/load balancer.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Nếu qua nhiều proxy, lấy IP đầu tiên trong chuỗi
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
