package com.example.logging.exception;

import java.util.List;
import java.util.Map;

/**
 * 유효성 검사 예외
 */
public class ValidationException extends RuntimeException {
    private final Map<String, List<String>> validationErrors;

    public ValidationException(String message, Map<String, List<String>> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }
}
