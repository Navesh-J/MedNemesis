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
            // OCR
            String extractedText = ocrService.extractText(file);

            if (extractedText == null || extractedText.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Could not extract any text from the report"
                        ));
            }

            // AI Analysis
            String analysis = aiAnalysisService.analyze(extractedText);

            // Generate PDF
            byte[] pdfBytes = pdfService.generatePDF(analysis);

            // Save PDF
            Path uploadDirectory = Paths.get("uploads");
            Files.createDirectories(uploadDirectory);
            String pdfFilename = "report_" + UUID.randomUUID() + ".pdf";
            Path pdfPath = uploadDirectory.resolve(pdfFilename);
            Files.write(pdfPath, pdfBytes);

            // PDF URL
            String pdfUrl = "/uploads/" + pdfFilename;

            ReportAnalysisResponse response =
                    new ReportAnalysisResponse(
                            true,
                            file.getOriginalFilename(),
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
