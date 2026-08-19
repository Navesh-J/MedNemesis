package com.spring.mednemesis.controller;

import com.spring.mednemesis.ai.AIAnalysisService;
import com.spring.mednemesis.model.ReportAnalysisResponse;
import com.spring.mednemesis.ocr.OCRService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final OCRService ocrService;
    private final AIAnalysisService aiAnalysisService;

    public ReportController(
            OCRService ocrService,
            AIAnalysisService aiAnalysisService) {
        this.ocrService = ocrService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeReport(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "File is required"
                    ));
        }

        try {
            String extractedText = ocrService.extractText(file);

            if (extractedText == null || extractedText.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Could not extract any text from the report"
                        ));
            }

            String analysis = aiAnalysisService.analyze(extractedText);

            return ResponseEntity.ok(
                    new ReportAnalysisResponse(
                            true,
                            file.getOriginalFilename(),
                            extractedText,
                            analysis,
                            null
                    )
            );

        } catch (IOException | TesseractException e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", "OCR processing failed",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error", "Report analysis failed",
                            "message", e.getMessage()
                    ));
        }
    }
}
