# TaskFlow

TaskFlow is a personal task manager for the full-stack feature assessment. The approved architecture is a React frontend, a Java 21/Spring Boot backend, and PostgreSQL. Database persistence, U3 authentication, U4 owner-scoped task APIs, and U5 React workflows are implemented.

## Project status

**Sprint 4 — U6 verification performed; owner review pending.** U1–U5 provide the application foundation, PostgreSQL persistence, authentication/revocation, owner-scoped task APIs, and React workflows. U6 backend/frontend checks, browser UAT, and the Compose black-box acceptance script have been run; see the results below. U7/CI and U8/deployment have not started. A fresh Compose configuration/start was not attempted because the current shell did not have the existing `TASKFLOW_JWT_SECRET` value available; no Compose services or PostgreSQL data were recreated or reset.

## Planning documents

- [Project brief and requirements](docs/01-project-brief-and-requirements.md)
- [Architecture and design](docs/02-architecture-and-design.md)
- [Construction plan](docs/03-construction-plan.md)
- [Validation and release plan](docs/04-validation-and-release-plan.md)
- [Sprint plan](docs/05-sprint-plan.md)
- [Sprint 0: Planning and design](docs/sprints/sprint-0-planning-and-design.md)
- [Sprint 1: Foundation and database](docs/sprints/sprint-1-foundation-and-database.md)
- [Sprint 2: Backend and security](docs/sprints/sprint-2-backend-and-security.md)
- [Sprint 3: React integration](docs/sprints/sprint-3-react-integration.md)
- [Sprint 4: Quality and release](docs/sprints/sprint-4-quality-and-release.md)

## U1 technology versions

| Component | Version |
|---|---|
| Java | 21 (container build and runtime) |
| Spring Boot | 3.5.3 |
| Maven | 3.9.12 (container build image) |
| Node.js | 22.16.0 (container) |
| React / React DOM | 19.1.0 |
| Vite | 6.4.4 |
| PostgreSQL | 16.9 (container image) |
| pgAdmin | 9.3 (optional Compose profile) |
| Flyway | Managed by Spring Boot 3.5.3 dependency management |

## Prerequisites

- Docker Engine/Desktop with Docker Compose v2.
- PowerShell for the example API check (or use a browser at the API URL).
- The `.env` file configured as described below.

Docker supplies the Java, Maven, Node.js, PostgreSQL, and pgAdmin versions used by this local stack. Host-installed Java or Node versions are not required for the Compose workflow.

## Local development

Run these commands from the repository root.

1. Create your local environment file:

   ```powershell
   Copy-Item .env.example .env
   ```

   The values in `.env.example` are for local development only. Replace them with your own local values if needed. `.env` is ignored by Git; do not put production credentials in it or commit it.

2. Set a local signing key in the current PowerShell session. Generate at least 32 random bytes; keep the value private and do not commit it. The Compose file passes it to the backend, which refuses to start without a sufficiently long key:

   ```powershell
   $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $bytes = New-Object byte[] 32
   $rng.GetBytes($bytes)
   $env:TASKFLOW_JWT_SECRET = [Convert]::ToBase64String($bytes)
   $rng.Dispose()
   ```

   Keep using the same value when restarting the backend; changing it invalidates existing access tokens. You may store your own value in the ignored local `.env` file, but do not put it in `.env.example` or source control.

3. Check the Compose configuration and build/start the frontend, backend, and PostgreSQL. On backend startup, Flyway applies pending migrations to the configured database:

   ```powershell
   docker compose config
   docker compose up --build -d
   docker compose ps
   ```

4. Verify the frontend and backend:

   - Frontend: <http://localhost:5173>
   - Backend health endpoint: <http://localhost:8080/api/health>

   ```powershell
   Invoke-RestMethod http://localhost:8080/api/health
   ```

   The health response should contain `status: UP`; the frontend also displays the API health result. The migration creates the three planned tables if they do not already exist.

5. To use optional pgAdmin, start the `admin` profile:

   ```powershell
   docker compose --profile admin up --build -d
   ```

   Open <http://localhost:5050> and sign in using `PGADMIN_DEFAULT_EMAIL` and `PGADMIN_DEFAULT_PASSWORD` from `.env`. To register the database server in pgAdmin, use host `db`, port `5432`, and the PostgreSQL credentials from `.env`.

6. To inspect startup logs:

   ```powershell
   docker compose logs -f backend
   ```

7. Stop the services:

   ```powershell
   docker compose down
   ```

   PostgreSQL data is stored in the named volume `taskflow-postgres-data` and is retained by `docker compose down` and container restarts. Do not use `docker compose down -v` unless you intentionally want to delete that database volume and its data.

## Backend checks

Run backend tests from the repository root. Authentication and persistence integration tests start a separate, temporary PostgreSQL 16.9 container through Testcontainers; they do not connect to the Compose development database:

```powershell
mvn -f backend\pom.xml test
```

