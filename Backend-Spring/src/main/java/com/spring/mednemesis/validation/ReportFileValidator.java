package com.spring.mednemesis.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class ReportFileValidator {

    private static final int MAX_FILES = 10;

    private static final long MAX_FILE_SIZE =
            10L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "pdf"
            );

    public void validate(MultipartFile[] files) {

        // =====================================================
        // FILE LIST
        // =====================================================

        if (files == null || files.length == 0) {

            throw new IllegalArgumentException(
                    "At least one report file is required."
            );
        }

        if (files.length > MAX_FILES) {

            throw new IllegalArgumentException(
                    "Maximum " + MAX_FILES +
                            " files are allowed per request."
            );
        }

        // =====================================================
        // EACH FILE
        // =====================================================

        for (MultipartFile file : files) {

            if (file == null) {

                throw new IllegalArgumentException(
                        "Invalid file received."
                );
            }

            if (file.isEmpty()) {

                throw new IllegalArgumentException(
                        getFileName(file) +
                                ": File is empty."
                );
            }

            if (file.getSize() > MAX_FILE_SIZE) {

                throw new IllegalArgumentException(
                        getFileName(file) +
                                ": File size must not exceed 10 MB."
                );
            }

            validateFileType(file);
        }
    }

    // =========================================================
    // FILE TYPE
    // =========================================================

    private void validateFileType(
            MultipartFile file
    ) {

        String filename =
                file.getOriginalFilename();

        if (
                filename == null ||
                        filename.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Uploaded file has no valid filename."
            );
        }

        String extension =
                getExtension(filename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {

            throw new IllegalArgumentException(
                    getFileName(file) +
                            ": Only JPG, JPEG, PNG and PDF files are supported."
            );
        }

        /*
         * The filename extension is used as the primary check.
         *
         * Content type is intentionally not required here because browsers
         * and clients can send different MIME values for otherwise valid
         * files.
         */
    }

    // =========================================================
    // EXTENSION
    // =========================================================

    private String getExtension(
            String filename
    ) {

        int dotIndex =
                filename.lastIndexOf('.');

        if (
                dotIndex < 0 ||
                        dotIndex == filename.length() - 1
        ) {

            return "";
        }

        return filename
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    // =========================================================
    // SAFE DISPLAY NAME
    // =========================================================

    private String getFileName(
            MultipartFile file
    ) {

        if (
                file == null ||
                        file.getOriginalFilename() == null ||
                        file.getOriginalFilename()
                                .isBlank()
        ) {

            return "Uploaded file";
        }

        return file
                .getOriginalFilename()
                .replace("\\", "_")
                .replace("/", "_");
    }
}