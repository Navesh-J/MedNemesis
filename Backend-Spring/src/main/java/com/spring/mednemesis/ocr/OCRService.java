package com.spring.mednemesis.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

@Service
public class OCRService {

    private final OCRTextCleaner textCleaner;

    public OCRService(OCRTextCleaner textCleaner) {
        this.textCleaner = textCleaner;
    }

    public String extractText(
            MultipartFile file
    ) throws IOException, TesseractException {

        ClassPathResource tessdataResource =
                new ClassPathResource("tessdata");

        File tessdataFolder =
                tessdataResource.getFile();

        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(
                tessdataFolder.getAbsolutePath()
        );

        tesseract.setLanguage("eng");

        /*
         * PSM 6:
         *
         * Assume a single uniform block of text.
         *
         * This works reasonably well for medical
         * laboratory reports containing tables.
         */
        tesseract.setPageSegMode(6);

        /*
         * Tell Tesseract to preserve spaces between
         * columns as much as possible.
         */
        tesseract.setVariable(
                "preserve_interword_spaces",
                "1"
        );

        /*
         * The uploaded images are relatively small.
         * Giving Tesseract a 300 DPI hint improves
         * character recognition.
         */
        tesseract.setVariable(
                "user_defined_dpi",
                "300"
        );

        File tempFile =
                File.createTempFile(
                        "mednemesis-",
                        getExtension(file)
                );

        File processedFile =
                File.createTempFile(
                        "mednemesis-processed-",
                        ".png"
                );

        try {

            // =================================================
            // SAVE ORIGINAL
            // =================================================

            file.transferTo(tempFile);

            // =================================================
            // READ IMAGE
            // =================================================

            BufferedImage original =
                    ImageIO.read(tempFile);

            /*
             * If the uploaded file cannot be decoded as
             * an image, fall back to the original file.
             *
             * This keeps the service from crashing on
             * unsupported input.
             */
            if (original == null) {

                return textCleaner.clean(
                        tesseract.doOCR(tempFile)
                );
            }

            // =================================================
            // PREPROCESS IMAGE
            // =================================================

            BufferedImage processed =
                    preprocessImage(original);

            // =================================================
            // SAVE PROCESSED IMAGE
            // =================================================

            ImageIO.write(
                    processed,
                    "png",
                    processedFile
            );

            // =================================================
            // OCR
            // =================================================

            String rawText =
                    tesseract.doOCR(processedFile);

            // =================================================
            // CLEAN OCR
            // =================================================

            return textCleaner.clean(rawText);

        } finally {

            // Delete temporary files

            if (tempFile.exists()) {
                tempFile.delete();
            }

            if (processedFile.exists()) {
                processedFile.delete();
            }
        }
    }

    // =========================================================
    // IMAGE PREPROCESSING
    // =========================================================

    private BufferedImage preprocessImage(
            BufferedImage original
    ) {

        /*
         * Upscale the image.
         *
         * The supplied report images are around
         * 650-700 pixels wide. That is quite small
         * for table text.
         *
         * 3x gives Tesseract much more information
         * to work with.
         */

        int scale = 3;

        int newWidth =
                original.getWidth() * scale;

        int newHeight =
                original.getHeight() * scale;

        BufferedImage resized =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.drawImage(
                original,
                0,
                0,
                newWidth,
                newHeight,
                null
        );

        graphics.dispose();

        // =====================================================
        // GRAYSCALE
        // =====================================================

        BufferedImage grayscale =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D grayGraphics =
                grayscale.createGraphics();

        grayGraphics.drawImage(
                resized,
                0,
                0,
                null
        );

        grayGraphics.dispose();

        // =====================================================
        // CONTRAST
        // =====================================================

        RescaleOp contrast =
                new RescaleOp(
                        1.35f,
                        -25f,
                        null
                );

        BufferedImage enhanced =
                contrast.filter(
                        grayscale,
                        null
                );

        // =====================================================
        // THRESHOLD
        // =====================================================

        return threshold(
                enhanced,
                175
        );
    }

    // =========================================================
    // BINARY THRESHOLD
    // =========================================================

    private BufferedImage threshold(
            BufferedImage image,
            int threshold
    ) {

        int width =
                image.getWidth();

        int height =
                image.getHeight();

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_BINARY
                );

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int gray =
                        image.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                int value =
                        gray < threshold
                                ? 0
                                : 255;

                result.getRaster()
                        .setSample(
                                x,
                                y,
                                0,
                                value
                        );
            }
        }

        return result;
    }

    // =========================================================
    // FILE EXTENSION
    // =========================================================

    private String getExtension(
            MultipartFile file
    ) {

        String filename =
                file.getOriginalFilename();

        if (
                filename == null ||
                        !filename.contains(".")
        ) {
            return ".tmp";
        }

        return filename.substring(
                filename.lastIndexOf(".")
        );
    }
}