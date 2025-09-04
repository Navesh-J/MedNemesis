import { useEffect } from "react";
import Home from "./pages/Home";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:4000";

function App() {
  useEffect(() => {
    // Ping backend once when the app loads to wake server
    fetch(`${BASE_URL}`)
      .then((res) => console.log("Backend pinged:", res.status))
      .catch((err) => console.error("Ping failed:", err));
  }, []);

  return <Home />;
}

export default App;
