package com.spring.mednemesis.controller;

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

    public ReportController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/ocr")
    public ResponseEntity<?> extractText(
            @RequestParam("file")MultipartFile file) {
        if(file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success",false,
                            "error","File is required"
                    ));
        }

        try{
            String extractedText = ocrService.extractText(file);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "filename", file.getOriginalFilename(),
                            "text", extractedText
                    )
            );
        } catch (IOException | TesseractException e){
            return ResponseEntity.badRequest().body(Map.of(
                    "success",false,
                    "error","OCR processing failed",
                    "message", e.getMessage()
            ));
        }
    }
}
