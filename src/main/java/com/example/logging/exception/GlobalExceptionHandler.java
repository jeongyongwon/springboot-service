package com.example.logging.exception;

import com.example.logging.config.AppConstants;
import com.example.logging.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * 모든 컨트롤러에서 발생하는 예외를 중앙에서 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {

        Map<String, Object> context = new HashMap<>();
        context.put("error_code", ex.getErrorCode());
        context.put("path", request.getDescription(false));

        LoggerUtil.logError(logger, ex.getMessage(), ex, context);

        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("error_code", ex.getErrorCode());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 리소스 미발견 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> context = new HashMap<>();
        context.put("resource_type", ex.getResourceType());
        context.put("resource_id", ex.getResourceId());
        context.put("path", request.getDescription(false));

        LoggerUtil.logError(logger, ex.getMessage(), ex, context);

        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("resource_type", ex.getResourceType());
        response.put("status", HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex, WebRequest request) {

        Map<String, Object> context = new HashMap<>();
        context.put("validation_errors", ex.getValidationErrors());
        context.put("path", request.getDescription(false));

        LoggerUtil.logError(logger, ex.getMessage(), ex, context);

        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("validation_errors", ex.getValidationErrors());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        Map<String, Object> context = new HashMap<>();
        context.put("exception_type", ex.getClass().getSimpleName());
        context.put("path", request.getDescription(false));

        LoggerUtil.logError(logger, "처리되지 않은 예외 발생", ex, context);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "서버 내부 오류가 발생했습니다");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
