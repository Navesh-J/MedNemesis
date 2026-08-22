package com.spring.mednemesis.controller;

import com.spring.mednemesis.exception.AIAnalysisException;
import com.spring.mednemesis.ai.AIAnalysisService;
import com.spring.mednemesis.exception.PDFGenerationException;
import com.spring.mednemesis.model.ReportAnalysisResponse;
import com.spring.mednemesis.ocr.OCRService;
import com.spring.mednemesis.pdf.PDFService;
import com.spring.mednemesis.validation.ReportFileValidator;
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
    private final ReportFileValidator reportFileValidator;

    public ReportController(
            OCRService ocrService,
            AIAnalysisService aiAnalysisService,
            PDFService pdfService,
            ReportFileValidator reportFileValidator) {

        this.ocrService = ocrService;
        this.aiAnalysisService = aiAnalysisService;
        this.pdfService = pdfService;
        this.reportFileValidator = reportFileValidator;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeReport(
            @RequestParam("files") MultipartFile[] files) {

        // =====================================================
        // VALIDATE UPLOAD
        // =====================================================

        reportFileValidator.validate(files);

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

        } catch (AIAnalysisException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error",
                            "AI analysis failed",
                            "message",
                            "The report could not be analyzed right now. Please try again."
                    ));

        } catch (PDFGenerationException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "error",
                            "PDF generation failed",
                            "message",
                            "The analysis was completed, but the PDF report could not be generated. Please try again."
                    ));

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
                            "Something went wrong while processing the report. Please try again."
                    ));
        }
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