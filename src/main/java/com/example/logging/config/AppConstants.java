package com.example.logging.config;

/**
 * 애플리케이션 전역 상수 정의
 */
public class AppConstants {

    // HTTP 상태 코드
    public static final class HttpStatus {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
    }

    // 에러 메시지
    public static final class ErrorMessages {
        public static final String USER_NOT_FOUND = "사용자를 찾을 수 없습니다";
        public static final String INVALID_USER_ID = "유효하지 않은 사용자 ID입니다";
        public static final String MISSING_REQUIRED_FIELDS = "필수 입력 항목이 누락되었습니다";
        public static final String INVALID_EMAIL = "이메일 형식이 올바르지 않습니다";
        public static final String DB_CONNECTION_FAILED = "데이터베이스 연결에 실패했습니다";
    }

    // 유효성 검사 규칙
    public static final class Validation {
        public static final int EMAIL_MAX_LENGTH = 254;
        public static final int NAME_MIN_LENGTH = 2;
        public static final int NAME_MAX_LENGTH = 100;
        public static final long USER_ID_MAX = 1000000L;
    }

    // 타임아웃 설정 (밀리초)
    public static final class Timeouts {
        public static final int DB_QUERY = 5000;
        public static final int REDIS_OPERATION = 2000;
        public static final int EXTERNAL_API = 10000;
    }

    // 캐시 TTL (초)
    public static final class CacheTTL {
        public static final int USER_DATA = 60;
        public static final int HEALTH_CHECK = 30;
        public static final int METRICS = 10;
    }

    private AppConstants() {
        // 유틸리티 클래스는 인스턴스화 방지
        throw new IllegalStateException("상수 클래스는 인스턴스화할 수 없습니다");
    }
}
