import express from "express";
import cors from "cors";
import reportRoutes from "./routes/reportRoutes.js";
import path from "path";

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// CORS (allow frontend)
app.use(
  cors({
    origin: ["http://localhost:5173","https://mednemesis.onrender.com"],
    methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
  })
);

// Serve temporary uploaded PDFs
app.use("/uploads", express.static(path.join(process.cwd(), "uploads")));

app.use("/reports", reportRoutes);

const PORT = 4000;
app.listen(PORT, () => {
  console.log(`🚀 Server running on http://localhost:${PORT}`);
});
