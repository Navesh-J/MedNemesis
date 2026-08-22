package com.spring.mednemesis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // FILE TOO LARGE
    // =========================================================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(
            MaxUploadSizeExceededException e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "error", "File too large",
                        "message",
                        "The uploaded file is too large. Maximum allowed size is 10 MB."
                ));
    }

    // =========================================================
    // UNEXPECTED ERROR
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(
            Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "error", "Internal server error",
                        "message",
                        "Something went wrong while processing the report. Please try again."
                ));
    }
}