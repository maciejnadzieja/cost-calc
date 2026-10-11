# Agent instructions

These instructions apply to all agents working in this repository. Follow additional directory-level AGENTS.md instructions for files in their scope. Explicit user instructions take precedence.

## Project Overview

This is a cost calculation and estimation web application named "Cost Calc AI". It is designed for small businesses and independent professionals to manage their services, materials, and generate quotes for clients. 
The application is built with Kotlin and Spring Boot, using MongoDB for data storage and a Vue.js frontend. 
It is containerized with Docker for easy deployment and uses Keycloak for authentication.

## Key Technologies

*   **Backend:** Kotlin, Spring Boot, Spring Web, Spring Security, Spring Data MongoDB
*   **Frontend:** Vue.js, Vite, Pinia, vue-i18n, Bootstrap
*   **Authentication:** Keycloak
*   **Database:** MongoDB
*   **Build Tool:** Maven, NPM
*   **Containerization:** Docker, Docker Compose
*   **Testing:** JUnit, Mockito/Mockito-Kotlin, Testcontainers, Vitest

## Architecture

The application is divided into two main parts: a Spring Boot backend and a Vue.js frontend.

### Backend

The backend follows a standard Spring Boot project structure.
*   `src/main/kotlin`: Contains the Kotlin source code.
    *   `com.costcalc.api`: The root package.
        *   `config`: Spring Security, web, and other configurations.
        *   `controller`: Spring MVC controllers for handling web requests.
        *   `dto`: Data Transfer Objects for forms.
        *   `model`: MongoDB document models.
        *   `repository`: Spring Data MongoDB repositories.
        *   `service`: Business logic and services.
*   `src/main/resources`: Application resources.
    *   `application.properties`: Main application configuration.
*   `pom.xml`: Maven project configuration.

### Frontend

The frontend is a Vue.js application located in the `frontend` directory.
*   `frontend/src`: Contains the Vue.js source code.
    *   `components`: Reusable Vue components.
    *   `views`: Application views.
    *   `router`: Vue Router configuration.
    *   `stores`: Pinia state management stores.
    *   `services`: Services for communicating with the backend and Keycloak.
*   `package.json`: NPM package configuration.

## Building and Running

## Prerequisites

*   Java 21 (as configured in pom.xml)
*   Maven
*   Node.js ^20.19.0 or >=22.12.0 (as configured in frontend/package.json)
*   Docker

## Running Locally

The backend and frontend run in separate processes in the current local environment. Do not start, restart, or suggest rerunning them as part of routine task handling. The commands below are setup reference for a new environment.

### Backend

```sh
mvn spring-boot:run
```

The backend api will be available at `http://localhost:8888`.

### Frontend

```sh
cd frontend
npm install
npm run dev
```

The application will be available at `http://localhost:5173`.

## Running with Docker

To run the application with Docker, use the following command:

```sh
docker-compose up --build
```

This will start the application, a MongoDB container, and a Keycloak container. The application will be available at `http://localhost:5173`.

# Development Conventions

## Work tracking

Use GitHub Issues in `maciejnadzieja/cost-calc` for requests, tasks, decisions, dependencies, and verification evidence. Use the private **Cost Calc delivery** Project owned by `maciejnadzieja` for live Status, Priority, and scheduling. Follow [the work management guide](docs/work/README.md) and [CLI connection instructions](docs/work/workflow.md#agent-access).

Use the Project's existing Status options: Backlog, Ready, In progress, In review, Done. Priority, Size, Estimate, Start date, and Target date are available; do not add mandatory custom fields. GitHub CLI is installed and authenticated in the current local environment for Project operations. Verify field values after updates. Keep work records and history in GitHub; do not create duplicate Markdown backlog trackers.

## Coding conventions

For software factory coordination, follow [the coordinator role guide](docs/work/coordinator.md) and its [agent instructions](.codex/agents/coordinator.toml). Use the role when assigned; this reference does not change every agent's role. The owner grants standing authorization for read-only reviewer subagents in coordinator-managed work, unless explicitly opting out for the selected task. Delegate substantive review/assessment inspection when work starts, and obtain independent review of implementation before presenting it for owner acceptance. Scope approval or "start work" activates these review steps without another routine permission request. Developer delegation, implementation, messaging other user-owned chats, pushing, merging, publishing and deployment retain their separate authorization requirements. The owner retains product priorities and final acceptance.

Use [coordinate-delivery](.agents/skills/coordinate-delivery/SKILL.md) when asked to coordinate or resume GitHub delivery. It loads the role into the current conversation; explicit invocation is `$coordinate-delivery` with the selected issue and stage. The skill does not grant additional action authorization.

Use [develop-task](.agents/skills/develop-task/SKILL.md) for assigned implementation and [review-task](.agents/skills/review-task/SKILL.md) for requested read-only review. Keep reviews independent by using a separate reviewer session; do not label inspection of your own implementation as independent. Skill invocation does not spawn agents or change runtime permissions.

Before starting implementation, use [persist-implementation-plan](.agents/skills/persist-implementation-plan/SKILL.md) to save a concrete implementation plan in the task's GitHub issue and verify it by reading it back. Resume from the persisted plan and update it before materially changing the approach. Do not begin application code changes until the plan is persisted and blocking decisions are resolved. This does not add a separate approval gate when implementation is already authorized.

*   **Coding Style:** The codebase follows standard Kotlin and Vue.js coding conventions.
*   **Testing:** The project uses spring-boot-starter-test, JUnit, mockito and mockito-kotlin for backend testing, and Vitest for frontend testing. Testcontainers is used for testing with a real MongoDB instance.
*   **Branching:** Use one branch per GitHub task, named `codex/<issue-number>-<short-description>` (lowercase words separated by hyphens), for example `codex/4-admin-user-list`. Before implementation, create or switch to the task branch; follow [the branching workflow](docs/work/workflow.md#task-branches).
*   **Commits:** Agents may commit task-related changes on the task branch named `codex/<issue-number>-<short-description>`. Before every commit, verify the current branch and staged diff. Never commit to `main`; include only changes belonging to the selected task. Read-only reviewers must not commit. Permission to commit does not authorize pushing, merging, publishing, or deploying.
*   **Commit frequency:** Commit often, after each small, coherent change that can be reviewed on its own. Keep each commit focused on one purpose, include relevant tests with the behavior they verify, and use a clear message explaining the change. Run appropriate checks before committing. Avoid combining unrelated changes or splitting a change into arbitrary file-by-file commits.
*   **Commit messages:** Format the subject as `#<task-id> | <description of changes>`, using the GitHub issue number for the task ID. Example: `#4 | Add admin user list pagination`.
*   **Dependencies:** Backend dependencies are managed with Maven, and frontend dependencies are managed with NPM.
*   **Running locally** Both backend and frontend will be running in separate processes - do not suggest to rerun them
