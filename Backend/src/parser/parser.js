import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

async function parseAndSaveReport(ocrText, fileName = "report.txt") {
  const lines = ocrText.split("\n").map(l => l.trim()).filter(l => l);

  // 1. Create a Report entry
  const report = await prisma.report.create({
    data: { fileName }
  });

  // Regex: Name | Value | Range | Unit
  const regex = /^([A-Za-z\s]+)\s+([\d.]+)\s+([\d.\-]+)\s+([^\s]+)$/;

  for (const line of lines) {
    const match = line.match(regex);
    if (!match) continue;

    const [, rawName, valueStr, rangeStr, unit] = match;
    const value = parseFloat(valueStr);

    // 2. Find parameter in DB (by name or synonym)
    const param = await prisma.parameter.findFirst({
      where: {
        OR: [
          { name: { equals: rawName.trim(), mode: "insensitive" } },
          { synonyms: { some: { synonym: { equals: rawName.trim(), mode: "insensitive" } } } }
        ]
      }
    });

    if (!param) {
      console.log(`⚠️ Parameter not found in DB: ${rawName}`);
      continue;
    }

    // 3. Save ReportValue
    await prisma.reportValue.create({
      data: {
        reportId: report.id,
        parameterId: param.id,
        value
      }
    });

    console.log(`✅ Saved ${rawName} = ${value} ${unit}`);
  }

  console.log("Report parsing complete.");
}
