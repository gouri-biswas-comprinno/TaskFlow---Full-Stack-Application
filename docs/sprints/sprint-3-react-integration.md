# Sprint 3 — React Integration

## 1. Sprint objective

Deliver the minimal React user journeys for registration, login, logout, and authenticated task management, integrated with the secured backend API.

**Progress:** U5 implemented; owner review pending. U6, U7, U8, deployment, and assessment UAT have not started.

## 2. Scope and exclusions

**In scope:** registration/sign-in forms, auth state and approved token-storage behavior, API client, task list/create/edit/complete/delete, loading/error/validation states, and focused frontend tests.

**Excluded:** frontend-only authorization, extra product features, broad visual redesign, client-managed ownership, and replacement of backend security tests.

## 3. Requirements covered

References: [01-project-brief-and-requirements.md](../01-project-brief-and-requirements.md), user stories US-01–US-07 and functional requirement 8; component boundaries in [02-architecture-and-design.md](../02-architecture-and-design.md); frontend validation in [04-validation-and-release-plan.md](../04-validation-and-release-plan.md).

## 4. Construction units covered

- **U5 — Frontend authentication and task workflows**, from [03-construction-plan.md](../03-construction-plan.md).

## 5. Dependencies and prerequisites

- U1 and U4 accepted; API paths and responses are implemented.
- U5 uses in-memory access-token storage only. `localStorage` and `sessionStorage` are not used; after reload, users sign in again.
- The Vite `/api` proxy uses `VITE_BACKEND_TARGET` for local backend routing; browser requests remain same-origin.
- Frontend test/build commands are available. No lint script is configured in this project.

## 6. Ordered implementation tasks

1. Add a small API client for JSON, bearer headers, safe error handling, and the existing Vite `/api` proxy.
2. Implement registration and login forms with approved client validation and useful success/error states.
3. Keep the access token in React memory only; restore no authentication after reload.
4. Implement task list and create/update/completion/delete interactions using the API; refresh state after mutations.
5. Implement sign-out by calling the backend logout endpoint before clearing local state; clear only after its documented `204` response. Report other responses/network failures as unconfirmed revocation.
6. Add accessible labels/basic keyboard behavior and loading/empty/error states for required flows.
7. Add component and API-client tests; perform browser smoke testing against the backend.

## 7. Expected deliverables and files

Expected React files within approved project layout: auth/task components or views, API client, minimal app/auth state, styles as needed, component/API-client tests, and frontend package scripts. Update [README.md](../../README.md) for actual commands and [02-architecture-and-design.md](../02-architecture-and-design.md) for token/API integration behavior.

## 8. Verifiable acceptance criteria

- User can register, sign in, view tasks, create/update/complete/delete own tasks, and sign out through the UI.
- Invalid credentials and API/network failures are presented clearly without disclosing internals.
- Protected calls include the current bearer token; sign-out calls server revocation then clears client auth only on the documented success path.
- Access-token state is in memory only; a fresh page load requires sign-in.
- UI does not claim to authorize data; the backend remains authoritative for per-user access.

## 9. Required automated and manual tests

- Automated frontend tests for form validation/submission, successful and failed requests, bearer header, task loading/mutation, and auth state clearing on logout.
- `npm test` and `npm run build` pass. No lint command exists in `frontend/package.json`.
- Manual browser smoke: register and sign in; verify task CRUD; sign out; replay the old token through protected API and confirm rejection; verify error/loading/empty states. Not performed as part of U5; reserve for U6/UAT.
- Cross-user authorization remains validated by backend tests; frontend tests cannot substitute for it.

## 10. Documentation updates required

README and [02-architecture-and-design.md](../02-architecture-and-design.md) record the `/api` proxy, in-memory token/reload behavior, logout confirmation, and actual frontend test/build commands. The backend remains authoritative for ownership.

## 11. Risks, open decisions, and mitigations

- **Risk:** frontend integration can reveal CORS, API base URL, or error-shape mismatches. Keep the API contract as the source of truth and resolve mismatches with owner-reviewed docs/code.
- **Risk:** a failed network logout may leave a valid server token. Keep the user signed in and report that revocation was not confirmed; retry or sign out only after a successful `204`.
- **Risk:** no lint script is configured. Keep that limitation visible rather than claiming lint coverage; CI/quality work remains for U7.

## 12. Definition of Done and sprint exit criteria

U5 acceptance criteria pass; frontend tests/build pass; manual auth/CRUD/logout smoke remains for U6/UAT; owner accepts the UI and error states.

## 13. Owner-review and approval gate

Owner reviews user journeys, memory-only token handling, logout failure/success behavior, and frontend test evidence. Owner commits/pushes manually. Do not begin U6 or final acceptance/release work until U5 is explicitly accepted.
