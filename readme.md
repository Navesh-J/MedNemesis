# MedNemesis

**MedNemesis** is an AI-powered medical report explanation web application that helps users understand medical reports in simple, easy-to-read language.

Users can upload medical report images, text-based PDFs, scanned PDFs, or multiple report files. MedNemesis extracts the available information using OCR/PDF text extraction, analyzes the report with Google Gemini, and generates a downloadable PDF explanation.

> **Disclaimer:** MedNemesis is an educational tool. It does not provide medical diagnoses or replace advice from a qualified healthcare professional.

---

## ✨ Features

- 📄 Upload medical report images
- 📑 Upload text-based PDF reports
- 🖨️ Process scanned/image-based PDFs using OCR
- 📚 Upload multiple report files in a single request
- 🔍 OCR using Tesseract/Tess4J
- 📖 Text extraction from text-based PDFs using Apache PDFBox
- 🤖 AI-powered report explanation using Google Gemini
- 🧠 OCR-aware interpretation of common OCR mistakes
- 📊 Identification and explanation of normal and abnormal findings
- 🩺 Separate diagnosis, severity, follow-up, treatment, prognosis, and preventive-care sections
- 📝 View raw extracted OCR text from the analysis dashboard
- 📥 Generate and download a formatted PDF explanation
- 🔤 Unicode-safe PDF generation using embedded Noto Sans fonts
- 🌐 React + Vite frontend
- ☁️ Deployable frontend and backend architecture
- 🐳 Docker support for the Spring Boot backend

---

## 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │   React Frontend    │
                         │     Vite + UI       │
                         └──────────┬──────────┘
                                    │
                                    │ POST /api/reports/analyze
                                    ▼
                         ┌─────────────────────┐
                         │   Spring Boot API   │
                         │      Backend        │
                         └──────────┬──────────┘
                                    │
                    ┌───────────────┼────────────────┐
                    │               │                │
                    ▼               ▼                ▼
             ┌────────────┐  ┌─────────────┐  ┌─────────────┐
             │ PDFBox     │  │ Tess4J /    │  │ Gemini AI   │
             │ PDF Text   │  │ Tesseract   │  │ Analysis    │
             │ Extraction │  │ OCR         │  │             │
             └────────────┘  └─────────────┘  └─────────────┘
                    │               │                │
                    └───────────────┼────────────────┘
                                    ▼
                         ┌─────────────────────┐
                         │     PDFService      │
                         │  Explanation PDF    │
                         └──────────┬──────────┘
                                    │
                                    ▼
                              PDF Download
```

---

## 🔄 Report Processing Flow

### Image report

```text
Image
  ↓
Image preprocessing
  ↓
Tesseract OCR
  ↓
Extracted text
  ↓
Gemini analysis
  ↓
PDF generation
```

### Text-based PDF

```text
PDF
  ↓
Apache PDFBox
  ↓
Embedded text extraction
  ↓
Gemini analysis
  ↓
PDF generation
```

### Scanned PDF

```text
Scanned PDF
  ↓
Apache PDFBox page rendering
  ↓
Tesseract OCR
  ↓
Extracted text
  ↓
Gemini analysis
  ↓
PDF generation
```

### Multiple files

```text
File 1 ─┐
File 2 ─┼─→ Extraction/OCR ─→ Combined report text
File 3 ─┘                         ↓
                              Gemini AI
                                  ↓
                           Single explanation
                                  ↓
                              PDF output
