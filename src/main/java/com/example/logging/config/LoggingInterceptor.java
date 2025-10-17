package com.example.logging.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP 요청/응답 로깅 인터셉터
 * 통합 로그 포맷에 맞춰 HTTP 로그를 생성합니다.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String TRACE_ID = "trace_id";
    private static final String SPAN_ID = "span_id";
    private static final String REQUEST_ID = "request_id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 요청 시작 시간 저장
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        // Trace ID 생성 또는 헤더에서 가져오기
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        String spanId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        // MDC에 컨텍스트 정보 저장
        MDC.put(TRACE_ID, traceId);
        MDC.put(SPAN_ID, spanId);
        MDC.put(REQUEST_ID, requestId);

        // 요청 시작 로그
        Map<String, Object> httpData = new HashMap<>();
        httpData.put("method", request.getMethod());
        httpData.put("path", request.getRequestURI());
        httpData.put("client_ip", getClientIp(request));
        httpData.put("user_agent", request.getHeader("User-Agent"));

        logger.info("HTTP request started",
            StructuredArguments.keyValue("http", httpData)
        );

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            // 요청 처리 시간 계산
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            long durationMs = System.currentTimeMillis() - startTime;

            // HTTP 로그 데이터 생성
            Map<String, Object> httpData = new HashMap<>();
            httpData.put("method", request.getMethod());
            httpData.put("path", request.getRequestURI());
            httpData.put("status_code", response.getStatus());
            httpData.put("duration_ms", durationMs);
            httpData.put("client_ip", getClientIp(request));
            httpData.put("user_agent", request.getHeader("User-Agent"));

            if (ex != null) {
                // 에러 발생 시
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("type", ex.getClass().getSimpleName());
                errorData.put("message", ex.getMessage());

                logger.error("HTTP request failed",
                    StructuredArguments.keyValue("http", httpData),
                    StructuredArguments.keyValue("error", errorData),
                    ex
                );
            } else {
                // 정상 처리
                logger.info("HTTP request completed",
                    StructuredArguments.keyValue("http", httpData)
                );
            }
        } finally {
            // MDC 클리어
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
