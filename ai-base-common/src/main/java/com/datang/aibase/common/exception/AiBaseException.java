package com.datang.aibase.common.exception;

public abstract class AiBaseException extends RuntimeException {
    private final String errorCode;

    protected AiBaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected AiBaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