```

---

## 🛠️ Technology Stack

### Frontend

- React
- Vite
- Tailwind CSS
- Lucide React
- JavaScript

### Backend

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- Spring AI
- Google Gemini
- Tess4J
- Tesseract OCR
- Apache PDFBox 3.0.7
- Maven

### PDF Generation

- Apache PDFBox
- Noto Sans
- Noto Sans Bold

### Deployment

- Docker
- Render — backend
- Vercel — frontend

---

## 📁 Project Structure

```text
MedNemesis/
│
├── Frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── analysis/
│   │   │   └── upload/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── assets/
│   │   └── ...
│   │
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── Backend-Spring/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/spring/mednemesis/
│   │   │   │       ├── ai/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── model/
│   │   │   │       ├── ocr/
│   │   │   │       └── pdf/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── fonts/
│   │   │       │   ├── NotoSans-Regular.ttf
│   │   │       │   └── NotoSans-Bold.ttf
│   │   │       └── tessdata/
│   │   │           └── eng.traineddata
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   ├── Dockerfile
│   └── ...
│
└── README.md
```

---

## 🚀 Running Locally

### Prerequisites

- Java 21
- Maven
- Node.js and npm
- Tesseract OCR
- Git
- Docker (optional)

### Backend

```bash
cd Backend-Spring
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

Set the Gemini API key as an environment variable:

```text
GEMINI_API_KEY=your_api_key
```

The application reads it through:

```text
${GEMINI_API_KEY}
```

### Frontend

```bash
cd Frontend
npm install
npm run dev
```

The frontend normally runs on:

```text
http://localhost:5173
```

---

## 🔌 API

### Analyze Report

```http
POST /api/reports/analyze
```

Request type:

```text
multipart/form-data
```

Parameter:

```text
files
```

The endpoint accepts one or more report files.

Supported report types:

- Images
- Text-based PDFs
- Scanned PDFs

Example:

```bash
curl -X POST   -F "files=@medical-report.pdf"   http://localhost:8080/api/reports/analyze
```

---

## 📦 Response

A successful response contains:

```json
{
  "success": true,
  "filename": "medical-report.pdf",
  "ocrText": "Extracted report text...",
  "analysis": "# 1. Patient Information\n...",
  "pdfUrl": "/uploads/report-example.pdf"
}
```

| Field | Description |
|---|---|
| `success` | Indicates whether processing succeeded |
| `filename` | Original filename or combined report description |
| `ocrText` | Extracted text from the uploaded report(s) |
| `analysis` | AI-generated medical report explanation |
| `pdfUrl` | URL of the generated explanation PDF |

---

## 🤖 AI Analysis

MedNemesis uses Google Gemini through Spring AI.

The AI is instructed to:

- Review the complete extracted report
- Explain medical terminology in simple language
- Identify important findings
- Compare results with reference ranges when available
- Separate normal and abnormal findings
- Distinguish confirmed diagnoses from possible conditions
- Avoid presenting possibilities as confirmed diagnoses
- Avoid prescribing medication
- Provide reasonable follow-up guidance
- Explain uncertainty caused by OCR
- Mark reconstructed OCR information as `(likely OCR-corrected)`

The generated explanation follows this structure:

```text
1. Patient Information
2. Report Type
3. Findings Summary
4. Normal Results
5. Abnormal Results
6. Diagnosis
7. Severity Assessment
8. Suggested Follow-Up
9. Treatment Recommendations
10. Prognosis
11. Preventive Care Recommendations
12. Conclusion
```

---

## 🔍 OCR Processing

### Images

```text
Image
 ↓
Upscaling
 ↓
Grayscale conversion
 ↓
Contrast enhancement
 ↓
Tesseract OCR
 ↓
Spatial OCR
```

### Text PDFs

Text-based PDFs are handled using Apache PDFBox. The application first attempts to extract embedded text directly, avoiding unnecessary OCR.

### Scanned PDFs

Scanned PDFs are rendered page-by-page with Apache PDFBox and processed using Tesseract OCR.

---

## 📄 PDF Generation

After AI analysis, MedNemesis generates a downloadable explanation PDF.

Noto Sans fonts are embedded in the application:

```text
src/main/resources/fonts/
├── NotoSans-Regular.ttf
└── NotoSans-Bold.ttf
```

This improves support for Unicode characters such as:

```text
•
–
—
°
≤
≥
₹
```

