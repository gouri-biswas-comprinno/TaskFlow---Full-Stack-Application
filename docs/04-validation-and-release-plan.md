# TaskFlow: Validation and Release Plan

## 1. Test strategy

Tests are added with the behavior they protect, not deferred to the end. Exact framework/library versions and commands will be recorded when the skeleton is approved and created.

| Level | Focus | Planned evidence |
|---|---|---|
| Backend unit | Auth/task service rules, validation, ownership behavior, JWT claim handling, error mapping. | Focused JUnit tests with deterministic inputs and no external deployment requirement. |
| Backend integration | Spring wiring, PostgreSQL mappings/migrations, controllers, serialization, repositories, error/status behavior. | Tests against an isolated PostgreSQL instance (containerized test DB if available) or an explicitly documented test DB setup. Never run destructive tests against a developer's data. |
| Authentication/security | Password hashing, valid/invalid/expired JWT, malformed signature/claims, revoked token before expiry, logout persistence, protected routes, fail-closed denylist lookup, cross-user task isolation. | Spring Security integration tests that traverse the real filter chain, not just token utility unit tests. |
| Frontend | Form validation, API client headers/errors, task states and UI behavior, logout clearing auth state. | Component and API-client tests with controlled network responses; no real credentials. |
| Container smoke | Service startup/readiness, API-to-database connection, frontend availability, schema migration. | Fresh Compose startup and health/status output; documented ports and shutdown/data-volume behavior. |
| User acceptance | Register, sign in, create/list/update/delete own tasks, attempt another user's task, sign out, replay old token. | Manual checklist and recorded pass/fail notes for each acceptance scenario. |
| CI | Backend test/quality command and frontend test/lint/build on pushes and pull requests. | Successful GitHub Actions runs linked from the manually reviewed/pushed commits or PR. |

## 2. Core acceptance/UAT scenarios

1. Start from the documented clean local setup and confirm frontend, API, and database become ready.
2. Register a new account; verify valid creation, duplicate-email rejection, and that responses/logs do not expose a password hash.
3. Sign in with valid credentials; verify invalid credentials fail generically and a valid token can access tasks.
4. Create two tasks; verify list ordering and task fields; update title/completion; delete one; verify it is absent.
5. Create/sign in as a second user and attempt to get, update, and delete the first user's task. Each attempt must not reveal or change it.
6. Sign out the first user, then replay that exact access token against a protected endpoint before its expiry. The request must be rejected; a second user's valid token must continue to work.
7. Restart the backend (without deleting the database volume) and verify user/task data persists and the logged-out token remains revoked. Include an automated Compose-level check that replays the exact revoked token against a protected endpoint after restarting only the backend while retaining PostgreSQL.
8. Run the backend and frontend checks locally; push a reviewed commit and verify the corresponding GitHub Actions checks.
9. On the separately approved container deployment target, repeat the security-sensitive smoke checks over HTTPS and verify secrets are environment-managed. If no host is selected or approved, deployment verification remains blocked; local Compose release verification is useful evidence but is not a substitute for the assessment deployment.

## 3. Quality gates

- A unit is not accepted until its acceptance criteria and relevant tests pass or a failure is explicitly recorded and resolved/accepted by the owner.
- Before integration acceptance: all backend and frontend tests, static quality checks, and production builds pass.
- Before deployment/release: CI is green; security and cross-user UAT scenarios pass; Compose/release setup is reproducible; no credentials or token material are present in source, image layers, or logs.
- Treat test failures as blockers, not as success-shaped fallbacks. Do not weaken a security test simply to obtain a green run.

## 4. Troubleshooting and evidence

For each issue encountered, record the observed symptom, exact command/test, relevant sanitized log excerpt, root cause, change, and retest result in the implementation/decision log (planned to be added during construction, not now). Never copy passwords, signing keys, database credentials, or bearer tokens into documentation or logs.

Useful evidence to retain for the assessment:

- Approved requirements, architecture, and construction plan.
- Per-unit test results and owner review/approval notes.
- GitHub Actions run links for pushes/pull requests.
- Sanitized Compose health/startup and API smoke-check output.
- UAT checklist results and a release/deployment verification record.
- Manual commits with focused messages and visible incremental history.

## 5. Release and deployment policy

- Docker Compose on a developer machine is the local development/integration target, not the final assessment deployment.
- A container-based external deployment is required for assessment completion. It is a separate, explicit decision after the owner chooses a host, confirms account access, and reviews current free-tier limits, data persistence, availability, and security trade-offs.
- Do not publish images or deploy automatically from pull requests. If a host is later selected, add a reviewed release workflow with protected secrets and an explicit release trigger.
- Release verification includes migration/startup, registration/login, JWT revocation, replay of a revoked token after a backend restart with PostgreSQL retained, task ownership, CRUD, HTTPS for public hosting, and a check that logs contain no sensitive authentication data.
- Record the release identifier, commit SHA, environment, verification results, and known limitations without recording secrets.
