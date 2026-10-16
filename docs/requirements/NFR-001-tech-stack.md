---
id: NFR-001
title: Technology stack and repository layout
type: non-functional
status: draft
priority: must
milestone: M0
---

## Rationale
A fixed, mainstream stack with a fast developer loop and strong test support. See ADR-0001.

## Acceptance criteria
- **AC-1** Given the repository, when it is built, then the backend is a Quarkus 3.x (LTS stream) application on Java 21, built with the Maven Wrapper (`./mvnw`) in `backend/`.
- **AC-2** Given the backend, then it uses these Quarkus extensions:
  - `rest-jackson`
  - `hibernate-orm-panache`
  - `jdbc-postgresql`
  - `flyway`
  - `oidc`
  - `smallrye-openapi`
  - `smallrye-health`
  - `container-image-jib`
  - `jacoco`
  - `quinoa`
- **AC-3** Given the frontend, then it is an Angular application (current major version) in `backend/src/main/webui/`. It uses standalone components, signals and Angular Material, and is integrated through Quarkus Quinoa.
- **AC-4** Given `./mvnw quarkus:dev`, when the app starts, then it serves both the API and the Angular UI (with live reload) on `http://localhost:8080`.
- **AC-5** Given `./mvnw package`, then the Angular production build is bundled into the Quarkus application. The UI and API are served from the same origin.
- **AC-6** Given the database schema, then every change is a versioned Flyway migration in `src/main/resources/db/migration`. Hibernate schema generation is disabled outside of tests.
- **AC-7** Given the REST API, then an OpenAPI document is published at `/q/openapi`. The Angular HTTP client for the API is generated from it with openapi-generator, never written by hand.
- **AC-8** Given the repository root, then it contains:
  - `backend/`
  - `e2e/` (Playwright)
  - `docs/`
  - `deploy/`
  - `scripts/`
  - `.github/`
  - `CLAUDE.md`
- **AC-9** Given a development machine with either rootless Podman or Docker, then Dev Services and all tests run without code changes. The README documents the Podman setup:
  - install `podman-docker`
  - `systemctl --user enable --now podman.socket`
  - `export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock`
  - `export TESTCONTAINERS_RYUK_DISABLED=true`

## Notes
- The application runs in JVM mode only; there is no native image (see NFR-003).
