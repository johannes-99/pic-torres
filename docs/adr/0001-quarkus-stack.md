# ADR-0001: Quarkus + Angular (via Quinoa) as the application stack

- **Status:** accepted
- **Date:** 2026-10-16

## Context
We need a Java backend and an Angular frontend for a small, self-hosted family gallery. It will be developed largely by AI agents, so the developer loop and testability must be strong.

## Decision
- **Backend:** Quarkus 3.x (LTS stream) on Java 21, built with the Maven Wrapper.
  - Persistence: Hibernate ORM with Panache, PostgreSQL, Flyway.
- **Frontend:** Angular (current major version) in `backend/src/main/webui`, integrated with **Quarkus Quinoa**:
  - `quarkus dev` also runs the Angular dev server.
  - The production build is bundled into the Quarkus app, so there is a single origin and a single container.
- **Dev infrastructure:** Quarkus Dev Services start PostgreSQL and Keycloak in dev and test mode, so no hand-written dev Compose file is needed.
- **API contract:** smallrye-openapi; the TypeScript client is generated with openapi-generator.

## Consequences
- **Positive:**
  - Fast dev loop: live reload and continuous testing.
  - Real dependencies in tests, with no mocks for the database or IdP.
  - One deployable artifact.
- **Negative:**
  - A container runtime with a Docker-compatible API (Podman with `podman.socket`, or Docker) is required for development and CI.
  - Quinoa couples the frontend build to Maven. Frontend developers can still run `npm` directly in `src/main/webui`.
- **Considered alternatives:** Spring Boot (more boilerplate for the same result here), and a separate frontend container (needs CORS or a proxy, and complicates the cookie-based auth).
