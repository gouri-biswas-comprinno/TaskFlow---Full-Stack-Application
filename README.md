# TaskFlow

TaskFlow is a personal task manager planned for a full-stack feature assessment. The application will let a user register, sign in, sign out, and manage only their own tasks.

## Project status

**Inception and planning.** No application code has been started. Construction is gated on review and explicit approval of the planning artifacts.

## Planning documents

- [Project brief and requirements](docs/01-project-brief-and-requirements.md)
- [Architecture and design](docs/02-architecture-and-design.md)
- [Construction plan](docs/03-construction-plan.md)
- [Validation and release plan](docs/04-validation-and-release-plan.md)
- [Sprint plan](docs/05-sprint-plan.md)
- [Sprint 0: Planning and design](docs/sprints/sprint-0-planning-and-design.md)
- [Sprint 1: Foundation and database](docs/sprints/sprint-1-foundation-and-database.md)
- [Sprint 2: Backend and security](docs/sprints/sprint-2-backend-and-security.md)
- [Sprint 3: React integration](docs/sprints/sprint-3-react-integration.md)
- [Sprint 4: Quality and release](docs/sprints/sprint-4-quality-and-release.md)

## Proposed stack

React, Java with Spring Boot, Spring Security and JWT, PostgreSQL, Docker Compose, GitHub Actions, and GitHub. pgAdmin is planned as a local database administration tool. GitHub Copilot in VS Code is the intended AI coding assistant.

## Testing, CI, and deployment

Automated frontend/backend checks and GitHub Actions are planned; no tests have been run because the project is still in planning. Docker Compose is for local development and integration. The assessment also requires a separate container-based deployment; the host is not selected and no deployment has been performed.

## Approval gate

Review the open decisions and the construction units in the planning and sprint documents. Do not begin implementation until the project owner explicitly approves the plan.
