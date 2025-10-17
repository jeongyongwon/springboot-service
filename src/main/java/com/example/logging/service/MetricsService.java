package com.example.logging.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 애플리케이션 메트릭 수집 서비스
 */
@Service
public class MetricsService {

    private final Map<String, RequestMetrics> metricsMap = new ConcurrentHashMap<>();
    private final long startTime = System.currentTimeMillis();

    /**
     * 요청 메트릭 기록
     */
    public void recordRequest(String method, String path, int statusCode, long durationMs) {
        String key = method + ":" + path;

        metricsMap.computeIfAbsent(key, k -> new RequestMetrics(method, path))
                .recordRequest(statusCode, durationMs);
    }

    /**
     * 전체 메트릭 조회
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> result = new HashMap<>();

        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        result.put("uptime_seconds", uptimeSeconds);

        long totalRequests = metricsMap.values().stream()
                .mapToLong(RequestMetrics::getTotalRequests)
                .sum();
        result.put("total_requests", totalRequests);

        long totalErrors = metricsMap.values().stream()
                .mapToLong(RequestMetrics::getTotalErrors)
                .sum();
        result.put("total_errors", totalErrors);

        double overallErrorRate = totalRequests > 0
                ? (double) totalErrors / totalRequests * 100
                : 0.0;
        result.put("overall_error_rate", Math.round(overallErrorRate * 100.0) / 100.0);

        Map<String, Map<String, Object>> endpoints = new HashMap<>();
        metricsMap.forEach((key, metrics) -> {
            endpoints.put(metrics.getPath(), metrics.toMap());
        });
        result.put("endpoints", endpoints);

        return result;
    }

    /**
     * 메트릭 초기화
     */
    public void reset() {
        metricsMap.clear();
    }

    /**
     * 요청 메트릭 클래스
     */
    private static class RequestMetrics {
        private final String method;
        private final String path;
        private long totalRequests = 0;
        private long totalErrors = 0;
        private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

        public RequestMetrics(String method, String path) {
            this.method = method;
            this.path = path;
        }

        public synchronized void recordRequest(int statusCode, long durationMs) {
            totalRequests++;

            if (statusCode >= 400) {
                totalErrors++;
            }

            responseTimes.add(durationMs);

            // 최근 100개만 유지
            if (responseTimes.size() > 100) {
                responseTimes.remove(0);
            }
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("method", method);
            map.put("total_requests", totalRequests);
            map.put("total_errors", totalErrors);

            double errorRate = totalRequests > 0
                    ? (double) totalErrors / totalRequests * 100
                    : 0.0;
            map.put("error_rate", Math.round(errorRate * 100.0) / 100.0);

            if (!responseTimes.isEmpty()) {
                double avg = responseTimes.stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0.0);
                map.put("avg_response_time_ms", Math.round(avg * 100.0) / 100.0);

                map.put("max_response_time_ms", responseTimes.stream()
                        .mapToLong(Long::longValue)
                        .max()
                        .orElse(0L));

                map.put("min_response_time_ms", responseTimes.stream()
                        .mapToLong(Long::longValue)
                        .min()
                        .orElse(0L));
            }

            return map;
        }
    }
}
