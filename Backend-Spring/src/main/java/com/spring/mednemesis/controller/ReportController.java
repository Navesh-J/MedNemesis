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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final int MAX_FILES = 10;

    private static final long MAX_FILE_SIZE =
            10L * 1024 * 1024;

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
        // VALIDATE FILE LIST
        // =====================================================

        if (files == null || files.length == 0) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "At least one file is required"
                    ));
        }

        if (files.length > MAX_FILES) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "error",
                            "Maximum " + MAX_FILES + " files are allowed"
                    ));
        }

        // =====================================================
        // VALIDATE EACH FILE
        // =====================================================

        for (MultipartFile file : files) {

            if (file == null) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Invalid file received"
                        ));
            }

            if (file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error",
                                getFileName(file)
                                        + ": File is empty"
                        ));
            }

            if (file.getSize() > MAX_FILE_SIZE) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error",
                                getFileName(file)
                                        + ": File size must not exceed 10 MB"
                        ));
            }

            if (!isSupportedFile(file)) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error",
                                getFileName(file)
                                        + ": Only JPG, JPEG, PNG and PDF files are supported"
                        ));
            }
        }

        try {

            // =================================================
            // OCR — PROCESS EVERY FILE
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

                    combinedText.append(
                            extractedText
                    );
                }
            }

            String extractedText =
                    combinedText
                            .toString()
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
            // AI ANALYSIS
            // =================================================

            String analysis =
                    aiAnalysisService.analyze(
                            extractedText
                    );

            if (
                    analysis == null
                            || analysis.isBlank()
            ) {

                return ResponseEntity
                        .internalServerError()
                        .body(Map.of(
                                "success", false,
                                "error",
                                "AI analysis returned no result"
                        ));
            }

            // =================================================
            // GENERATE PDF
            // =================================================

            byte[] pdfBytes =
                    pdfService.generatePDF(
                            analysis
                    );

            if (
                    pdfBytes == null
                            || pdfBytes.length == 0
            ) {

                return ResponseEntity
                        .internalServerError()
                        .body(Map.of(
                                "success", false,
                                "error",
                                "Could not generate the PDF report"
                        ));
            }

            // =================================================
            // SAVE PDF
            // =================================================

            Path uploadDirectory =
                    Paths.get("uploads");

            Files.createDirectories(
                    uploadDirectory
            );

            String pdfFilename =
                    "report_"
                            + UUID.randomUUID()
                            + ".pdf";

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
                    "/uploads/"
                            + pdfFilename;

            // =================================================
            // RESPONSE FILENAME
            // =================================================

            String filename =
                    files.length == 1
                            ? files[0].getOriginalFilename()
                            : files.length + " page report";

            // =================================================
            // RESPONSE
            // =================================================

            ReportAnalysisResponse response =
                    new ReportAnalysisResponse(
                            true,
                            filename,
                            extractedText,
                            analysis,
                            pdfUrl
                    );

            return ResponseEntity.ok(
                    response
            );

        } catch (IOException | TesseractException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error",
                            "OCR processing failed",
                            "message",
                            safeMessage(e)
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error",
                            "Report analysis failed",
                            "message",
                            safeMessage(e)
                    ));
        }
    }

    // =========================================================
    // FILE TYPE VALIDATION
    // =========================================================

    private boolean isSupportedFile(
            MultipartFile file
    ) {

        String contentType =
                file.getContentType();

        String filename =
                file.getOriginalFilename();

        String lowerFilename =
                filename == null
                        ? ""
                        : filename
                        .toLowerCase(Locale.ROOT);

        // -----------------------------------------------------
        // PDF
        // -----------------------------------------------------

        if (
                "application/pdf"
                        .equalsIgnoreCase(contentType)
                        || lowerFilename.endsWith(".pdf")
        ) {
            return true;
        }

        // -----------------------------------------------------
        // JPEG
        // -----------------------------------------------------

        if (
                "image/jpeg"
                        .equalsIgnoreCase(contentType)
                        || lowerFilename.endsWith(".jpg")
                        || lowerFilename.endsWith(".jpeg")
        ) {
            return true;
        }

        // -----------------------------------------------------
        // PNG
        // -----------------------------------------------------

        if (
                "image/png"
                        .equalsIgnoreCase(contentType)
                        || lowerFilename.endsWith(".png")
        ) {
            return true;
        }

        return false;
    }

    // =========================================================
    // FILE NAME
    // =========================================================

    private String getFileName(
            MultipartFile file
    ) {

        if (
                file == null
                        || file.getOriginalFilename() == null
                        || file.getOriginalFilename().isBlank()
        ) {

            return "Uploaded file";
        }

        return file.getOriginalFilename();
    }

    // =========================================================
    // SAFE EXCEPTION MESSAGE
    // =========================================================

    private String safeMessage(
            Exception exception
    ) {

        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {
            return "No additional information available";
        }

        return message;
    }
}