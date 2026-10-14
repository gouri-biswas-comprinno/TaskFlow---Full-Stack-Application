import { useState } from "react";
import AuthForm from "./components/auth/AuthForm.jsx";
import Footer from "./components/layout/Footer.jsx";
import Header from "./components/layout/Header.jsx";
import TaskBoard from "./components/tasks/TaskBoard.jsx";
import "./App.css";

export default function App() {
  const [session, setSession] = useState(null);

  return session ? (
    <main className="app-shell dashboard-shell">
      <aside className="dashboard-sidebar" aria-label="TaskFlow sidebar">
        <a aria-label="TaskFlow home" className="brand sidebar-brand" href="/">
          <span aria-hidden="true" className="brand-mark">T</span>
          <span>TaskFlow</span>
        </a>
        <nav aria-label="Main navigation" className="sidebar-navigation">
          <a aria-current="page" className="sidebar-link" href="#task-workspace">
            <span aria-hidden="true">▦</span>
            <span>Tasks</span>
          </a>
        </nav>
        <div className="sidebar-account">
          <span aria-hidden="true" className="account-avatar">
            {session.email.slice(0, 1).toUpperCase()}
          </span>
          <span className="sidebar-account-email">{session.email}</span>
        </div>
      </aside>
      <div className="dashboard-main">
        <Header userEmail={session.email} />
        <TaskBoard
          key={session.accessToken}
          onSignedOut={() => setSession(null)}
          session={session}
        />
        <Footer />
      </div>
    </main>
  ) : (
    <main className="app-shell">
      <Header />
      <AuthForm onSignedIn={setSession} />
      <Footer />
    </main>
  );
}
