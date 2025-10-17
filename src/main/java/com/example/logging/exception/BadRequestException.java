package com.example.logging.exception;

import org.springframework.http.HttpStatus;

/**
 * 잘못된 요청 예외
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
