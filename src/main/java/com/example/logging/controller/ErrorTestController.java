package com.example.logging.controller;

import com.example.logging.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * 다양한 에러 시나리오 테스트 컨트롤러 (RAG 테스트용)
 */
@RestController
@RequestMapping("/api/test")
public class ErrorTestController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorTestController.class);

    /**
     * GET /api/test/null-pointer - Null Pointer Exception
     */
    @GetMapping("/null-pointer")
    public ResponseEntity<?> testNullPointer() {
        try {
            String str = null;
            int length = str.length(); // NullPointerException 발생

            return ResponseEntity.ok(Map.of("length", length));

        } catch (NullPointerException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "access_method");
            context.put("endpoint", "/api/test/null-pointer");
            context.put("attempted_access", "str.length()");

            LoggerUtil.logError(logger, "Null pointer exception occurred", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Cannot invoke method on null object"));
        }
    }

    /**
     * GET /api/test/array-index-out-of-bounds - Array Index Out of Bounds (유사 시나리오 1)
     */
    @GetMapping("/array-index-out-of-bounds")
    public ResponseEntity<?> testArrayIndexOutOfBounds() {
        try {
            String[] items = {"a", "b", "c"};
            String item = items[10]; // ArrayIndexOutOfBoundsException 발생

            return ResponseEntity.ok(Map.of("item", item));

        } catch (ArrayIndexOutOfBoundsException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "array_access");
            context.put("endpoint", "/api/test/array-index-out-of-bounds");
            context.put("array_length", 3);
            context.put("attempted_index", 10);

            LoggerUtil.logError(logger, "Array index out of bounds error", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Array index error"));
        }
    }

    /**
     * GET /api/test/class-cast-exception - Class Cast Exception
     */
    @GetMapping("/class-cast-exception")
    public ResponseEntity<?> testClassCastException() {
        try {
            Object obj = "This is a string";
            Integer number = (Integer) obj; // ClassCastException 발생

            return ResponseEntity.ok(Map.of("number", number));

        } catch (ClassCastException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "type_cast");
            context.put("endpoint", "/api/test/class-cast-exception");
            context.put("from_type", "String");
            context.put("to_type", "Integer");

            LoggerUtil.logError(logger, "Class cast exception", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Type casting failed"));
        }
    }

    /**
     * GET /api/test/number-format-exception - Number Format Exception (유사 시나리오 2)
     */
    @GetMapping("/number-format-exception")
    public ResponseEntity<?> testNumberFormatException() {
        try {
            String value = "not a number";
            int number = Integer.parseInt(value); // NumberFormatException 발생

            return ResponseEntity.ok(Map.of("number", number));

        } catch (NumberFormatException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "parse_number");
            context.put("endpoint", "/api/test/number-format-exception");
            context.put("value", "not a number");
            context.put("target_type", "int");

            LoggerUtil.logError(logger, "Number format exception", ex, context);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid number format"));
        }
    }

    /**
     * GET /api/test/db-connection-timeout - Database Connection Timeout
     */
    @GetMapping("/db-connection-timeout")
    public ResponseEntity<?> testDbConnectionTimeout() {
        long startTime = System.currentTimeMillis();

        try {
            // DB 연결 타임아웃 시뮬레이션
            Thread.sleep(100);
            throw new TimeoutException("Connection timeout: Unable to connect to database after 5000ms");

        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - startTime;

            Map<String, Object> context = new HashMap<>();
            context.put("operation", "db_connect");
            context.put("endpoint", "/api/test/db-connection-timeout");
            context.put("database", "postgresql");
            context.put("host", "localhost");
            context.put("port", 5432);
            context.put("timeout_ms", 5000);
            context.put("duration_ms", durationMs);

            LoggerUtil.logError(logger, "Database connection timeout", ex, context);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Database connection timeout"));
        }
    }

    /**
     * GET /api/test/sql-exception - SQL Exception (유사 시나리오 3)
     */
    @GetMapping("/sql-exception")
    public ResponseEntity<?> testSQLException() {
        try {
            throw new SQLException("SQL error: Duplicate entry 'user@example.com' for key 'email'", "23000", 1062);

        } catch (SQLException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "db_insert");
            context.put("endpoint", "/api/test/sql-exception");
            context.put("sql_state", ex.getSQLState());
            context.put("error_code", ex.getErrorCode());
            context.put("database", "mysql");

            LoggerUtil.logError(logger, "SQL exception occurred", ex, context);

            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Database constraint violation"));
        }
    }

    /**
     * GET /api/test/redis-connection-error - Redis Connection Error
     */
    @GetMapping("/redis-connection-error")
    public ResponseEntity<?> testRedisConnectionError() {
        try {
            throw new RuntimeException("Redis connection failed: connect ECONNREFUSED 127.0.0.1:6379");

        } catch (RuntimeException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "redis_connect");
            context.put("endpoint", "/api/test/redis-connection-error");
            context.put("redis_host", "127.0.0.1");
            context.put("redis_port", 6379);
            context.put("error_code", "ECONNREFUSED");

            LoggerUtil.logError(logger, "Redis connection error", ex, context);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Redis connection failed"));
        }
    }

    /**
     * GET /api/test/redis-timeout - Redis Operation Timeout (유사 시나리오 4)
     */
    @GetMapping("/redis-timeout")
    public ResponseEntity<?> testRedisTimeout() {
        try {
            throw new TimeoutException("Redis operation timeout: GET operation exceeded 2000ms");

        } catch (TimeoutException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "redis_get");
            context.put("endpoint", "/api/test/redis-timeout");
            context.put("redis_host", "127.0.0.1");
            context.put("redis_port", 6379);
            context.put("timeout_ms", 2000);
            context.put("redis_operation", "GET");

            LoggerUtil.logError(logger, "Redis operation timeout", ex, context);

            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(Map.of("error", "Redis operation timeout"));
        }
    }

    /**
     * GET /api/test/file-not-found - File Not Found Exception
     */
    @GetMapping("/file-not-found")
    public ResponseEntity<?> testFileNotFound() {
        try {
            FileReader reader = new FileReader("/nonexistent/path/config.json");

            return ResponseEntity.ok(Map.of("status", "ok"));

        } catch (FileNotFoundException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "read_file");
            context.put("endpoint", "/api/test/file-not-found");
            context.put("file_path", "/nonexistent/path/config.json");

            LoggerUtil.logError(logger, "File not found error", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Configuration file not found"));
        }
    }

    /**
     * GET /api/test/io-exception - I/O Exception (유사 시나리오 5)
     */
    @GetMapping("/io-exception")
    public ResponseEntity<?> testIOException() {
        try {
            throw new java.io.IOException("I/O error: Permission denied when writing to /etc/protected/config.json");

        } catch (java.io.IOException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "write_file");
            context.put("endpoint", "/api/test/io-exception");
            context.put("file_path", "/etc/protected/config.json");

            LoggerUtil.logError(logger, "I/O exception occurred", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "File I/O error"));
        }
    }

    /**
     * GET /api/test/json-parse-error - JSON Parse Error
     */
    @GetMapping("/json-parse-error")
    public ResponseEntity<?> testJsonParseError() {
        try {
            throw new com.fasterxml.jackson.core.JsonParseException(null,
                "Unexpected character ('{' (code 123)): was expecting double-quote to start field name");

        } catch (Exception ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "parse_json");
            context.put("endpoint", "/api/test/json-parse-error");
            context.put("input", "{name: 'test', invalid}");

            LoggerUtil.logError(logger, "JSON parse error", ex, context);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid JSON format"));
        }
    }

    /**
     * GET /api/test/illegal-argument - Illegal Argument Exception (유사 시나리오 6)
     */
    @GetMapping("/illegal-argument")
    public ResponseEntity<?> testIllegalArgument() {
        try {
            throw new IllegalArgumentException("Invalid parameter: age must be between 0 and 150, got -5");

        } catch (IllegalArgumentException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "validate_input");
            context.put("endpoint", "/api/test/illegal-argument");
            context.put("parameter_name", "age");
            context.put("provided_value", -5);
            context.put("valid_range", "0-150");

            LoggerUtil.logError(logger, "Illegal argument exception", ex, context);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid parameter value"));
        }
    }

    /**
     * GET /api/test/stack-overflow - Stack Overflow Error
     */
    @GetMapping("/stack-overflow")
    public ResponseEntity<?> testStackOverflow() {
        try {
            recursiveFunction(0);
            return ResponseEntity.ok(Map.of("status", "ok"));

        } catch (StackOverflowError ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "recursive_call");
            context.put("endpoint", "/api/test/stack-overflow");
            context.put("recursion_depth", "exceeded");

            LoggerUtil.logError(logger, "Stack overflow error", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Stack overflow occurred"));
        }
    }

    private int recursiveFunction(int depth) {
        return recursiveFunction(depth + 1);
    }

    /**
     * GET /api/test/out-of-memory - Out of Memory Error Simulation
     */
    @GetMapping("/out-of-memory")
    public ResponseEntity<?> testOutOfMemory() {
        try {
            // 메모리 할당 시뮬레이션 (실제 OOM은 발생시키지 않음)
            List<byte[]> memoryList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                memoryList.add(new byte[1024 * 1024]); // 1MB씩
            }

            logger.warn("High memory usage detected: operation=memory_allocation, endpoint=/api/test/out-of-memory, " +
                "allocated_mb=" + memoryList.size());

            return ResponseEntity.ok(Map.of(
                "warning", "Memory allocation simulation",
                "allocated_mb", memoryList.size()
            ));

        } catch (OutOfMemoryError ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "memory_allocation");
            context.put("endpoint", "/api/test/out-of-memory");

            LoggerUtil.logError(logger, "Out of memory error", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Out of memory"));
        }
    }

    /**
     * GET /api/test/http-client-error - HTTP Client Request Error
     */
    @GetMapping("/http-client-error")
    public ResponseEntity<?> testHttpClientError() {
        try {
            throw new RuntimeException("HTTP request failed: connect ETIMEDOUT external-api.com:443");

        } catch (RuntimeException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "http_request");
            context.put("endpoint", "/api/test/http-client-error");
            context.put("target_url", "https://external-api.com/api/data");
            context.put("error_code", "ETIMEDOUT");
            context.put("timeout_ms", 5000);

            LoggerUtil.logError(logger, "HTTP client request error", ex, context);

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "External API request failed"));
        }
    }

    /**
     * GET /api/test/circuit-breaker-open - Circuit Breaker Open (유사 시나리오 7)
     */
    @GetMapping("/circuit-breaker-open")
    public ResponseEntity<?> testCircuitBreakerOpen() {
        try {
            throw new RuntimeException("Circuit breaker is OPEN: too many failures detected for external-api.com");

        } catch (RuntimeException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "http_request");
            context.put("endpoint", "/api/test/circuit-breaker-open");
            context.put("target_service", "external-api.com");
            context.put("circuit_state", "OPEN");
            context.put("failure_count", 5);
            context.put("threshold", 5);

            LoggerUtil.logError(logger, "Circuit breaker open", ex, context);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Service temporarily unavailable (circuit breaker open)"));
        }
    }

    /**
     * GET /api/test/rate-limit-exceeded - Rate Limit Exceeded
     */
    @GetMapping("/rate-limit-exceeded")
    public ResponseEntity<?> testRateLimitExceeded() {
        try {
            throw new RuntimeException("Rate limit exceeded: 429 Too Many Requests");

        } catch (RuntimeException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "api_call");
            context.put("endpoint", "/api/test/rate-limit-exceeded");
            context.put("rate_limit", 100);
            context.put("window", "1 minute");
            context.put("current_count", 105);

            LoggerUtil.logError(logger, "Rate limit exceeded", ex, context);

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Too many requests"));
        }
    }

    /**
     * GET /api/test/auth-token-expired - Authentication Token Expired
     */
    @GetMapping("/auth-token-expired")
    public ResponseEntity<?> testAuthTokenExpired() {
        try {
            throw new RuntimeException("JWT token expired at 2025-10-15T12:00:00Z");

        } catch (RuntimeException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "authenticate");
            context.put("endpoint", "/api/test/auth-token-expired");
            context.put("token_type", "JWT");
            context.put("expired_at", "2025-10-15T12:00:00Z");

            LoggerUtil.logError(logger, "Authentication token expired", ex, context);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token expired"));
        }
    }

    /**
     * GET /api/test/concurrent-modification - Concurrent Modification Exception
     */
    @GetMapping("/concurrent-modification")
    public ResponseEntity<?> testConcurrentModification() {
        try {
            List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
            for (String item : list) {
                if (item.equals("b")) {
                    list.remove(item); // ConcurrentModificationException 발생
                }
            }

            return ResponseEntity.ok(Map.of("list", list));

        } catch (ConcurrentModificationException ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "modify_collection");
            context.put("endpoint", "/api/test/concurrent-modification");
            context.put("collection_type", "ArrayList");

            LoggerUtil.logError(logger, "Concurrent modification exception", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Concurrent modification error"));
        }
    }
}
