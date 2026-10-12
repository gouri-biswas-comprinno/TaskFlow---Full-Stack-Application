# TaskFlow: Gated Construction Plan

## 1. Working agreement

This plan is proposed; implementation starts only after the owner explicitly approves it and resolves or accepts the decisions in the project brief. Work proceeds in small units. At the end of each unit:

1. Run that unit's stated automated checks and record actual results.
2. Review the diff and test evidence together.
3. Update documentation with the outcome and any deviation.
4. The owner decides whether to accept/reject, makes the meaningful commit, and pushes manually.
5. Do not start a dependent unit until the owner approves the preceding unit.

No generated commit, push, or deployment is part of the implementation agent's work.

## 2. Construction units

| Unit | Scope and deliverables | Dependencies | Acceptance criteria | Required tests/evidence | Approval gate |
|---|---|---|---|---|---|
| U1 — Repository skeleton and reproducible local environment | Create React and Spring Boot project skeletons; pin/document compatible tool versions; add Dockerfiles and Compose for frontend/backend/PostgreSQL plus optional pgAdmin; add environment example and ignore rules. No business feature behavior. | Planning approval | Fresh checkout can build/start the three required services; DB health is observable; no real secrets committed; pgAdmin works when enabled; README has exact prerequisites and commands. | Compose config validation; image builds; health/readiness check; smoke-check frontend and backend process; inspect git diff for secrets. | Owner reviews scaffold, ports, versions, and Compose ergonomics before persistence/features. |
| U2 — Persistence model and migrations | Add user, task, and revoked-token schema/migrations; repositories; DB integration-test setup. | U1 accepted | Fresh DB migration creates constraints/indexes; normalized unique email and owner FK work; tasks are queryable by owner; revocation key is unique. | Migration on empty DB; repository integration tests for uniqueness, FK, owner filtering, and revocation lookup. | Owner reviews schema and migration before authentication. |
| U3 — Registration, login, JWT security, and revocation | Add DTO/controller/service/security responsibilities for register/login/logout; BCrypt; JWT issuance/validation; DB denylist checked before protected controllers. | U2 accepted | Registration never returns hash; duplicate email rejected; good credentials issue expected claims; bad/expired/invalid/revoked JWT rejected; logout revokes token before expiry; denylist/database failure is fail-closed. | Unit tests for auth services/token validation; Spring integration/security tests for endpoint status and logout replay; verify no secrets/tokens in logs. | Owner reviews security behavior and tests before task API/UI. |
| U4 — Task API and per-user authorization | Add task DTO, controller, service, repository operations and validation; apply authenticated user ID on every operation. | U3 accepted | Authenticated user can create/list/get/update/delete own tasks; unauthenticated requests rejected; a second user cannot access or mutate the first user's tasks; not-found response is consistent. | Service/repository tests; API integration tests for CRUD, validation, 401/404 behavior, and cross-user isolation. | Owner reviews API contract and security evidence before frontend integration. |
| U5 — Frontend authentication and task workflows | Add registration/sign-in, token state according to approved storage decision, API client, task list/create/edit/complete/delete, logout, loading/errors. Keep styling focused. | U1 and U4 accepted | Required user journeys work against the API; server errors and validation are visible; sign-out clears client auth; protected API calls use bearer token; no ownership decision is made only in client. | Component/API-client tests for success/error states and token handling; frontend build/lint; manual browser smoke of auth and CRUD. | Owner reviews usable feature and browser evidence before CI/release work. |
| U6 — Cross-stack integration and acceptance evidence | Close integration gaps; run full stack from Compose; execute documented user acceptance scenarios; update troubleshooting and implementation/data-flow docs. | U5 accepted | Complete registration-to-logout lifecycle works in the integrated stack; all core stories pass; fresh start is reproducible; known limitations documented. | Full backend/frontend test suites; Compose smoke/UAT checklist; security regression: logout token rejected and cross-user task access blocked. | Owner accepts feature scope and integration before pipeline/release finalization. |
| U7 — CI quality checks | Add GitHub Actions for backend tests/quality checks and frontend tests/lint/build on pushes and pull requests; optionally build (not publish) container images. | U1, U5 accepted (prefer U6 for final workflow validation) | Workflow is understandable, uses least necessary permissions, fails on test/build errors, and does not need deployment secrets for PR checks. | Inspect workflow; trigger/verify a push or PR run; verify deliberate failure is surfaced using a controlled test if practical. | Owner approves checks and branch workflow; commits/pushes manually. |
| U8 — Deployment, release, and presentation verification | Select and verify an appropriate host, configure only necessary deployment assets/secrets, deploy the frontend and backend as containers with persistent PostgreSQL, smoke-test the release, and document version/evidence. Local Compose verification is not a substitute for assessment deployment. | U6 and U7 accepted; explicit owner approval of host, cost/limits, and external deployment | Assessment deployment runs the documented container build; PostgreSQL data and revoked-token state persist across backend restarts; HTTPS and secret configuration are appropriate for a public deployment; auth, revocation, task isolation, and CRUD smoke checks pass; release evidence is recorded. | Release checklist; automated deployment smoke test including logout, backend restart while retaining database, and replay of the revoked token; manual environment checks; CI evidence; sanitized logs; recovery note appropriate to host. | Owner approves the target before deployment and separately approves the release; owner makes commits/pushes manually. |

## 3. Dependency view

```text
Planning approval
      |
      v
     U1 -> U2 -> U3 -> U4 -> U5 -> U6 -> U8
      \____________________/         ^
                 U7 (after U1/U5) ---|
```

U7 can be implemented once the project skeleton and frontend checks exist, but must be verified against the final integrated test commands. U8 does not authorize deployment by itself; external deployment is required for the assessment, but needs a selected target and explicit owner approval. Local Compose remains the development/integration environment, not a replacement for the assessment deployment.

## 4. Change control and scope protection

- A change to user journeys, task fields, token storage/lifetime, or deployment target requires updating the relevant requirements/design/API/test documents before implementation.
- A discovered security or data-integrity issue may block the next unit even if feature acceptance criteria pass.
- Do not combine unrelated units into one large implementation change. If deadline pressure requires reordering or combining work, ask the owner and preserve unit-level acceptance evidence.
- Every unit must leave the main branch in a reviewable, runnable state where practical; partial integration must be clearly documented rather than hidden.
