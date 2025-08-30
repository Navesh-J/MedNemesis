import { useState, useCallback, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";

import ReportView from "./ReportView";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:4000";

function FileUpload() {
  const [file, setFile] = useState(null);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);

  const handleUpload = async () => {
    if (!file) return;
    setLoading(true);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await fetch(`${BASE_URL}/reports/upload`, {
        method: "POST",
        body: formData,
      });
      const data = await res.json();
      setReport(data.report);
    } catch (err) {
      console.error("Upload failed:", err);
      alert("Upload failed. Check console.");
    } finally {
      setLoading(false);
    }
  };

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    const droppedFile = e.dataTransfer.files[0]; // Fix: pick first file
    if (droppedFile) setFile(droppedFile);
  }, []);

  const handleFileSelect = (e) => {
    const selectedFile = e.target.files[0]; // Fix: pick first file
    if (selectedFile) setFile(selectedFile);
  };

  const openFileDialog = () => {
    if (fileInputRef.current) fileInputRef.current.click();
  };

  const isImage = file && file.type.startsWith("image/");
  const isPDF = file && file.type === "application/pdf";

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#141032] via-[#232159] to-[#09346b] p-6">
      <motion.div
        className="bg-[#22213a]/90 backdrop-blur-xl rounded-3xl shadow-2xl p-8 w-full max-w-md flex flex-col items-center border border-[#456ceb] border-opacity-40"
        initial={{ opacity: 0, y: 50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.9 }}
      >
        <motion.h1
          className="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-[#70d7ff] via-[#69f0ae] to-[#456ceb] text-center mb-8 drop-shadow-md tracking-wide"
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1.2 }}
        >
          Upload Medical Report
        </motion.h1>

        <motion.div
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={(e) => {
            e.preventDefault();
            setIsDragging(false);
          }}
          onDrop={handleDrop}
          onClick={openFileDialog}
          className={`w-full p-6 mb-6 rounded-xl border-2 border-dashed transition-all cursor-pointer flex flex-col items-center justify-center 
            ${
              isDragging
                ? "border-[#69f0ae] bg-[#29405b]/30 shadow-lg"
                : "border-[#456ceb] bg-[#21213a]/60"
            }`}
          style={{
            boxShadow: isDragging
              ? "0 0 24px #69f0ae66"
              : "0 2px 32px #456ceb22",
            transition: "all 0.3s cubic-bezier(.45,.03,.52,.96)",
          }}
        >
          <p className="text-center text-[#70d7ff] font-semibold mb-2">
            {file
              ? `Selected: ${file.name}`
              : "Drag & drop a file or click to select"}
          </p>
          <input
            ref={fileInputRef}
            type="file"
            className="hidden"
            onChange={handleFileSelect}
          />

          <AnimatePresence mode="wait">
            {file && (
              <motion.div
                key={file.name}
                className="mt-4 flex flex-col items-center"
                initial={{ opacity: 0, scale: 0.85 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.85 }}
                transition={{ duration: 0.3 }}
              >
                {isImage && (
                  <motion.img
                    src={URL.createObjectURL(file)}
                    alt="preview"
                    className="w-32 h-32 object-contain rounded-xl shadow-lg border border-[#70d7ff]/50"
                    initial={{ scale: 0.7 }}
                    animate={{ scale: 1 }}
                    transition={{ duration: 0.4 }}
                  />
                )}
                {isPDF && (
                  <div className="flex flex-col items-center justify-center w-32 h-32 bg-[#31213a]/60 rounded-lg shadow-md p-4 border border-[#e05355]/40">
                    <svg
                      className="w-12 h-12 text-[#e05355] mb-2"
                      fill="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path d="M12 0C5.37 0 0 5.37 0 12s5.37 12 12 12 12-5.37 12-12S18.63 0 12 0zm0 18a1.5 1.5 0 110-3 1.5 1.5 0 010 3zm1-9h-2v6h2V9z" />
                    </svg>
                    <p className="text-[#e05355] text-xs text-center break-words">
                      {file.name}
                    </p>
                  </div>
                )}
              </motion.div>
            )}
          </AnimatePresence>
        </motion.div>

        <motion.button
          onClick={handleUpload}
          disabled={loading || !file}
          className={`w-full px-6 py-3 font-bold rounded-xl text-white shadow-lg transition-all 
            ${
              loading || !file
                ? "bg-gradient-to-br from-[#4367cf] to-[#2889c1] opacity-50 cursor-not-allowed"
                : "bg-gradient-to-br from-[#11d1a7] via-[#456ceb] to-[#091e42] hover:from-[#70d7ff] hover:to-[#456ceb] hover:shadow-neon"
            }`}
          style={{
            boxShadow: loading
              ? "0 0 0"
              : "0 0 18px #456ceb55, 0 2px 24px #11d1a755",
            transition: "box-shadow 0.3s cubic-bezier(.45,.03,.52,.96)",
          }}
          whileHover={{ scale: loading ? 1 : 1.05 }}
          whileTap={{ scale: loading ? 1 : 0.96 }}
        >
          {loading ? (
            <motion.span
              className="inline-block"
              animate={{ rotate: 360 }}
              transition={{ repeat: Infinity, duration: 0.7 }}
            >
              ⏳
            </motion.span>
          ) : (
            "Upload"
          )}
        </motion.button>

        <AnimatePresence>
          {report && (
            <motion.div
              key="report"
              className="mt-8 w-full bg-[#1a1830]/80 border border-[#69f0ae]/40 rounded-2xl p-6 shadow-xl backdrop-blur-lg"
              initial={{ opacity: 0, scale: 0.94, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              transition={{ duration: 0.7 }}
            >
              <ReportView report={report} onRemove={() => setReport(null)} />
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </div>
  );
}

export default FileUpload;
