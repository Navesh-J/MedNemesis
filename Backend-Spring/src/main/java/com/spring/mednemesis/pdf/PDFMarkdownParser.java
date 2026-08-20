package com.spring.mednemesis.pdf;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PDFMarkdownParser {

    public List<PDFBlock> parse(String markdown) {

        List<PDFBlock> blocks = new ArrayList<>();

        if (markdown == null || markdown.isBlank()) {
            return blocks;
        }

        String[] lines = markdown.split("\\R");

        for (String rawLine : lines) {

            String line = rawLine.trim();

            // Empty line
            if (line.isEmpty()) {

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.SPACING,
                                ""
                        )
                );

                continue;
            }

            // Markdown headings
            // # Heading
            // ## Heading
            if (line.matches("^#{1,6}\\s+.*")) {

                String heading =
                        line.replaceFirst(
                                "^#{1,6}\\s+",
                                ""
                        );

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.SECTION,
                                cleanMarkdown(heading)
                        )
                );

                continue;
            }

            // Numbered section headings
            // 1. Patient Information
            // 2. Report Type
            // ...
            if (line.matches("^\\d+\\.\\s+.+")) {

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.SECTION,
                                cleanMarkdown(line)
                        )
                );

                continue;
            }

            // Horizontal separators
            // ---
            // ***
            // ___
            if (
                    line.matches("^-{3,}$")
                            || line.matches("^\\*{3,}$")
                            || line.matches("^_{3,}$")
            ) {

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.SEPARATOR,
                                ""
                        )
                );

                continue;
            }

            // Bullet points
            // - Something
            // * Something
            // + Something
            if (
                    line.startsWith("- ")
                            || line.startsWith("* ")
                            || line.startsWith("+ ")
            ) {

                String bullet =
                        line.substring(2)
                                .trim();

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.BULLET,
                                cleanMarkdown(bullet)
                        )
                );

                continue;
            }

            // Numbered list items
            // 1) Something
            // 2) Something
            if (line.matches("^\\d+[\\)]\\s+.+")) {

                String numberedItem =
                        line.replaceFirst(
                                "^\\d+[\\)]\\s+",
                                ""
                        );

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.BULLET,
                                cleanMarkdown(numberedItem)
                        )
                );

                continue;
            }

            // Bold-only line
            // **Simple Explanation**
            if (
                    line.startsWith("**")
                            && line.endsWith("**")
                            && line.length() > 4
            ) {

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.SUBHEADING,
                                cleanMarkdown(line)
                        )
                );

                continue;
            }

            // Italic-only line
            // *Explanation*
            if (
                    line.startsWith("*")
                            && line.endsWith("*")
                            && !line.startsWith("**")
            ) {

                blocks.add(
                        new PDFBlock(
                                PDFBlockType.SUBHEADING,
                                cleanMarkdown(line)
                        )
                );

                continue;
            }

            // Normal paragraph
            blocks.add(
                    new PDFBlock(
                            PDFBlockType.PARAGRAPH,
                            cleanMarkdown(line)
                    )
            );
        }

        return blocks;
    }

    // =========================================================
    // MARKDOWN CLEANING
    // =========================================================

    private String cleanMarkdown(String text) {

        if (text == null) {
            return "";
        }

        return text.trim();

    }

    // =========================================================
    // PDF BLOCK
    // =========================================================

    public record PDFBlock(
            PDFBlockType type,
            String text
    ) {
    }

    // =========================================================
    // BLOCK TYPES
    // =========================================================

    public enum PDFBlockType {

        SECTION,

        SUBHEADING,

        BULLET,

        PARAGRAPH,

        SEPARATOR,

        SPACING
    }
}