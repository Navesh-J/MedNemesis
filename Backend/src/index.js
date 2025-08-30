import express from "express";
import reportRoutes from "./routes/reportRoutes.js";

const app = express();
app.use(express.json());

app.use("/reports", reportRoutes);

const PORT = 4000;
app.listen(PORT, () => {
  console.log(`🚀 Server running on http://localhost:${PORT}`);
});
