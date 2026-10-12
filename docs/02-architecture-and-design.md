# TaskFlow: Architecture and Design

## 1. Architecture overview (HLD)

TaskFlow is a small three-tier application:

```text
Browser
  React UI
    | HTTPS in a hosted deployment; local HTTP in development
    | JSON + Authorization: Bearer <JWT>
    v
  Spring Boot REST API
    | Spring Security JWT authentication + PostgreSQL revocation lookup
    | controller -> service -> repository
    v
  PostgreSQL
    users | tasks | revoked_tokens
```

Docker Compose provides the frontend, backend, and PostgreSQL for local development. pgAdmin is optional and local-only. GitHub Actions runs checks on pushes and pull requests. Deployment host and topology are open until assessment constraints are confirmed; do not substitute a different database or application stack.

## 2. Components and responsibilities (LLD)

### Frontend

- **Auth views/components:** registration and sign-in forms, validation, sign-out action.
- **Task views/components:** task list, create/edit form, completion control, delete action.
- **API client:** base URL, JSON/error handling, and bearer authorization header. Keep token handling in one place.
- **App/auth state:** signed-in state and current task data; do not treat frontend checks as authorization.
- No separate client-side router is required for the first release; a simple view/state flow is sufficient.

### Backend

- **Controllers:** map REST paths and request/response DTOs; do not contain persistence or ownership policy.
- **Services:** implement registration, authentication/logout, validation rules not covered at the boundary, and task ownership/business rules.
- **Repositories:** persist users, tasks, and revocations in PostgreSQL. Task queries must include the owner ID.
- **Security:** password encoder, authentication manager, JWT issue/validation, bearer filter, authorization rules, and denylist lookup. Protected requests must pass signature/expiry and denylist checks.
- **DTO/validation:** expose only contract fields; never serialize persistence entities or password hashes directly.
- **Database schema:** versioned SQL migrations make fresh environments reproducible.

### Database

| Table | Proposed columns and constraints |
|---|---|
| `users` | `id UUID` primary key; normalized `email` unique/not null; `password_hash` not null; `created_at` not null. |
| `tasks` | `id UUID` primary key; `owner_id UUID` not null FK to `users`; `title` not null; nullable `description`; `completed` not null default false; `created_at`, `updated_at` not null. Index `owner_id` for owner-scoped list/access. |
| `revoked_tokens` | `jti` primary key; `expires_at` not null; `revoked_at` not null. Index `expires_at` for optional cleanup. Store the unique token identifier, not the bearer token. |

All timestamps should be stored consistently (UTC/`timestamptz`). Deleting a user should cascade their tasks and revocation records only if that behavior is explicitly implemented; user deletion is not part of the first release.

## 3. Authentication and authorization flow

1. Registration normalizes the email, validates input, hashes the password with BCrypt, and persists the account. A uniqueness violation maps to a conflict response.
2. Sign-in checks the stored BCrypt hash. On success the API issues a signed access JWT with `sub` set to the user UUID, a cryptographically unique `jti`, `iat`, `exp`, and configured `iss`. The signing key is supplied through environment configuration, never committed.
3. The browser sends the token in the `Authorization: Bearer` header. Spring Security verifies signature, issuer, and expiry; it then checks whether `jti` is present in `revoked_tokens`. A revoked or unverifiable token is rejected before controller execution.
4. Logout requires a valid, non-revoked token. The API records its `jti` and expiration. Subsequent requests with that token fail even if its signature and expiry are otherwise valid. Expired tokens are rejected by normal JWT validation. Expired denylist rows may be cleaned up, but cleanup must not be required for revocation correctness.
5. The authenticated principal supplies the user ID for task operations. Service/repository calls always filter by both task ID and owner ID. Do not accept an owner ID from request data. Return `404` for a task not owned by the caller.
6. If the database/revocation lookup is unavailable, protected authentication fails closed rather than treating the token as unrevoked.

Proposed token signing is HS256 with a sufficiently long random secret injected as environment configuration. A public deployment must use HTTPS and an externally managed secret. Token storage in the browser is an owner decision (see the project brief); the frontend must never place a signing secret in its bundle.

