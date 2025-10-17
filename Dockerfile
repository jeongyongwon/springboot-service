FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Maven Wrapper 사용 (없으면 maven 설치 필요)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
