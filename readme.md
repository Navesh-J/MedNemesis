# 🧾 Medical Report Diagnosis Web App (MVP)

## 📌 Project Overview
The **Medical Report Diagnosis Web App** is a simplified MVP that allows users to **upload medical reports** (image or PDF), automatically extract test results using OCR, and get **easy-to-understand insights and suggestions**.  

⚠️ **Disclaimer:** This project is **for educational and informational purposes only**. It does **not** provide medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional for medical concerns.  

---

## 🎯 Goals
- Simplify medical report interpretation for users in **plain English**.  
- Highlight **abnormal values** clearly for user awareness.  
- Provide **general suggestions** for lifestyle or health monitoring (non-diagnostic).  
- Present results in a **clean, visually appealing format** with PDF export.  

---

## 🚀 MVP Features
1. **Upload Report**
   - Accepts PDF or image (JPG/PNG).  
   - File size ≤ 10 MB.  

2. **OCR Processing**
   - Uses **Tesseract.js** to extract text from uploaded files.  

3. **AI Analysis**
   - Uses **Google Gemini API** to analyze report text.  
   - Generates **structured JSON** with all headings:
     - Patient Information, Report Type, Findings Summary, Normal & Abnormal Results, Diagnosis, Severity, Suggested Follow-Up, Treatment Recommendations, Prognosis, Preventive Care, Conclusion.  
   - Summaries are in **easy-to-understand language** for non-medical users.  

4. **PDF Export**
   - Generates a **visually appealing PDF** with:
     - Headings
     - Bullet points
     - Highlights for abnormal results
     - Colors and spacing for readability

5. **Frontend (React)**
   - Upload interface for reports.  
   - Displays full OCR text.  
   - Displays AI analysis with headings and readable formatting.  
   - Download PDF button for generated report.  

---

## 🛠️ Tech Stack

### Frontend
- **React.js** → User interface  
- **TailwindCSS** → Styling  
- **Framer Motion** → Animations  
- **PDFKit** → PDF generation  

### Backend
- **Node.js (Express.js)** → REST API server  
- **Tesseract.js** → OCR for text extraction  
- **Google Gemini API** → AI-powered analysis and suggestions  

### Deployment
- **Render** → Backend hosting  
- **Vercel** → Frontend hosting  

---

## 🔄 System Flow
1. User uploads report via the frontend.  
2. Backend extracts text using **Tesseract.js**.  
3. Extracted text is sent to **Gemini AI** for analysis.  
4. AI returns structured JSON with headings and explanations in **easy language**.  
5. Backend generates a **formatted PDF** highlighting important points and abnormal results.  
6. Frontend displays:
   - Full report text
   - Simplified AI insights with bullet points
   - Downloadable PDF

---

### ⚠️ Disclaimer
This application is **not a substitute for medical advice**. It is intended for **educational purposes only**. Always consult a qualified healthcare professional for diagnosis or treatment.
