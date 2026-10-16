# pic-torres

pic-torres is a self-hosted family image gallery: a Quarkus backend with an Angular frontend bundled through Quinoa. The requirements drive all work. Start with `docs/vision.md`, `docs/requirements/` and `docs/backlog.md`.

## How to work here
- Work only on requirements with `status: approved`, in milestone order (`docs/backlog.md`). Never change a status to `approved` yourself.
- Use one branch per requirement or milestone: `feat/REQ-NNN-slug`. Merge through pull requests only. Never push to `main`.
- Follow this loop for each item (NFR-007 AC-7):
  1. The `test-author` subagent writes failing tests.
  2. Implement until the tests pass.
  3. Review with the `reviewer` subagent and `/code-review`.
  4. Open a PR.
  5. Merge once CI is green.
- **Stop and report** to the human in any of these cases:
  - CI is still red after 3 fix attempts.
  - A requirement is ambiguous or contradicts another.
  - A step needs sudo, secrets, or a setting outside the allowed permissions.
- Record architectural decisions as a new ADR in `docs/adr/` (next number, status `proposed`).

## Definition of Done
- [ ] Every acceptance criterion has a passing test that references it (see "Tests").
- [ ] `./mvnw verify` passes in `backend/`. It runs:
  - backend unit and `@QuarkusTest` tests
  - the `@QuarkusIntegrationTest`
  - the JaCoCo ≥ 80 % coverage gate
  - the Spotless format check
  - frontend lint, Prettier and Vitest
- [ ] `npx playwright test` passes in `e2e/` for the affected journeys.
- [ ] `node --test scripts/*.test.mjs && node scripts/trace.mjs` passes, and the regenerated `docs/traceability.md` is committed.
- [ ] The requirement's `status` is set to `implemented` and `docs/backlog.md` is ticked off, in the same PR.

## Commands
| What | Command | Where |
|---|---|---|
| Dev mode (API + Angular with live reload, Dev Services) | `./mvnw quarkus:dev` → http://localhost:8080 (login `alice`/`alice`) | `backend/` |
| Full build and all checks | `./mvnw verify` | `backend/` |
| Backend unit tests only (no containers) | `./mvnw test -Dtest='!*QuarkusTest' -Dsurefire.failIfNoSpecifiedTests=false` | `backend/` |
| Format Java | `./mvnw spotless:apply` | `backend/` |
| Frontend checks | `npm run verify` (lint, Prettier check, Vitest) | `backend/src/main/webui/` |
| E2E | `npx playwright test` (starts dev mode unless `E2E_BASE_URL` is set) | `e2e/` |
| Traceability | `node scripts/trace.mjs` | repository root |

Containers come from Podman through its Docker-compatible socket. The shell needs `DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock` and `TESTCONTAINERS_RYUK_DISABLED=true`. Node comes from nvm (LTS).

## Code conventions
- **Backend packages** under `com.johannesmager.pictorres`. ArchUnit enforces the dependency direction (`ArchitectureTest`):
  - `api`: REST resources and DTOs
  - `service`: use cases and transactions
  - `persistence`: Panache entities and repositories
  - `storage`: file storage (all file I/O goes through `StorageService`)
  - `image`: thumbnails and EXIF
- The allowed dependency direction is `api → service → persistence | storage | image`.
- **Schema changes** are made only as Flyway migrations in `src/main/resources/db/migration` (`V<n>__<description>.sql`).
- **Configuration** goes into `application.properties` (`pictorres.*` for app settings). Production values come from environment variables, and secrets are never committed.
- **Security:**
  - Everything except `/q/health` requires the role `member`.
  - State-changing endpoints rely on CSRF (`XSRF-TOKEN` cookie / `X-XSRF-TOKEN` header, Angular's default).
  - User input never forms a file path.
- **Frontend:**
  - Standalone components, signals, Angular Material.
  - Don't hand-write the API client. Generate it from `/q/openapi` into `src/app/api`.
  - Fonts are self-hosted; there are no third-party CDNs.
- **Formatting:** google-java-format (Spotless) and Prettier. The PostToolUse hook formats edited files.

## Tests
- Reference criteria exactly as `REQ-002/AC-3` or `NFR-003/AC-1`:
  - **JUnit:** `@Tag("REQ-002")` on the class and `@DisplayName("REQ-002/AC-3 …")` on the method
  - **Vitest:** `describe('[REQ-002/AC-3] …')`
  - **Playwright:** `{ tag: ['@REQ-002'] }`, with `REQ-002/AC-3` in the title
- Name classes that start Quarkus `*QuarkusTest`. Use `*IT` only for `@QuarkusIntegrationTest` (failsafe).
- **Dev realm users:** `alice` and `bob` (role `member`), and `carol` (signed in, but no role). Each user's password is the same as their username.
- **Never** disable, skip or delete tests, remove acceptance criteria, or weaken assertions to make a build pass. The hooks in `.claude/hooks/` block the obvious cases.
