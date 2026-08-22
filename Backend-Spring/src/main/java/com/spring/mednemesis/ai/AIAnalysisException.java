package com.spring.mednemesis.ai;

public class AIAnalysisException extends RuntimeException {

    public AIAnalysisException(String message) {
        super(message);
    }

    public AIAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}