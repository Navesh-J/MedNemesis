package com.spring.mednemesis.pdf;

public class PDFGenerationException extends RuntimeException {

    public PDFGenerationException(String message) {
        super(message);
    }

    public PDFGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}