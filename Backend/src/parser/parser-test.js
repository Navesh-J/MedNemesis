import { parseReportText } from "./reportParser.js";

// Simulated OCR output (raw text)
const sampleOCR = `
Hemoglobin         13.5     12-16    g/dl
WBC Count          8500     4000-11000 /cumm
Platelet Count     2.1      1.5-4.5  lakh/cumm
Vitamin D          22.3     30-100   ng/ml
Urine Protein      Present  -        -
`;

console.log("🔍 Raw OCR text:\n", sampleOCR);

const parsed = parseReportText(sampleOCR);

console.log("\n✅ Parsed Results:");
console.log(parsed);
