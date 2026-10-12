# Sprint 1 — Foundation and Database

## 1. Sprint objective

Create a reproducible project foundation and local PostgreSQL-backed environment with versioned schema, health checks, and persistent storage. No feature auth or task behavior is delivered in this sprint.

## 2. Scope and exclusions

**In scope:** React and Spring Boot foundation; Docker Compose for local frontend/backend/PostgreSQL; optional local pgAdmin; safe configuration; persistent PostgreSQL storage; health checks; schema setup and persistence access.

**Excluded:** registration/login/logout endpoints, JWT/security behavior, task CRUD UI/API, publishing images, CI completion, and external deployment.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md) functional requirements 9 and 11; NFRs Correctness, Testability, Maintainability, and Reproducibility. Related configuration/schema design is in [02-architecture-and-design.md](../02-architecture-and-design.md).

## 4. Construction units covered

- **U1 — Repository skeleton and reproducible local environment**
- **U2 — Persistence model and migrations**

Both are defined in [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- Sprint 0 complete and explicit owner approval to implement U1.
- Owner accepts runtime/version choices and local port conventions.
- Database schema choices for users, tasks, and revoked tokens reviewed before migration is finalized.
- Access to Docker-capable development environment.

## 6. Ordered implementation tasks

1. Scaffold frontend and backend using approved versions; document commands and folder boundaries.
2. Add environment variable templates and ignore rules; ensure placeholders only and no secrets enter source or image layers.
3. Add frontend/backend Dockerfiles and Compose services for frontend, backend, and PostgreSQL; define readiness/health checks and a named database volume.
4. Add optional pgAdmin service/profile and document safe local access; it is not exposed in hosted deployment.
5. Define PostgreSQL schema changes for `users`, `tasks`, and `revoked_tokens`, including unique email, owner relation, `jti` key, and required timestamps/indexes.
6. Add persistence mappings/repositories and isolated integration-test configuration.
7. Verify clean startup, migration, health state, DB restart persistence, and repository constraints.

## 7. Expected deliverables and files

Expected deliverables: React and Spring Boot foundations, Dockerfiles and Compose configuration, safe environment example, PostgreSQL schema changes and persistence tests, and updated local setup instructions.

## 8. Verifiable acceptance criteria

- Fresh checkout builds and Compose starts frontend, backend, and PostgreSQL with observable health.
- Database schema is reproducible from a clean database.
- PostgreSQL named volume preserves data across service restart.
- pgAdmin is usable when enabled and is not required for app startup.
- Email uniqueness, task owner FK, owner index, and revocation `jti` uniqueness are enforced.
- No real credentials/secrets are present in source, sample config, or Docker build artifacts.

## 9. Required automated and manual tests

- Automated: frontend/backend build, Compose configuration validation, clean-database setup, persistence tests for uniqueness/ownership/revocation, and health checks.
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

U1/U2 acceptance criteria pass; relevant tests and clean setup evidence are recorded; docs match actual commands/schema; changes are reviewable and runnable; owner accepts Sprint 1.

## 13. Owner-review and approval gate

Owner reviews the scaffold, schema, Compose services, persistence behavior, and test evidence; owner commits/pushes manually. Do not start Sprint 2 authentication/security until Sprint 1 is explicitly accepted.
