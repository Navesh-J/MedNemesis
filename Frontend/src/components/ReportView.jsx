import { motion, AnimatePresence } from "framer-motion";

export default function ReportView({ report, onRemove }) {
  if (!report) {
    return <p className="text-gray-400 mt-4">No report data to display.</p>;
  }

  const ai = report.aiResult || {};

  // Mapping headings to human-readable labels
  const headingsMap = {
    patientInformation: "Patient Information",
    reportType: "Report Type",
    findingsSummary: "Findings Summary",
    normalResults: "Normal Results",
    abnormalResults: "Abnormal Results",
    diagnosis: "Diagnosis",
    severityAssessment: "Severity Assessment",
    suggestedFollowUp: "Suggested Follow-Up",
    treatmentRecommendations: "Treatment Recommendations",
    prognosis: "Prognosis",
    preventiveCare: "Preventive Care Recommendations",
    conclusion: "Conclusion",
  };

  // Convert values to readable text with bullets and color for abnormalities
  const formatValue = (value, headingKey) => {
    if (!value) return <span>Not Available</span>;

    if (typeof value === "string") return <span>{value}</span>;

    if (typeof value === "object") {
      return (
        <ul className="ml-4 list-disc">
          {Object.entries(value).map(([key, val]) => {
            // Highlight abnormalities in red
            const isAbnormal =
              headingKey === "abnormalResults" ||
              key.toLowerCase().includes("outofrange") ||
              key.toLowerCase().includes("significance");
            const textColor = isAbnormal ? "text-red-400" : "text-white";

            return (
              <li key={key} className={`${textColor} mb-1`}>
                <strong>{key.replace(/([A-Z])/g, " $1")}</strong>: {val || "Not Available"}
              </li>
            );
          })}
        </ul>
      );
    }

    return <span>{String(value)}</span>;
  };

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={report.id || "report"}
        className="mt-6 w-full border border-[#69f0ae]/40 rounded-2xl bg-[#1a1830]/80 shadow-xl backdrop-blur-lg p-6 flex flex-col gap-6"
        initial={{ opacity: 0, scale: 0.94, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.94, y: 20 }}
        transition={{ duration: 0.7 }}
      >
        <h2 className="text-[#70d7ff] text-2xl font-bold mb-4 drop-shadow-md">
          Report Details
        </h2>

        {/* Extracted Report Text */}
        {report.fullText && (
          <div className="p-4 rounded-xl bg-[#22213a]/80 text-white overflow-x-auto">
            <h3 className="text-lg font-semibold text-[#70d7ff] mb-2">
              Extracted Report
            </h3>
            <pre className="whitespace-pre-wrap text-sm">{report.fullText}</pre>
          </div>
        )}

        {/* AI Analysis */}
        {ai && (
          <div className="mt-4 p-6 border rounded-2xl bg-[#22213a]/80 text-[#70d7ff] shadow-lg">
            <h3 className="text-xl font-bold mb-4">AI Analysis & Suggestions</h3>
            {Object.keys(headingsMap).map((key) => {
              const value = ai[key];
              if (!value) return null;

              return (
                <div key={key} className="mb-4 border-b border-[#69f0ae]/20 pb-2">
                  <h4 className="text-lg font-semibold mb-1">{headingsMap[key]}</h4>
                  <div className="ml-2 text-sm">{formatValue(value, key)}</div>
                </div>
              );
            })}
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-4 mt-4 justify-end">
          {report.pdfUrl && (
            <a
              href={report.pdfUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="px-4 py-2 rounded-xl bg-gradient-to-br from-[#11d1a7] via-[#456ceb] to-[#091e42] text-white font-bold shadow-lg hover:from-[#70d7ff] hover:to-[#456ceb]"
            >
              Download PDF
            </a>
          )}
          <motion.button
            onClick={onRemove}
            className="px-4 py-2 rounded-xl bg-gradient-to-br from-[#e05355] via-[#d9534f] to-[#b22222] text-white font-bold shadow-lg hover:from-[#ff6b6b] hover:to-[#e55353]"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Remove
          </motion.button>
        </div>
      </motion.div>
    </AnimatePresence>
  );
}
