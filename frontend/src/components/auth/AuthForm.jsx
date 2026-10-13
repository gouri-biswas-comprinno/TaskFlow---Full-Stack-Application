import { useState } from "react";
import { login, register } from "../../services/api.js";

export default function AuthForm({ onSignedIn }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const registering = mode === "register";

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    const normalizedEmail = email.trim();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      setError("Enter a valid email address.");
      return;
    }
    if (registering && password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    if (!password) {
      setError("Enter your password.");
      return;
    }

    setSubmitting(true);
    try {
      if (registering) {
        await register({ email: normalizedEmail, password });
        setMode("login");
        setPassword("");
        setMessage("Account created. Sign in to continue.");
      } else {
        const result = await login({ email: normalizedEmail, password });
        onSignedIn({ ...result, email: normalizedEmail });
      }
    } catch (requestError) {
      setError(requestError.message || "The request could not be completed.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-card" aria-labelledby="auth-heading">
      <p className="eyebrow">YOUR PERSONAL TASK SPACE</p>
      <h2 id="auth-heading">{registering ? "Create your account" : "Welcome back"}</h2>
      <p className="muted">
        {registering
          ? "Create an account to keep your tasks in one place."
          : "Sign in to see what needs your attention."}
      </p>
      {message && <p className="success-message" role="status">{message}</p>}
      {error && <p className="error-message" role="alert">{error}</p>}
      <form onSubmit={handleSubmit} noValidate>
        <label htmlFor="email">Email</label>
        <input
          autoComplete="email"
          id="email"
          name="email"
          onChange={(event) => setEmail(event.target.value)}
          required
          type="email"
          value={email}
        />
        <label htmlFor="password">Password</label>
        <input
          autoComplete={registering ? "new-password" : "current-password"}
          id="password"
          minLength={registering ? 8 : undefined}
          name="password"
          onChange={(event) => setPassword(event.target.value)}
          required
          type="password"
          value={password}
        />
        {registering && <p className="field-hint">At least 8 characters.</p>}
        <button className="primary-button full-width" disabled={submitting} type="submit">
          {submitting ? "Please wait…" : registering ? "Create account" : "Sign in"}
        </button>
      </form>
      <p className="auth-switch">
        {registering ? "Already have an account?" : "New to TaskFlow?"}{" "}
        <button
          className="text-button"
          onClick={() => {
            setMode(registering ? "login" : "register");
            setError("");
            setMessage("");
          }}
          type="button"
        >
          {registering ? "Sign in" : "Create an account"}
        </button>
      </p>
    </section>
  );
}
