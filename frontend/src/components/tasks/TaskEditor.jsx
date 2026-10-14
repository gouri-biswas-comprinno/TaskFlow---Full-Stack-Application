import { useState } from "react";

export default function TaskEditor({ initialTask, onCancel, onSave, saving }) {
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
    const saved = await onSave({ title: trimmedTitle, description: description.trim() || null });
    if (!initialTask && saved) {
      setTitle("");
      setDescription("");
    }
  }

  return (
    <form className={`task-editor${initialTask ? " is-editing" : ""}`} onSubmit={handleSubmit}>
      <label htmlFor={initialTask ? `edit-title-${initialTask.id}` : "new-task-title"}>
        Task title
      </label>
      <input
        autoFocus={Boolean(initialTask)}
        id={initialTask ? `edit-title-${initialTask.id}` : "new-task-title"}
        maxLength={200}
        onChange={(event) => setTitle(event.target.value)}
        placeholder="What needs to get done?"
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
        placeholder="Add details to help you remember."
        rows={2}
        value={description}
      />
      {error && <p className="error-message" role="alert">{error}</p>}
      <div className="button-row">
        <button className="primary-button" disabled={saving} type="submit">
          {saving ? "Saving…" : initialTask ? "Save Changes" : "Add task"}
        </button>
        {initialTask && (
          <button className="secondary-button cancel-edit-button" onClick={onCancel} type="button">
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
