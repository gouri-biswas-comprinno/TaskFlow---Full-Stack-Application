# Sprint 1 — Foundation and Database

## 1. Sprint objective

Create a reproducible project foundation and local PostgreSQL-backed environment with versioned schema, health checks, and persistent storage. No feature auth or task behavior is delivered in this sprint.

## 2. Scope and exclusions

**In scope:** React and Spring Boot foundation; Docker Compose for local frontend/backend/PostgreSQL; optional local pgAdmin; safe configuration; persistent PostgreSQL storage; health checks; schema setup and persistence access.

**Excluded:** registration/login/logout endpoints, JWT/security behavior, task CRUD UI/API, publishing images, CI completion, and external deployment.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md) functional requirements 9 and 11; NFRs Correctness, Testability, Maintainability, and Reproducibility. Related configuration/schema design is in [02-architecture-and-design.md](../02-architecture-and-design.md).

## 4. Construction units covered

- **U1 — Repository skeleton and reproducible local environment** — completed and owner-approved.
- **U2 — Persistence model and migrations** — scope approved and implementation present; isolated integration verification passed with Testcontainers 2.0.5; final owner acceptance is pending.

Both are defined in [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- Sprint 0 complete and explicit owner approval to implement U1.
- Owner-approved U1 foundation, runtime versions, local port conventions, and Compose setup.
- The migration follows the existing schema in [02-architecture-and-design.md](../02-architecture-and-design.md).
- Access to Docker-capable development environment.

## 6. Ordered implementation tasks

1. **U1 complete:** Scaffold the frontend and backend using approved versions; document commands and folder boundaries.
2. **U1 complete:** Add environment variable templates and ignore rules; ensure placeholders only and no secrets enter source or image layers.
3. **U1 complete:** Add frontend/backend Dockerfiles and Compose services for frontend, backend, and PostgreSQL; define readiness/health checks and a named database volume.
4. **U1 complete:** Add optional pgAdmin service/profile and document safe local access; it is not exposed in hosted deployment.
5. **U2 implemented:** Define PostgreSQL schema changes for `users`, `tasks`, and `revoked_tokens`, including unique email, owner relation, `jti` key, and required timestamps/indexes.
6. **U2 implemented:** Add Spring JDBC persistence records/repositories and integration tests configured to use a disposable PostgreSQL Testcontainer.
7. **U2 verification complete:** Run the migration and constraint tests against a disposable PostgreSQL Testcontainer. Preserve the existing PostgreSQL volume and data; do not use destructive cleanup.

## 7. Expected deliverables and files

Expected deliverables: React and Spring Boot foundations, Dockerfiles and Compose configuration, safe environment example, PostgreSQL schema migration, Spring JDBC records/repositories, isolated persistence tests, and updated local setup instructions.

## 8. Verifiable acceptance criteria

- Fresh checkout builds and Compose starts frontend, backend, and PostgreSQL with observable health.
- Database schema is reproducible from a clean database.
- PostgreSQL named volume preserves data across service restart.
- pgAdmin is usable when enabled and is not required for app startup.
- Email uniqueness, task owner FK, owner index, and revocation `jti` uniqueness are enforced and verified by integration tests.
- No real credentials/secrets are present in source, sample config, or Docker build artifacts.

## 9. Required automated and manual tests

- Automated: frontend/backend build, Compose configuration validation, isolated clean-database setup, persistence tests for uniqueness/ownership/revocation, and health checks.
- Manual: clean Compose startup; optional pgAdmin connection; restart PostgreSQL without removing its volume and verify test record remains; inspect diff/config for secrets and published ports.
- Do not use volume deletion as routine test cleanup where it risks user data; use an isolated test project/volume.

## 10. Documentation updates required

Document local prerequisites and commands, pgAdmin access, environment setup, and how PostgreSQL data persists. Update the architecture if approved choices differ.

## 11. Risks, open decisions, and mitigations

- **Risk:** mismatched React/Java/Spring/Docker versions; pin compatible versions only after owner approves.
- **Risk:** port collisions and Docker availability; document ports and diagnose health logs before moving on.
- **Open:** exact persistence implementation and schema-change format; choose a simple approach consistent with the approved stack.
- **Risk:** Docker availability or port conflicts; document and resolve before dependent work.

## 12. Definition of Done and sprint exit criteria

U1/U2 acceptance criteria pass; relevant tests and clean setup evidence are recorded; docs match actual commands/schema; changes are reviewable and runnable; owner accepts Sprint 1. Current U2 progress and test limitations are recorded in section 15.

## 13. Owner-review and approval gate

U1 has been reviewed and explicitly approved by the owner. The owner has explicitly approved the proposed U2 scope. U2 still requires verification and owner acceptance before dependent U3 work begins. Owner commits/pushes manually. Do not start Sprint 2 authentication/security until U2 is accepted and Sprint 1 is explicitly accepted.

## 14. U1 completion and approval record

The owner approved U1 and reported that the frontend, backend, and PostgreSQL images built successfully; all three Compose services are running and healthy; Compose configuration validation passed; and `git diff --check` passed. The owner also confirmed that U1 was committed manually. These results are recorded as owner-reported verification; this documentation update does not rerun container checks or change application code.

U1 is complete. Sprint 1 remains in progress until U2 is implemented, verified, and separately accepted. No U2 work is authorized by U1 approval.

## 15. U2 implementation and verification status

- **Implemented:** Flyway migration creates only `users`, `tasks`, and `revoked_tokens`, with the columns, keys, foreign key, and indexes in the architecture document.
- **Implemented:** Spring JDBC records and repositories provide basic inserts/lookups; task lookup includes both task ID and owner ID.
- **Runtime verification:** The updated backend image built successfully and started against the existing Compose database. Flyway logged successful application of migration V1; a read-only query confirmed `users`, `tasks`, and `revoked_tokens` exist. The existing named volume and its data were not deleted or reset. The API health endpoint returned `UP`.
- **Automated verification:** Backend package build passed; `HealthControllerTest` passed (1 test); Compose configuration validation and `git diff --check` passed.
- **Resolved:** Testcontainers 1.21.2 failed Docker discovery on the Windows host. Updating only the test-scoped Testcontainers dependencies to 2.0.5, aligning the core and renamed module artifacts, and adjusting the PostgreSQL container import/declaration resolved the issue. Docker Desktop settings, environment files, production dependencies, and the Compose database/volume were not changed.
- **Automated verification:** `mvn -f backend\pom.xml -Dtest=PersistenceIntegrationTest test` passed (4 tests, 0 failures/errors). `mvn -f backend\pom.xml test` passed (5 tests total, 0 failures/errors). Testcontainers started an isolated PostgreSQL 16.9 container for the integration tests.
- **Next gate:** U2 is ready for owner review and acceptance. Do not start U3 until the owner explicitly accepts U2.
