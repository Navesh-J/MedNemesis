package com.spring.mednemesis.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PDFService {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

    private static final float MARGIN = 50;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);

    private static final float TITLE_SIZE = 22;
    private static final float SECTION_SIZE = 15;
    private static final float BODY_SIZE = 10.5f;

    public byte[] generatePDF(String analysis) throws IOException {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDFWriter writer = new PDFWriter(document);

            writer.addPage();

            // Header
            writer.writeTitle("MEDNEMESIS");
            writer.writeSubtitle("Medical Report Explanation");

            writer.addSpacing(20);

            // Parse Gemini Markdown
            String[] lines = analysis.split("\\R");

            for (String line : lines) {

                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    writer.addSpacing(8);
                    continue;
                }

                // Main numbered headings
                if (trimmed.matches("^#\\s+\\d+\\..*")) {

                    String heading = trimmed
                            .replaceFirst("^#\\s+", "")
                            .trim();

                    writer.writeSectionHeading(heading);
                    continue;
                }

                // Horizontal separator
                if (trimmed.equals("---")) {
                    writer.writeSeparator();
                    continue;
                }

                // Bullet point
                if (trimmed.startsWith("- ") ||
                        trimmed.startsWith("* ")) {

                    String bullet = trimmed.substring(2).trim();

                    writer.writeBullet(bullet);
                    continue;
                }

                // Bold-only / bold label
                if (trimmed.startsWith("**") && trimmed.endsWith("**")) {

                    String heading = trimmed
                            .replace("**", "")
                            .trim();

                    writer.writeSubHeading(heading);
                    continue;
                }

                // Normal paragraph
                writer.writeParagraph(trimmed);
            }

            writer.closeCurrentStream();

            document.save(output);

            return output.toByteArray();
        }
    }

    private static class PDFWriter {

        private final PDDocument document;

        private PDPage page;
        private PDPageContentStream stream;

        private float cursorY;

        private final PDType1Font regular =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        private final PDType1Font bold =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        PDFWriter(PDDocument document) {
            this.document = document;
        }

        void addPage() throws IOException {

            closeCurrentStream();

            page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            stream = new PDPageContentStream(document, page);

            cursorY = PAGE_HEIGHT - MARGIN;
        }

        void closeCurrentStream() throws IOException {

            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        void writeTitle(String text) throws IOException {

            ensureSpace(40);

            stream.beginText();

            stream.setFont(bold, TITLE_SIZE);

            stream.newLineAtOffset(
                    MARGIN,
                    cursorY
            );

            stream.showText(text);

            stream.endText();

            cursorY -= 28;
        }

        void writeSubtitle(String text) throws IOException {

            ensureSpace(25);

            stream.beginText();

            stream.setFont(regular, 12);

            stream.newLineAtOffset(
                    MARGIN,
                    cursorY
            );

            stream.showText(text);

            stream.endText();

            cursorY -= 22;
        }

        void writeSectionHeading(String text) throws IOException {

            ensureSpace(55);

            cursorY -= 10;

            stream.beginText();

            stream.setFont(bold, SECTION_SIZE);

            stream.newLineAtOffset(
                    MARGIN,
                    cursorY
            );

            stream.showText(cleanText(text));

            stream.endText();

            cursorY -= 8;

            writeSeparator();

            cursorY -= 8;
        }

        void writeSubHeading(String text) throws IOException {

            ensureSpace(30);

            stream.beginText();

            stream.setFont(bold, 11.5f);

            stream.newLineAtOffset(
                    MARGIN,
                    cursorY
            );

            stream.showText(cleanText(text));

            stream.endText();

            cursorY -= 18;
        }

        void writeParagraph(String text) throws IOException {

            List<String> lines = wrapText(
                    cleanText(text),
                    regular,
                    BODY_SIZE,
                    CONTENT_WIDTH
            );

            for (String line : lines) {

                ensureSpace(18);

                stream.beginText();

                stream.setFont(regular, BODY_SIZE);

                stream.newLineAtOffset(
                        MARGIN,
                        cursorY
                );

                stream.showText(line);

                stream.endText();

                cursorY -= 15;
            }

            cursorY -= 5;
        }

        void writeBullet(String text) throws IOException {

            String cleaned = cleanText(text);

            List<String> lines = wrapText(
                    cleaned,
                    regular,
                    BODY_SIZE,
                    CONTENT_WIDTH - 18
            );

            for (int i = 0; i < lines.size(); i++) {

                ensureSpace(18);

                stream.beginText();

                stream.setFont(
                        regular,
                        BODY_SIZE
                );

                float x = MARGIN;

                if (i == 0) {

                    stream.newLineAtOffset(
                            x,
                            cursorY
                    );

                    stream.showText("• " + lines.get(i));

                } else {

                    stream.newLineAtOffset(
                            x + 12,
                            cursorY
                    );

                    stream.showText(lines.get(i));
                }

                stream.endText();

                cursorY -= 15;
            }

            cursorY -= 3;
        }

        void writeSeparator() throws IOException {

            ensureSpace(15);

            stream.setLineWidth(0.7f);

            stream.moveTo(
                    MARGIN,
                    cursorY
            );

            stream.lineTo(
                    PAGE_WIDTH - MARGIN,
                    cursorY
            );

            stream.stroke();

            cursorY -= 10;
        }

        void addSpacing(float spacing) throws IOException {

            cursorY -= spacing;

            if (cursorY < MARGIN + 20) {
                addPage();
            }
        }

        void ensureSpace(float required) throws IOException {

            if (cursorY - required < MARGIN) {
                addPage();
            }
        }

        String cleanText(String text) {

            return text
                    .replace("**", "")
                    .replace("__", "")
                    .replace("*", "")
                    .replace("`", "")
                    .trim();
        }

        List<String> wrapText(
                String text,
                PDType1Font font,
                float fontSize,
                float maxWidth
        ) {

            List<String> lines = new ArrayList<>();

            if (text.isBlank()) {
                return lines;
            }

            String[] words = text.split("\\s+");

            StringBuilder currentLine =
                    new StringBuilder();

            for (String word : words) {

                String testLine;

                if (currentLine.isEmpty()) {
                    testLine = word;
                } else {
                    testLine =
                            currentLine + " " + word;
                }

                float width;

                try {

                    width = font.getStringWidth(testLine)
                            / 1000f
                            * fontSize;

                } catch (IOException e) {

                    width = testLine.length()
                            * fontSize
                            * 0.5f;
                }

                if (width <= maxWidth) {

                    currentLine = new StringBuilder(testLine);

                } else {

                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                    }

                    currentLine =
                            new StringBuilder(word);
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }

            return lines;
        }
    }
}