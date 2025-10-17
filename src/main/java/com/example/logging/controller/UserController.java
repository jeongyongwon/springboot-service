package com.example.logging.controller;

import com.example.logging.entity.User;
import com.example.logging.repository.UserRepository;
import com.example.logging.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 API 컨트롤러
 * 통합 로그 포맷을 사용한 로깅 예시를 포함합니다.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        logger.info("Root endpoint accessed");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Spring Boot Logging Example");
        response.put("status", "healthy");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            logger.warn("Invalid user ID provided: {}", id);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "User ID must be positive"));
        }
        long startTime = System.currentTimeMillis();

        try {
            // 사용자 조회
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

            long durationMs = System.currentTimeMillis() - startTime;

            // 쿼리 로그
            LoggerUtil.logQuery(logger, "SELECT",
                "SELECT * FROM users WHERE id = ?",
                durationMs, 1, "user_db");

            return ResponseEntity.ok(user);

        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - startTime;

            Map<String, Object> context = new HashMap<>();
            context.put("user_id", id);
            context.put("operation", "get_user");

            LoggerUtil.logError(logger, "Failed to get user", ex, context);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> userData) {
        long startTime = System.currentTimeMillis();

        try {
            String name = userData.get("name");
            String email = userData.get("email");

            User user = new User(name, email);
            User savedUser = userRepository.save(user);

            long durationMs = System.currentTimeMillis() - startTime;

            // INSERT 쿼리 로그
            LoggerUtil.logQuery(logger, "INSERT",
                "INSERT INTO users (name, email) VALUES (?, ?)",
                durationMs, 1, "user_db");

            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

        } catch (Exception ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("user_data", userData);
            context.put("operation", "create_user");

            LoggerUtil.logError(logger, "Failed to create user", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create user"));
        }
    }

    @GetMapping("/error")
    public ResponseEntity<?> triggerError() {
        try {
            // 의도적으로 에러 발생
            int result = performDivision(1, 0);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            Map<String, Object> context = new HashMap<>();
            context.put("operation", "divide");
            context.put("endpoint", "/error");

            LoggerUtil.logError(logger,
                "Division by zero error occurred", ex, context);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * 에러 발생 위치를 명확히 하기 위한 헬퍼 메서드
     */
    private int performDivision(int a, int b) {
        return a / b;  // 이 라인에서 ArithmeticException 발생
    }

    @GetMapping("/slow-query")
    public ResponseEntity<?> slowQuery() {
        long startTime = System.currentTimeMillis();

        try {
            // 느린 쿼리 시뮬레이션
            Thread.sleep(800);

            // 더미 쿼리 실행
            userRepository.findAll();

            long durationMs = System.currentTimeMillis() - startTime;

            // 느린 쿼리 경고 로그
            LoggerUtil.logSlowQuery(logger, "SELECT",
                "SELECT * FROM large_table WHERE complex_condition = ?",
                durationMs, 1000, "analytics_db", 1000);

            return ResponseEntity.ok(Map.of(
                "status", "completed",
                "warning", "Query was slow"
            ));

        } catch (Exception ex) {
            LoggerUtil.logError(logger, "Slow query test failed", ex, null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Test failed"));
        }
    }
}

    /**
     * DTO for user creation with validation
     */
    public static class CreateUserRequest {
        private String name;
        private String email;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public void validate() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }
