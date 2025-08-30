import { PrismaClient } from "@prisma/client";
import { parseReportText } from "../parser/reportParser.js";

const prisma = new PrismaClient();

/**
 * Save OCR text as a Report with linked ReportValues
 * Only numeric values are saved
 * @param {string} ocrText - Raw OCR extracted text
 * @param {string} fileName - Name of uploaded file
 * @returns {object} report object with linked values
 */
export async function saveReport(ocrText, fileName = "report.txt") {
    // 1. Parse OCR text
    const parsed = parseReportText(ocrText);

    if (!parsed.length) {
        throw new Error("No numeric values found in OCR text");
    }

    // 2. Create a new Report entry
    const report = await prisma.report.create({
        data: { fileName },
    });

    // 3. Loop through parsed results
    for (const { name, value, unit, minValue, maxValue } of parsed) {
        // Find matching parameter by name or synonym
        const param = await prisma.parameter.findFirst({
            where: {
                OR: [
                    { name: { equals: name, mode: "insensitive" } },
                    { synonyms: { some: { synonym: { equals: name, mode: "insensitive" } } } },
                ],
            },
        });

        if (!param) {
            console.log(`⚠️ Parameter not found in DB: ${name}`);
            continue; // skip unknown parameters
        }

        // 4. Insert ReportValue
        await prisma.reportValue.create({
            data: {
                reportId: report.id,
                parameterId: param.id,
                value,
            },
        });

        console.log(`✅ Saved ${name} = ${value} ${unit} (Range: ${minValue}-${maxValue})`);
    }

    console.log("📌 Report parsing and saving complete.");

    return report;
}
