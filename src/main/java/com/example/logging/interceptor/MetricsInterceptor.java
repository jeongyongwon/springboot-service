package com.example.logging.interceptor;

import com.example.logging.service.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 메트릭 수집 인터셉터
 * 모든 HTTP 요청에 대한 메트릭을 자동으로 수집
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private final MetricsService metricsService;
    private static final String START_TIME_ATTRIBUTE = "startTime";

    public MetricsInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long durationMs = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String path = request.getRequestURI();
            int statusCode = response.getStatus();

            metricsService.recordRequest(method, path, statusCode, durationMs);
        }
    }
}
