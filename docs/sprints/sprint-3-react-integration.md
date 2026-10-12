# Sprint 3 — React Integration

## 1. Sprint objective

Deliver the minimal React user journeys for registration, login, logout, and authenticated task management, integrated with the secured backend API.

## 2. Scope and exclusions

**In scope:** registration/sign-in forms, auth state and approved token-storage behavior, API client, task list/create/edit/complete/delete, loading/error/validation states, and focused frontend tests.

**Excluded:** frontend-only authorization, extra product features, broad visual redesign, client-managed ownership, and replacement of backend security tests.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), user stories US-01–US-07 and functional requirement 8; component boundaries in [02-architecture-and-design.md](../02-architecture-and-design.md); frontend validation in [04-validation-and-release-plan.md](../04-validation-and-release-plan.md).

## 4. Construction units covered

- **U5 — Frontend authentication and task workflows**, from [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- U1 and U4 explicitly accepted; documented API responses and CORS/local origin strategy work.
- Owner has selected in-memory or `sessionStorage` token storage; in-memory remains recommended.
- React test/lint/build commands are agreed and usable locally.

## 6. Ordered implementation tasks

1. Add a small API client for JSON, bearer headers, safe error handling, and configured API base URL.
2. Implement registration and login forms with approved client validation and useful success/error states.
3. Store/clear token only according to the approved decision; restore no authentication from an unapproved source.
4. Implement task list and create/update/completion/delete interactions using the API; refresh state after mutations.
5. Implement sign-out by calling the backend logout endpoint before clearing local state; report API failure explicitly and do not imply server revocation succeeded if it did not.
6. Add accessible labels/basic keyboard behavior and loading/empty/error states for required flows.
7. Add component and API-client tests; perform browser smoke testing against the backend.

## 7. Expected deliverables and files

Expected React files within approved project layout: auth/task components or views, API client, minimal app/auth state, styles as needed, component/API-client tests, and frontend package scripts. Update [README.md](../../README.md) for actual commands and [02-architecture-and-design.md](../02-architecture-and-design.md) for token/API integration behavior.

## 8. Verifiable acceptance criteria

- User can register, sign in, view tasks, create/update/complete/delete own tasks, and sign out through the UI.
- Invalid credentials and API/network failures are presented clearly without disclosing internals.
- Protected calls include the current bearer token; sign-out calls server revocation then clears client auth only on the documented success path.
- Reload behavior matches the approved token-storage choice.
- UI does not claim to authorize data; the backend remains authoritative for per-user access.

## 9. Required automated and manual tests

- Automated frontend tests for form validation/submission, successful and failed requests, bearer header, task loading/mutation, and auth state clearing on logout.
- Frontend lint/build/test commands pass.
- Manual browser smoke: register and sign in; verify task CRUD; sign out; replay the old token through protected API and confirm rejection; verify error/loading/empty states.
- Cross-user authorization remains validated by backend tests; frontend tests cannot substitute for it.

## 10. Documentation updates required

Update README with UI/API startup and test commands, approved token-storage reload behavior, and local-browser troubleshooting (CORS/base URL). Update actual data-flow and UI boundary notes; do not document a client-side ownership check as security.

## 11. Risks, open decisions, and mitigations

- **Risk:** frontend integration can reveal CORS, API base URL, or error-shape mismatches. Keep the API contract as the source of truth and resolve mismatches with owner-reviewed docs/code.
- **Risk:** a failed network logout may leave a valid server token. Show failure clearly and retain/handle the token according to approved behavior; never report successful revocation without API success.
- **Open:** token storage choice and minimal accessibility/styling baseline. Resolve in Sprint 0, keep UI deliberately small.

## 12. Definition of Done and sprint exit criteria

U5 acceptance criteria pass; frontend automated tests/build pass; manual auth/CRUD/logout smoke evidence is recorded; owner accepts the UI and error states.

## 13. Owner-review and approval gate

Owner reviews user journeys, token handling, error behavior, and frontend test evidence. Owner commits/pushes manually. Do not begin final acceptance/release work in Sprint 4 until Sprint 3 is explicitly accepted.
