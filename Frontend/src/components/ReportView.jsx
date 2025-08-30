import { motion, AnimatePresence } from "framer-motion";
import jsPDF from "jspdf";
import "jspdf-autotable";

export default function ReportView({ report, onRemove }) {
  if (!report || !report.values || !report.values.length) {
    return <p className="text-gray-400 mt-4">No report data to display.</p>;
  }

  const handleDownloadPDF = () => {
    const doc = new jsPDF();

    doc.setFontSize(18);
    doc.setTextColor(50, 150, 255); // neon blue title
    doc.text("Medical Report", 14, 20);

    const tableColumn = ["Parameter", "Value", "Unit", "Safe Range"];
    const tableRows = [];

    report.values.forEach((val) => {
      const rowData = [
        val.parameter.name,
        val.value,
        val.parameter.unit,
        `${val.parameter.minValue} - ${val.parameter.maxValue}`,
      ];
      tableRows.push(rowData);
    });

    doc.autoTable({
      startY: 30,
      head: [tableColumn],
      body: tableRows,
      headStyles: {
        fillColor: [60, 80, 150], // dark neon header
        textColor: 255,
        fontStyle: "bold",
      },
      bodyStyles: {
        fillColor: [30, 30, 50],
        textColor: 180,
      },
      alternateRowStyles: {
        fillColor: [40, 40, 70],
      },
      margin: { top: 10, left: 14, right: 14 },
    });

    doc.save(`report_${Date.now()}.pdf`);
  };

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={report.id || "report"}
        className="mt-6 w-full border border-[#69f0ae]/40 rounded-2xl bg-[#1a1830]/80 shadow-xl backdrop-blur-lg p-4 flex flex-col gap-4"
        initial={{ opacity: 0, scale: 0.94, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.94, y: 20 }}
        transition={{ duration: 0.7 }}
      >
        <h2 className="text-[#70d7ff] text-xl font-bold mb-2 drop-shadow-md">
          Report Details
        </h2>

        <div className="overflow-x-auto">
          <table className="w-full table-auto border-collapse text-sm">
            <thead>
              <tr className="bg-[#22213a]/80 text-[#70d7ff]">
                <th className="border px-2 py-1 text-left">Parameter</th>
                <th className="border px-2 py-1 text-center">Value</th>
                <th className="border px-2 py-1 text-center">Unit</th>
                <th className="border px-2 py-1 text-center">Safe Range</th>
              </tr>
            </thead>
            <tbody>
              {report.values.map((val) => (
                <motion.tr
                  key={val.id}
                  className="hover:bg-[#29405b]/50 transition-colors"
                  whileHover={{ scale: 1.01 }}
                  transition={{ duration: 0.2 }}
                >
                  <td className="border px-2 py-1">{val.parameter.name}</td>
                  <td className="border px-2 py-1 text-center">{val.value}</td>
                  <td className="border px-2 py-1 text-center">
                    {val.parameter.unit}
                  </td>
                  <td className="border px-2 py-1 text-center">
                    {val.parameter.minValue} - {val.parameter.maxValue}
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-4 mt-4 justify-end">
          <motion.button
            onClick={handleDownloadPDF}
            className="px-4 py-2 rounded-xl bg-gradient-to-br from-[#11d1a7] via-[#456ceb] to-[#091e42] text-white font-bold shadow-lg hover:from-[#70d7ff] hover:to-[#456ceb]"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Download PDF
          </motion.button>

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
