# Sprint 4 — Quality and Release

## 1. Sprint objective

Complete cross-stack verification and CI, run security-sensitive UAT including revoked-token replay after backend restart, deploy the frontend/backend as containers to an explicitly approved suitable host with persistent PostgreSQL, and prepare release and assessment-presentation evidence.

## 2. Scope and exclusions

**In scope:** full-stack UAT and smoke tests; persistent revocation across backend restart; GitHub Actions; troubleshooting/architecture/SDLC documentation; host selection and free-tier check; containerized assessment deployment; release verification and presentation preparation.

**Excluded:** deployment without owner approval; image publishing unless separately approved; automatic deployment from pull requests; public pgAdmin; unapproved paid services; new product features.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), US-07, functional requirements 9–11, delivery/security/reproducibility NFRs; release architecture in [02-architecture-and-design.md](../02-architecture-and-design.md); tests and release policy in [04-validation-and-release-plan.md](../04-validation-and-release-plan.md).

## 4. Construction units covered

- **U6 — Cross-stack integration and acceptance evidence**
- **U7 — CI quality checks**
- **U8 — Deployment, release, and presentation verification**

All are defined in [03-construction-plan.md](../03-construction-plan.md). U7 may be implemented after U1/U5 acceptance; Sprint 4 requires final revalidation against integrated commands.

## 5. Dependencies and prerequisites

- U6 prerequisites: Sprint 3/U5 accepted and Compose stack available.
- U7 prerequisites: U1/U5 accepted; GitHub repository and Actions available.
- U8 prerequisites: U6/U7 accepted; selected provider supports frontend/backend containers, durable PostgreSQL, secrets, and HTTPS; owner explicitly approves provider and deployment.
- Owner has reviewed current free-tier conditions, including suspension/sleep behavior, quotas, data lifetime, build/runtime compatibility, account requirements, and any payment risk.

## 6. Ordered implementation tasks

1. Run complete frontend/backend suites, static checks, production builds, and Compose fresh-start smoke; resolve integration failures.
2. Exercise acceptance scenarios with two independent users, including duplicate registration, invalid login, CRUD, and cross-user GET/PATCH/DELETE rejection.
3. Add/run an automated Compose-level black-box smoke test: register/login, create task, logout, restart only backend while preserving PostgreSQL volume, then replay the exact revoked token against a protected endpoint and assert rejection. Confirm another valid token still works where applicable.
4. Run manual UAT, document pass/fail and sanitized troubleshooting evidence; check task/user and revoked-token persistence across backend/container restarts.
5. Add GitHub Actions for backend tests/quality checks and frontend test/lint/build on pushes and pull requests; use least necessary permissions and no deployment secrets for normal PR checks.
6. Trigger/inspect CI results for the reviewed branch/PR; confirm failures surface as failures and final integrated commands are covered.
7. Select deployment host with owner. Recommended candidate for evaluation (not a guarantee of current availability): Render free Docker web services for React and Spring Boot plus Neon free-tier managed PostgreSQL. Verify current free-tier eligibility, limits, sleep behavior, PostgreSQL persistence/expiry, connection restrictions, region/network/TLS, and expected demo availability before approval. Keep a container-capable alternative available if unsuitable. This deploys frontend/backend containers; database may be provider-managed PostgreSQL rather than a separately hosted database container.
8. With explicit approval, configure secret values through host secret management; deploy frontend/backend containers; apply migrations; keep PostgreSQL data persistent; do not expose pgAdmin publicly. Avoid paid resources unless separately approved.
9. Verify the deployed release over HTTPS: registration/login, task CRUD, cross-user denial, logout, backend restart with DB intact, revoked-token replay, logs and configuration; document commit SHA, CI links, deployment identifier/environment, sanitized results, limitations, and recovery/rollback steps.
10. Prepare a short assessment walkthrough: requirements/design → architecture/data flow → staged commits and tests → security demonstration → CI → deployed feature → lessons/known limitations. Capture only non-sensitive screenshots/log evidence.

