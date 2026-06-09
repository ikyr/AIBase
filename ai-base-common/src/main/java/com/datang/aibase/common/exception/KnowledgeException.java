package com.datang.aibase.common.exception;

public class KnowledgeException extends AiBaseException {
    public KnowledgeException(String errorCode, String message) {
        super("KB_" + errorCode, message);
    }
}
