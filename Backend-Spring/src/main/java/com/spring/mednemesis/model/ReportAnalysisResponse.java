package com.spring.mednemesis.model;

public record ReportAnalysisResponse (
        boolean success,
        String filename,
        String ocrText,
        String analysis,
        String pdfUrl
) {
}
