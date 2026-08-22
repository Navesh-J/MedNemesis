import { useCallback, useRef, useState } from "react";
import {
  FileImage,
  FileText,
  UploadCloud,
  X,
  Plus,
  LoaderCircle,
} from "lucide-react";

import { analyzeReport } from "../../services/reportService";

const MAX_FILES = 10;
const MAX_FILE_SIZE = 10 * 1024 * 1024;

function UploadZone({ onAnalysisComplete }) {
  const [files, setFiles] = useState([]);
  const [isDragging, setIsDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fileInputRef = useRef(null);

  // =========================================================
  // VALIDATE FILE
  // =========================================================

  const validateFile = (file) => {
    if (!file) {
      return "Invalid file.";
    }

    const isImage = file.type.startsWith("image/");
    const isPDF = file.type === "application/pdf";

    if (!isImage && !isPDF) {
      return `${file.name}: Only image and PDF files are supported.`;
    }

    if (file.size > MAX_FILE_SIZE) {
      return `${file.name}: File size must be less than 10 MB.`;
    }

    return null;
  };

  // =========================================================
  // ADD FILES
  // =========================================================

  const addFiles = useCallback((selectedFiles) => {
    const incomingFiles = Array.from(selectedFiles || []);

    if (incomingFiles.length === 0) {
      return;
    }

    setFiles((currentFiles) => {
      const availableSlots = MAX_FILES - currentFiles.length;

      if (availableSlots <= 0) {
        setError(`You can upload a maximum of ${MAX_FILES} pages.`);

        return currentFiles;
      }

      const filesToAdd = [];

      for (const file of incomingFiles) {
        if (filesToAdd.length >= availableSlots) {
          break;
        }

        const validationError = validateFile(file);

        if (validationError) {
          setError(validationError);
          continue;
        }

        const duplicate =
          currentFiles.some(
            (existingFile) =>
              existingFile.name === file.name &&
              existingFile.size === file.size &&
              existingFile.lastModified === file.lastModified,
          ) ||
          filesToAdd.some(
            (existingFile) =>
              existingFile.name === file.name &&
              existingFile.size === file.size &&
              existingFile.lastModified === file.lastModified,
          );

        if (duplicate) {
          continue;
        }

        filesToAdd.push(file);
      }

      return [...currentFiles, ...filesToAdd];
    });
  }, []);

  // =========================================================
  // FILE SELECT
  // =========================================================

  const handleFileSelect = (event) => {
    addFiles(event.target.files);

    event.target.value = "";
  };

  // =========================================================
  // DROP
  // =========================================================

  const handleDrop = (event) => {
    event.preventDefault();
    event.stopPropagation();

    setIsDragging(false);

    addFiles(event.dataTransfer.files);
  };

  // =========================================================
  // REMOVE
  // =========================================================

  const removeFile = (index) => {
    setFiles((currentFiles) =>
      currentFiles.filter((_, fileIndex) => fileIndex !== index),
    );
  };

  // =========================================================
  // CLEAR
  // =========================================================

  const clearFiles = () => {
    setFiles([]);
    setError("");
  };

  // =========================================================
  // OPEN FILE DIALOG
  // =========================================================

  const openFileDialog = () => {
    if (loading) return;

    fileInputRef.current?.click();
  };

  // =========================================================
  // ANALYZE
  // =========================================================

  const handleAnalyze = async () => {
    if (files.length === 0 || loading) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const result = await analyzeReport(files);

      if (onAnalysisComplete) {
        onAnalysisComplete(result);
      }
    } catch (err) {
      console.error("Report analysis failed:", err);

      setError(
        err.message || "Something went wrong while analyzing the report.",
      );
    } finally {
      setLoading(false);
    }
  };

  const isImage = (file) => file.type.startsWith("image/");

  const isPDF = (file) => file.type === "application/pdf";

  return (
    <div className="w-full max-w-4xl">
      {/* =====================================================
          UPLOAD AREA
      ====================================================== */}

      <button
        type="button"
        disabled={loading}
        onClick={openFileDialog}
        onDragOver={(event) => {
          event.preventDefault();

          if (!loading) {
            setIsDragging(true);
          }
        }}
        onDragLeave={(event) => {
          event.preventDefault();
          setIsDragging(false);
        }}
        onDrop={handleDrop}
        className={`
          group flex w-full flex-col items-center justify-center
          rounded-3xl border-2 border-dashed
          px-8 py-8
          transition-all duration-300
          ${
            isDragging
              ? "border-cyan-400 bg-cyan-400/10 shadow-[0_0_40px_rgba(34,211,238,0.12)]"
              : "border-slate-700 bg-slate-900/60 hover:border-slate-500 hover:bg-slate-900"
          }
          ${loading ? "cursor-not-allowed opacity-60" : "cursor-pointer"}
        `}
      >
        <div
          className={`
            mb-5 flex h-16 w-16 items-center justify-center
            rounded-2xl transition-all duration-300
            ${
              isDragging
                ? "bg-cyan-400/20 text-cyan-300"
                : "bg-slate-800 text-slate-400 group-hover:text-cyan-300"
            }
          `}
        >
          <UploadCloud size={30} strokeWidth={1.8} />
        </div>

        <h3 className="text-lg font-semibold text-white">
          Upload your medical report
        </h3>

        <p className="mt-2 text-center text-sm text-slate-400">
          Drag & drop one or multiple pages here
        </p>

        <p className="mt-4 text-xs text-slate-500">
          JPG, PNG, JPEG, PDF • Maximum {MAX_FILES} files • 10 MB each
        </p>

        <input
          ref={fileInputRef}
          type="file"
          accept="image/*,.pdf"
          multiple
          onChange={handleFileSelect}
          className="hidden"
        />
      </button>

      {/* =====================================================
          ERROR
      ====================================================== */}

      {error && (
        <div className="mt-4 rounded-2xl border border-rose-500/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">
          {error}
        </div>
      )}

      {/* =====================================================
          SELECTED FILES
      ====================================================== */}

      {files.length > 0 && (
        <div className="mt-6 rounded-3xl border border-slate-800 bg-slate-900/70 p-5">
          <div className="mb-5 flex items-center justify-between">
            <div>
              <h3 className="font-semibold text-white">Report Pages</h3>

              <p className="mt-1 text-xs text-slate-500">
                {files.length}{" "}
                {files.length === 1 ? "page selected" : "pages selected"}
              </p>
            </div>

            <button
              type="button"
              disabled={loading}
              onClick={clearFiles}
              className="text-xs font-medium text-slate-500 transition hover:text-rose-400 disabled:cursor-not-allowed disabled:opacity-40"
            >
              Clear all
            </button>
          </div>

          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4">
            {files.map((file, index) => (
              <div
                key={`${file.name}-${file.lastModified}-${index}`}
                className="group relative overflow-hidden rounded-2xl border border-slate-800 bg-slate-950"
              >
                <div className="absolute left-2 top-2 z-10 flex h-7 min-w-7 items-center justify-center rounded-lg bg-slate-950/90 px-2 text-xs font-bold text-cyan-300 backdrop-blur">
                  {index + 1}
                </div>

                <button
                  type="button"
                  disabled={loading}
                  onClick={() => removeFile(index)}
                  className="absolute right-2 top-2 z-10 flex h-7 w-7 items-center justify-center rounded-lg bg-slate-950/90 text-slate-400 opacity-0 backdrop-blur transition group-hover:opacity-100 hover:text-rose-400 disabled:cursor-not-allowed"
                  aria-label={`Remove ${file.name}`}
                >
                  <X size={15} />
                </button>

                <div className="flex h-44 items-center justify-center">
                  {isImage(file) ? (
                    <img
                      src={URL.createObjectURL(file)}
                      alt={`Report page ${index + 1}`}
                      className="h-full w-full object-contain"
                    />
                  ) : (
                    <div className="flex flex-col items-center justify-center">
                      <FileText size={40} className="text-rose-400" />

                      <span className="mt-2 text-xs text-slate-500">PDF</span>
                    </div>
                  )}
                </div>

                <div className="border-t border-slate-800 px-3 py-2">
                  <p className="truncate text-xs text-slate-400">{file.name}</p>
                </div>
              </div>
            ))}

            {files.length < MAX_FILES && (
              <button
                type="button"
                disabled={loading}
                onClick={openFileDialog}
                className="flex h-53.25 flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-800 text-slate-600 transition hover:border-slate-600 hover:text-cyan-400 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <Plus size={24} />

                <span className="mt-2 text-xs">Add page</span>
              </button>
            )}
          </div>

          {/* =================================================
              ANALYZE BUTTON
          ================================================== */}

          <button
            type="button"
            onClick={handleAnalyze}
            disabled={loading || files.length === 0}
            className={`
              mt-6 flex w-full items-center justify-center gap-2
              rounded-2xl px-6 py-3
              font-semibold
              transition-all duration-300
              ${
                loading || files.length === 0
                  ? "cursor-not-allowed bg-slate-800 text-slate-500"
                  : "bg-cyan-400 text-slate-950 hover:bg-cyan-300 hover:shadow-[0_0_30px_rgba(34,211,238,0.18)]"
              }
            `}
          >
            {loading ? (
              <>
                <LoaderCircle size={18} className="animate-spin" />
                Analyzing Report...
              </>
            ) : (
              "Analyze Report"
            )}
          </button>

          {loading && (
            <p className="mt-3 text-center text-xs text-slate-500">
              Extracting text, analyzing your report, and generating your PDF...
            </p>
          )}
        </div>
      )}
    </div>
  );
}

export default UploadZone;
