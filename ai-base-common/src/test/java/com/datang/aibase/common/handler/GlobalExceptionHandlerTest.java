package com.datang.aibase.common.handler;

import com.datang.aibase.common.dto.ApiResponse;
import com.datang.aibase.common.exception.AiBaseException;
import com.datang.aibase.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleAiBaseException returns error with code and message")
    void handleAiBaseException_returnsErrorWithCodeAndMessage() {
        var ex = new AiBaseException("AI_001", "AI service unavailable") {};

        ApiResponse<Void> resp = handler.handleAiBaseException(ex);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorCode()).isEqualTo("AI_001");
        assertThat(resp.getErrorMsg()).isEqualTo("AI service unavailable");
    }

    @Test
    @DisplayName("handleBusiness returns fail with message")
    void handleBusiness_returnsFailWithMessage() {
        var ex = new BusinessException("invalid request");

        ApiResponse<Void> resp = handler.handleBusiness(ex);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorMsg()).isEqualTo("invalid request");
    }

    @Test
    @DisplayName("handleNoResourceFound returns NOT_FOUND error")
    void handleNoResourceFound_returnsNotFound() {
        var ex = new NoResourceFoundException(HttpMethod.GET, "/api/nonexistent");

        ApiResponse<Void> resp = handler.handleNoResourceFound(ex);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("handleIllegalArg returns fail with message")
    void handleIllegalArg_returnsFailWithMessage() {
        var ex = new IllegalArgumentException("name must not be blank");

        ApiResponse<Void> resp = handler.handleIllegalArg(ex);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorMsg()).isEqualTo("name must not be blank");
    }

    @Test
    @DisplayName("handleUnknown returns UNKNOWN error with generic message")
    void handleUnknown_returnsGenericError() {
        var ex = new RuntimeException("something crashed");

        ApiResponse<Void> resp = handler.handleUnknown(ex);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorCode()).isEqualTo("UNKNOWN");
        assertThat(resp.getErrorMsg()).isEqualTo("Internal server error");
    }
}
