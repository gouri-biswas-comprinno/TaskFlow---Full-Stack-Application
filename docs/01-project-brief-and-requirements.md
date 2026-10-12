# TaskFlow: Project Brief and Requirements

## 1. Purpose and outcome

TaskFlow is a deliberately small personal task manager for the assessment **Build & Present a Full Stack Feature (Design + Code + Deploy)**. Its value is demonstrating a traceable software lifecycle and a working full-stack slice, rather than a broad product feature set.

The planned outcome is a locally reproducible, containerized React + Spring Boot + PostgreSQL application. A user can register, authenticate, revoke their access token by signing out, and perform CRUD operations on tasks they own. Automated tests and GitHub Actions checks provide repeatable evidence; deployment and release verification are planned but not authorized by this inception document.

## 2. Scope

### In scope

- Email-and-password registration and sign-in.
- Sign-out that revokes the current JWT on the server, so it is rejected before its expiry.
- Authenticated task create, list, update, and delete operations.
- Authorization that prevents one user from reading or changing another user's tasks.
- React user interface for authentication and task management.
- PostgreSQL persistence, administered locally with pgAdmin.
- Docker images and Docker Compose for frontend, backend, and database; pgAdmin may be an optional Compose service/profile.
- Backend, frontend, integration, and security-focused tests, plus manual user acceptance testing.
- GitHub Actions checks on pushes and pull requests.
- Architecture, lifecycle, setup, troubleshooting, and release documentation.

### Out of scope for the assessment-sized first release

- Social login, email verification, password reset, refresh tokens, roles/teams, task sharing, labels, attachments, reminders, recurring tasks, search, and real-time updates.
- Microservices, Kubernetes, cloud-specific infrastructure, production-grade observability, and a public multi-tenant service.
- A promise of external hosting until a deployment target and its account/credential requirements are agreed.

## 3. Users and user stories

| ID | User story | Acceptance criteria |
|---|---|---|
| US-01 | As a new user, I want to register with my email and password so I can use TaskFlow. | Valid input creates one account; email is normalized consistently and is unique; invalid input receives a useful client error; the response never contains the password or password hash. |
| US-02 | As a registered user, I want to sign in so I can access my tasks. | Valid credentials return a signed access token and expiry metadata; invalid credentials are rejected without revealing which credential was wrong. |
| US-03 | As a signed-in user, I want to sign out so my current token can no longer be used. | Sign-out revokes the presented token server-side; subsequent use of that token fails before its expiry; another valid user's token is unaffected. |
| US-04 | As a signed-in user, I want to create and view my tasks so I can track work. | A valid task is persisted and shown to its owner; unauthenticated calls are rejected; task fields are validated. |
| US-05 | As a signed-in user, I want to update my tasks so I can keep their details and completion status current. | The owner can update allowed fields; attempts to access another user's task do not disclose or modify it. |
| US-06 | As a signed-in user, I want to delete my tasks so I can remove work I no longer need. | The owner can delete their task; attempts to delete another user's task do not disclose or modify it. |
| US-07 | As an assessor/developer, I want a reproducible test and run workflow so I can verify the feature. | Documented commands start the stack and run relevant checks; CI runs automated checks for pushes and pull requests; failures are visible and fail the workflow. |

## 4. Functional requirements

1. The system shall register users with a normalized, unique email and a securely hashed password.
2. The system shall authenticate credentials and issue a signed JWT containing a user subject, unique token identifier (`jti`), issued-at time, and expiry.
3. The system shall validate JWT signature, expiry, and expected issuer/configuration before granting access.
4. The system shall persist token revocations server-side and reject a revoked token on every protected request before token expiry.
5. The system shall require authentication for all task endpoints.
6. The system shall scope every task read, update, and delete to the authenticated user on the server; client-supplied ownership is never trusted.
7. The system shall create, list, update, and delete tasks with validation and consistent HTTP status/error responses.
8. The frontend shall support registration, sign-in, sign-out, and task CRUD against the API.
9. The development environment shall run the frontend, backend, and PostgreSQL using Docker Compose. pgAdmin shall be available for local database inspection, preferably as an optional service.
10. GitHub Actions shall run backend and frontend checks on pushes and pull requests.
11. The project shall include test evidence and practical setup, architecture, data-flow, troubleshooting, and release instructions.

