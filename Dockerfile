FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app

# 의존성 파일만 먼저 복사 (캐싱 최적화)
COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# 소스 코드 복사 및 빌드
COPY src ./src
RUN mvn clean package -DskipTests && \
    mv target/*.jar target/app.jar

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine

# 보안 업데이트 및 필수 도구 설치
RUN apk update && apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/app.jar app.jar

# 로그 디렉토리 생성
RUN mkdir -p /var/log/springboot-service

# 비-root 사용자로 실행
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 && \
    chown -R spring:spring /app /var/log/springboot-service

USER spring

# 환경 변수 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
