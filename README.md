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
docker save -o cost_calc_ai-app.tar cost_calc_ai-app:latest
...
docker load -i cost_calc_ai-app.tar
docker compose down
docker compose up
```