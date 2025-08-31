import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

/**
 * Generate AI suggestions for a report
 * @param {number} reportId
 * @param {string} aiResponse - response from AI (Gemini)
 */
export async function saveAnalysis(reportId, summary, suggestions) {
  // Save analysis linked to report
  const analysis = await prisma.analysis.upsert({
    where: { reportId },
    update: { summary, suggestions },
    create: { reportId, summary, suggestions },
  });
  return analysis;
}
