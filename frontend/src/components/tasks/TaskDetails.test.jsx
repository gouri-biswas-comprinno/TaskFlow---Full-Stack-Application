import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import TaskDetails from "./TaskDetails.jsx";

const task = {
  id: "task-1",
  title: "Prepare slides",
  description: "Draft the demo",
  completed: false,
  createdAt: "2026-10-13T18:00:00.152911Z",
  updatedAt: "2026-10-13T18:00:00.152911Z",
};

describe("TaskDetails timestamps", () => {
  it("shows readable local date and time without exposing the raw ISO format", () => {
    render(
      <TaskDetails
        editing={false}
        onBack={() => {}}
        onCancelEdit={() => {}}
        onEdit={() => {}}
        onSave={() => {}}
        saving={false}
        task={task}
      />,
    );

    const expected = new Intl.DateTimeFormat("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    })
      .formatToParts(new Date(task.createdAt))
      .reduce((parts, item) => ({ ...parts, [item.type]: item.value }), {});
    const expectedText =
      `${expected.day} ${expected.month} ${expected.year}, ${expected.hour}:${expected.minute} ${expected.dayPeriod.toUpperCase()}`;
    const timestamps = screen.getAllByText(expectedText);

    expect(timestamps).toHaveLength(2);
    expect(timestamps[0]).toHaveAttribute("datetime", task.createdAt);
    expect(timestamps[0]).not.toHaveTextContent(/[TZ]|\.\d+/);
  });

  it("shows a safe fallback for missing and invalid timestamps", () => {
    render(
      <TaskDetails
        editing={false}
        onBack={() => {}}
        onCancelEdit={() => {}}
        onEdit={() => {}}
        onSave={() => {}}
        saving={false}
        task={{ ...task, createdAt: null, updatedAt: "not-a-date" }}
      />,
    );

    expect(screen.getAllByText("Not available")).toHaveLength(2);
    expect(screen.getAllByText("Not available")[0]).not.toHaveAttribute("datetime");
  });
});
