# Sprint 0 — Planning and Design

## 1. Sprint objective

Complete inception, resolve material scope and design questions, and obtain the owner's explicit approval of the planning artifacts. Sprint 0 creates no application code and does not authorize construction.

## 2. Scope and exclusions

**In scope:** requirements and acceptance criteria review; architecture/API/database/security review; sprint and construction plan review; choose or explicitly defer technical options; assess the deadline and deployment approach.

**Excluded:** application source, scaffolding, dependency installation, container startup, implementation tasks, commits/pushes, and deployment.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), especially scope, user stories, constraints, assumptions, decisions, and risks; [02-architecture-and-design.md](../02-architecture-and-design.md) for architecture, API, persistence, and security decisions.

Confirm the required registration/login/logout and own-task CRUD behavior; Spring Security/JWT; persistent server-side revocation; PostgreSQL; Docker/Compose; tests; GitHub Actions; and container-based assessment deployment.

## 4. Construction units covered

No implementation construction unit. This is the prerequisite approval gate for U1–U8 in [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- Owner review of current requirements, architecture, construction plan, validation plan, and this sprint plan.
- Assessment deadline, rubric, required versions, and any institution-provided deployment options, if available.
- No repository changes beyond planning-document corrections approved by the owner.

## 6. Ordered tasks

1. Review user stories, API contracts, data model, and out-of-scope list against the assessment rubric.
2. Confirm task fields, email normalization/password rules, JWT lifetime/claims, browser token-storage choice, and error semantics.
3. Confirm baseline tool versions and local development operating system/prerequisites.
4. Review the persistent denylist design, fail-closed behavior, owner-scoped query approach, and restart/replay test.
5. Select a candidate free-tier deployment approach and verify current service limits, container support, persistent PostgreSQL, HTTPS, sleep/availability behavior, and account requirements. Recommended candidate: Render free Docker web services for frontend/backend containers plus Neon free-tier managed PostgreSQL, subject to checking current eligibility/limits and compatibility. Do not create accounts, deploy, or incur costs in this sprint.
6. Check dependency mapping and available time; adjust estimates/scope only with owner agreement, retaining mandatory security, CI, tests, Docker, and deployment.
7. Obtain explicit written approval to begin U1. Approval of this document alone does not approve external deployment; that requires a later separate gate.

## 7. Expected deliverables and files

- Approved/revised planning Markdown: [README.md](../../README.md), [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), [02-architecture-and-design.md](../02-architecture-and-design.md), [03-construction-plan.md](../03-construction-plan.md), [04-validation-and-release-plan.md](../04-validation-and-release-plan.md), [05-sprint-plan.md](../05-sprint-plan.md), and Sprint 0–4 documents.
- A recorded owner decision on open choices and whether construction may start. Decision/outcome notes are added to the relevant docs when the owner approves; no new decision log file is assumed at this stage.

## 8. Verifiable acceptance criteria

- Every mandatory assessment requirement maps to a requirement, a construction unit, and validation evidence.
- No unresolved deployment wording implies local Compose replaces external deployment.
- Open decisions have options and recommendations rather than hidden assumptions.
- No plan authorizes implementation before explicit owner approval.
- Owner explicitly approves or requests changes; absent approval, remain in Sprint 0.

## 9. Required tests and review evidence

- No software tests: no application exists.
- Manual review checklist: scope; requirements traceability; U1–U8 mapping; denylist replay/restart; cross-user access; duplicate registration/invalid login; migrations/persistent volume; frontend/backend/CI tests; external deployment and release checks.
- Record any conflicts and accepted trade-offs in the relevant planning document.

## 10. Documentation updates required

Resolve only owner-approved planning changes in the affected documents. Keep README status as planning until construction is approved and begun. Record the chosen token-storage option and deployment candidate/constraints; do not claim tests, implementation, or deployment occurred.

## 11. Risks, open decisions, and mitigations

- **Open:** task fields; password policy; token lifetime; browser token storage; required versions; pgAdmin profile; CI image-build scope. Confirm against [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md).
- **Open:** final deployment host and current free-tier suitability. Verify provider limits before committing. Render + Neon is a candidate, not a promise; fallback must remain a container-capable host with durable PostgreSQL and HTTPS.
- **Risk:** less than one week. Keep feature scope fixed; use small units and protect release/presentation time.

## 12. Definition of Done and sprint exit criteria

All review topics are answered or explicitly accepted as open with an owner and decision point; requirements and plan are consistent; owner approves starting U1; no implementation or deployment has happened.

## 13. Owner-review and approval gate

The owner reviews the complete plan and explicitly approves U1 before Sprint 1 begins. External deployment is not approved by this gate; confirm and approve the selected target at the Sprint 4 deployment gate.
