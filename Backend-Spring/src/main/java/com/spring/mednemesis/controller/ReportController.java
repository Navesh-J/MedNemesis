package com.spring.mednemesis.controller;

import com.spring.mednemesis.ai.AIAnalysisService;
import com.spring.mednemesis.model.ReportAnalysisResponse;
import com.spring.mednemesis.ocr.OCRService;
import com.spring.mednemesis.pdf.PDFService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final OCRService ocrService;
    private final AIAnalysisService aiAnalysisService;
    private final PDFService pdfService;

    public ReportController(
            OCRService ocrService,
            AIAnalysisService aiAnalysisService,
            PDFService pdfService) {

        this.ocrService = ocrService;
        this.aiAnalysisService = aiAnalysisService;
        this.pdfService = pdfService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeReport(
            @RequestParam("files") MultipartFile[] files) {

        // =====================================================
        // VALIDATE FILES
        // =====================================================

        if (files == null || files.length == 0) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "At least one file is required"
                    ));
        }

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "One or more uploaded files are empty"
                        ));
            }
        }

        try {

            // =================================================
            // OCR — PROCESS EVERY PAGE
            // =================================================

            StringBuilder combinedText =
                    new StringBuilder();

            for (int i = 0; i < files.length; i++) {

                MultipartFile file = files[i];

                String extractedText =
                        ocrService.extractText(file);

                if (
                        extractedText != null
                                && !extractedText.isBlank()
                ) {

                    combinedText
                            .append("\n\n")
                            .append("===== PAGE ")
                            .append(i + 1)
                            .append(" =====\n\n");

                    combinedText.append(extractedText);
                }
            }

            String extractedText =
                    combinedText.toString()
                            .trim();

            // =================================================
            // VALIDATE OCR RESULT
            // =================================================

            if (extractedText.isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error",
                                "Could not extract any text from the uploaded report"
                        ));
            }

            // =================================================
            // ONE AI ANALYSIS
            // =================================================

            String analysis =
                    aiAnalysisService.analyze(extractedText);

            // =================================================
            // GENERATE ONE PDF
            // =================================================

            byte[] pdfBytes =
                    pdfService.generatePDF(analysis);

            // =================================================
            // SAVE PDF
            // =================================================

            Path uploadDirectory =
                    Paths.get("uploads");

            Files.createDirectories(
                    uploadDirectory
            );

            String pdfFilename =
                    "report_" +
                            UUID.randomUUID() +
                            ".pdf";

            Path pdfPath =
                    uploadDirectory.resolve(
                            pdfFilename
                    );

            Files.write(
                    pdfPath,
                    pdfBytes
            );

            // =================================================
            // PDF URL
            // =================================================

            String pdfUrl =
                    "/uploads/" + pdfFilename;

            // =================================================
            // RESPONSE
            // =================================================

            String filename =
                    files.length == 1
                            ? files[0].getOriginalFilename()
                            : files.length + " page report";

            ReportAnalysisResponse response =
                    new ReportAnalysisResponse(
                            true,
                            filename,
                            extractedText,
                            analysis,
                            pdfUrl
                    );

            return ResponseEntity.ok(response);

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