This requires Maven, a Java runtime compatible with the project's Java 21 target, and a Docker Engine endpoint that Testcontainers can access. Testcontainers 2.0.5 starts a disposable PostgreSQL 16.9 container; the tests do not connect to or modify the Compose development database.

**U2 verification (2026-10-13):** `mvn -f backend\pom.xml -Dtest=PersistenceIntegrationTest test` passed (4 tests), and `mvn -f backend\pom.xml test` passed (5 tests total), using Testcontainers 2.0.5. The test-only dependency update resolved the Docker discovery issue; no Docker Desktop settings or Compose database/volume were changed.

**U3 verification (2026-10-13):** `mvn -f backend\pom.xml "-Dtest=AuthenticationIntegrationTest,RevocationCheckingJwtDecoderTest" test` passed (6 tests), and `mvn -f backend\pom.xml package` passed (11 tests total). Revocation replay was rejected using a newly constructed decoder against the same PostgreSQL database; a separate full backend-process restart verification remains planned for Sprint 4.

**U4 verification (2026-10-13):** `mvn -f backend\pom.xml -Dtest=TaskApiIntegrationTest test` passed (5 tests), and `mvn -f backend\pom.xml test` passed (16 tests total). These cover the task CRUD lifecycle, validation, unauthenticated requests, unknown IDs, and cross-user read/update/delete denial against an isolated PostgreSQL container.

## Frontend checks

Run the frontend test and build scripts from the repository root:

```powershell
npm --prefix frontend test
npm --prefix frontend run build
```

Access tokens are held in React memory only; the app starts signed out after a page reload. Do not use browser storage to preserve access tokens. The browser requests `/api`; Vite proxies that path to `VITE_BACKEND_TARGET` during local development. No frontend lint script is currently configured.

**U5 verification (2026-10-13):** `npm test` passed (9 tests across 2 files); `npm run build` passed. `npm audit` reported 0 vulnerabilities with Vitest 5.0.4. No lint command exists, and manual browser UAT was not performed in U5.

## U6 integration and acceptance verification

**U6 verification (2026-10-14):**

- `mvn -f backend\pom.xml test`: passed, 16 tests; `mvn -f backend\pom.xml package`: passed, 16 tests.
- `npm --prefix frontend test`: passed, 15 tests across 4 files; `npm --prefix frontend run build`: passed. No frontend lint script is configured.
- Manual browser UAT passed registration, duplicate-registration rejection, invalid-login handling, login/logout, task creation, completion, details, editing, and deletion.
- `scripts\uat-compose-smoke.ps1`: passed all 27 checks. It verified registration and login responses, task CRUD, user B's GET/PATCH/DELETE denials for user A's task, no cross-user mutation, logout, rejection of the revoked token after restarting only the backend, and continued validity of user B's token. It removed only its generated test records; database row counts returned to their pre-test values.
- After the script, frontend/backend/PostgreSQL containers were running and healthy; `/api/health` returned `{"status":"UP"}`, the frontend returned HTTP 200, and the existing `taskflow-postgres-data` named volume remained present and unchanged.

To rerun the Compose acceptance test, use PowerShell from the repository root:

```powershell
.\scripts\uat-compose-smoke.ps1
```

The script uses uniquely generated UAT accounts and cleans up only its own task, users, and revocation records. It does not reset or delete the development database or volume.

**Not verified:** `docker compose ps --all` was blocked during Compose variable interpolation because the current shell did not have the required `TASKFLOW_JWT_SECRET`; a fresh `docker compose config` or startup was not run. The already-running containers retained their configured environment and passed health checks; do not generate a replacement signing key, which would invalidate existing tokens. Before running Compose commands from a new shell, make sure its expected local environment configuration is available without printing or committing secrets. No Compose services were recreated for U6.

The persistence integration tests cover Flyway table creation, unique email, task ownership foreign-key and owner-scoped lookup, unique revocation `jti`, authentication, logout replay, and task API behavior. CI/GitHub Actions and assessment deployment remain planned; U6 did not configure or run CI or deploy the application. One account and its logout revocation row from the manual browser UAT remain in the development database; the app has no user-deletion route, and those records were not removed.

Assessment deployment is required, but the provider and topology remain open until the constraints are confirmed and approved. Docker Compose in this repository is for local development only.

## Security and scope

The committed `.env.example` contains local-only placeholder credentials, not production secrets. Keep real secrets in the ignored `.env` locally and use an approved secret store for any future hosted environment. The backend connects to PostgreSQL over the Compose network using the existing PostgreSQL environment values. Flyway applies additive schema migrations at backend startup. Keep the named volume; do not run `docker compose down -v`.

U3 delivers Spring Security/JWT access tokens and persistent PostgreSQL-backed revocation. U4 enforces task ownership on the server; U5 calls that API but does not make authorization decisions in the browser.

Sign-out clears browser memory only after the backend returns `204 No Content`. A network failure or unexpected response is reported as unconfirmed revocation and leaves the current session active.

Review and explicitly accept U6 before starting U7 or U8. U6 did not run CI, configure deployment, or change the existing Compose configuration or database volume.
