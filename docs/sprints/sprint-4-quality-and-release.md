# Sprint 4 — Quality and Release

## 1. Sprint objective

Complete cross-stack verification and CI, run security-sensitive UAT including revoked-token replay after backend restart, deploy the frontend/backend as containers to an explicitly approved suitable host with persistent PostgreSQL, and prepare release and assessment-presentation evidence.

## 2. Scope and exclusions

**In scope:** full-stack UAT and smoke tests; persistent revocation across backend restart; GitHub Actions; essential troubleshooting/release notes; assessment deployment; presentation preparation.

**Excluded:** deployment without owner approval; image publishing unless separately approved; automatic deployment from pull requests; public pgAdmin; unapproved paid services; new product features.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), US-07, functional requirements 9–11, delivery/security/reproducibility NFRs; release architecture in [02-architecture-and-design.md](../02-architecture-and-design.md); tests and release policy in [04-validation-and-release-plan.md](../04-validation-and-release-plan.md).

## 4. Construction units covered

- **U6 — Cross-stack integration and acceptance evidence**
- **U7 — CI quality checks**
- **U8 — Deployment, release, and presentation verification**

All are defined in [03-construction-plan.md](../03-construction-plan.md). U7 may be implemented after U1/U5 acceptance; Sprint 4 requires final revalidation against integrated commands.

**Execution status (2026-10-14):** U6 verification has been performed and is awaiting owner review. Backend/frontend suites and builds, manual browser UAT, and the Compose black-box restart/revocation smoke test passed. A fresh Compose configuration/start was not verified because `TASKFLOW_JWT_SECRET` was unavailable to the invoking shell. Existing Compose services and the PostgreSQL named volume were left intact. U7 and U8 have not started.

## 5. Dependencies and prerequisites

- U6 prerequisites: Sprint 3/U5 accepted and Compose stack available.
- U7 prerequisites: U1/U5 accepted; GitHub repository and Actions available.
- U8 prerequisites: U6/U7 accepted; assessment constraints and a suitable deployment option are confirmed; owner explicitly approves the approach.
- Confirm required persistence, availability, security, account, and cost constraints before selecting a deployment option.

## 6. Ordered implementation tasks

1. Run complete frontend/backend suites, static checks, production builds, and Compose fresh-start smoke; resolve integration failures.
2. Exercise acceptance scenarios with two independent users, including duplicate registration, invalid login, CRUD, and cross-user GET/PATCH/DELETE rejection.
3. Add/run an automated Compose-level black-box smoke test: register/login, create task, logout, restart only backend while preserving PostgreSQL volume, then replay the exact revoked token against a protected endpoint and assert rejection. Confirm another valid token still works where applicable.
4. Run manual UAT, document pass/fail and sanitized troubleshooting evidence; check task/user and revoked-token persistence across backend/container restarts.
5. Add GitHub Actions for backend tests/quality checks and frontend test/lint/build on pushes and pull requests; use least necessary permissions and no deployment secrets for normal PR checks.
6. Trigger/inspect CI results for the reviewed branch/PR; confirm failures surface as failures and final integrated commands are covered.
7. Confirm assessment deployment requirements and review suitable hosting options with the owner. Do not assume a provider or change the required stack.
8. After explicit approval, configure runtime secrets and deploy using the approved React, Spring Boot, and PostgreSQL architecture; keep PostgreSQL data persistent and do not expose pgAdmin publicly.
9. Verify the deployment: registration/login, task CRUD, cross-user denial, logout, backend restart with PostgreSQL retained, revoked-token replay, and safe configuration/logs. Record sanitized release evidence and known limitations.
10. Prepare a short assessment walkthrough: requirements/design → architecture/data flow → staged commits and tests → security demonstration → CI → deployed feature → lessons/known limitations. Capture only non-sensitive screenshots/log evidence.

## 7. Expected deliverables and files

- GitHub Actions checks, integration/restart smoke test, and documented test invocation.
- Updated setup, validation, troubleshooting, UAT, and release evidence in the existing planning/README documents.
- Deployment configuration and presentation evidence only after the owner approves the deployment approach; never include credentials or tokens.

## 8. Verifiable acceptance criteria

- All backend and frontend automated checks and production builds pass; CI runs on push and pull request and reports accurate status.
- Duplicate-registration, invalid-login, cross-user task access, and logout/replay tests pass.
- Automated release smoke test proves that a token revoked before expiry remains rejected after the backend is restarted while PostgreSQL storage is retained.
- UAT covers register → login → own CRUD → logout, plus unauthorized access and persisted revocation; sanitized results are recorded.
- The owner has confirmed assessment deployment requirements and approved a suitable deployment option using the specified stack. Local Compose alone does not satisfy this criterion.
- HTTPS, environment-managed secrets, no publicly exposed pgAdmin/database unless intentionally protected, and no sensitive values in source/image/logs.
- Release evidence and concise presentation walkthrough are ready; known limitations are explicit.

