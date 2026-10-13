import { useState } from "react";
import AuthForm from "./components/auth/AuthForm.jsx";
import Footer from "./components/layout/Footer.jsx";
import Header from "./components/layout/Header.jsx";
import TaskBoard from "./components/tasks/TaskBoard.jsx";
import "./App.css";

export default function App() {
  const [session, setSession] = useState(null);

  return (
    <main className="app-shell">
      <Header />
      {session ? (
        <TaskBoard key={session.accessToken} onSignedOut={() => setSession(null)} session={session} />
      ) : (
        <AuthForm onSignedIn={setSession} />
      )}
      <Footer />
    </main>
  );
}
