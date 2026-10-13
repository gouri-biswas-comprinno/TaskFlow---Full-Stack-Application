import { useEffect, useState } from "react";
import {
  createTask,
  deleteTask,
  listTasks,
  login,
  logout,
  register,
  updateTask,
} from "./api.js";
import "./App.css";

function AuthForm({ onSignedIn }) {
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

function TaskEditor({ initialTask, onCancel, onSave, saving }) {
  const [title, setTitle] = useState(initialTask?.title ?? "");
  const [description, setDescription] = useState(initialTask?.description ?? "");
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setError("A task title is required.");
      return;
    }
    if (trimmedTitle.length > 200) {
      setError("Task title must be 200 characters or fewer.");
      return;
    }
    if (description.length > 2000) {
      setError("Description must be 2,000 characters or fewer.");
      return;
    }
    setError("");
    await onSave({ title: trimmedTitle, description: description.trim() || null });
  }

  return (
    <form className="task-editor" onSubmit={handleSubmit}>
      <label htmlFor={initialTask ? `edit-title-${initialTask.id}` : "new-task-title"}>
        Task title
      </label>
      <input
        autoFocus={Boolean(initialTask)}
        id={initialTask ? `edit-title-${initialTask.id}` : "new-task-title"}
        maxLength={200}
        onChange={(event) => setTitle(event.target.value)}
        required
        value={title}
      />
      <label htmlFor={initialTask ? `edit-description-${initialTask.id}` : "new-task-description"}>
        Description <span className="muted">(optional)</span>
      </label>
      <textarea
        id={initialTask ? `edit-description-${initialTask.id}` : "new-task-description"}
        maxLength={2000}
        onChange={(event) => setDescription(event.target.value)}
        rows={2}
        value={description}
      />
      {error && <p className="error-message" role="alert">{error}</p>}
      <div className="button-row">
        <button className="primary-button" disabled={saving} type="submit">
          {saving ? "Saving…" : initialTask ? "Save changes" : "Add task"}
        </button>
        {initialTask && (
          <button className="secondary-button" onClick={onCancel} type="button">
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}

function TaskBoard({ session, onSignedOut }) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState("");
  const [taskError, setTaskError] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [busyId, setBusyId] = useState("");
  const [creating, setCreating] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  useEffect(() => {
    let active = true;
    listTasks(session.accessToken)
      .then((result) => {
        if (active) setTasks(result);
      })
      .catch((requestError) => {
        if (active) setListError(requestError.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [session.accessToken]);

  async function addTask(fields) {
    setCreating(true);
    setTaskError("");
    try {
      const task = await createTask(session.accessToken, fields);
      setTasks((current) => [task, ...current]);
    } catch (requestError) {
      setTaskError(requestError.message);
    } finally {
      setCreating(false);
    }
  }

  async function saveTask(id, fields) {
    setBusyId(id);
    setTaskError("");
    try {
      const updated = await updateTask(session.accessToken, id, fields);
      setTasks((current) => current.map((task) => (task.id === id ? updated : task)));
      setEditingId(null);
    } catch (requestError) {
      setTaskError(requestError.message);
    } finally {
      setBusyId("");
    }
  }

  async function toggleTask(task) {
    setBusyId(task.id);
    setTaskError("");
    try {
      const updated = await updateTask(session.accessToken, task.id, {
        completed: !task.completed,
      });
      setTasks((current) => current.map((item) => (item.id === task.id ? updated : item)));
    } catch (requestError) {
      setTaskError(requestError.message);
    } finally {
      setBusyId("");
    }
  }

  async function removeTask(task) {
    setBusyId(task.id);
    setTaskError("");
    try {
      await deleteTask(session.accessToken, task.id);
      setTasks((current) => current.filter((item) => item.id !== task.id));
    } catch (requestError) {
      setTaskError(requestError.message);
    } finally {
      setBusyId("");
    }
  }

  async function handleLogout() {
    setLoggingOut(true);
    setTaskError("");
    try {
      await logout(session.accessToken);
      onSignedOut();
    } catch (requestError) {
      setTaskError(
        `Sign-out failed; server revocation was not confirmed. You are still signed in. ${requestError.message}`,
      );
    } finally {
      setLoggingOut(false);
    }
  }

  return (
    <section className="workspace" aria-labelledby="tasks-heading">
      <header className="workspace-header">
        <div>
          <p className="eyebrow">YOUR PERSONAL TASK SPACE</p>
          <h2 id="tasks-heading">My tasks</h2>
          <p className="muted signed-in-as">Signed in as {session.email}</p>
        </div>
        <button
          className="secondary-button"
          disabled={loggingOut}
          onClick={handleLogout}
          type="button"
        >
          {loggingOut ? "Signing out…" : "Sign out"}
        </button>
      </header>

      {taskError && <p className="error-message" role="alert">{taskError}</p>}
      <section className="add-task-panel" aria-label="Add a task">
        <h3>Capture a task</h3>
        <TaskEditor onSave={addTask} saving={creating} />
      </section>

      <section className="task-list-section" aria-label="Task list">
        <div className="section-heading">
          <h3>Task list</h3>
          {!loading && !listError && <span className="task-count">{tasks.length}</span>}
        </div>
        {loading && <p className="muted" role="status">Loading your tasks…</p>}
        {listError && <p className="error-message" role="alert">{listError}</p>}
        {!loading && !listError && tasks.length === 0 && (
          <p className="empty-state" role="status">No tasks yet. Add your first task above.</p>
        )}
        <ul className="task-list">
          {tasks.map((task) => (
            <li className={`task-card${task.completed ? " is-complete" : ""}`} key={task.id}>
              {editingId === task.id ? (
                <TaskEditor
                  initialTask={task}
                  onCancel={() => setEditingId(null)}
                  onSave={(fields) => saveTask(task.id, fields)}
                  saving={busyId === task.id}
                />
              ) : (
                <>
                  <div className="task-main">
                    <input
                      aria-label={`Mark ${task.title} complete`}
                      checked={task.completed}
                      disabled={busyId === task.id}
                      onChange={() => toggleTask(task)}
                      type="checkbox"
                    />
                    <div className="task-copy">
                      <h4>{task.title}</h4>
                      {task.description && <p>{task.description}</p>}
                    </div>
                  </div>
                  <div className="task-actions">
                    <button
                      className="text-button"
                      disabled={busyId === task.id}
                      onClick={() => {
                        setTaskError("");
                        setEditingId(task.id);
                      }}
                      type="button"
                    >
                      Edit
                    </button>
                    <button
                      className="text-button delete-button"
                      disabled={busyId === task.id}
                      onClick={() => removeTask(task)}
                      type="button"
                    >
                      Delete
                    </button>
                  </div>
                </>
              )}
            </li>
          ))}
        </ul>
      </section>
    </section>
  );
}

export default function App() {
  const [session, setSession] = useState(null);

  return (
    <main className="app-shell">
      <header className="site-header">
        <a aria-label="TaskFlow home" className="brand" href="/">
          <span aria-hidden="true" className="brand-mark">T</span>
          <span>TaskFlow</span>
        </a>
        <span className="header-note">A little more focus, every day.</span>
      </header>
      {session ? (
        <TaskBoard key={session.accessToken} onSignedOut={() => setSession(null)} session={session} />
      ) : (
        <AuthForm onSignedIn={setSession} />
      )}
      <footer className="site-footer">Your tasks are private to your account.</footer>
    </main>
  );
}
