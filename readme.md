# 🧾 Medical Report Diagnosis Web App

## 📌 Project Overview
The **Medical Report Diagnosis Web App** is an MVP project that allows users to **upload medical reports** (image/PDF), automatically extract test results, compare them against safe reference ranges, and provide **easy-to-understand insights and suggestions**.  

⚠️ **Disclaimer:** This project is **for educational and informational purposes only**. It does not provide medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional for medical concerns.  

---

## 🎯 Goals
- Help users interpret medical report values in **simple, human-readable terms**.  
- Identify **deficiencies and excesses** by comparing values with standard ranges.  
- Provide **general lifestyle & diet suggestions** (non-diagnostic).  
- Present insights in a **clean and user-friendly dashboard**.  

---

## 🚀 MVP Features
1. **Upload Report**
   - Accepts PDF or image (JPG/PNG).  
   - File size ≤ 10 MB.  

2. **OCR Processing**
   - Uses **Google Cloud Vision API** to extract text and tables from reports.  

3. **Parsing & Normalization**
   - Identifies medical parameters (e.g., Hemoglobin, Glucose, Cholesterol).  
   - Normalizes values and units.  
   - Matches synonyms like `Hb`, `HGB`, `Hemoglobin`.  

4. **Reference Ranges**
   - Safe ranges stored in **PostgreSQL database**.  
   - Compares extracted values against standard adult ranges.  

5. **Analysis Engine**
   - Flags **LOW / NORMAL / HIGH** for each parameter.  
   - Generates structured JSON output.  

6. **AI Insights**
   - Uses **Gemini API** to summarize findings in plain English.  
   - Provides **general tips** (diet, hydration, lifestyle).  
   - Adds strong disclaimer.  

7. **Frontend (React)**
   - File upload interface.  
   - Results table with **color-coded statuses**.  
   - Cards for insights and suggestions.  
   - Option to copy/download summary.  

8. **Export Option**
   - An option to export and download the generated report as a PDF file.
---

## 🛠️ Tech Stack

### Frontend
- **React.js / Next.js** → User interface  
- **TailwindCSS** → Styling  
- **Recharts / Chart.js** → Graphs & trends  

### Backend
- **Node.js (Express.js)** → REST API  
- **Google Cloud Vision API** → OCR for text extraction  
- **Gemini API (Google AI)** → Summaries & insights  
- **PostgreSQL** → Store safe ranges & results  

---

## 🔄 System Flow
1. User uploads report via frontend.  
2. Backend sends file to **Cloud Vision API** → extracts raw text.  
3. Text is parsed → values normalized → mapped to **reference ranges** (Postgres).  
4. Backend generates structured JSON → sends facts to **Gemini API**.  
5. Gemini returns **summary + suggestions**.  
6. Frontend displays results:  
   - Table of parameters (value, unit, safe range, status).  
   - Insights and recommendations.  
   - Download/Copy option.  

---

## 🗄️ Database Design (Postgres)
### `parameters`
- `id` (uuid)  
- `code` (HB, FBS, LDL)  
- `name` (Hemoglobin, Fasting Glucose)  
- `unit` (g/dL, mg/dL)  
- `low_normal`, `high_normal`  
- `notes`  

### `parameter_synonyms`
- `parameter_code` → `Hb`, `HGB`  

### `unit_conversions`
- Convert between units (e.g., mmol/L ↔ mg/dL).  

### `reports`
- Report metadata (filename, type, created_at).  

### `report_results`
- OCR raw text  
- Structured JSON (parsed values + statuses)  
- AI summary + tips  

---

## 📊 Example Workflow

**Uploaded Report:**
- Hemoglobin: 11.2 g/dL
- LDL Cholesterol: 160 mg/dL
- HDL Cholesterol: 38 mg/dL


**Parsed JSON:**
```json
{
  "parameters": [
    {
      "code": "HB",
      "name": "Hemoglobin",
      "value": 11.2,
      "unit": "g/dL",
      "range": { "low": 13, "high": 17 },
      "status": "LOW"
    },
    {
      "code": "LDL",
      "name": "LDL Cholesterol",
      "value": 160,
      "unit": "mg/dL",
      "range": { "low": 0, "high": 100 },
      "status": "HIGH"
    },
    {
      "code": "HDL",
      "name": "HDL Cholesterol",
      "value": 38,
      "unit": "mg/dL",
      "range": { "low": 40, "high": 999 },
      "status": "LOW"
    }
  ]
}
```
### Sample Output
Your hemoglobin is low, which may suggest anemia.
LDL cholesterol is high, increasing cardiovascular risk.
HDL cholesterol is low, which is protective cholesterol.

### Suggestions:
- Eat iron-rich foods (spinach, legumes, lean meats).
- Reduce saturated fats and processed foods.
- Exercise regularly and increase fiber intake.

⚠️ This is educational and not medical advice. Please consult a doctor.

### 📈 Future Enhancements

- User authentication & private storage.
- Trends & history (multi-report comparison).
- Lab-specific templates for better parsing.
- Support for pediatric/geriatric ranges.
- Export to PDF.

### ⚠️ Disclaimer

This application is not a substitute for medical advice.
It is intended for educational purposes only. Always consult a qualified healthcare professional for diagnosis or treatment.