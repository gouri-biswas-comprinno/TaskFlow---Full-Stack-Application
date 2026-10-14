import TaskEditor from "./TaskEditor.jsx";

function TaskDate({ label, value }) {
  if (!value) return null;

  return (
    <div className="task-detail-meta-item">
      <dt>{label}</dt>
      <dd>
        <time dateTime={value}>{value}</time>
      </dd>
    </div>
  );
}

export default function TaskDetails({
  editing,
  onBack,
  onCancelEdit,
  onEdit,
  onSave,
  saving,
  task,
}) {
  return (
    <section className="task-details" aria-labelledby="task-details-heading">
      <button className="secondary-button task-back-button" onClick={onBack} type="button">
        Back to tasks
      </button>
      {editing ? (
        <>
          <h3 id="task-details-heading">Edit Task</h3>
          <TaskEditor
            initialTask={task}
            onCancel={onCancelEdit}
            onSave={onSave}
            saving={saving}
          />
        </>
      ) : (
        <>
          <div className="task-details-heading">
            <div>
              <p className="eyebrow">TASK DETAILS</p>
              <h3 id="task-details-heading">{task.title}</h3>
            </div>
            <button className="primary-button" onClick={onEdit} type="button">
              Edit
            </button>
          </div>
          <p className="task-detail-status">
            Status: <strong>{task.completed ? "Completed" : "Active"}</strong>
          </p>
          <div className="task-detail-description">
            <h4>Description</h4>
            <p>{task.description || "No description provided."}</p>
          </div>
          <dl className="task-detail-meta">
            <TaskDate label="Created" value={task.createdAt} />
            <TaskDate label="Last updated" value={task.updatedAt} />
          </dl>
        </>
      )}
    </section>
  );
}