## 7. Expected deliverables and files

- GitHub Actions workflow(s) under `.github/workflows/`.
- Automated Compose restart/replay smoke test and documented invocation, plus any release checklist/script.
- Updated [04-validation-and-release-plan.md](../04-validation-and-release-plan.md), [README.md](../../README.md), architecture/setup/troubleshooting notes, UAT and release evidence.
- Container deployment configuration and provider settings documented without credentials; a deployment URL/identifier only after an approved deployment.
- Presentation outline/evidence in an owner-approved location; no secrets in screenshots.

## 8. Verifiable acceptance criteria

- All backend and frontend automated checks and production builds pass; CI runs on push and pull request and reports accurate status.
- Duplicate-registration, invalid-login, cross-user task access, and logout/replay tests pass.
- Automated release smoke test proves that a token revoked before expiry remains rejected after the backend is restarted while PostgreSQL storage is retained.
- UAT covers register → login → own CRUD → logout, plus unauthorized access and persisted revocation; sanitized results are recorded.
- A suitable external host is selected with owner approval; frontend/backend run as containers and deployed PostgreSQL state persists. Local Compose alone does not satisfy this criterion.
- HTTPS, environment-managed secrets, no publicly exposed pgAdmin/database unless intentionally protected, and no sensitive values in source/image/logs.
- Release evidence and concise presentation walkthrough are ready; known limitations are explicit.

## 9. Required automated and manual tests

- Full backend unit/integration/security suite and frontend unit/component suite.
- CI workflow checks for pushes and pull requests; verify actual GitHub Actions run.
- Automated Compose end-to-end release smoke including backend-only restart with retained DB volume and revoked-token replay.
- Manual UAT: duplicate register; invalid login; own CRUD; user B cannot GET/PATCH/DELETE user A's task; logout and replay old token after backend restart; valid other user's token still functions.
- Deployment smoke over HTTPS for auth, CRUD, ownership, revocation, health, database persistence, and safe logs.
- Confirm tests use isolated data and do not delete a user/developer volume as cleanup.

## 10. Documentation updates required

Update [README.md](../../README.md) with truthful local and deployed run instructions, test commands, CI status links, deployment URL only if approved, and known limitations. Update architecture/runtime and validation/release docs with the selected host and verified constraints. Add troubleshooting entries with symptom, command, sanitized log, cause, resolution, and retest. Preserve evidence without credentials or tokens.

## 11. Risks, open decisions, and mitigations

- **High risk — external free-tier limitations:** terms, sleeping services, database expiry/storage, egress, build quotas, and Java container support can change. Verify before choosing; owner approves any spend; keep alternative container-capable providers under review.
- **High risk — scope/time:** CI, complete security regression, external deployment, UAT, and presentation are significant work. Freeze features, start host evaluation during Sprint 0, and reserve a focused final block for verification and presentation.
- **Risk — persistent revocation test:** restarting backend must not recreate/delete DB or change signing configuration. Script backend-only restart and assert old token is rejected by the same stable API origin.
- **Risk — credentials/data exposure:** use platform secret storage and restricted DB networking; redact logs/screenshots; never publish environment files.
- **Open:** final host and exact service combination; Render + Neon is only a candidate and must pass owner review and current policy/limits check.

## 12. Definition of Done and sprint exit criteria

U6/U7/U8 criteria pass; security regressions and restart/replay test evidence are retained; CI is green; an approved external container deployment is verified (or the sprint is blocked awaiting the explicit provider/deployment decision); UAT, sanitized troubleshooting, release evidence, and presentation outline are complete; owner accepts the release.

## 13. Owner-review and approval gate

Before deployment, owner explicitly approves provider, resource limits/terms, secret configuration, and any cost/account implications. After deployment, owner reviews CI, security tests, UAT, release evidence, and demo. Owner performs all commits and pushes manually and decides whether to present/release. No deployment proceeds on inferred approval.