## 5. Non-functional requirements and quality targets

| Area | Target |
|---|---|
| Security | BCrypt password hashing; JWT signature and expiry validation; denylist check for every protected request; fail closed if revocation state cannot be checked; server-enforced per-user task access; no secrets committed to Git. |
| Privacy | Never return password hashes, signing secrets, or raw authorization tokens in API responses or application logs. Use generic sign-in failure messages. |
| Correctness | Persistent data survives application/container restarts when the Compose database volume is retained. Database schema changes are versioned. |
| Testability | Each construction unit has focused acceptance criteria and tests. CI runs deterministic automated checks; user acceptance scenarios are documented for manual verification. |
| Maintainability | Keep a single frontend and a single backend application with clear UI / controller / service / repository / security boundaries. Avoid needless abstractions. |
| Usability | Essential flows have clear success, validation, loading, and error states; no unrelated product features are required. |
| Reproducibility | A developer can follow the documented prerequisites and Compose workflow on a supported machine. Tool versions and commands will be recorded during construction. |
| Delivery | Pull requests and pushes receive automated quality checks. The owner reviews each unit and makes/ pushes commits manually; no agent-created commits. |

## 6. Constraints, assumptions, and open decisions

### Constraints

- Assessment deadline is less than one week.
- The mandatory stack and security requirements are fixed, including JWT revocation, tests, Docker, and CI/CD.
- Work is incremental. Each unit needs focused scope, acceptance criteria, tests, and owner approval before the next unit.
- This stage is planning only: no application code, dependencies, containers, or deployment.

### Planning assumptions (confirm before construction)

- Single-user-owned tasks; no shared access or roles.
- One short-lived access JWT (proposed 30-minute lifetime); no refresh-token flow in the first release.
- Task fields are title, optional description, completion status, and server-managed creation/update timestamps. No due dates or priorities unless requested.
- Email is the login identifier; password minimum length proposed as 8 characters.
- API uses JSON and bearer tokens. Unknown or non-owned task IDs return `404` to reduce resource-existence disclosure.
- Local Docker Compose is the reproducible deployment target unless an external host is selected.
- pgAdmin is a local administration aid, not an application runtime dependency.

### Decisions requiring owner confirmation

1. Approve the proposed minimal task fields and exclusion of extra task features.
2. Approve the proposed email/password rules, 8-character minimum, and 30-minute access-token lifetime.
3. Choose browser token storage: in-memory (safer against persistence, but reload signs out) or `sessionStorage` (survives reload in a tab, but remains exposed to XSS). The recommendation is in-memory for the smallest security footprint, with a clear reload-to-sign-in behavior.
4. Confirm local Compose-only delivery or name the external deployment target and available time/accounts.
5. Confirm the target operating system/runtime constraints and preferred versions, if the assessment specifies them. Proposed baseline: Java 21 LTS, a compatible Spring Boot release, Node.js LTS, and PostgreSQL 16 or later.
6. Approve optional pgAdmin in a Compose profile and whether CI should also build container images (recommended: build only; do not publish images).

## 7. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Less than one week for implementation, debugging, presentation, and deployment | High | Freeze the scope, build one end-to-end vertical slice at a time, reserve the final day for integration and presentation evidence, and defer all out-of-scope features. |
| JWT revocation introduces state into otherwise stateless authentication | Medium | Keep a small PostgreSQL denylist indexed by `jti` and expiry; test revoked-token rejection through the actual security filter; fail closed on database/denylist errors. |
| CORS, token storage, and logout semantics can create browser/API integration bugs | Medium | Settle token storage before frontend auth work; test browser requests and revoked-token behavior; avoid logging tokens. |
| Docker networking/health and local port conflicts delay integration | Medium | Document ports and prerequisites, add database health checks and service readiness handling, and validate Compose early as its own unit. |
| External deployment credentials or platform setup consume assessment time | High if required | Confirm the host now; keep local Compose as the baseline and do not promise external deployment until the target is known. |
| Tests are postponed until integration | High | Make test evidence and unit-level test work explicit in every construction unit and enforce checks in CI before feature completion. |
