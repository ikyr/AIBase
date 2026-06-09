package com.datang.aibase.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("ok returns success with data")
    void ok_returnsSuccessWithData() {
        var resp = ApiResponse.ok("hello");

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData()).isEqualTo("hello");
        assertThat(resp.getErrorCode()).isNull();
        assertThat(resp.getErrorMsg()).isNull();
    }

    @Test
    @DisplayName("ok returns success with null data")
    void ok_handlesNullData() {
        var resp = ApiResponse.ok(null);

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData()).isNull();
    }

    @Test
    @DisplayName("ok with pagination returns success")
    void ok_paginated_returnsSuccess() {
        var resp = ApiResponse.ok("data", 100L, 1, 20);

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("fail returns not success with message")
    void fail_returnsErrorWithMessage() {
        var resp = ApiResponse.fail("something went wrong");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorMsg()).isEqualTo("something went wrong");
        assertThat(resp.getData()).isNull();
        assertThat(resp.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("error returns not success with code and message")
    void error_returnsCodeAndMessage() {
        var resp = ApiResponse.error("ERR_001", "validation failed");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getErrorCode()).isEqualTo("ERR_001");
        assertThat(resp.getErrorMsg()).isEqualTo("validation failed");
        assertThat(resp.getData()).isNull();
    }

    @Test
    @DisplayName("traceId can be set and retrieved")
    void traceId_settable() {
        var resp = new ApiResponse<>();
        resp.setTraceId("trace-abc-123");

        assertThat(resp.getTraceId()).isEqualTo("trace-abc-123");
    }
}
