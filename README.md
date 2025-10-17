# Spring Boot 로깅 예시

Logback과 Logstash Encoder를 사용한 Spring Boot 애플리케이션의 통합 로그 포맷 구현 예시입니다.

## 주요 기능

- **Logback**: Spring Boot 기본 로깅 프레임워크
- **Logstash Logback Encoder**: JSON 로그 생성
- **MDC (Mapped Diagnostic Context)**: 분산 추적 컨텍스트
- **Interceptor**: HTTP 요청/응답 자동 로깅
- **Structured Arguments**: 구조화된 로그 데이터
- **전역 예외 처리**: 중앙화된 예외 처리 및 에러 응답
- **메트릭 수집**: 엔드포인트별 요청 수, 응답 시간, 에러율 추적
- **보안 헤더**: XSS, Clickjacking 방지 등 보안 헤더 자동 추가
- **캐시 시스템**: TTL 기반 인메모리 캐시
- **표준 API 응답**: 일관된 응답 형식 (ApiResponse DTO)

## 필수 요구사항

- Java 17 이상
- Maven 3.6 이상

## 설치 및 실행

```bash
# 의존성 설치 및 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run

# 또는 JAR 직접 실행
java -jar target/springboot-logging-example-1.0.0.jar
```

## Docker 실행

```bash
docker build -t springboot-logging-example .
docker run -p 8080:8080 \
  -e SPRING_APPLICATION_NAME=springboot-service \
  -e ENVIRONMENT=production \
  springboot-logging-example
```

## API 엔드포인트

- `GET /api/` - 헬스 체크
- `GET /api/users/{id}` - 사용자 조회 (SELECT 쿼리 로그)
- `POST /api/users` - 사용자 생성 (INSERT 쿼리 로그)
- `GET /api/error` - 에러 로그 테스트
- `GET /api/slow-query` - 느린 쿼리 경고 로그 테스트

## 로그 설정

`src/main/resources/logback-spring.xml`에서 로그 설정을 변경할 수 있습니다.

### 로그 출력 위치

- **콘솔**: JSON 형식으로 stdout 출력
- **파일**: `./logs/app.log` (일별 로테이션)
- **에러 파일**: `./logs/error.log` (ERROR 레벨만)

### 환경 변수

- `SPRING_APPLICATION_NAME`: 서비스 이름 (기본: springboot-service)
- `ENVIRONMENT`: 환경 (dev/staging/production, 기본: development)
- `LOG_PATH`: 로그 파일 경로 (기본: ./logs)

## 로그 예시

### HTTP 요청 로그
```json
{
  "timestamp": "2025-10-14T12:34:56.789Z",
  "level": "INFO",
  "service": "springboot-service",
  "environment": "production",
  "host": "server-01",
  "message": "HTTP request completed",
  "trace_id": "uuid-trace-id",
  "span_id": "uuid-span-id",
  "request_id": "uuid-request-id",
  "http": {
    "method": "GET",
    "path": "/api/users/1",
    "status_code": 200,
    "duration_ms": 52,
    "client_ip": "192.168.1.100",
    "user_agent": "Mozilla/5.0..."
  },
  "logger_name": "com.example.logging.config.LoggingInterceptor",
  "thread_name": "http-nio-8080-exec-1"
}
```

### 쿼리 로그
```json
{
  "timestamp": "2025-10-14T12:34:56.789Z",
  "level": "INFO",
  "service": "springboot-service",
  "environment": "production",
  "host": "server-01",
  "message": "Database query executed",
  "trace_id": "uuid-trace-id",
  "span_id": "uuid-span-id",
  "query": {
    "type": "SELECT",
    "statement": "SELECT * FROM users WHERE id = ?",
    "duration_ms": 45,
    "rows_affected": 1,
    "database": "user_db"
  },
  "logger_name": "com.example.logging.controller.UserController",
  "thread_name": "http-nio-8080-exec-1"
}
```

### 에러 로그
```json
{
  "timestamp": "2025-10-14T12:34:56.789Z",
  "level": "ERROR",
  "service": "springboot-service",
  "environment": "production",
  "host": "server-01",
  "message": "Division by zero error occurred",
  "trace_id": "uuid-trace-id",
  "span_id": "uuid-span-id",
  "error": {
    "type": "ArithmeticException",
    "message": "/ by zero"
  },
  "stack_trace": "java.lang.ArithmeticException: / by zero\n  at com.example.logging.controller.UserController.triggerError(UserController.java:87)\n  ...",
  "context": {
    "operation": "divide",
    "endpoint": "/error"
  },
  "logger_name": "com.example.logging.controller.UserController",
  "thread_name": "http-nio-8080-exec-1"
}
```

## 테스트

```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# 사용자 조회
curl http://localhost:8080/api/users/1

# 에러 테스트
curl http://localhost:8080/api/error

# 느린 쿼리 테스트
curl http://localhost:8080/api/slow-query
```
