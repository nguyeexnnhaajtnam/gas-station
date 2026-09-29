# Gas Station Management System

Foundation for a Vietnamese gas-station management application with an Angular frontend, Spring Boot modular monolith, PostgreSQL, and an isolated read-only SeenPro adapter.

## Local development (no Docker)

Requirements: Java 21, Node.js 20+, and PostgreSQL installed locally. Create the development database once with a PostgreSQL administrator account:

```sql
CREATE USER gas_dev WITH PASSWORD '123';
CREATE DATABASE "gas-dev" OWNER gas_dev;
```

Copy `.env.dev.example` to `.env.dev` and adjust the local credentials. `.env.dev` is ignored by Git and imported automatically by the Spring `dev` profile. `dev` is also the default profile for local IDE runs.

Backend terminal:

```powershell
cd D:\gas\backend
D:\gas\.tools\apache-maven-3.9.11\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Flyway creates and updates the schema in `gas-dev` when the backend starts.

Frontend terminal:

```powershell
cd D:\gas\frontend
npm.cmd install
npm.cmd run start:dev
```

Open `http://localhost:4200`. The Angular development build calls `http://localhost:8080` directly; Spring Security explicitly allows the configured development origin through CORS.

## Docker/PostgreSQL development

Requirements: Java 21, Maven 3.9+, Node.js 20+, Docker.

1. Copy `.env.example` to `.env` and set local values. Do not commit it.
2. Start PostgreSQL: `docker compose up -d postgres`.
3. Start backend: `cd backend && mvn spring-boot:run`.
4. Install/start frontend: `cd frontend && npm install && npm start`.
5. Open `http://localhost:4200`; Swagger UI is at `http://localhost:8080/swagger-ui.html`.

SeenPro calls intentionally return 503 until confirmed login and HTML fixtures are supplied. See [integration status](docs/seenpro-integration.md).
