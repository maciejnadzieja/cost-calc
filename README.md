# Cost Calc AI

Work planning: the private **Cost Calc delivery** GitHub Project, [GitHub Issues](https://github.com/maciejnadzieja/cost-calc/issues), and [request workflow](docs/work/README.md). See [CLI connection instructions](docs/work/workflow.md#agent-access) to access the Project.

Shared instructions for development agents: [AGENTS.md](AGENTS.md).

Cost Calc AI is a powerful cost calculation and estimation application designed for small businesses and independent professionals. It leverages Artificial Intelligence to help users manage services, materials, and generate accurate quotes for clients.

The application is built with a modern technology stack, featuring a robust Kotlin Spring Boot backend and a responsive Vue.js frontend, all containerized with Docker for seamless deployment.

## ✨ Features

-   **AI-Powered Estimation**: Intelligent cost calculation algorithms.
-   **Project Management**: Manage services, materials, and client details.
-   **Quote Generation**: Create and export professional PDF quotes.
-   **Secure Authentication**: Integrated with Keycloak for robust security.
-   **Modern UI**: Responsive interface built with Vue.js and Bootstrap.
-   **RESTful API**: Fully documented API for backend interactions.

## 🛠 Technology Stack

### Backend
-   **Language**: Kotlin
-   **Framework**: Spring Boot (Web, Security, Data MongoDB)
-   **Database**: MongoDB
-   **Testing**: JUnit, MockK, Testcontainers

### Frontend
-   **Framework**: Vue.js 3
-   **Build Tool**: Vite
-   **State Management**: Pinia
-   **Styling**: Bootstrap
-   **Testing**: Vitest

### Infrastructure
-   **Containerization**: Docker & Docker Compose
-   **Authentication**: Keycloak

## 📋 Prerequisites

Ensure you have the following installed on your local machine:
-   **Java 17+**
-   **Maven**
-   **Node.js 20+**
-   **Docker** & **Docker Compose**

## 🚀 Getting Started

### Running with Docker (Recommended)

The easiest way to run the entire application (Backend, Frontend, Database, Auth) is using Docker Compose.

```bash
docker-compose up --build
```

-   **Frontend**: [http://localhost:5173](http://localhost:5173)
-   **Backend API**: [http://localhost:8888](http://localhost:8888)

### Running Locally

If you prefer to run services individually for development:

#### 1. Backend

```bash
# From the root directory
mvn spring-boot:run
```
The backend server will start at `http://localhost:8888`.

#### 2. Frontend

```bash
# From the root directory
cd frontend
npm install
npm run dev
```
The frontend development server will start at `http://localhost:5173`.

## ⚙️ Configuration

-   **Backend**: Main configuration is located in `src/main/resources/application.properties`.
-   **Database**: MongoDB initialization scripts are in `docker/mongodb/init-mongo.js`.
-   **Environment**: You can customize environment variables in `docker-compose.yml`.
-   **Admin user directory**: See [Keycloak roles and backend directory credentials](docs/admin-user-directory.md) before using the admin-only user list.

## 🧪 Testing

### Backend Tests
```bash
mvn test
```

### Frontend Tests
```bash
cd frontend
npm run test
```
