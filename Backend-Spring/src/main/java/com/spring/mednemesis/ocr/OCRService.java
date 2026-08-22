package com.spring.mednemesis.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OCRService {

    private final OCRTextCleaner textCleaner;

    public OCRService(OCRTextCleaner textCleaner) {
        this.textCleaner = textCleaner;
    }

    // =========================================================
    // MAIN OCR
    // =========================================================

    public String extractText(MultipartFile file) throws IOException, TesseractException {

        ClassPathResource tessdataResource = new ClassPathResource("tessdata");

        File tessdataFolder = tessdataResource.getFile();

        File originalFile = File.createTempFile("mednemesis-original-", getExtension(file));

        File enhancedFile = File.createTempFile("mednemesis-enhanced-", ".png");

        try {

            file.transferTo(originalFile);

            BufferedImage original = ImageIO.read(originalFile);

            if (original == null) {

                return runNormalOCR(tessdataFolder, originalFile);
            }

            // -------------------------------------------------
            // Image preprocessing
            // -------------------------------------------------

            BufferedImage enhanced = createEnhancedImage(original);

            ImageIO.write(enhanced, "png", enhancedFile);

            // -------------------------------------------------
            // Normal OCR
            // -------------------------------------------------

            String normalOCR = runNormalOCR(tessdataFolder, enhancedFile);

            // -------------------------------------------------
            // Spatial OCR
            // -------------------------------------------------

            String spatialOCR = runSpatialOCR(tessdataFolder, enhanced);

            // -------------------------------------------------
            // Combine
            // -------------------------------------------------

            String combinedOCR = combineOCR(normalOCR, spatialOCR);

            return textCleaner.clean(combinedOCR);

        } finally {

            deleteFile(originalFile);
            deleteFile(enhancedFile);
        }
    }

    // =========================================================
    // NORMAL OCR
    // =========================================================

    private String runNormalOCR(File tessdataFolder, File imageFile) throws TesseractException {

        Tesseract tesseract = createTesseract(tessdataFolder);

        /*
         * PSM 6:
         * Assume a single uniform block of text.
         */
        tesseract.setPageSegMode(6);

        return tesseract.doOCR(imageFile);
    }

    // =========================================================
    // SPATIAL OCR
    // =========================================================

    private String runSpatialOCR(File tessdataFolder, BufferedImage image) throws TesseractException {

        Tesseract tesseract = createTesseract(tessdataFolder);

        /*
         * PSM 11:
         * Sparse text.
         */
        tesseract.setPageSegMode(11);

        /*
         * Tess4J 5.19.0 expects a LIST of images here.
         *
         * This was the source of our previous problem.
         */
        List<BufferedImage> images = List.of(image);

        List<Word> words = tesseract.getWords(images, 3);

        return reconstructLines(words);
    }

    // =========================================================
    // TESSERACT CONFIGURATION
    // =========================================================

    private Tesseract createTesseract(File tessdataFolder) {

        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(tessdataFolder.getAbsolutePath());

        tesseract.setLanguage("eng");

        // Preserve spaces between columns.

        tesseract.setVariable("preserve_interword_spaces", "1");

        // Our preprocessing enlarges the image.

        tesseract.setVariable("user_defined_dpi", "300");

        return tesseract;
    }

    // =========================================================
    // RECONSTRUCT TABLE ROWS
    // =========================================================

    private String reconstructLines(List<Word> words) {

        if (words == null || words.isEmpty()) {
            return "";
        }

        List<Word> filteredWords = new ArrayList<>();

        for (Word word : words) {

            if (word == null) {
                continue;
            }

            String text = word.getText();

            if (text == null || text.isBlank()) {
                continue;
            }

            if (word.getConfidence() < 25) {
                continue;
            }

            filteredWords.add(word);
        }

        // Sort from top → bottom, then left → right

        filteredWords.sort((word1, word2) -> {

            int yDifference = Integer.compare(word1.getBoundingBox().y, word2.getBoundingBox().y);

            if (yDifference != 0) {
                return yDifference;
            }

            return Integer.compare(word1.getBoundingBox().x, word2.getBoundingBox().x);
        });

        List<OCRLine> lines = new ArrayList<>();

        /*
         * Group words that belong to the same horizontal line.
         */
        for (Word word : filteredWords) {

            int wordY = word.getBoundingBox().y;

            OCRLine matchingLine = null;

            for (OCRLine line : lines) {

                if (Math.abs(line.averageY() - wordY) <= 18) {

                    matchingLine = line;
                    break;
                }
            }

            if (matchingLine == null) {

                matchingLine = new OCRLine();

                lines.add(matchingLine);
            }

            matchingLine.add(word);
        }

        /*
         * Sort lines vertically.
         */
        lines.sort((line1, line2) -> Integer.compare(line1.averageY(), line2.averageY()));

        StringBuilder result = new StringBuilder();

        /*
         * Build every line from left → right.
         */
        for (OCRLine line : lines) {

            line.words.sort((word1, word2) -> Integer.compare(word1.getBoundingBox().x, word2.getBoundingBox().x));

            StringBuilder lineText = new StringBuilder();

            int previousRight = -1;

            for (Word word : line.words) {

                String text = word.getText();

                if (text == null || text.isBlank()) {
                    continue;
                }

                text = text.trim();

                int x = word.getBoundingBox().x;

                int right = word.getBoundingBox().x + word.getBoundingBox().width;

                if (previousRight >= 0) {

                    int gap = x - previousRight;

                    /*
                     * Normal word spacing.
                     */
                    if (gap <= 12) {

                        lineText.append(" ");

                    }
                    /*
                     * Larger gap.
                     *
                     * This probably means we're moving
                     * into another table column.
                     */
                    else {

                        lineText.append("    ");
                    }
                }

                lineText.append(text);

                previousRight = right;
            }

            if (!lineText.isEmpty()) {

                result.append(lineText);
                result.append("\n");
            }
        }

        return result.toString();
    }

    // =========================================================
    // COMBINE OCR
    // =========================================================

    private String combineOCR(
            String normalOCR,
            String spatialOCR
    ) {

        if (spatialOCR != null && !spatialOCR.isBlank()) {
            return spatialOCR;
        }

        return normalOCR == null ? "" : normalOCR;
    }

    // =========================================================
    // IMAGE PREPROCESSING
    // =========================================================

    private BufferedImage createEnhancedImage(BufferedImage original) {

        /*
         * Upscale 3x.
         */
        int scale = 3;

        int width = original.getWidth() * scale;

        int height = original.getHeight() * scale;

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(original, 0, 0, width, height, null);

        graphics.dispose();

        // -----------------------------------------------------
        // Grayscale
        // -----------------------------------------------------

        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D grayGraphics = grayscale.createGraphics();

        grayGraphics.drawImage(resized, 0, 0, null);

        grayGraphics.dispose();

        // -----------------------------------------------------
        // Contrast
        // -----------------------------------------------------

        RescaleOp contrast = new RescaleOp(1.25f, -15f, null);

        return contrast.filter(grayscale, null);
    }

    // =========================================================
    // OCR LINE
    // =========================================================

    private static class OCRLine {

        private final List<Word> words = new ArrayList<>();

        void add(Word word) {

            words.add(word);
        }

        int averageY() {

            if (words.isEmpty()) {
                return 0;
            }

            int total = 0;

            for (Word word : words) {

                total += word.getBoundingBox().y;
            }

            return total / words.size();
        }
    }

    // =========================================================
    // DELETE TEMP FILE
    // =========================================================

    private void deleteFile(File file) {

        if (file != null && file.exists()) {
            file.delete();
        }
    }

    // =========================================================
    // FILE EXTENSION
    // =========================================================

    private String getExtension(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            return ".tmp";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}