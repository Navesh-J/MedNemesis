import express from "express";
import multer from "multer";
import path from "path";
import fs from "fs";
import Tesseract from "tesseract.js";
import { getAIAnalysis } from "../services/aiService.js";
import PDFDocument from "pdfkit";

const router = express.Router();
const upload = multer({ dest: "uploads/" });

router.post("/upload", upload.array("files"), async (req, res) => {
  try {
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({ success: false, error: "At least one file is required" });
    }

    let combinedText = "";
    const ocrResults = [];

    // 🔹 OCR for each file
    for (const file of req.files) {
      const filePath = path.resolve(file.path);
      const {
        data: { text: ocrText },
      } = await Tesseract.recognize(filePath, "eng");

      combinedText += `\n--- Page ${ocrResults.length + 1} ---\n${ocrText}\n`;
      ocrResults.push(ocrText);

      fs.unlinkSync(filePath); // cleanup temp file
    }

    // 🔹 AI Analysis on combined text
    const aiResult = await getAIAnalysis(combinedText);

    // 🔹 Generate PDF
    const pdfName = `report_${Date.now()}.pdf`;
    const pdfPath = path.join("uploads", pdfName);
    const doc = new PDFDocument({ margin: 40, size: "A4" });
    doc.pipe(fs.createWriteStream(pdfPath));

    // Title
    doc.fontSize(22).fillColor("#0B3D91").text("Medical Report", { align: "center" });
    doc.moveDown(2);

    // Optional: Add OCR pages section
    doc.fontSize(18).fillColor("#1E90FF").text("Extracted Text (OCR)", { underline: true });
    doc.moveDown(1);

    ocrResults.forEach((pageText, idx) => {
      doc.fontSize(14).fillColor("#0B3D91").text(`Page ${idx + 1}`, { underline: true });
      doc.moveDown(0.5);
      doc.fontSize(10).fillColor("#333").text(pageText.trim() || "No text found");
      doc.moveDown(1);
    });

    // Helper to format AI output
    function writeSection(title, content) {
      doc.fontSize(16).fillColor("#1E90FF").text(title, { underline: true });
      doc.moveDown(0.5);

      if (typeof content === "object" && !Array.isArray(content)) {
        for (const [key, value] of Object.entries(content)) {
          if (typeof value === "object") {
            doc.fontSize(12).fillColor("#000").text(`${key}:`);
            for (const [k, v] of Object.entries(value)) {
              let textColor = "#000";
              if (title.toLowerCase().includes("abnormal") || k.toLowerCase().includes("outofrange")) {
                textColor = "#FF0000"; // highlight abnormal
              }
              doc.fontSize(12).fillColor(textColor).text(`  • ${k}: ${v || "Not Available"}`);
            }
          } else {
            let textColor = "#000";
            if (title.toLowerCase().includes("abnormal") || key.toLowerCase().includes("outofrange")) {
              textColor = "#FF0000";
            }
            doc.fontSize(12).fillColor(textColor).text(`• ${key}: ${value || "Not Available"}`);
          }
        }
      } else if (Array.isArray(content)) {
        content.forEach((item) => {
          doc.fontSize(12).fillColor("#000").text(`• ${item || "Not Available"}`);
        });
      } else {
        doc.fontSize(12).fillColor("#000").text(content || "Not Available");
      }

      doc.moveDown(1);
    }

    // 🔹 AI Analysis section
    doc.addPage();
    doc.fontSize(18).fillColor("#1E90FF").text("AI Analysis", { underline: true });
    doc.moveDown(1);

    for (const [heading, content] of Object.entries(aiResult)) {
      const formattedHeading = heading
        .replace(/([A-Z])/g, " $1")
        .replace(/^./, (str) => str.toUpperCase());
      writeSection(formattedHeading, content);
    }

    doc.end();

    const pdfUrl = `/uploads/${pdfName}`;

    res.json({ success: true, report: { fullText: combinedText, aiResult, pdfUrl } });
  } catch (err) {
    console.error("Error processing reports:", err);
    res.status(500).json({ success: false, error: err.message });
  }
});

export default router;
