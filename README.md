# TaskFlow

TaskFlow is a personal task manager for the full-stack feature assessment. The approved architecture is a React frontend, a Java 21/Spring Boot backend, and PostgreSQL. Authentication, task management, and database persistence features are staged for later construction units.

## Project status

**Sprint 1 — U1 approved; U2 implemented and verified, owner acceptance pending.** The foundation and PostgreSQL persistence layer are in place. U2 adds Flyway migrations and Spring JDBC repositories; authentication, JWT revocation, and task APIs remain later work. The migration was applied successfully to the local Compose database without deleting its volume or data. The isolated PostgreSQL integration tests now pass with Testcontainers 2.0.5 from the Windows host. Do not begin U3 until the owner accepts U2.

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

2. Check the Compose configuration and build/start the frontend, backend, and PostgreSQL. On backend startup, Flyway applies pending migrations to the configured database:

   ```powershell
   docker compose config
   docker compose up --build -d
   docker compose ps
   ```

3. Verify the frontend and backend:

   - Frontend: <http://localhost:5173>
   - Backend health endpoint: <http://localhost:8080/api/health>

   ```powershell
   Invoke-RestMethod http://localhost:8080/api/health
   ```

   The health response should contain `status: UP`; the frontend also displays the API health result. The migration creates the three planned tables if they do not already exist.

4. To use optional pgAdmin, start the `admin` profile:

   ```powershell
   docker compose --profile admin up --build -d
   ```

   Open <http://localhost:5050> and sign in using `PGADMIN_DEFAULT_EMAIL` and `PGADMIN_DEFAULT_PASSWORD` from `.env`. To register the database server in pgAdmin, use host `db`, port `5432`, and the PostgreSQL credentials from `.env`.

5. To inspect startup logs:

   ```powershell
   docker compose logs -f backend
   ```

6. Stop the services:

   ```powershell
   docker compose down
   ```

   PostgreSQL data is stored in the named volume `taskflow-postgres-data` and is retained by `docker compose down` and container restarts. Do not use `docker compose down -v` unless you intentionally want to delete that database volume and its data.

## U1 and U2 checks

Run backend tests from the repository root. The persistence integration tests start a separate, temporary PostgreSQL 16.9 container through Testcontainers; they do not connect to the Compose development database:

```powershell
mvn -f backend\pom.xml test
```

This requires Maven, a Java runtime compatible with the project's Java 21 target, and a Docker Engine endpoint that Testcontainers can access. Testcontainers 2.0.5 starts a disposable PostgreSQL 16.9 container; the tests do not connect to or modify the Compose development database.

**U2 verification (2026-10-13):** `mvn -f backend\pom.xml -Dtest=PersistenceIntegrationTest test` passed (4 tests), and `mvn -f backend\pom.xml test` passed (5 tests total), using Testcontainers 2.0.5. The test-only dependency update resolved the Docker discovery issue; no Docker Desktop settings or Compose database/volume were changed.

Frontend production build:

```powershell
docker compose run --rm --no-deps frontend npm run build
```

Container startup and health:

```powershell
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://localhost:8080/api/health
```

The persistence integration test is intended to cover Flyway table creation, unique email, task ownership foreign-key and owner-scoped lookup, and unique revocation `jti`. Authentication/security, task APIs, GitHub Actions, and assessment deployment are planned for later units and are not delivered by U2.

Assessment deployment is required, but the provider and topology remain open until the constraints are confirmed and approved. Docker Compose in this repository is for local development only.

## Security and scope

The committed `.env.example` contains local-only placeholder credentials, not production secrets. Keep real secrets in the ignored `.env` locally and use an approved secret store for any future hosted environment. The backend connects to PostgreSQL over the Compose network using the existing PostgreSQL environment values. Flyway applies additive schema migrations at backend startup. Keep the named volume; do not run `docker compose down -v`.

Later units retain the planned Spring Security/JWT access tokens, persistent PostgreSQL-backed revocation, server-side task ownership enforcement, automated tests, and GitHub Actions checks.

U2 does not implement authentication, JWT revocation, task APIs, or UI behavior. Rerun and pass the isolated persistence integration tests and obtain explicit owner acceptance before starting U3.
