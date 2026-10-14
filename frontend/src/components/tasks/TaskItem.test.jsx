import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TaskItem from "./TaskItem.jsx";

const task = {
  id: "task-1",
  title: "Prepare slides",
  description: "Draft the demo",
  completed: false,
};

describe("TaskItem", () => {
  it("renders a task and exposes edit, delete, and completion actions", async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onOpen = vi.fn();
    const onToggle = vi.fn();

    render(
      <ul>
        <TaskItem
          busy={false}
          editing={false}
          onDelete={onDelete}
          onEdit={onEdit}
          onOpen={onOpen}
          onSave={vi.fn()}
          onToggle={onToggle}
          task={task}
        />
      </ul>,
    );

    expect(screen.getByText(task.title)).toBeInTheDocument();
    expect(screen.getByText(task.description)).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /Prepare slides/ }));
    await user.click(screen.getByRole("button", { name: "Edit" }));
    await user.click(screen.getByRole("button", { name: "Delete" }));
    await user.click(screen.getByRole("checkbox", { name: "Mark Prepare slides complete" }));

    expect(onEdit).toHaveBeenCalledOnce();
    expect(onDelete).toHaveBeenCalledOnce();
    expect(onToggle).toHaveBeenCalledOnce();
    expect(onOpen).toHaveBeenCalledOnce();
  });

  it("renders the editor when editing", () => {
    render(
      <ul>
        <TaskItem
          busy={false}
          editing
          onDelete={vi.fn()}
          onEdit={vi.fn()}
          onOpen={vi.fn()}
          onSave={vi.fn()}
          onToggle={vi.fn()}
          task={task}
        />
      </ul>,
    );

    expect(screen.getByLabelText("Task title")).toHaveValue(task.title);
    expect(screen.getByRole("button", { name: "Save Changes" })).toBeInTheDocument();
  });
});
