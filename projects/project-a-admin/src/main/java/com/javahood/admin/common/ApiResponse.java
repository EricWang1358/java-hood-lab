package com.javahood.admin.common;

import org.slf4j.MDC;

import java.time.Instant;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String traceId,
        long timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data, MDC.get(TraceIdFilter.MDC_TRACE_KEY), Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                errorCode.getCode(),
                message,
                null,
                MDC.get(TraceIdFilter.MDC_TRACE_KEY),
                Instant.now().toEpochMilli()
        );
    }
}
