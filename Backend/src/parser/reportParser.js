// Responsible for parsing OCR text -> structured values

// reportParser.js
export function parseReportText(text) {
  const lines = text.split("\n").map(l => l.trim()).filter(Boolean);

  const results = [];

  for (const line of lines) {
    // Match: name  value  range  unit
    // Example: "Hemoglobin   13.5   12-16   g/dl"
    const match = line.match(/^([A-Za-z0-9 .'%+-]+?)\s+([\d.]+)\s+([\d.-]+)\s+([A-Za-z/%]+)$/);

    if (match) {
      const [, name, valueStr, rangeStr, unit] = match;
      const value = parseFloat(valueStr);

      // Split range into min and max (e.g., "12-16" → 12, 16)
      let minValue = null;
      let maxValue = null;
      if (rangeStr.includes("-")) {
        const [minStr, maxStr] = rangeStr.split("-");
        minValue = parseFloat(minStr);
        maxValue = parseFloat(maxStr);
      }

      results.push({
        name: name.trim(),
        value,
        unit,
        minValue,
        maxValue,
      });
    }
  }

  return results;
}
