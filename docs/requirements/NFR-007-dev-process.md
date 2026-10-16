---
id: NFR-007
title: Development process, AI agents and guard rails
type: non-functional
status: draft
priority: must
milestone: M0
---

## Rationale
The app is built largely by AI agents (Claude Code). The process and its guard rails make sure that agent output meets the requirements and cannot quietly bypass the tests.

## Acceptance criteria

### Traceability
- **AC-1** Tests reference requirements as follows:
  - JUnit: `@Tag("REQ-002")`, plus `@DisplayName("REQ-002/AC-3 ...")`
  - Playwright: test tag `@REQ-002`, with `REQ-002/AC-3` in the test title
  - Vitest: `describe('[REQ-002/AC-3] ...')`
- **AC-2** `scripts/trace.mjs`:
  - parses the front matter and acceptance criteria of every requirement, and all test sources
  - writes `docs/traceability.md` (a matrix of requirements and criteria against tests)
  - exits non-zero when coverage is missing (see NFR-004 AC-9)

### Definition of Done (recorded in `CLAUDE.md`)
- **AC-3** A requirement counts as done only when:
  - every acceptance criterion has a passing tagged test
  - `./mvnw verify` passes
  - the affected Playwright journeys pass
  - the trace check passes
  - an ADR exists for every architectural decision
  - the requirement's status is set to `implemented` in the same PR

### Claude Code configuration
- **AC-4** `.claude/settings.json` sets these permissions:
  - Allow `./mvnw`, `npm`, `npx`, `docker compose`, `git`, and `gh pr`.
  - Deny force pushes, pushes to `main`, recursive deletes outside the repository, and reading `.env*` or `deploy/secrets/**`.
- **AC-5** Hooks:
  - **PostToolUse** (Edit/Write) runs the formatter.
  - **PreToolUse** blocks edits that do any of the following:
    - remove acceptance criteria from an `approved` requirement
    - change a requirement status from `approved` back to `draft`
    - add `@Disabled`, `.skip(` or `.only(` to tests
    - delete test files
  - **Stop** runs the fast unit tests and the trace check, so a session cannot end while they fail.
- **AC-6** These subagents are defined in `.claude/agents/`:
  - `requirements-analyst`: turns an idea or issue into a requirement file with Given/When/Then criteria.
  - `test-author`: writes failing tests from approved criteria, and may write only under test directories.
  - `reviewer`: read-only tools; reviews a diff against its requirement, `CLAUDE.md` and the ADRs.

### Workflow
- **AC-7** Each requirement goes through these steps:
  1. a feature branch named `feat/REQ-NNN-slug`
  2. failing tests written by `test-author`
  3. the implementation
  4. a `reviewer` pass and `/code-review`
  5. a pull request
  6. CI green
  7. a merge
- **AC-8** An autonomous run stops and reports to the human when any of these happen:
  - CI is still red after 3 fix attempts on the same item.
  - A requirement is ambiguous or contradicts another.
  - A needed action is outside the allowed permissions (sudo, secrets, making the repository public, etc.).
