package com.datang.aibase.common.exception;

public class WorkflowException extends AiBaseException {
    public WorkflowException(String errorCode, String message) {
        super("WF_" + errorCode, message);
    }
}
