package com.spring.mednemesis.pdf;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PDFInlineMarkdownParser {

    public List<InlineText> parse(String text) {

        List<InlineText> result = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return result;
        }

        int i = 0;

        while (i < text.length()) {

            // Bold: **text**
            if (text.startsWith("**", i)) {

                int end = text.indexOf("**", i + 2);

                if (end != -1) {

                    result.add(
                            new InlineText(
                                    text.substring(i + 2, end),
                                    true,
                                    false
                            )
                    );

                    i = end + 2;
                    continue;
                }
            }

            // Italic: *text*
            if (text.charAt(i) == '*') {

                int end = text.indexOf("*", i + 1);

                if (end != -1) {

                    result.add(
                            new InlineText(
                                    text.substring(i + 1, end),
                                    false,
                                    true
                            )
                    );

                    i = end + 1;
                    continue;
                }
            }

            // Inline code: `text`
            if (text.charAt(i) == '`') {

                int end = text.indexOf("`", i + 1);

                if (end != -1) {

                    result.add(
                            new InlineText(
                                    text.substring(i + 1, end),
                                    false,
                                    false
                            )
                    );

                    i = end + 1;
                    continue;
                }
            }

            // Normal text
            int nextSpecial = findNextSpecialCharacter(
                    text,
                    i
            );

            if (nextSpecial == -1) {

                result.add(
                        new InlineText(
                                text.substring(i),
                                false,
                                false
                        )
                );

                break;
            }

            if (nextSpecial > i) {

                result.add(
                        new InlineText(
                                text.substring(
                                        i,
                                        nextSpecial
                                ),
                                false,
                                false
                        )
                );
            }

            i = nextSpecial;
        }

        return result;
    }

    private int findNextSpecialCharacter(
            String text,
            int start
    ) {

        for (int i = start; i < text.length(); i++) {

            char c = text.charAt(i);

            if (c == '*' || c == '`') {
                return i;
            }
        }

        return -1;
    }

    public record InlineText(
            String text,
            boolean bold,
            boolean italic
    ) {
    }
}