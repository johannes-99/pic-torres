---
id: NFR-005
title: Continuous integration and delivery (GitHub Actions)
type: non-functional
status: draft
priority: must
milestone: M0
---

## Rationale
The pipeline is the hard quality gate. Nothing reaches `main` without green checks.

## Acceptance criteria
- **AC-1** `.github/workflows/ci.yml` runs on every pull request and every push to `main`. It runs `./mvnw verify` and the trace check.
- **AC-2** `.github/workflows/e2e.yml` runs on pull requests to `main`:
  - It builds the container image, starts the stack with Compose (using the Docker preinstalled on the GitHub runner), and runs Playwright.
  - It uploads the HTML report and traces as artifacts.
- **AC-3** `.github/workflows/release.yml` runs on tags `v*`:
  - It builds the image with Jib and pushes it to GitHub Container Registry (GHCR).
  - It creates a GitHub Release with generated notes.
- **AC-4** CodeQL analysis runs for Java and TypeScript, on pull requests and weekly.
- **AC-5** Dependabot is configured for Maven, npm and GitHub Actions.
- **AC-6** Branch protection on `main` requires pull requests and the passing checks `ci`, `e2e` and `codeql`. Direct pushes and force pushes are not allowed.
- **AC-7** Commit messages follow Conventional Commits.
- **AC-8** The repository includes a pull request template (with a checklist for the Definition of Done) and issue templates for requirements and bugs.
