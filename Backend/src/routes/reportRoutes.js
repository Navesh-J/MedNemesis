import express from "express";
import multer from "multer";
import path from "path";
import fs from "fs";
import Tesseract from "tesseract.js";
import { getAIAnalysis } from "../services/aiService.js";
import PDFDocument from "pdfkit";

const router = express.Router();
const upload = multer({ dest: "uploads/" });

router.post("/upload", upload.single("file"), async (req, res) => {
  try {
    if (!req.file)
      return res
        .status(400)
        .json({ success: false, error: "File is required" });

    const filePath = path.resolve(req.file.path);

    // OCR extraction
    const {
      data: { text: ocrText },
    } = await Tesseract.recognize(filePath, "eng");
    console.log("OCR Text:", ocrText);

    // AI generates structured report with all headings
    const aiResult = await getAIAnalysis(ocrText);

    // Generate PDF
    const pdfName = `report_${Date.now()}.pdf`;
    const pdfPath = path.join("uploads", pdfName);
    const doc = new PDFDocument({ margin: 40, size: "A4" });
    doc.pipe(fs.createWriteStream(pdfPath));

    // PDF Title
    doc.fontSize(22).fillColor("#0B3D91").text("Medical Report", { align: "center" });
    doc.moveDown(2);

    // Helper function to format headings
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
                textColor = "#FF0000"; // highlight abnormal results
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

    // Loop through AI headings
    for (const [heading, content] of Object.entries(aiResult)) {
      // Convert camelCase to Title Case for PDF
      const formattedHeading = heading
        .replace(/([A-Z])/g, " $1")
        .replace(/^./, (str) => str.toUpperCase());
      writeSection(formattedHeading, content);
    }

    doc.end();

    // Remove uploaded file
    fs.unlinkSync(filePath);

    const pdfUrl = `/uploads/${pdfName}`;

    // Return AI result + OCR text + PDF URL
    res.json({ success: true, report: { fullText: ocrText, aiResult, pdfUrl } });
  } catch (err) {
    console.error("Error processing report:", err);
    res.status(500).json({ success: false, error: err.message });
  }
});

export default router;
