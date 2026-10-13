# Sprint 0 — Planning and Design

## 1. Sprint objective

Complete inception, resolve material scope and design questions, and obtain the owner's explicit approval of the planning artifacts. Sprint 0 creates no application code and does not authorize construction.

## 2. Scope and exclusions

**In scope:** review requirements, architecture, implementation order, open decisions, and deployment constraints.

**Excluded:** application source, scaffolding, dependency installation, container startup, implementation tasks, commits/pushes, and deployment.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), especially scope, user stories, constraints, assumptions, decisions, and risks; [02-architecture-and-design.md](../02-architecture-and-design.md) for architecture, API, persistence, and security decisions.

Confirm the required registration/login/logout and own-task CRUD behavior; Spring Security/JWT; persistent server-side revocation; PostgreSQL; Docker/Compose; tests; GitHub Actions; and container-based assessment deployment.

## 4. Construction units covered

No implementation construction unit. This is the prerequisite approval gate for U1–U8 in [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- Owner review of the planning documents and any available assessment rubric or deployment requirements.
- Owner approval recorded below for the decisions required before Sprint 1.

## 6. Ordered tasks

1. Review scope and acceptance criteria against the assessment.
2. Confirm essential open choices in the project brief, including task fields, password/JWT rules, and browser token handling.
3. Confirm any required language/runtime versions and local prerequisites.
4. Confirm the assessment deployment requirements and identify suitable options for later review; do not assume a provider or alter the stack.
5. Review dependencies and approve the plan explicitly before U1.

## 7. Approved decisions for Sprint 1

- Frontend: React.
- Backend: Java 21 LTS with a compatible Spring Boot version.
- Node.js LTS with a compatible React version.
- Database: PostgreSQL 16 or later.
- Authentication: JWT access tokens with a 30-minute lifetime and no refresh-token flow.
- Frontend token storage: `sessionStorage`.
- Password policy: minimum 8 characters, with no additional complexity rules.
- Persistent server-side JWT revocation using the token's `jti`; revoked tokens must be rejected before expiry and after backend restarts.
- Local development through Docker Compose.
- External deployment is required; provider and topology remain open for Sprint 4.

## 8. Expected deliverables and files

- Reviewed planning documents and explicit owner decision on whether U1 may start. Record decisions in the relevant document.

## 9. Verifiable acceptance criteria

- Essential requirements map to construction and validation work.
- Deployment remains an open decision; Compose is for local development.
- No implementation starts without explicit owner approval.
- Sprint 1 decisions above are accepted and recorded before construction begins.

## 10. Required tests and review evidence

- No software tests are run in planning. Review requirement mapping, security tests, unit sequence, and deployment assumptions.

## 11. Documentation updates required

Update only decisions accepted by the owner. Keep README status as planning until construction is approved and begun.

## 12. Risks, open decisions, and mitigations

- **Open:** the assessment deployment provider and topology remain to be selected during Sprint 4; runtime deployment is required, but not yet approved.
- **Risk:** less than one week; keep the feature scope small and reserve time for verification and demonstration.

## 13. Definition of Done and sprint exit criteria

All review topics are answered or explicitly accepted as open with an owner and decision point; requirements and plan are consistent; owner approves starting U1; no implementation or deployment has happened.

## 14. Owner-review and approval gate

The owner reviewed the complete plan and explicitly approved Sprint 0 before Sprint 1 begins. This approval confirms the decisions recorded above and authorizes U1. External deployment is not approved in this gate; the provider and topology remain open and will be confirmed at the Sprint 4 deployment gate.
