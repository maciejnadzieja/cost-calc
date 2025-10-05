# Project Overview

This is a cost calculation and estimation web application named "Cost Calc AI". It is designed for small businesses and independent professionals to manage their services, materials, and generate quotes for clients. The application is built with Kotlin and Spring Boot, using MongoDB for data storage and Thymeleaf for the user interface. It is containerized with Docker for easy deployment.

## Key Technologies

*   **Backend:** Kotlin, Spring Boot, Spring Web, Spring Security, Spring Data MongoDB
*   **Frontend:** Thymeleaf, HTML, CSS, JavaScript
*   **Database:** MongoDB
*   **Build Tool:** Maven
*   **Containerization:** Docker, Docker Compose
*   **PDF Generation:** iText
*   **Testing:** JUnit, MockK, Testcontainers

## Architecture

The application follows a standard Spring Boot project structure.
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
    *   `static`: Static assets like CSS and JavaScript.
    *   `templates`: Thymeleaf templates for the UI.
*   `pom.xml`: Maven project configuration.
*   `docker-compose.yml`: Docker Compose configuration for running the application and MongoDB.
*   `Dockerfile`: Dockerfile for building the application image.

# Building and Running

## Prerequisites

*   Java 17+
*   Maven
*   Docker

## Running Locally

To run the application locally, use the following command:

```sh
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

## Running with Docker

To run the application with Docker, use the following command:

```sh
docker-compose up --build
```

This will start the application and a MongoDB container. The application will be available at `http://localhost:8080`.

# Development Conventions

*   **Coding Style:** The codebase follows standard Kotlin coding conventions.
*   **Testing:** The project uses JUnit and MockK for unit and integration testing. Testcontainers is used for testing with a real MongoDB instance.
*   **Branching:** The project appears to use a feature-branching workflow, though no explicit branching strategy is documented.
*   **Commits:** Commit messages should be clear and concise.
*   **Dependencies:** Dependencies are managed with Maven.
