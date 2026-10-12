# TaskFlow: Gated Construction Plan

## 1. Working agreement

Implementation starts only after explicit owner approval. Each unit has a focused scope, acceptance criteria, relevant tests, and an owner-review gate. The owner reviews and manually commits/pushes accepted changes.

## 2. Construction units

| Unit | Scope and deliverables | Dependencies | Acceptance criteria | Required tests/evidence | Approval gate |
|---|---|---|---|---|---|
| U1 — Repository skeleton and reproducible local environment | Create React and Spring Boot project skeletons; pin/document compatible tool versions; add Dockerfiles and Compose for frontend/backend/PostgreSQL plus optional pgAdmin; add environment example and ignore rules. No business feature behavior. | Planning approval | Fresh checkout can build/start the three required services; DB health is observable; no real secrets committed; pgAdmin works when enabled; README has exact prerequisites and commands. | Compose config validation; image builds; health/readiness check; smoke-check frontend and backend process; inspect git diff for secrets. | Owner reviews scaffold, ports, versions, and Compose ergonomics before persistence/features. |
| U2 — PostgreSQL persistence | Add versioned SQL schema changes for users, tasks, and revoked tokens; persistence access and DB integration-test setup. | U1 accepted | Fresh database setup creates required constraints/indexes; unique email, task ownership relation, and revocation key work. | Fresh database setup and focused persistence tests for uniqueness, ownership, and revocation lookup. | Owner reviews schema and tests before authentication. |
| U3 — Registration, login, JWT security, and revocation | Add DTO/controller/service/security responsibilities for register/login/logout; BCrypt; JWT issuance/validation; DB denylist checked before protected controllers. | U2 accepted | Registration never returns hash; duplicate email rejected; good credentials issue expected claims; bad/expired/invalid/revoked JWT rejected; logout revokes token before expiry; denylist/database failure is fail-closed. | Unit tests for auth services/token validation; Spring integration/security tests for endpoint status and logout replay; verify no secrets/tokens in logs. | Owner reviews security behavior and tests before task API/UI. |
| U4 — Task API and per-user authorization | Add task DTO, controller, service, repository operations and validation; apply authenticated user ID on every operation. | U3 accepted | Authenticated user can create/list/get/update/delete own tasks; unauthenticated requests rejected; a second user cannot access or mutate the first user's tasks; not-found response is consistent. | Service/repository tests; API integration tests for CRUD, validation, 401/404 behavior, and cross-user isolation. | Owner reviews API contract and security evidence before frontend integration. |
| U5 — Frontend authentication and task workflows | Add registration/sign-in, token state according to approved storage decision, API client, task list/create/edit/complete/delete, logout, loading/errors. Keep styling focused. | U1 and U4 accepted | Required user journeys work against the API; server errors and validation are visible; sign-out clears client auth; protected API calls use bearer token; no ownership decision is made only in client. | Component/API-client tests for success/error states and token handling; frontend build/lint; manual browser smoke of auth and CRUD. | Owner reviews usable feature and browser evidence before CI/release work. |
| U6 — Cross-stack integration and acceptance evidence | Close integration gaps; run full stack from Compose; execute documented user acceptance scenarios; update troubleshooting and implementation/data-flow docs. | U5 accepted | Complete registration-to-logout lifecycle works in the integrated stack; all core stories pass; fresh start is reproducible; known limitations documented. | Full backend/frontend test suites; Compose smoke/UAT checklist; security regression: logout token rejected and cross-user task access blocked. | Owner accepts feature scope and integration before pipeline/release finalization. |
| U7 — CI quality checks | Add GitHub Actions for backend tests/quality checks and frontend tests/lint/build on pushes and pull requests; optionally build (not publish) container images. | U1, U5 accepted (prefer U6 for final workflow validation) | Workflow is understandable, uses least necessary permissions, fails on test/build errors, and does not need deployment secrets for PR checks. | Inspect workflow; trigger/verify a push or PR run; verify deliberate failure is surfaced using a controlled test if practical. | Owner approves checks and branch workflow; commits/pushes manually. |
| U8 — Deployment and assessment verification | Confirm assessment constraints and a suitable hosting option; deploy using the approved stack only after owner approval; verify the release and prepare demo evidence. Local Compose is not assessment deployment. | U6 and U7 accepted; owner approves deployment approach before deployment | Assessment deployment uses the required application/database stack; persistence, authentication, revocation, ownership, and CRUD checks pass; release evidence is recorded. | Deployment smoke tests, including revoked-token replay after backend restart with PostgreSQL retained; UAT, CI evidence, and sanitized logs. | Deployment architecture/provider remain open until confirmed; owner explicitly approves before deployment and release. |

## 3. Dependency view

```text
Planning approval
      |
      v
     U1 -> U2 -> U3 -> U4 -> U5 -> U6 -> U8
      \____________________/         ^
                 U7 (after U1/U5) ---|
```

U7 can start once U1 and U5 are accepted and is verified against the final integrated checks. U8 requires confirmation of the assessment deployment constraints and a suitable option; local Compose is only for development/integration.

## 4. Change control and scope protection

- Keep scope to the approved user stories. Document and approve any change affecting requirements, security, API behavior, or deployment.
- Security or data-integrity failures block dependent work. Do not skip required tests or approval gates to meet the deadline.
