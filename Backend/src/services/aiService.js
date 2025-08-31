import { GoogleGenerativeAI } from "@google/generative-ai";
import dotenv from "dotenv";

dotenv.config();

if (!process.env.GEMINI_API_KEY) {
  console.error("❌ GEMINI_API_KEY is missing!");
}

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

export async function getAIAnalysis(ocrText) {
  const model = genAI.getGenerativeModel({ model: "gemini-2.0-flash" });

  const prompt = `
You are a Medical Report Assistant. Analyze the following medical report and provide a **user-friendly, easy-to-understand explanation** under the specified headings. 
Always include all headings. If information is missing or cannot be inferred, write "Not Available" or "N/A".

Report Text:
"""${ocrText}"""

Output Format (JSON only) with same headings:

{
  "patientInformation": {
    "patientIDName": "",
    "ageGender": "",
    "medicalHistory": "",
    "dateOfReport": ""
  },
  "reportType": {
    "testProcedure": "",
    "reasonForTest": ""
  },
  "findingsSummary": {
    "overview": "",
    "normalAbnormalStatus": ""
  },
  "normalResults": {
    "parametersWithinRange": "",
    "noSignificantFindings": ""
  },
  "abnormalResults": {
    "outOfRangeResults": "",
    "significanceOfAbnormalFindings": ""
  },
  "diagnosis": {
    "confirmedDiagnosis": "",
    "differentialDiagnosis": ""
  },
  "severityAssessment": {
    "severity": "",
    "urgencyLevel": ""
  },
  "suggestedFollowUp": {
    "additionalTests": "",
    "referralRecommendations": ""
  },
  "treatmentRecommendations": {
    "medications": "",
    "lifestyleAdjustments": "",
    "therapiesInterventions": ""
  },
  "prognosis": {
    "expectedOutcome": "",
    "riskFactors": ""
  },
  "preventiveCare": {
    "preventiveMeasures": "",
    "riskReductions": ""
  },
  "conclusion": {
    "finalSummary": "",
    "finalDiagnosisImpression": ""
  }
}

Instructions for writing values:
- Write in **plain, simple language** suitable for a normal user.
- Use short sentences and bullet points where needed.
- Explain abnormal results and medical terms in simple words.
- Use "Not Available" if info cannot be determined.
`;

  const result = await model.generateContent(prompt);
  let text = result.response.text().trim();
  text = text.replace(/```json|```/g, "");

  try {
    return JSON.parse(text);
  } catch (e) {
    return {
      patientInformation: {
        patientIDName: "Not Available",
        ageGender: "Not Available",
        medicalHistory: "Not Available",
        dateOfReport: "Not Available",
      },
      reportType: { testProcedure: "Not Available", reasonForTest: "Not Available" },
      findingsSummary: { overview: "Not Available", normalAbnormalStatus: "Not Available" },
      normalResults: { parametersWithinRange: "Not Available", noSignificantFindings: "Not Available" },
      abnormalResults: { outOfRangeResults: "Not Available", significanceOfAbnormalFindings: "Not Available" },
      diagnosis: { confirmedDiagnosis: "Not Available", differentialDiagnosis: "Not Available" },
      severityAssessment: { severity: "Not Available", urgencyLevel: "Not Available" },
      suggestedFollowUp: { additionalTests: "Not Available", referralRecommendations: "Not Available" },
      treatmentRecommendations: { medications: "Not Available", lifestyleAdjustments: "Not Available", therapiesInterventions: "Not Available" },
      prognosis: { expectedOutcome: "Not Available", riskFactors: "Not Available" },
      preventiveCare: { preventiveMeasures: "Not Available", riskReductions: "Not Available" },
      conclusion: { finalSummary: "Not Available", finalDiagnosisImpression: "Not Available" },
    };
  }
}
