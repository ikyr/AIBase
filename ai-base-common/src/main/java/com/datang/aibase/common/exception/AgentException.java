package com.datang.aibase.common.exception;

public class AgentException extends AiBaseException {
    public AgentException(String errorCode, String message) {
        super("AG_" + errorCode, message);
    }
}
