# Cost Calc AI

Cost calculation/estimation application powered by AI, built with Kotlin and Spring Boot. It leverages MongoDB for data storage and Docker for easy deployment.

## Features
- AI vibe coded cost calculation
- RESTful API
- MongoDB integration
- Dockerized setup

## Prerequisites
- Java 17+
- Maven
- Docker

## Usage
- To run locally:
  ```powershell
  mvn spring-boot:run
  ```
- To run with Docker Compose:
  ```powershell
  docker-compose up --build
  ```

## Configuration
- Application properties are in `src/main/resources/application.properties`.
- MongoDB initialization script: `docker/mongodb/init-mongo.js`.
- Customize environment variables in `docker-compose.yml` as needed.

## Docker & Database
- MongoDB data is stored in `mongodb_data/`.
- For custom database setup, edit `docker/mongodb/init-mongo.js`.


## Relase
```
docker compose build
docker save -o cost-calc-app.tar cost-calc-app:latest
...
docker load -i cost-calc-app.tar
docker compose down
docker compose up -d
```