## 9. Required automated and manual tests

- Full backend unit/integration/security suite and frontend unit/component suite.
- CI workflow checks for pushes and pull requests; verify actual GitHub Actions run.
- Automated Compose end-to-end release smoke including backend-only restart with retained DB volume and revoked-token replay.
- Manual UAT: duplicate register; invalid login; own CRUD; user B cannot GET/PATCH/DELETE user A's task; logout and replay old token after backend restart; valid other user's token still functions.
- Deployment smoke over HTTPS for auth, CRUD, ownership, revocation, health, database persistence, and safe logs.
- Confirm tests use isolated data and do not delete a user/developer volume as cleanup.

### U6 execution evidence

| Check | Result |
|---|---|
| `mvn -f backend\pom.xml test` | PASS — 16 tests |
| `mvn -f backend\pom.xml package` | PASS — 16 tests |
| `npm --prefix frontend test` | PASS — 15 tests across 4 files |
| `npm --prefix frontend run build` | PASS |
| Manual browser UAT | PASS — registration, duplicate registration, invalid login, login/logout, create/complete/view/edit/delete task |
| `scripts\uat-compose-smoke.ps1` | PASS — all 27 checks, including two-user GET/PATCH/DELETE denial, backend-only restart, revoked-token rejection (401), and a still-valid second user's token (200) |
| Cleanup and data retention | PASS — only generated smoke-test records removed; counts returned to baseline; existing database and named volume retained |
| Final service health | PASS — frontend/backend/PostgreSQL containers running and healthy; frontend HTTP 200; backend health `UP` |
| Fresh `docker compose config`/startup | NOT RUN — current shell did not have the configured `TASKFLOW_JWT_SECRET`; no service recreation or signing-key replacement was attempted |
| CI, deployment, HTTPS release checks | NOT RUN — outside the U6 implementation completed here; U7/U8 remain pending |

Manual browser UAT left one test account and its logout revocation row in the development database. They were not removed because the application has no user-deletion route. The black-box script's own generated records were cleaned without resetting the database or volume.

## 10. Documentation updates required

Update [README.md](../../README.md) with truthful local and deployed run instructions, test commands, CI status links, deployment URL only if approved, and known limitations. Update architecture/runtime and validation/release docs with the selected host and verified constraints. Add troubleshooting entries with symptom, command, sanitized log, cause, resolution, and retest. Preserve evidence without credentials or tokens.

**U6 troubleshooting evidence:** `docker compose ps --all` was blocked during Compose variable interpolation because the required `TASKFLOW_JWT_SECRET` was not available in the invoking PowerShell process. The existing containers remained healthy and the acceptance script restarted only the existing backend container, retaining its configured signing key and database connection. For future Compose commands, load the already-approved local environment configuration in the invoking shell without exposing it in logs or source control; do not generate a replacement key. Retest `docker compose config` only after the expected variable is available. A fresh Compose start remains unverified.

## 11. Risks, open decisions, and mitigations

- **Open — deployment constraints:** host, topology, persistence, and availability requirements are unconfirmed. Resolve with the assessment/owner before selecting an option; do not assume a provider or database service.
- **High risk — scope/time:** CI, complete security regression, external deployment, UAT, and presentation are significant work. Freeze features, start host evaluation during Sprint 0, and reserve a focused final block for verification and presentation.
- **Risk — persistent revocation test:** restarting backend must not recreate/delete DB or change signing configuration. Script backend-only restart and assert old token is rejected by the same stable API origin.
- **Risk — credentials/data exposure:** use platform secret storage and restricted DB networking; redact logs/screenshots; never publish environment files.
- **Open:** final host and deployment topology. Confirm that the required stack can be deployed and verified before approval.

## 12. Definition of Done and sprint exit criteria

U6/U7/U8 criteria pass; security and restart/replay evidence are retained; CI is green; the approved assessment deployment is verified; UAT and demonstration evidence are complete; owner accepts the release. If deployment constraints remain unresolved, deployment work remains blocked.

## 13. Owner-review and approval gate

Before deployment, owner explicitly approves the hosting option and deployment approach. Afterward, owner reviews CI, security tests, UAT, release evidence, and demo. Owner performs all commits and pushes manually. No deployment proceeds on inferred approval.
