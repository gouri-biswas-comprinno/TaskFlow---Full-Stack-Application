import TaskEditor from "./TaskEditor.jsx";

export default function TaskItem({
  busy,
  onDelete,
  onEdit,
  onOpen,
  onSave,
  onToggle,
  task,
  editing,
}) {
  return (
    <li className={`task-card${task.completed ? " is-complete" : ""}`}>
      {editing ? (
        <TaskEditor
          initialTask={task}
          onCancel={onEdit}
          onSave={onSave}
          saving={busy}
        />
      ) : (
        <>
          <div className="task-main">
            <input
              aria-label={`Mark ${task.title} complete`}
              checked={task.completed}
              disabled={busy}
              onChange={onToggle}
              type="checkbox"
            />
            <div className="task-copy">
              <button
                className="task-open-button"
                onClick={onOpen}
                type="button"
              >
                <h4>{task.title}</h4>
                {task.description && <p>{task.description}</p>}
              </button>
            </div>
          </div>
          <div className="task-actions">
            <button className="text-button" disabled={busy} onClick={onEdit} type="button">
              Edit
            </button>
            <button
              className="text-button delete-button"
              disabled={busy}
              onClick={onDelete}
              type="button"
            >
              Delete
            </button>
          </div>
        </>
      )}
    </li>
  );
}
