package com.spring.mednemesis.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PDFService {

    // =========================================================
    // PAGE SETTINGS
    // =========================================================

    private static final float PAGE_WIDTH =
            PDRectangle.A4.getWidth();

    private static final float PAGE_HEIGHT =
            PDRectangle.A4.getHeight();

    private static final float MARGIN = 50;

    private static final float FOOTER_HEIGHT = 55;

    private static final float CONTENT_WIDTH =
            PAGE_WIDTH - (MARGIN * 2);

    private static final float CONTENT_BOTTOM =
            MARGIN + FOOTER_HEIGHT;

    // =========================================================
    // FONT SIZES
    // =========================================================

    private static final float TITLE_SIZE = 23;

    private static final float SECTION_SIZE = 15;

    private static final float SUBHEADING_SIZE = 11.5f;

    private static final float BODY_SIZE = 10.5f;

    private static final float LINE_HEIGHT = 15;

    // =========================================================
    // COLORS
    // =========================================================

    private static final Color PRIMARY =
            new Color(11, 61, 145);

    private static final Color TEXT =
            new Color(35, 35, 35);

    private static final Color MUTED =
            new Color(100, 100, 100);

    private static final Color ABNORMAL_BACKGROUND =
            new Color(220, 70, 70);

    private static final Color ABNORMAL_TEXT =
            new Color(150, 30, 30);

    private static final Color HEADING_TEXT =
            Color.WHITE;

    private static final Color SEPARATOR =
            new Color(200, 210, 220);

    // =========================================================
    // DEPENDENCIES
    // =========================================================

    private final PDFMarkdownParser markdownParser;

    private final PDFInlineMarkdownParser inlineMarkdownParser;

    public PDFService(
            PDFMarkdownParser markdownParser,
            PDFInlineMarkdownParser inlineMarkdownParser
    ) {

        this.markdownParser = markdownParser;
        this.inlineMarkdownParser = inlineMarkdownParser;
    }

    // =========================================================
    // GENERATE PDF
    // =========================================================

    public byte[] generatePDF(
            String analysis
    ) {

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PDFWriter writer =
                    new PDFWriter(
                            document,
                            inlineMarkdownParser
                    );

            writer.addPage();

            writer.writeHeader();

            List<PDFMarkdownParser.PDFBlock> blocks =
                    markdownParser.parse(analysis);

            String currentSection = "";

            for (PDFMarkdownParser.PDFBlock block : blocks) {

                switch (block.type()) {

                    case SECTION -> {

                        currentSection =
                                block.text();

                        writer.writeSectionHeading(
                                block.text(),
                                isAbnormalSection(
                                        currentSection
                                )
                        );
                    }

                    case SUBHEADING -> {

                        writer.writeSubHeading(
                                block.text()
                        );
                    }

                    case BULLET -> {

                        writer.writeBullet(
                                block.text(),
                                isAbnormalSection(
                                        currentSection
                                )
                        );
                    }

                    case PARAGRAPH -> {

                        writer.writeParagraph(
                                block.text(),
                                isAbnormalSection(
                                        currentSection
                                )
                        );
                    }

                    case SEPARATOR -> {

                        writer.writeSeparator();
                    }

                    case SPACING -> {

                        writer.addSpacing(7);
                    }
                }
            }

            writer.writeFooterOnCurrentPage();

            writer.closeCurrentStream();

            document.save(output);

            byte[] result = output.toByteArray();

            if (result.length == 0) {
                throw new PDFGenerationException(
                        "Generated PDF is empty."
                );
            }

            return result;

        } catch (PDFGenerationException e) {

            throw e;

        } catch (IOException | RuntimeException e) {

            throw new PDFGenerationException(
                    "Unable to generate the PDF report.",
                    e
            );
        }
    }

    // =========================================================
    // ABNORMAL SECTION DETECTION
    // =========================================================

    private boolean isAbnormalSection(
            String section
    ) {

        if (section == null) {
            return false;
        }

        String normalized =
                section.toLowerCase();

        return normalized.contains("abnormal")
                || normalized.contains("severity");
    }

    // =========================================================
    // PDF WRITER
    // =========================================================

    private static class PDFWriter {

        private final PDDocument document;

        private final PDFInlineMarkdownParser inlineMarkdownParser;

        private PDPage page;

        private PDPageContentStream stream;

        private float cursorY;

        private int pageNumber = 0;

        private final PDType1Font regular =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        private final PDType1Font bold =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

        PDFWriter(
                PDDocument document,
                PDFInlineMarkdownParser inlineMarkdownParser
        ) {

            this.document = document;

            this.inlineMarkdownParser =
                    inlineMarkdownParser;
        }

        // =====================================================
        // ADD PAGE
        // =====================================================

        void addPage()
                throws IOException {

            closeCurrentStream();

            page =
                    new PDPage(
                            PDRectangle.A4
                    );

            document.addPage(page);

            pageNumber++;

            stream =
                    new PDPageContentStream(
                            document,
                            page
                    );

            cursorY =
                    PAGE_HEIGHT - MARGIN;
        }

        // =====================================================
        // HEADER
        // =====================================================

        void writeHeader()
                throws IOException {

            ensureSpace(80);

            stream.beginText();

            stream.setFont(
                    bold,
                    TITLE_SIZE
            );

            stream.setNonStrokingColor(
                    PRIMARY
            );

            stream.newLineAtOffset(
                    MARGIN,
                    cursorY
            );

            stream.showText(
                    "MEDNEMESIS"
            );

            stream.endText();

            cursorY -= 27;

            stream.beginText();

            stream.setFont(
                    regular,
                    11
            );

            stream.setNonStrokingColor(
                    MUTED
            );

            stream.newLineAtOffset(
                    MARGIN,
                    cursorY
            );

            stream.showText(
                    "Medical Report Explanation"
            );

            stream.endText();

            cursorY -= 15;

            writeSeparator();

            cursorY -= 8;
        }

        // =====================================================
        // SECTION HEADING
        // =====================================================

        void writeSectionHeading(
                String text,
                boolean abnormal
        ) throws IOException {

            ensureSpace(55);

            cursorY -= 8;

            if (abnormal) {

                drawSectionBackground(
                        ABNORMAL_BACKGROUND
                );

            } else {

                drawSectionBackground(
                        PRIMARY
                );
            }

            stream.beginText();

            stream.setFont(
                    bold,
                    SECTION_SIZE
            );

            stream.setNonStrokingColor(
                    HEADING_TEXT
            );

            stream.newLineAtOffset(
                    MARGIN + 8,
                    cursorY
            );

            stream.showText(
                    cleanText(text)
            );

            stream.endText();

            cursorY -= 28;

            stream.setNonStrokingColor(
                    TEXT
            );
        }

        // =====================================================
        // SECTION BACKGROUND
        // =====================================================

        void drawSectionBackground(
                Color color
        ) throws IOException {

            stream.saveGraphicsState();

            stream.setNonStrokingColor(
                    color
            );

            stream.addRect(
                    MARGIN,
                    cursorY - 7,
                    CONTENT_WIDTH,
                    27
            );

            stream.fill();

            stream.restoreGraphicsState();
        }

        // =====================================================
        // SUBHEADING
        // =====================================================

        void writeSubHeading(
                String text
        ) throws IOException {

            ensureSpace(30);

            List<StyledPiece> pieces =
                    createStyledPieces(text);

            drawStyledPieces(
                    pieces,
                    MARGIN,
                    cursorY,
                    TEXT
            );

            cursorY -= 18;
        }

        // =====================================================
        // PARAGRAPH
        // =====================================================

        void writeParagraph(
                String text,
                boolean abnormal
        ) throws IOException {

            writeInlineText(
                    text,
                    abnormal,
                    MARGIN,
                    CONTENT_WIDTH
            );

            cursorY -= 5;
        }

        // =====================================================
        // BULLET
        // =====================================================

        void writeBullet(
                String text,
                boolean abnormal
        ) throws IOException {

            List<StyledPiece> pieces =
                    new ArrayList<>();

            pieces.add(
                    new StyledPiece(
                            "• ",
                            false,
                            false
                    )
            );

            pieces.addAll(
                    createStyledPieces(text)
            );

            writeStyledPiecesWrapped(
                    pieces,
                    abnormal,
                    MARGIN,
                    CONTENT_WIDTH,
                    14
            );

            cursorY -= 3;
        }

        // =====================================================
        // INLINE MARKDOWN
        // =====================================================

        void writeInlineText(
                String text,
                boolean abnormal,
                float x,
                float maxWidth
        ) throws IOException {

            List<StyledPiece> pieces =
                    createStyledPieces(text);

            writeStyledPiecesWrapped(
                    pieces,
                    abnormal,
                    x,
                    maxWidth,
                    0
            );
        }

        // =====================================================
        // CREATE STYLED PIECES
        // =====================================================

        List<StyledPiece> createStyledPieces(
                String text
        ) {

            List<StyledPiece> pieces =
                    new ArrayList<>();

            List<PDFInlineMarkdownParser.InlineText>
                    segments =
                    inlineMarkdownParser.parse(text);

            for (
                    PDFInlineMarkdownParser.InlineText segment
                    : segments
            ) {

                if (
                        segment.text() == null
                                || segment.text().isEmpty()
                ) {
                    continue;
                }

                pieces.add(
                        new StyledPiece(
                                segment.text(),
                                segment.bold(),
                                segment.italic()
                        )
                );
            }

            return pieces;
        }

        // =====================================================
        // WRAP + RENDER
        // =====================================================

        void writeStyledPiecesWrapped(
                List<StyledPiece> pieces,
                boolean abnormal,
                float x,
                float maxWidth,
                float continuationIndent
        ) throws IOException {

            List<StyledPiece> currentLine =
                    new ArrayList<>();

            float currentWidth = 0;

            float lineX = x;

            float lineMaxWidth = maxWidth;

            for (StyledPiece piece : pieces) {

                String[] words =
                        piece.text()
                                .split(
                                        "(?<=\\s)|(?=\\s)"
                                );

                for (String word : words) {

                    if (word.isEmpty()) {
                        continue;
                    }

                    PDType1Font font =
                            getFont(piece);

                    float wordWidth =
                            getTextWidth(
                                    word,
                                    font,
                                    BODY_SIZE
                            );

                    // If adding this word would exceed
                    // the available width, start a new line.
                    if (
                            currentWidth
                                    + wordWidth
                                    > lineMaxWidth
                                    && !currentLine.isEmpty()
                    ) {

                        ensureSpace(
                                LINE_HEIGHT + 5
                        );

                        drawStyledPieces(
                                currentLine,
                                lineX,
                                cursorY,
                                abnormal
                                        ? ABNORMAL_TEXT
                                        : TEXT
                        );

                        cursorY -= LINE_HEIGHT;

                        currentLine.clear();

                        currentWidth = 0;

                        if (
                                continuationIndent > 0
                        ) {

                            lineX =
                                    MARGIN
                                            + continuationIndent;

                            lineMaxWidth =
                                    CONTENT_WIDTH
                                            - continuationIndent;

                        } else {

                            lineX = x;

                            lineMaxWidth =
                                    maxWidth;
                        }
                    }

                    currentLine.add(
                            new StyledPiece(
                                    word,
                                    piece.bold(),
                                    piece.italic()
                            )
                    );

                    currentWidth += wordWidth;
                }
            }

            if (!currentLine.isEmpty()) {

                ensureSpace(
                        LINE_HEIGHT + 5
                );

                drawStyledPieces(
                        currentLine,
                        lineX,
                        cursorY,
                        abnormal
                                ? ABNORMAL_TEXT
                                : TEXT
                );

                cursorY -= LINE_HEIGHT;
            }
        }

        // =====================================================
        // DRAW STYLED PIECES
        // =====================================================

        void drawStyledPieces(
                List<StyledPiece> pieces,
                float x,
                float y,
                Color color
        ) throws IOException {

            float currentX = x;

            for (StyledPiece piece : pieces) {

                if (
                        piece.text() == null
                                || piece.text().isEmpty()
                ) {
                    continue;
                }

                PDType1Font font =
                        getFont(piece);

                /*
                 * IMPORTANT:
                 *
                 * Do NOT call trim() here.
                 *
                 * Whitespace is meaningful because the
                 * inline parser creates separate pieces such as:
                 *
                 * "Serum"
                 * " "
                 * "Glutamic"
                 * " "
                 * "Pyruvic"
                 *
                 * Trimming the " " piece would remove the
                 * space between words.
                 */

                String pieceText =
                        piece.text();

                stream.beginText();

                stream.setFont(
                        font,
                        BODY_SIZE
                );

                stream.setNonStrokingColor(
                        color
                );

                stream.newLineAtOffset(
                        currentX,
                        y
                );

                stream.showText(
                        pieceText
                );

                stream.endText();

                currentX +=
                        getTextWidth(
                                pieceText,
                                font,
                                BODY_SIZE
                        );
            }
        }

        // =====================================================
        // FONT SELECTION
        // =====================================================

        PDType1Font getFont(
                StyledPiece piece
        ) {

            if (piece.bold()) {

                return bold;
            }

            return regular;
        }

        // =====================================================
        // TEXT WIDTH
        // =====================================================

        float getTextWidth(
                String text,
                PDType1Font font,
                float fontSize
        ) {

            try {

                return font.getStringWidth(
                        text
                )
                        / 1000f
                        * fontSize;

            } catch (IOException e) {

                return text.length()
                        * fontSize
                        * 0.5f;
            }
        }

        // =====================================================
        // SEPARATOR
        // =====================================================

        void writeSeparator()
                throws IOException {

            ensureSpace(15);

            stream.saveGraphicsState();

            stream.setStrokingColor(
                    SEPARATOR
            );

            stream.setLineWidth(
                    0.7f
            );

            stream.moveTo(
                    MARGIN,
                    cursorY
            );

            stream.lineTo(
                    PAGE_WIDTH - MARGIN,
                    cursorY
            );

            stream.stroke();

            stream.restoreGraphicsState();

            cursorY -= 10;
        }

        // =====================================================
        // SPACING
        // =====================================================

        void addSpacing(
                float spacing
        ) throws IOException {

            cursorY -= spacing;

            if (
                    cursorY < CONTENT_BOTTOM
            ) {

                startNewPage();
            }
        }

        // =====================================================
        // PAGE SPACE CHECK
        // =====================================================

        void ensureSpace(
                float required
        ) throws IOException {

            if (
                    cursorY - required
                            < CONTENT_BOTTOM
            ) {

                startNewPage();
            }
        }

        // =====================================================
        // NEW PAGE
        // =====================================================

        void startNewPage()
                throws IOException {

            if (stream != null) {

                writeFooterOnCurrentPage();

                closeCurrentStream();
            }

            addPage();
        }

        // =====================================================
        // FOOTER
        // =====================================================

        void writeFooterOnCurrentPage()
                throws IOException {

            if (stream == null) {
                return;
            }

            // Footer separator

            stream.saveGraphicsState();

            stream.setStrokingColor(
                    SEPARATOR
            );

            stream.setLineWidth(
                    0.5f
            );

            stream.moveTo(
                    MARGIN,
                    42
            );

            stream.lineTo(
                    PAGE_WIDTH - MARGIN,
                    42
            );

            stream.stroke();

            stream.restoreGraphicsState();

            // Footer text

            stream.beginText();

            stream.setFont(
                    regular,
                    8
            );

            stream.setNonStrokingColor(
                    MUTED
            );

            stream.newLineAtOffset(
                    MARGIN,
                    28
            );

            stream.showText(
                    "MedNemesis • Educational report explanation"
            );

            stream.endText();

            // Page number

            String pageText =
                    "Page " + pageNumber;

            float width =
                    getTextWidth(
                            pageText,
                            regular,
                            8
                    );

            stream.beginText();

            stream.setFont(
                    regular,
                    8
            );

            stream.setNonStrokingColor(
                    MUTED
            );

            stream.newLineAtOffset(
                    PAGE_WIDTH
                            - MARGIN
                            - width,
                    28
            );

            stream.showText(
                    pageText
            );

            stream.endText();
        }

        // =====================================================
        // CLOSE STREAM
        // =====================================================

        void closeCurrentStream()
                throws IOException {

            if (stream != null) {

                stream.close();

                stream = null;
            }
        }

        // =====================================================
        // CLEAN TEXT
        // =====================================================

        String cleanText(
                String text
        ) {

            if (text == null) {
                return "";
            }

            return text
                    .replace("\r", "")
                    .replace("\n", "")
                    .trim();
        }

        // =====================================================
        // STYLED PIECE
        // =====================================================

        private record StyledPiece(
                String text,
                boolean bold,
                boolean italic
        ) {
        }
    }
}