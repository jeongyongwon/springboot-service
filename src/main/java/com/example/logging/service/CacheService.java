package com.example.logging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 간단한 인메모리 캐시 서비스
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private long hits = 0;
    private long misses = 0;

    /**
     * 캐시에서 값 조회
     */
    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            misses++;
            logger.debug("캐시 미스: {}", key);
            return null;
        }

        if (Instant.now().isAfter(entry.getExpiresAt())) {
            cache.remove(key);
            misses++;
            logger.debug("캐시 만료: {}", key);
            return null;
        }

        hits++;
        logger.debug("캐시 히트: {}", key);
        return type.cast(entry.getValue());
    }

    /**
     * 캐시에 값 저장
     */
    public void put(String key, Object value, long ttlSeconds) {
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        cache.put(key, new CacheEntry(value, expiresAt));
        logger.debug("캐시 저장: {} (TTL: {}초)", key, ttlSeconds);
    }

    /**
     * 캐시에서 값 삭제
     */
    public void evict(String key) {
        cache.remove(key);
        logger.debug("캐시 삭제: {}", key);
    }

    /**
     * 모든 캐시 삭제
     */
    public void clear() {
        int size = cache.size();
        cache.clear();
        hits = 0;
        misses = 0;
        logger.info("캐시 전체 삭제: {}개 항목", size);
    }

    /**
     * 만료된 캐시 정리
     */
    public int cleanup() {
        Instant now = Instant.now();
        int cleanedCount = 0;

        cache.entrySet().removeIf(entry -> {
            if (now.isAfter(entry.getValue().getExpiresAt())) {
                cleanedCount++;
                return true;
            }
            return false;
        });

        if (cleanedCount > 0) {
            logger.info("캐시 정리 완료: {}개 항목 삭제, {}개 항목 남음", cleanedCount, cache.size());
        }

        return cleanedCount;
    }

    /**
     * 캐시 통계 조회
     */
    public Map<String, Object> getStats() {
        long totalRequests = hits + misses;
        double hitRate = totalRequests > 0 ? (double) hits / totalRequests * 100 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("entries", cache.size());
        stats.put("hits", hits);
        stats.put("misses", misses);
        stats.put("hit_rate", Math.round(hitRate * 100.0) / 100.0);
        stats.put("total_requests", totalRequests);

        return stats;
    }

    /**
     * 캐시 엔트리 클래스
     */
    private static class CacheEntry {
        private final Object value;
        private final Instant expiresAt;

        public CacheEntry(Object value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        public Object getValue() {
            return value;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }
    }
}
