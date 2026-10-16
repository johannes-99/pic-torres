---
id: NFR-004
title: Automated testing and quality gates
type: non-functional
status: draft
priority: must
milestone: M0
---

## Rationale
Every change is verified automatically, following the test pyramid: many fast unit tests, fewer integration tests, and a few end-to-end tests.

## Acceptance criteria
- **AC-1** Backend unit tests use JUnit 5, AssertJ and Mockito (`quarkus-junit5-mockito`).
- **AC-2** Backend integration tests use `@QuarkusTest` with REST Assured, against the real PostgreSQL and Keycloak started by Dev Services. Security is tested with `quarkus-test-security` (`@TestSecurity`).
- **AC-3** The packaged application is smoke-tested with `@QuarkusIntegrationTest`.
- **AC-4** ArchUnit tests enforce the layering: resource → service → repository/storage. There are no cycles, and resources never access repositories directly.
- **AC-5** Frontend unit and component tests use Vitest and Angular Testing Library.
- **AC-6** End-to-end tests use Playwright (TypeScript) in `e2e/`. They run against the full stack, with the seeded Keycloak realm and test users.
- **AC-7** `./mvnw verify` runs all of the following, and fails if any one fails:
  - backend tests
  - frontend lint and tests (via Quinoa)
  - Spotless format check
  - JaCoCo line coverage of at least 80 % for the backend
- **AC-8** The frontend uses ESLint and Prettier, and a lint error fails the build.
- **AC-9** `node scripts/trace.mjs` fails if an `approved` or `implemented` requirement has an acceptance criterion without a referencing test.
- **AC-10** Tests are deterministic: there are no real network calls outside the local test containers, and no sleeps used for synchronization.
