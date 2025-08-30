import express from "express";
import multer from "multer";
import path from "path";
import fs from "fs";
import Tesseract from "tesseract.js";
import { saveReport } from "../services/reportService.js";

const router = express.Router();

// Configure multer for file uploads
const upload = multer({ dest: "uploads/" }); // temporary folder

/**
 * POST /reports/upload
 * Body: form-data file: report image/pdf
 * Extracts text with Tesseract, parses numeric values, saves report
 */
router.post("/upload", upload.single("file"), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ success: false, error: "File is required" });

    const filePath = path.resolve(req.file.path);

    // OCR with Tesseract
    const { data: { text: ocrText } } = await Tesseract.recognize(filePath, "eng", {
      logger: info => console.log(info), // optional progress logs
    });

    // Save parsed report to DB
    const report = await saveReport(ocrText, req.file.originalname);

    // Remove temp uploaded file
    fs.unlinkSync(filePath);

    res.json({ success: true, report });
  } catch (err) {
    console.error("Error processing uploaded report:", err);
    res.status(500).json({ success: false, error: err.message });
  }
});

export default router;
