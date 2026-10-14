import TaskEditor from "./TaskEditor.jsx";

function TaskDate({ label, value }) {
  const date = value ? new Date(value) : null;
  const isValidDate = date && !Number.isNaN(date.getTime());
  const formattedDate = isValidDate
    ? formatTaskTimestamp(date)
    : "Not available";

  return (
    <div className="task-detail-meta-item">
      <dt>{label}</dt>
      <dd>
        <time dateTime={isValidDate ? value : undefined}>{formattedDate}</time>
      </dd>
    </div>
  );
}

function formatTaskTimestamp(date) {
  const parts = new Intl.DateTimeFormat("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  }).formatToParts(date);
  const part = (type) => parts.find((item) => item.type === type)?.value ?? "";

  return `${part("day")} ${part("month")} ${part("year")}, ${part("hour")}:${part("minute")} ${part("dayPeriod").toUpperCase()}`;
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
