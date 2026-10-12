# TaskFlow: Assessment Sprint Plan

## Purpose and status

This is a compact milestone plan for the assessment. It organizes [03-construction-plan.md](03-construction-plan.md) and does not authorize implementation. Owner approval is required before work and before dependent units.

### Delivery approach

- Work in order: planning approval → foundation/database → backend/security → React → integration/CI/deployment/UAT.
- Keep the scope to essential account and task flows. Preserve JWT revocation, ownership checks, tests, CI, Docker, and required assessment deployment.
- Compose is for local development. Deployment host and topology remain undecided pending confirmation of assessment constraints.

## Sprint-to-construction-unit mapping

| Sprint | Objective | Construction units |
|---|---|---|
| Sprint 0 — Planning and design | Agree scope, architecture, decisions, and approve the plan before code. | None; inception/planning gate before U1. |
| Sprint 1 — Foundation and database | Establish runnable React/Spring/PostgreSQL foundation, migrations, local containers, and health checks. | U1 and U2. |
| Sprint 2 — Backend and security | Deliver persistence-backed auth, persistent JWT revocation, and protected owner-scoped task API. | U3 and U4. |
| Sprint 3 — React integration | Deliver registration/login/logout and task workflows against the secured API. | U5. |
| Sprint 4 — Integration and delivery | Complete integration, tests, CI, required deployment, UAT, and demonstration. | U6, U7, and U8. U7 may start after U1/U5 and is verified against the final checks. |

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

U7 can be developed earlier, but final integrated checks belong in Sprint 4.

## Essential verification

Test duplicate registration, invalid login, task CRUD and cross-user denial, logout and revoked-token replay before expiry, and replay after backend restart with PostgreSQL retained. Run frontend/backend checks in GitHub Actions. Record UAT and release/demo evidence.

## Approval and change control

The owner explicitly approves the planning artifacts before U1 and reviews each unit before dependent work. Confirm assessment deployment constraints and obtain owner approval for the host/topology before deployment. If timing is constrained, replan with the owner; do not silently drop security or assessment requirements.

See the individual [Sprint 0](sprints/sprint-0-planning-and-design.md), [Sprint 1](sprints/sprint-1-foundation-and-database.md), [Sprint 2](sprints/sprint-2-backend-and-security.md), [Sprint 3](sprints/sprint-3-react-integration.md), and [Sprint 4](sprints/sprint-4-quality-and-release.md) plans.
