import { useEffect, useState } from "react";

export default function App() {
  const [health, setHealth] = useState("Checking...");

  useEffect(() => {
    fetch("/api/health")
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Backend responded with ${response.status}`);
        }
        return response.json();
      })
      .then((result) => setHealth(result.status))
      .catch(() => setHealth("Unavailable"));
  }, []);

  return (
    <main>
      <h1>TaskFlow</h1>
      <p>Application foundation is running.</p>
      <p>
        Backend health: <strong>{health}</strong>
      </p>
    </main>
  );
}
