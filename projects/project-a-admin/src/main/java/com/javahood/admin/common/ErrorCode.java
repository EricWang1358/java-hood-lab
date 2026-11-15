package com.javahood.admin.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(40001, HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    TOKEN_REVOKED(40102, HttpStatus.UNAUTHORIZED, "Token revoked or expired"),
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(40401, HttpStatus.NOT_FOUND, "Resource not found"),
    CONFLICT(40901, HttpStatus.CONFLICT, "Resource conflict"),
    TOO_MANY_REQUESTS(42901, HttpStatus.TOO_MANY_REQUESTS, "Too many failed login attempts"),
    INTERNAL_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final int code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
