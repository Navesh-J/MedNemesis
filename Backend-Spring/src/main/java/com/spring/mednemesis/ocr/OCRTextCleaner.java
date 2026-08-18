package com.spring.mednemesis.ocr;

import org.springframework.stereotype.Service;

@Service
public class OCRTextCleaner {

    public String clean(String text) {

        if(text == null || text.isBlank()) {
            return "";
        }

        String cleaned = text;

        // Normalize Windows / Mac line endings
        cleaned = cleaned.replace("\r\n","\n");
        cleaned = cleaned.replace("\r","\n");

        //Remove non-printable control characters, while keeping newlines and tabs.
        cleaned = cleaned.replaceAll("[^\\x20-\\x7E\\n\\t]", "");

        // Normalize tabs
        cleaned = cleaned.replaceAll("\\t+", " ");

        //Remove spaces at the beginning/end of lines
        cleaned = cleaned.replaceAll("(?m)^\\s+", "");
        cleaned = cleaned.replaceAll("(?m)\\s+$", "");

        // Collapse 3+ consecutive blank lines into one blank line
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        return cleaned.trim();
    }
}
