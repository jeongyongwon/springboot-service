# Spring Boot Service with Structured Logging

Spring Boot REST API with SLF4J/Logback structured logging

## Features

- **Structured Logging**: JSON logs via Logback
- **Health Monitoring**: /health endpoint with JVM metrics
- **CORS Support**: Configurable for cross-origin requests
- **Error Tracking**: Comprehensive error logging
- **JPA Integration**: H2 database for development

## Quick Start

```bash
./mvnw spring-boot:run  # Development
./mvnw clean package    # Build JAR
java -jar target/*.jar  # Run production
```

## API Endpoints

- `GET /api/` - Root health check
- `GET /health` - Detailed health with memory stats
- `GET /api/users/{id}` - Get user (with validation)
- `POST /api/users` - Create user (with DTO validation)
- `GET /api/error` - Error simulation
- `GET /api/slow-query` - Slow query test

## Configuration

Edit `application.properties`:
```
server.port=8080
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.com.example=INFO
```

## Recent Updates

### v0.2.0
- Added health check endpoint with JVM metrics
- Implemented CORS configuration
- Added DTO validation
- Performance optimizations
- Improved error handling

### v0.1.0
- Initial release with structured logging
- Basic CRUD operations
