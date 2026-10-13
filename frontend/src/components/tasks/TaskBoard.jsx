import { useEffect, useState } from "react";
import { createTask, deleteTask, listTasks, logout, updateTask } from "../../services/api.js";
import TaskEditor from "./TaskEditor.jsx";
import TaskItem from "./TaskItem.jsx";

export default function TaskBoard({ session, onSignedOut }) {
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
            <TaskItem
              busy={busyId === task.id}
              editing={editingId === task.id}
              key={task.id}
              onDelete={() => removeTask(task)}
              onEdit={() => {
                setTaskError("");
                setEditingId(editingId === task.id ? null : task.id);
              }}
              onSave={(fields) => saveTask(task.id, fields)}
              onToggle={() => toggleTask(task)}
              task={task}
            />
          ))}
        </ul>
      </section>
    </section>
  );
}
