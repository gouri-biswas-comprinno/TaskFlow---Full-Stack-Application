# TaskFlow: Assessment Sprint Plan

## Purpose and status

This is a proposed compact milestone plan for the less-than-one-week assessment. It organizes the construction units in [03-construction-plan.md](03-construction-plan.md) without authorizing implementation. Sprint 0 is review and approval only. Each subsequent milestone remains gated by owner review and approval.

### Delivery approach

- Preserve the construction-unit boundaries, tests, and review gates; these are assessment milestones, not fixed two-week Scrum sprints.
- Work in dependency order: planning approval → foundation/data → backend/security → React integration → quality/release.
- Proposed planning estimate is approximately 5–6 focused working days after approval, with little contingency. The estimates are planning guidance, not commitments; backend/security and deployment carry the most uncertainty.
- Prioritize one complete, secure vertical slice. Do not remove JWT denylist, authorization, tests, CI, Docker, or the required external container deployment to meet the date.
- Local Docker Compose supports development and integration. It does not satisfy the final assessment deployment requirement.

## Sprint-to-construction-unit mapping

| Sprint | Objective | Construction units |
|---|---|---|
| Sprint 0 — Planning and design | Agree scope, architecture, decisions, and approve the plan before code. | None; inception/planning gate before U1. |
| Sprint 1 — Foundation and database | Establish runnable React/Spring/PostgreSQL foundation, migrations, local containers, and health checks. | U1 and U2. |
| Sprint 2 — Backend and security | Deliver persistence-backed auth, persistent JWT revocation, and protected owner-scoped task API. | U3 and U4. |
| Sprint 3 — React integration | Deliver registration/login/logout and task workflows against the secured API. | U5. |
| Sprint 4 — Quality and release | Integrate and verify, complete CI, security/UAT and restart-persistence checks, deploy containers, and prepare presentation evidence. | U6, U7, and U8. U7 can be started after its U1/U5 prerequisites are accepted, then revalidated here. |

## Critical path and shared gates

```text
Sprint 0 approval
       |
       v
Sprint 1 (U1 -> U2)
       |
       v
Sprint 2 (U3 -> U4)
       |
       v
Sprint 3 (U5)
       |
       v
Sprint 4 (U6 + final U7 verification + U8)
```

U7's construction-plan dependency permits starting CI once U1 and U5 exist; this sprint grouping puts CI completion and full-stack validation in Sprint 4 to avoid a premature workflow that misses final checks. Sprint 4 is on the critical path and must include a real deployment target decision early enough to resolve provider limits.

## Cross-sprint security and quality invariants

- Every task API requires authenticated JWT validation and a database denylist lookup; task ownership is derived from the authenticated principal and enforced server-side.
- Logout stores revocation durably. A protected request using that token is rejected before expiry.
- Verification includes replaying a revoked token after restarting the backend while retaining PostgreSQL data. Include an automated Compose-level smoke test and a manual UAT confirmation.
- Registration tests include duplicate email; login tests include invalid credentials; task API tests include cross-user read/update/delete attempts.
- Database schema is migration-managed and task/revocation data use persistent PostgreSQL storage.
- Frontend and backend checks run in GitHub Actions on pushes and pull requests. Owner reviews and manually commits/pushes.
- Record test/UAT results, sanitized troubleshooting notes, CI run links, deployment/release evidence, and presentation evidence.

## Approval and change control

The owner approves the planning artifacts before U1. At the end of each sprint, review deliverables, acceptance criteria, tests, and deviations; the owner approves before dependent work begins. Deployment requires a separate decision on the external host, its free-tier limits, and any terms/cost implications. If a dependency or deadline risk makes a sprint infeasible, propose a replan; do not silently skip required security or assessment work.

See the individual [Sprint 0](sprints/sprint-0-planning-and-design.md), [Sprint 1](sprints/sprint-1-foundation-and-database.md), [Sprint 2](sprints/sprint-2-backend-and-security.md), [Sprint 3](sprints/sprint-3-react-integration.md), and [Sprint 4](sprints/sprint-4-quality-and-release.md) plans.
