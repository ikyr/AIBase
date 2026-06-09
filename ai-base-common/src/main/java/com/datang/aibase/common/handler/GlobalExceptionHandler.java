package com.datang.aibase.common.handler;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.exception.AiBaseException;
import com.datang.aibase.common.exception.BusinessException;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired(required = false)
    private Tracer tracer;

    @ExceptionHandler(AiBaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleAiBaseException(AiBaseException e) {
        log.error("AIBase exception: code={}, msg={}", e.getErrorCode(), e.getMessage(), e);
        ApiResponse<Void> resp = ApiResponse.error(e.getErrorCode(), e.getMessage());
        if (tracer != null && tracer.currentSpan() != null) {
            resp.setTraceId(tracer.currentSpan().context().traceId());
        }
        return resp;
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNoResourceFound(NoResourceFoundException e) {
        return ApiResponse.error("NOT_FOUND", "The requested resource was not found");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArg(IllegalArgumentException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        ApiResponse<Void> resp = ApiResponse.error("UNKNOWN", "Internal server error");
        if (tracer != null && tracer.currentSpan() != null) {
            resp.setTraceId(tracer.currentSpan().context().traceId());
        }
        return resp;
    }
}
