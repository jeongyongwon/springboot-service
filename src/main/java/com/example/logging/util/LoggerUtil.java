package com.example.logging.util;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 로깅 유틸리티
 * 통합 로그 포맷에 맞춰 구조화된 로그를 쉽게 생성할 수 있도록 돕습니다.
 */
public class LoggerUtil {

    /**
     * 일반 정보 로그 생성
     */
    public static void logInfo(Logger logger, String message, Map<String, Object> context) {
        if (context != null) {
            logger.info(message, StructuredArguments.keyValue("context", context));
        } else {
            logger.info(message);
        }
    }


    /**
     * 데이터베이스 쿼리 로그 생성
     */
    public static void logQuery(Logger logger, String queryType, String statement,
                                long durationMs, int rowsAffected, String database) {
        Map<String, Object> queryData = new HashMap<>();
        queryData.put("type", queryType);
        queryData.put("statement", statement);
        queryData.put("duration_ms", durationMs);
        queryData.put("rows_affected", rowsAffected);
        queryData.put("database", database);

        logger.info("Database query executed",
            StructuredArguments.keyValue("query", queryData)
        );
    }

    /**
     * 느린 쿼리 경고 로그 생성
     */
    public static void logSlowQuery(Logger logger, String queryType, String statement,
                                   long durationMs, int rowsAffected, String database,
                                   long thresholdMs) {
        Map<String, Object> queryData = new HashMap<>();
        queryData.put("type", queryType);
        queryData.put("statement", statement);
        queryData.put("duration_ms", durationMs);
        queryData.put("rows_affected", rowsAffected);
        queryData.put("database", database);

        Map<String, Object> context = new HashMap<>();
        context.put("threshold_ms", thresholdMs);
        context.put("warning", "Query exceeded performance threshold");

        logger.warn("Slow database query detected",
            StructuredArguments.keyValue("query", queryData),
            StructuredArguments.keyValue("context", context)
        );
    }

    /**
     * 에러 로그 생성
     */
    public static void logError(Logger logger, String message, Exception ex,
                               Map<String, Object> context) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("type", ex.getClass().getSimpleName());
        errorData.put("message", ex.getMessage());

        // 에러 발생 위치 추출
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            // 프로젝트 패키지의 첫 번째 스택 프레임 찾기
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                // 프로젝트 패키지인지 확인 (com.example로 시작)
                if (className.startsWith("com.example")) {
                    Map<String, Object> location = new HashMap<>();
                    location.put("file", element.getFileName());
                    location.put("line", element.getLineNumber());
                    location.put("function", element.getMethodName());
                    location.put("class", element.getClassName());
                    errorData.put("location", location);
                    break;
                }
            }

            // 프로젝트 패키지를 못 찾은 경우 첫 번째 프레임 사용
            if (!errorData.containsKey("location") && stackTrace.length > 0) {
                StackTraceElement element = stackTrace[0];
                Map<String, Object> location = new HashMap<>();
                location.put("file", element.getFileName());
                location.put("line", element.getLineNumber());
                location.put("function", element.getMethodName());
                location.put("class", element.getClassName());
                errorData.put("location", location);
            }
        }

        if (context != null) {
            logger.error(message,
                StructuredArguments.keyValue("error", errorData),
                StructuredArguments.keyValue("context", context),
                ex
            );
        } else {
            logger.error(message,
                StructuredArguments.keyValue("error", errorData),
                ex
            );
        }
    }
}