## 4. API contract (proposed v1)

Base path: `/api`. JSON request/response bodies. All endpoints except register and sign-in require a valid bearer token; logout also requires one. Error responses use a consistent shape:

```json
{
  "timestamp": "ISO-8601",
  "status": 400,
  "error": "Bad Request",
  "message": "A safe, actionable message",
  "path": "/api/tasks"
}
```

Do not expose stack traces, SQL errors, password hashes, or token values.

| Method and path | Auth | Request | Success | Expected failure cases |
|---|---|---|---|---|
| `POST /api/auth/register` | No | `{ "email": "person@example.com", "password": "..." }` | `201` `{ "id": "uuid", "email": "person@example.com" }` | `400` invalid input; `409` email already registered. |
| `POST /api/auth/login` | No | `{ "email": "person@example.com", "password": "..." }` | `200` `{ "accessToken": "...", "tokenType": "Bearer", "expiresAt": "ISO-8601" }` | `400` malformed input; `401` invalid credentials. |
| `POST /api/auth/logout` | Yes | No body | `204` and current `jti` recorded as revoked | `401` missing, invalid, expired, or already-revoked token. |
| `GET /api/tasks` | Yes | No body | `200` array of current user's tasks, newest first | `401` invalid authentication. |
| `POST /api/tasks` | Yes | `{ "title": "Prepare demo", "description": "Optional" }` | `201` task representation | `400` invalid input; `401` invalid authentication. |
| `GET /api/tasks/{id}` | Yes | No body | `200` task if owned by caller | `404` unknown or non-owned task; `401` invalid authentication. |
| `PATCH /api/tasks/{id}` | Yes | One or more of `title`, `description`, `completed` | `200` updated task | `400` invalid/empty update; `404` unknown or non-owned task; `401` invalid authentication. |
| `DELETE /api/tasks/{id}` | Yes | No body | `204` | `404` unknown or non-owned task; `401` invalid authentication. |

Task representation:

```json
{
  "id": "uuid",
  "title": "Prepare demo",
  "description": "Optional",
  "completed": false,
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

The exact validation limits (email normalization, password length, title maximum, description maximum, and whether an empty description is equivalent to null) must be set in the first relevant construction unit and shared between API documentation and tests.

## 5. Key architecture decisions and rationale

| Decision | Rationale / trade-off |
|---|---|
| One React app, one Spring Boot API, and PostgreSQL | The simplest structure for the required feature and stack. |
| PostgreSQL for application state, including revocations | Durable owner/task data and revocation state survive backend restarts; supports the mandatory relational database requirement. |
| JWT bearer auth plus a database denylist | Meets the explicit revocation-before-expiry requirement. It is not fully stateless; the database lookup is a deliberate correctness/security cost. |
| Use `jti` as the revocation key | Avoids storing reusable bearer tokens; unique key supports a direct indexed lookup. |
| Derive task owner from authenticated principal | Prevents a client from claiming another user's ownership; ownership is enforced server-side. |
| Versioned SQL migrations | Reproducible schema for developers, tests, and containers. |
| Owner-scoped queries and `404` for non-owned task IDs | Access control is performed in persistence operations as well as service logic, with reduced existence disclosure. |
| pgAdmin is optional and local | Meets database administration needs without making the app depend on an admin UI or exposing it as a public service. |
| Compose for local development | Provides a repeatable local environment; assessment deployment details remain open. |

## 6. Runtime configuration and container design

Proposed services: `frontend`, `backend`, `db`; optional `pgadmin` profile. Use a named PostgreSQL volume, readiness health check, backend database dependency/readiness handling, and documented port bindings. Keep credentials and JWT signing key in an ignored local environment file or injected environment; provide a safe example file containing placeholders only. No secrets in Docker image layers, source, CI logs, or frontend build variables.

The deployment host and topology remain undecided pending confirmation of assessment constraints and suitable hosting options. Do not expose pgAdmin publicly or commit secrets. For any public deployment, confirm HTTPS, database persistence, and secure runtime secret configuration before approval.
