import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import App from "./App.jsx";
import * as api from "./services/api.js";

vi.mock("./services/api.js", () => ({
  createTask: vi.fn(),
  deleteTask: vi.fn(),
  listTasks: vi.fn(),
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
  updateTask: vi.fn(),
}));

const session = {
  accessToken: "token-in-memory",
  expiresAt: "2030-01-01T00:30:00Z",
  email: "owner@example.com",
};

const initialTask = {
  id: "task-1",
  title: "Prepare slides",
  description: "Draft the demo",
  completed: false,
  createdAt: "2030-01-01T00:00:00Z",
  updatedAt: "2030-01-01T00:00:00Z",
};

beforeEach(() => {
  vi.clearAllMocks();
  api.listTasks.mockResolvedValue([]);
});

afterEach(() => {
  cleanup();
});

describe("TaskFlow user interface", () => {
  it("validates registration, shows API errors, then signs in after registration", async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole("button", { name: "Create an account" }));
    await user.type(screen.getByLabelText("Email"), "not-an-email");
    await user.type(screen.getByLabelText("Password"), "short");
    await user.click(screen.getByRole("button", { name: "Create account" }));

    expect(screen.getByRole("alert")).toHaveTextContent("Enter a valid email address.");
    expect(api.register).not.toHaveBeenCalled();

    await user.clear(screen.getByLabelText("Email"));
    await user.type(screen.getByLabelText("Email"), "owner@example.com");
    await user.clear(screen.getByLabelText("Password"));
    await user.type(screen.getByLabelText("Password"), "password1");
    api.register.mockRejectedValueOnce(new Error("Email is already registered"));
    await user.click(screen.getByRole("button", { name: "Create account" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Email is already registered");

    api.register.mockResolvedValueOnce({ id: "user-1", email: "owner@example.com" });
    await user.click(screen.getByRole("button", { name: "Create account" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Account created. Sign in to continue.");
    expect(api.register).toHaveBeenLastCalledWith({
      email: "owner@example.com",
      password: "password1",
    });
    expect(screen.getByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
  });

  it("signs in, creates, edits, completes, and deletes a task", async () => {
    const user = userEvent.setup();
    api.login.mockResolvedValueOnce(session);
    api.createTask.mockResolvedValueOnce(initialTask);
    api.updateTask
      .mockResolvedValueOnce({ ...initialTask, title: "Updated slides" })
      .mockResolvedValueOnce({ ...initialTask, title: "Updated slides", completed: true });
    api.deleteTask.mockResolvedValueOnce(undefined);
    api.listTasks.mockResolvedValueOnce([]);
    render(<App />);

    await user.type(screen.getByLabelText("Email"), session.email);
    await user.type(screen.getByLabelText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    expect(await screen.findByRole("heading", { name: "My tasks" })).toBeInTheDocument();
    expect(api.listTasks).toHaveBeenCalledWith(session.accessToken);

    await user.type(screen.getByLabelText("Task title"), "Prepare slides");
    await user.type(screen.getByLabelText("Description (optional)"), "Draft the demo");
    await user.click(screen.getByRole("button", { name: "Add task" }));
    expect(await screen.findByRole("heading", { name: "Prepare slides" })).toBeInTheDocument();
    expect(api.createTask).toHaveBeenCalledWith(session.accessToken, {
      title: "Prepare slides",
      description: "Draft the demo",
    });

    const card = screen.getByText("Prepare slides").closest("li");
    await user.click(within(card).getByRole("button", { name: "Edit" }));
    const titleInput = within(card).getByLabelText("Task title");
    await user.clear(titleInput);
    await user.type(titleInput, "Updated slides");
    await user.click(screen.getByRole("button", { name: "Save changes" }));
    expect(await screen.findByRole("heading", { name: "Updated slides" })).toBeInTheDocument();

    await user.click(screen.getByRole("checkbox", { name: "Mark Updated slides complete" }));
    await waitFor(() => expect(api.updateTask).toHaveBeenLastCalledWith(
      session.accessToken,
      "task-1",
      { completed: true },
    ));
    expect(screen.getByRole("checkbox", { name: "Mark Updated slides complete" })).toBeChecked();

    await user.click(within(screen.getByText("Updated slides").closest("li"))
      .getByRole("button", { name: "Delete" }));
    await waitFor(() => expect(api.deleteTask).toHaveBeenCalledWith(session.accessToken, "task-1"));
    expect(await screen.findByText("No tasks yet. Add your first task above.")).toBeInTheDocument();
  });

  it("does not clear in-memory auth or claim revocation when logout fails", async () => {
    const user = userEvent.setup();
    api.login.mockResolvedValueOnce(session);
    api.logout.mockRejectedValueOnce(new Error("Could not reach TaskFlow"));
    render(<App />);

    await user.type(screen.getByLabelText("Email"), session.email);
    await user.type(screen.getByLabelText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    await user.click(await screen.findByRole("button", { name: "Sign out" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Sign-out failed; server revocation was not confirmed. You are still signed in.",
    );
    expect(screen.getByRole("heading", { name: "My tasks" })).toBeInTheDocument();
    expect(api.logout).toHaveBeenCalledWith(session.accessToken);
  });

  it("clears auth only after successful logout and starts signed out on a new mount", async () => {
    const user = userEvent.setup();
    api.login.mockResolvedValueOnce(session);
    api.logout.mockResolvedValueOnce(undefined);
    const view = render(<App />);

    await user.type(screen.getByLabelText("Email"), session.email);
    await user.type(screen.getByLabelText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    await user.click(await screen.findByRole("button", { name: "Sign out" }));
    expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();

    view.unmount();
    api.listTasks.mockClear();
    render(<App />);
    expect(screen.getByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
    expect(api.listTasks).not.toHaveBeenCalled();
    expect(localStorage.getItem("taskflow-token")).toBeNull();
    expect(sessionStorage.getItem("taskflow-token")).toBeNull();
  });

  it("shows the empty state and task API failures", async () => {
    const user = userEvent.setup();
    api.login.mockResolvedValueOnce(session);
    api.listTasks.mockRejectedValueOnce(new Error("Tasks are temporarily unavailable"));
    render(<App />);

    await user.type(screen.getByLabelText("Email"), session.email);
    await user.type(screen.getByLabelText("Password"), "password1");
    await user.click(screen.getByRole("button", { name: "Sign in" }));
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Tasks are temporarily unavailable",
    );

  });
});
