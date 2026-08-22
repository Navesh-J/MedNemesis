package com.spring.mednemesis.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
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

    /*
     * PDF rendering resolution for scanned PDFs.
     */
    private static final float PDF_DPI = 200f;

    /*
     * Minimum amount of useful text required for a PDF
     * to be considered a text-based PDF.
     *
     * If PDFBox extracts less than this, we assume the
     * PDF is scanned/image-based and fall back to OCR.
     */
    private static final int MIN_PDF_TEXT_LENGTH = 100;

    private final OCRTextCleaner textCleaner;

    public OCRService(OCRTextCleaner textCleaner) {
        this.textCleaner = textCleaner;
    }

    // =========================================================
    // MAIN ENTRY POINT
    // =========================================================

    public String extractText(
            MultipartFile file
    ) throws IOException, TesseractException {

        if (file == null || file.isEmpty()) {
            return "";
        }

        if (isPDF(file)) {
            return extractTextFromPDF(file);
        }

        return extractTextFromImage(file);
    }

    // =========================================================
    // IMAGE OCR
    // =========================================================

    private String extractTextFromImage(
            MultipartFile file
    ) throws IOException, TesseractException {

        File tempFile =
                File.createTempFile(
                        "mednemesis-image-",
                        getExtension(file)
                );

        File enhancedFile =
                File.createTempFile(
                        "mednemesis-enhanced-",
                        ".png"
                );

        try {

            file.transferTo(tempFile);

            BufferedImage original =
                    ImageIO.read(tempFile);


            if (original == null) {

                String rawText =
                        runNormalOCR(
                                createTesseract(),
                                tempFile
                        );

                return textCleaner.clean(rawText);
            }

            return processImage(
                    original,
                    enhancedFile
            );

        } finally {

            deleteFile(tempFile);
            deleteFile(enhancedFile);
        }
    }

    // =========================================================
    // PDF HANDLING
    // =========================================================

    private String extractTextFromPDF(
            MultipartFile file
    ) throws IOException, TesseractException {

        File pdfFile =
                File.createTempFile(
                        "mednemesis-pdf-",
                        ".pdf"
                );

        try {

            file.transferTo(pdfFile);


            try (PDDocument document =
                         Loader.loadPDF(pdfFile)) {


                String extractedText =
                        extractEmbeddedPDFText(document);

                if (hasEnoughText(extractedText)) {

                    return textCleaner.clean(
                            extractedText
                    );
                }

                return extractScannedPDFText(
                        document
                );
            }

        } finally {

            deleteFile(pdfFile);
        }
    }

    // =========================================================
    // TEXT-BASED PDF
    // =========================================================

    private String extractEmbeddedPDFText(
            PDDocument document
    ) throws IOException {

        PDFTextStripper stripper =
                new PDFTextStripper();

        /*
         * Preserve the natural page order.
         */
        stripper.setSortByPosition(true);

        return stripper.getText(document);
    }

    // =========================================================
    // CHECK PDF TEXT
    // =========================================================

    private boolean hasEnoughText(
            String text
    ) {

        if (text == null) {
            return false;
        }

        String cleaned =
                text
                        .replaceAll("\\s+", " ")
                        .trim();

        return cleaned.length()
                >= MIN_PDF_TEXT_LENGTH;
    }

    // =========================================================
    // SCANNED PDF OCR
    // =========================================================

    private String extractScannedPDFText(
            PDDocument document
    ) throws IOException, TesseractException {

        PDFRenderer renderer =
                new PDFRenderer(document);

        /*
         * Helps reduce memory consumption for PDFs
         * containing very large embedded images.
         */
        renderer.setSubsamplingAllowed(true);

        StringBuilder combinedText =
                new StringBuilder();

        int pageCount =
                document.getNumberOfPages();

        for (
                int pageIndex = 0;
                pageIndex < pageCount;
                pageIndex++
        ) {

            BufferedImage pageImage =
                    renderer.renderImageWithDPI(
                            pageIndex,
                            PDF_DPI,
                            ImageType.RGB
                    );

            try {

                String pageText =
                        processPDFPage(
                                pageImage
                        );

                if (
                        pageText != null
                                && !pageText.isBlank()
                ) {

                    if (!combinedText.isEmpty()) {
                        combinedText.append("\n\n");
                    }

                    combinedText
                            .append("===== PAGE ")
                            .append(pageIndex + 1)
                            .append(" =====\n\n");

                    combinedText.append(pageText);
                }

            } finally {

                pageImage.flush();
            }
        }

        return textCleaner.clean(
                combinedText.toString()
        );
    }

    // =========================================================
    // PROCESS IMAGE
    // =========================================================

    private String processImage(
            BufferedImage original,
            File enhancedFile
    ) throws IOException, TesseractException {

        BufferedImage enhanced =
                createEnhancedImage(original);

        try {

            ImageIO.write(
                    enhanced,
                    "png",
                    enhancedFile
            );

            /*
             * Standard Tesseract OCR.
             */
            String normalOCR =
                    runNormalOCR(
                            createTesseract(),
                            enhancedFile
                    );

            /*
             * Spatial OCR using Tess4J Word objects.
             */
            String spatialOCR =
                    runSpatialOCR(
                            createTesseract(),
                            enhanced
                    );

            /*
             * Keep the working behavior:
             * prefer spatial OCR when available.
             */
            String combinedOCR =
                    combineOCR(
                            normalOCR,
                            spatialOCR
                    );

            return textCleaner.clean(
                    combinedOCR
            );

        } finally {

            enhanced.flush();
        }
    }

    // =========================================================
    // PROCESS SCANNED PDF PAGE
    // =========================================================

    private String processPDFPage(
            BufferedImage pageImage
    ) throws IOException, TesseractException {

        BufferedImage enhanced =
                createEnhancedImage(pageImage);

        File enhancedFile =
                File.createTempFile(
                        "mednemesis-pdf-page-",
                        ".png"
                );

        try {

            ImageIO.write(
                    enhanced,
                    "png",
                    enhancedFile
            );

            String normalOCR =
                    runNormalOCR(
                            createTesseract(),
                            enhancedFile
                    );

            String spatialOCR =
                    runSpatialOCR(
                            createTesseract(),
                            enhanced
                    );

            return combineOCR(
                    normalOCR,
                    spatialOCR
            );

        } finally {

            enhanced.flush();
            deleteFile(enhancedFile);
        }
    }

    // =========================================================
    // NORMAL TESSERACT OCR
    // =========================================================

    private String runNormalOCR(
            Tesseract tesseract,
            File imageFile
    ) throws TesseractException {


        tesseract.setPageSegMode(6);

        return tesseract.doOCR(imageFile);
    }

    // =========================================================
    // SPATIAL OCR
    // =========================================================

    private String runSpatialOCR(
            Tesseract tesseract,
            BufferedImage image
    ) throws TesseractException {

        /*
         * Keep the exact Tess4J approach that is currently
         * working in your project.
         */
        List<BufferedImage> images =
                List.of(image);

        List<Word> words =
                tesseract.getWords(
                        images,
                        3
                );

        return reconstructLines(words);
    }

    // =========================================================
    // TESSERACT CONFIGURATION
    // =========================================================

    private Tesseract createTesseract()
            throws IOException {

        ClassPathResource tessdataResource =
                new ClassPathResource("tessdata");

        File tessdataFolder =
                tessdataResource.getFile();

        Tesseract tesseract =
                new Tesseract();

        tesseract.setDatapath(
                tessdataFolder.getAbsolutePath()
        );

        tesseract.setLanguage("eng");

        /*
         * Preserve spaces between table columns.
         */
        tesseract.setVariable(
                "preserve_interword_spaces",
                "1"
        );

        /*
         * Tell Tesseract the image resolution.
         */
        tesseract.setVariable(
                "user_defined_dpi",
                "300"
        );

        return tesseract;
    }

    // =========================================================
    // RECONSTRUCT OCR LINES
    // =========================================================

    private String reconstructLines(
            List<Word> words
    ) {

        if (
                words == null
                        || words.isEmpty()
        ) {
            return "";
        }

        List<Word> filteredWords =
                new ArrayList<>();

        for (Word word : words) {

            if (word == null) {
                continue;
            }

            String text =
                    word.getText();

            if (
                    text == null
                            || text.isBlank()
            ) {
                continue;
            }

            /*
             * Ignore very low-confidence words.
             */
            if (word.getConfidence() < 25) {
                continue;
            }

            filteredWords.add(word);
        }

        /*
         * Sort:
         *
         * top → bottom
         * left → right
         */
        filteredWords.sort(
                (word1, word2) -> {

                    int yDifference =
                            Integer.compare(
                                    word1.getBoundingBox().y,
                                    word2.getBoundingBox().y
                            );

                    if (yDifference != 0) {
                        return yDifference;
                    }

                    return Integer.compare(
                            word1.getBoundingBox().x,
                            word2.getBoundingBox().x
                    );
                }
        );

        List<OCRLine> lines =
                new ArrayList<>();

        /*
         * Group words that belong to the same
         * horizontal line.
         */
        for (Word word : filteredWords) {

            int wordY =
                    word.getBoundingBox().y;

            OCRLine matchingLine =
                    null;

            for (OCRLine line : lines) {

                if (
                        Math.abs(
                                line.averageY()
                                        - wordY
                        ) <= 18
                ) {

                    matchingLine = line;
                    break;
                }
            }

            if (matchingLine == null) {

                matchingLine =
                        new OCRLine();

                lines.add(
                        matchingLine
                );
            }

            matchingLine.add(word);
        }

        /*
         * Sort lines vertically.
         */
        lines.sort(
                (line1, line2) ->
                        Integer.compare(
                                line1.averageY(),
                                line2.averageY()
                        )
        );

        StringBuilder result =
                new StringBuilder();

        /*
         * Build each line from left → right.
         */
        for (OCRLine line : lines) {

            line.words.sort(
                    (word1, word2) ->
                            Integer.compare(
                                    word1.getBoundingBox().x,
                                    word2.getBoundingBox().x
                            )
            );

            StringBuilder lineText =
                    new StringBuilder();

            int previousRight = -1;

            for (Word word : line.words) {

                String text =
                        word.getText();

                if (
                        text == null
                                || text.isBlank()
                ) {
                    continue;
                }

                text = text.trim();

                int x =
                        word.getBoundingBox().x;

                int right =
                        word.getBoundingBox().x
                                + word.getBoundingBox().width;

                if (previousRight >= 0) {

                    int gap =
                            x - previousRight;

                    /*
                     * Normal word spacing.
                     */
                    if (gap <= 12) {

                        lineText.append(" ");

                    } else {

                        /*
                         * Larger gap usually indicates
                         * another table column.
                         */
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

        /*
         * Spatial OCR is currently the preferred output
         * because it preserves report/table structure better.
         */
        if (
                spatialOCR != null
                        && !spatialOCR.isBlank()
        ) {

            return spatialOCR;
        }

        return normalOCR == null
                ? ""
                : normalOCR;
    }

    // =========================================================
    // IMAGE PREPROCESSING
    // =========================================================

    private BufferedImage createEnhancedImage(
            BufferedImage original
    ) {

        /*
         * Upscale image 3x.
         */
        int scale = 3;

        int width =
                original.getWidth()
                        * scale;

        int height =
                original.getHeight()
                        * scale;

        BufferedImage resized =
                new BufferedImage(
                        width,
                        height,
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
                width,
                height,
                null
        );

        graphics.dispose();

        /*
         * Convert to grayscale.
         */
        BufferedImage grayscale =
                new BufferedImage(
                        width,
                        height,
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

        resized.flush();

        /*
         * Contrast enhancement.
         */
        RescaleOp contrast =
                new RescaleOp(
                        1.25f,
                        -15f,
                        null
                );

        return contrast.filter(
                grayscale,
                null
        );
    }

    // =========================================================
    // OCR LINE MODEL
    // =========================================================

    private static class OCRLine {

        private final List<Word> words =
                new ArrayList<>();

        void add(Word word) {
            words.add(word);
        }

        int averageY() {

            if (words.isEmpty()) {
                return 0;
            }

            int total = 0;

            for (Word word : words) {

                total +=
                        word.getBoundingBox().y;
            }

            return total / words.size();
        }
    }

    // =========================================================
    // PDF DETECTION
    // =========================================================

    private boolean isPDF(
            MultipartFile file
    ) {

        String contentType =
                file.getContentType();

        if (
                "application/pdf"
                        .equalsIgnoreCase(contentType)
        ) {
            return true;
        }

        String filename =
                file.getOriginalFilename();

        return filename != null
                && filename
                .toLowerCase()
                .endsWith(".pdf");
    }

    // =========================================================
    // TEMP FILE CLEANUP
    // =========================================================

    private void deleteFile(File file) {

        if (
                file != null
                        && file.exists()
        ) {

            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
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
                filename == null
                        || !filename.contains(".")
        ) {
            return ".tmp";
        }

        return filename.substring(
                filename.lastIndexOf(".")
        );
    }
}