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

*   **Coding Style:** The codebase follows standard Kotlin and Vue.js coding conventions.
*   **Testing:** The project uses spring-boot-starter-test, JUnit, mockito and mockito-kotlin for backend testing, and Vitest for frontend testing. Testcontainers is used for testing with a real MongoDB instance.
*   **Branching:** The project appears to use a feature-branching workflow, though no explicit branching strategy is documented.
*   **Commits:** Do not commit anything to git
*   **Dependencies:** Backend dependencies are managed with Maven, and frontend dependencies are managed with NPM.
*   **Running locally** Both backend and frontend will be running in separate processes - do not suggest to rerun them
