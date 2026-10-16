# Backlog

The ordered milestones that drive implementation. Work starts only on requirements with `status: approved`. The process and Definition of Done are defined in NFR-007.

## Prerequisites (done by a human, once)
- [x] Install Node LTS (e.g. via nvm), a container runtime (Podman with `podman-docker`, `podman.socket` enabled and `DOCKER_HOST` set; or Docker Engine), and the GitHub CLI `gh`.
- [x] Run `gh auth login`, then create an empty GitHub repository `pic-torres` (made public in M0) and add it as `origin`.
- [x] Review all requirements and ADRs, and set the accepted ones to `approved` / `accepted`.

## M0: Foundation ✅
The repository is public, because branch protection, auto-merge and CodeQL are not free for private repositories. Criteria of the M0 NFRs that depend on features (e.g. login redirects for API calls, the production stack) are covered in M1–M4; see `docs/traceability.md`.

| Item | Requirements |
|---|---|
| Guard rails: `CLAUDE.md` (Definition of Done), `.claude/settings.json` (permissions, hooks), `.claude/agents/*` | NFR-007 |
| Quarkus skeleton with extensions, Flyway baseline, health endpoints | NFR-001 |
| Angular app in `src/main/webui` with Material, ESLint and Vitest; Quinoa wiring | NFR-001 |
| Keycloak realm export, OIDC web-app config, Dev Services | NFR-002 |
| `StorageService` + filesystem implementation | NFR-003 |
| Test setup (JUnit, `@QuarkusTest`, ArchUnit, JaCoCo gate, Spotless, Playwright skeleton), `scripts/trace.mjs` | NFR-004, NFR-007 |
| GitHub workflows, templates, Dependabot, branch protection | NFR-005 |

## M1: Sign in
- REQ-001: sign in and sign out

## M2: Upload
- REQ-002: upload images

## M3: Gallery
- REQ-003: browse the gallery
- REQ-004: view a single image

## M4: Release v0.1.0
- NFR-006: production Compose with Caddy, deployment README, backup docs
- Full Playwright suite against the Compose stack
- Tag `v0.1.0` → image on GHCR

## Later (drafts, not scheduled)
- REQ-101: delete images
- REQ-102: albums
- REQ-104: EXIF details, filtering and search

## Build loop per item (see NFR-007 AC-7 and AC-8)
1. Branch `feat/REQ-NNN-slug`.
2. `test-author` writes failing tests for every acceptance criterion.
3. Implement until the tests pass.
4. Run the `reviewer` subagent and `/code-review`, and fix the findings.
5. Set the requirement status to `implemented`, regenerate `docs/traceability.md`, and open a PR.
6. Merge once CI is green. Then move to the next item.

**Stop and report** when any of these happen:
- CI is still red after 3 fix attempts.
- A requirement is ambiguous or contradicts another.
- An action is outside the allowed permissions.