and other Unicode punctuation and symbols that can occur in AI-generated content.

---

## 🐳 Docker

Build the backend image:

```bash
docker build -t mednemesis-backend .
```

Run it:

```bash
docker run --rm -p 8080:8080   -e GEMINI_API_KEY="your_api_key"   mednemesis-backend
```

The application will be available at:

```text
http://localhost:8080
```

Tesseract and the required OCR data are configured for the container environment.

---

## ☁️ Deployment

### Frontend

The frontend can be deployed to Vercel.

Production configuration:

```text
VITE_API_URL=https://your-backend-url
```

### Backend

The Spring Boot backend can be deployed to Render using Docker.

Typical deployment flow:

```text
GitHub
   ↓
Render
   ↓
Docker build
   ↓
Spring Boot container
```

Backend port:

```text
8080
```

---

## 🌐 CORS

The backend allows the development frontend and production frontend origins.

Example:

```text
http://localhost:5173
https://mednemesis.vercel.app
```

---

## 📂 Generated PDF Storage

Generated PDFs are currently stored in the backend's local:

```text
uploads/
```

directory.

Generated files are exposed through:

```text
/uploads/**
```

This is suitable for the current MVP/demo deployment.

The current Render deployment uses ephemeral server storage, so generated PDFs should be downloaded by the user after generation rather than treated as permanent cloud storage.

Persistent object storage can be added later if report history or long-term PDF retrieval is introduced.

---

## ⚙️ Configuration

Example application configuration:

```yaml
spring:
  application:
    name: mednemesis-backend

  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          model: gemini-3.6-flash

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB

server:
  port: 8080
```

Keep API keys and other secrets outside source control. Use environment variables for deployment.

---

## 🔐 Security & Privacy Considerations

MedNemesis is designed as an educational medical-report explanation tool.

The current MVP does not provide:

- Medical diagnosis
- Emergency medical decision-making
- Prescription recommendations
- Long-term medical record management
- Permanent report storage

Users should avoid uploading information they do not have permission to process.

Generated reports should be downloaded if they need to be retained because the current deployment does not use persistent PDF storage.

---

## ⚠️ Limitations

- OCR accuracy depends on source document quality.
- Poorly scanned or distorted documents may produce incorrect OCR.
- OCR can misread numbers, units, or medical terminology.
- AI interpretation should not be treated as a medical diagnosis.
- Very large or complex scanned PDFs can require substantial CPU and memory during OCR.
- The current PDF storage system is not persistent cloud storage.
- Gemini API availability and usage limits depend on the configured Google AI service.
- Prognosis and severity cannot always be determined from a laboratory report alone.

---

## 🛣️ Future Improvements

- User authentication
- Report history
- Persistent report storage
- Cloud object storage for generated PDFs
- Automatic PDF cleanup
- Better OCR confidence detection
- More advanced table extraction
- Improved multi-page report handling
- Report comparison over time
- Patient-friendly charts and visualizations
- More robust medical parameter extraction
- Additional AI providers/models
- Improved scanned-PDF optimization
- Background processing for large reports
- Progress indicators for multi-page OCR
- Automated testing for different report formats

---

## 🧪 Development Stack

```text
Java 21
Spring Boot 4.0.7
Spring AI 2.0.0
Tess4J 5.19.0
Tesseract 5.x
Apache PDFBox 3.0.7
React
Vite
Tailwind CSS
Docker
```

---

## 📜 Disclaimer

MedNemesis is intended for **educational and informational purposes only**.

The generated explanations are not a substitute for professional medical advice, diagnosis, or treatment.

Always consult a qualified healthcare professional for interpretation of medical results and decisions about your health.

---

## 👨‍💻 Project

**MedNemesis**

AI-powered medical report explanation platform.

Built with:

```text
React + Vite
Spring Boot
Spring AI
Google Gemini
Tesseract OCR
Tess4J
Apache PDFBox
Docker
```
