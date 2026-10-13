const API_BASE_URL = "/api";

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function request(path, { token, body, expectedStatus, method = "GET" } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    });
  } catch {
    throw new ApiError("Could not reach TaskFlow. Check your connection and try again.");
  }

  const responseBody = await response.text();
  let data;
  if (responseBody) {
    try {
      data = JSON.parse(responseBody);
    } catch {
      data = null;
    }
  }

  if (!response.ok || (expectedStatus !== undefined && response.status !== expectedStatus)) {
    throw new ApiError(
      expectedStatus === 204 && response.ok
        ? "Logout was not confirmed by the server."
        : data?.message || `TaskFlow request failed (${response.status}).`,
      response.status,
    );
  }
  if (response.status === 204 || !responseBody) return undefined;
  if (data === null) throw new ApiError("TaskFlow returned an unreadable response.", response.status);
  return data;
}

export function register(credentials) {
  return request("/auth/register", { method: "POST", body: credentials });
}

export function login(credentials) {
  return request("/auth/login", { method: "POST", body: credentials });
}

export function logout(token) {
  return request("/auth/logout", { expectedStatus: 204, method: "POST", token });
}

export function listTasks(token) {
  return request("/tasks", { token });
}

export function createTask(token, task) {
  return request("/tasks", { method: "POST", token, body: task });
}

export function updateTask(token, id, changes) {
  return request(`/tasks/${encodeURIComponent(id)}`, {
    method: "PATCH",
    token,
    body: changes,
  });
}

export function deleteTask(token, id) {
  return request(`/tasks/${encodeURIComponent(id)}`, { method: "DELETE", token });
}
