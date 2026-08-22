const BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function analyzeReport(files) {
  if (!files || files.length === 0) {
    throw new Error("At least one report file is required.");
  }

  const formData = new FormData();

  files.forEach((file) => {
    formData.append("files", file);
  });

  const response = await fetch(
    `${BASE_URL}/api/reports/analyze`,
    {
      method: "POST",
      body: formData,
    }
  );

  let data;

  try {
    data = await response.json();
  } catch {
    throw new Error(
      "The server returned an invalid response."
    );
  }

  if (!response.ok || !data.success) {
    throw new Error(
      data.error ||
        data.message ||
        "Report analysis failed."
    );
  }

  return {
    success: data.success,
    filename: data.filename,
    ocrText: data.ocrText,
    analysis: data.analysis,
    pdfUrl: data.pdfUrl
      ? `${BASE_URL}${data.pdfUrl}`
      : null,
  };
}