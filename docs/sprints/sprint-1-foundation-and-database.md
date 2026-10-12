# Sprint 1 — Foundation and Database

## 1. Sprint objective

Create a reproducible project foundation and local PostgreSQL-backed environment with versioned schema, health checks, and persistent storage. No feature auth or task behavior is delivered in this sprint.

## 2. Scope and exclusions

**In scope:** React and Spring Boot skeletons; documented tool versions; Dockerfiles; Docker Compose for frontend/backend/database; optional pgAdmin profile; safe environment examples; PostgreSQL named volume; health/readiness checks; Flyway migrations and persistence repositories.

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
5. Define initial PostgreSQL schema and Flyway migrations for `users`, `tasks`, and `revoked_tokens`, including unique email, owner FK/index, `jti` key, timestamps, and expiry index as approved.
6. Add persistence mappings/repositories and isolated integration-test configuration.
7. Verify clean startup, migration, health state, DB restart persistence, and repository constraints.

## 7. Expected deliverables and files

Expected paths, subject to approved project conventions: root frontend/backend build manifests; frontend source skeleton; backend source/test skeleton; `Dockerfile` files; `compose.yaml`; `.env.example` and ignore rules; migration SQL under backend resources; repository integration tests; setup instructions in [README.md](../../README.md) and design updates in [02-architecture-and-design.md](../02-architecture-and-design.md).

## 8. Verifiable acceptance criteria

- Fresh checkout builds and Compose starts the frontend, backend, and PostgreSQL with observable health/readiness.
- Database schema is created by migrations on an empty database; no runtime ORM auto-create dependency.
- PostgreSQL named volume preserves data across service restart.
- pgAdmin is usable when enabled and is not required for app startup.
- Email uniqueness, task owner FK, owner index, and revocation `jti` uniqueness are enforced.
- No real credentials/secrets are present in source, sample config, or Docker build artifacts.

## 9. Required automated and manual tests

- Automated: frontend/backend skeleton build; Compose configuration validation; migration tests on an empty database; repository integration tests for unique email, owner FK/query, and revocation lookup; health checks.
- Manual: clean Compose startup; optional pgAdmin connection; restart PostgreSQL without removing its volume and verify test record remains; inspect diff/config for secrets and published ports.
- Do not use volume deletion as routine test cleanup where it risks user data; use an isolated test project/volume.

## 10. Documentation updates required

Update README with exact prerequisites, approved versions, start/stop/test commands, port map, pgAdmin profile, environment setup, and volume retention/reset warning. Update architecture/schema if accepted implementation choices differ; record sanitized setup issues and resolutions.

## 11. Risks, open decisions, and mitigations

- **Risk:** mismatched React/Java/Spring/Docker versions; pin compatible versions only after owner approves.
- **Risk:** port collisions and Docker availability; document ports and diagnose health logs before moving on.
- **Open:** ORM/persistence implementation details and exact migration naming; choose conventional Spring Data/JPA + Flyway unless review identifies a better minimal fit.
- **Open:** CI image build scope remains per project brief; no image publishing in this sprint.

## 12. Definition of Done and sprint exit criteria

U1/U2 acceptance criteria pass; relevant tests and clean setup evidence are recorded; docs match actual commands/schema; changes are reviewable and runnable; owner accepts Sprint 1.

## 13. Owner-review and approval gate

Owner reviews the scaffold, schema, Compose services, persistence behavior, and test evidence; owner commits/pushes manually. Do not start Sprint 2 authentication/security until Sprint 1 is explicitly accepted.
