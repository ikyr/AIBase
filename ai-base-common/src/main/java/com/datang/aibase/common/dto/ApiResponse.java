package com.datang.aibase.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String errorCode;
    private String errorMsg;
    private String traceId;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, String errorCode, String errorMsg, String traceId) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.traceId = traceId;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = true;
        resp.data = data;
        return resp;
    }

    public static <T> ApiResponse<T> ok(T data, long total, int page, int limit) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = true;
        resp.data = data;
        return resp;
    }

    public static <T> ApiResponse<T> fail(String msg) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = false;
        resp.errorMsg = msg;
        return resp;
    }

    public static <T> ApiResponse<T> error(String code, String msg) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = false;
        resp.errorCode = code;
        resp.errorMsg = msg;
        return resp;
    }
}
