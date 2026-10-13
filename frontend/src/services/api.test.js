import { afterEach, describe, expect, it, vi } from "vitest";
import {
  ApiError,
  createTask,
  deleteTask,
  listTasks,
  login,
  logout,
  register,
  updateTask,
} from "./api.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("TaskFlow API client", () => {
  it("posts registration and login JSON without a bearer token", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(Response.json({ id: "user-1", email: "a@example.com" }, { status: 201 }))
      .mockResolvedValueOnce(Response.json({
        accessToken: "signed-token",
        tokenType: "Bearer",
        expiresAt: "2030-01-01T00:30:00Z",
      }));
    vi.stubGlobal("fetch", fetchMock);

    await register({ email: "a@example.com", password: "password1" });
    const result = await login({ email: "a@example.com", password: "password1" });

    expect(fetchMock.mock.calls[0][0]).toBe("/api/auth/register");
    expect(fetchMock.mock.calls[0][1]).toMatchObject({
      method: "POST",
      headers: { Accept: "application/json", "Content-Type": "application/json" },
      body: JSON.stringify({ email: "a@example.com", password: "password1" }),
    });
    expect(fetchMock.mock.calls[0][1].headers.Authorization).toBeUndefined();
    expect(result.accessToken).toBe("signed-token");
  });

  it("sends the bearer token and the documented task methods and payloads", async () => {
    const task = {
      id: "task-1",
      title: "Read",
      description: null,
      completed: false,
      createdAt: "2030-01-01T00:00:00Z",
      updatedAt: "2030-01-01T00:00:00Z",
    };
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(Response.json([task]))
      .mockResolvedValueOnce(Response.json(task, { status: 201 }))
      .mockResolvedValueOnce(Response.json({ ...task, completed: true }))
      .mockResolvedValueOnce(new Response(null, { status: 204 }))
      .mockResolvedValueOnce(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);

    expect(await listTasks("jwt")).toEqual([task]);
    await createTask("jwt", { title: "Read", description: null });
    await updateTask("jwt", "task / 1", { completed: true });
    await deleteTask("jwt", "task-1");
    await logout("jwt");

    expect(fetchMock.mock.calls.map(([url, options]) => [url, options.method])).toEqual([
      ["/api/tasks", "GET"],
      ["/api/tasks", "POST"],
      ["/api/tasks/task%20%2F%201", "PATCH"],
      ["/api/tasks/task-1", "DELETE"],
      ["/api/auth/logout", "POST"],
    ]);
    for (const [, options] of fetchMock.mock.calls) {
      expect(options.headers.Authorization).toBe("Bearer jwt");
    }
    expect(fetchMock.mock.calls[1][1].body).toBe(
      JSON.stringify({ title: "Read", description: null }),
    );
    expect(fetchMock.mock.calls[2][1].body).toBe(JSON.stringify({ completed: true }));
  });

  it("uses API error messages and reports network failures", async () => {
    vi.stubGlobal("fetch", vi.fn()
      .mockResolvedValueOnce(Response.json(
        { message: "Task not found", status: 404 },
        { status: 404 },
      ))
      .mockRejectedValueOnce(new TypeError("connection details")));

    await expect(listTasks("jwt")).rejects.toMatchObject({
      name: "ApiError",
      status: 404,
      message: "Task not found",
    });
    await expect(listTasks("jwt")).rejects.toThrow(
      "Could not reach TaskFlow. Check your connection and try again.",
    );
    expect(new ApiError("example", 400).status).toBe(400);
  });

  it("requires the documented 204 logout response before treating revocation as confirmed", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(
      Response.json({ message: "Unexpected response" }, { status: 200 }),
    ));

    await expect(logout("jwt")).rejects.toMatchObject({
      status: 200,
      message: "Logout was not confirmed by the server.",
    });
  });
});
