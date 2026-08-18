package com.spring.mednemesis.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class OCRService {

    public String extractText(MultipartFile file) throws IOException, TesseractException {

        // Load tessdata from resources
        ClassPathResource tessdataResource = new ClassPathResource("tessdata");

        File tessdataFolder = tessdataResource.getFile();

        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(tessdataFolder.getAbsolutePath());
        tesseract.setLanguage("eng");

        // Create temporary file from uploaded file
        File tempFile = File.createTempFile("mednemesis-",getExtension(file));

        try{
            file.transferTo(tempFile);
            return tesseract.doOCR(tempFile);
        } finally {
            // Delete temporary file
            tempFile.delete();
        }
    }

    public String getExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();

        if(filename == null || !filename.contains(".")) {
            return ".tmp";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}
