# Sprint 2 — Backend and Security

## 1. Sprint objective

Implement the backend registration, sign-in, sign-out, JWT security, persistent server-side revocation, and authenticated owner-scoped task CRUD API, with tests that exercise the real Spring Security request path.

## 2. Scope and exclusions

**In scope:** BCrypt; generic invalid-login behavior; JWT issuance/signature/issuer/expiry validation; persisted `jti` denylist; fail-closed revocation lookup; DTO validation/error mapping; protected CRUD and per-user ownership.

**Excluded:** React workflows, refresh tokens, social auth, roles/sharing, password recovery, deployment, and weakening security to save time.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), functional requirements 1–7 and security/privacy NFRs; API and flow in [02-architecture-and-design.md](../02-architecture-and-design.md); auth/security validation in [04-validation-and-release-plan.md](../04-validation-and-release-plan.md).

## 4. Construction units covered

- **U3 — Registration, login, JWT security, and revocation**
- **U4 — Task API and per-user authorization**

Both are defined in [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- U1/U2 and Sprint 1 explicitly accepted.
- Approved email/password limits, normalized-email behavior, JWT lifetime/issuer/signing configuration, task field limits, and API error behavior.
- PostgreSQL migrations and repository integration test setup available.
- Signing key supplied by runtime configuration; never committed or returned to the frontend.

## 6. Ordered implementation tasks

1. Add DTOs and validation for registration/login, mapping, and safe error responses.
2. Implement account registration with email normalization, BCrypt, unique-email conflict handling, and response that excludes hashes.
3. Implement login with generic invalid-credential response and JWT claims (`sub`, `jti`, `iat`, `exp`, configured issuer).
4. Implement Spring Security bearer validation for signature, issuer, expiry, and denylist lookup on every protected request. Fail closed on lookup errors.
5. Implement logout to persist the current token's `jti` and expiration. Do not treat client token deletion as revocation.
6. Implement task create/list/get/update/delete; derive owner from authenticated principal; ensure each repository operation filters by task ID and owner ID.
7. Map unknown and non-owned task IDs consistently to `404`; validate empty/invalid updates and avoid trusting any client-supplied owner.
8. Add focused unit, repository, controller, and full-filter-chain integration tests; verify only intended routes are public.

## 7. Expected deliverables and files

Expected additions under backend conventions: auth/task request handling, services, persistence access, security/JWT components, safe error handling, and unit/integration/security tests. Update API/security documentation only where actual behavior differs from the approved design.

## 8. Verifiable acceptance criteria

- Duplicate registration is rejected with the documented conflict response; registration response contains neither password nor hash.
- Invalid login is rejected generically; valid login returns a verifiable token with required claims and configured expiration.
- Missing, malformed, wrong-signature, wrong-issuer, expired, and revoked tokens cannot access protected endpoints.
- Logout durably records `jti`; replaying that token on a protected API is rejected before expiry. Denylist database failure does not grant access.
- Authenticated owner can perform CRUD. Unauthenticated calls fail; another user cannot read, update, or delete the task; task API never accepts ownership from caller input.
- API validation/status/error shape matches documented contract.

## 9. Required automated and manual tests

- Automated unit tests: password hashing/verification, normalization, token claims/validation, task service ownership/error behavior.
- Automated integration tests: duplicate registration; invalid login; successful auth; real security filter rejects missing/invalid/expired/revoked tokens; logout then replay; denylist row remains persisted after recreating application/security context; task CRUD and validation.
- Automated cross-user tests: user B GET/PATCH/DELETE against user A's task returns `404` and leaves A's data unchanged.
- Manual: inspect API responses/logs for no hashes, signing keys, or raw tokens; exercise relevant requests with API client. The full backend-process restart/replay test is a Sprint 4 release smoke gate.

## 10. Documentation updates required

Keep API contract, claims, token expiry, storage, ownership, and error behavior in [02-architecture-and-design.md](../02-architecture-and-design.md) synchronized. Add the real test commands and outcomes to validation notes; document sanitized debugging evidence and accepted deviations.

## 11. Risks, open decisions, and mitigations

- **Risk:** security/filter and database interaction is the highest backend complexity. Test through actual filters and denylist storage, not only utility mocks.
- **Risk:** persistence outage could create a success-shaped bypass. Authentication must fail closed and tests must cover repository failure.
- **Open:** approved JWT algorithm/key rotation policy and exact validation lengths. Use the planning-approved simple configuration; record production limitations instead of expanding scope.
- **Open:** whether repeated logout returns 401 or idempotent 204 is currently proposed as 401; confirm against API contract before implementation.

## 12. Definition of Done and sprint exit criteria

U3/U4 criteria and required automated tests pass; authorization and denylist behavior are demonstrated; API docs match actual behavior; owner accepts backend security evidence. No known critical/high security issue remains unreviewed.

## 13. Owner-review and approval gate

Owner reviews auth/logout behavior, replay evidence, fail-closed semantics, API contract, and cross-user tests. Owner commits/pushes manually. Do not start Sprint 3 until Sprint 2 is explicitly accepted.
