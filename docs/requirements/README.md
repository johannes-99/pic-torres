# Requirements

## File naming
- `REQ-NNN-slug.md`: a functional requirement. 001 to 099 are v1; 100 and up are the backlog.
- `NFR-NNN-slug.md`: a non-functional or technical requirement.

## Format
```markdown
---
id: REQ-001
title: Short title
type: functional | non-functional
status: draft | approved | implemented | rejected
priority: must | should | could
milestone: M0 | M1 | ...
---

## User story / Rationale
As a <role> I want <capability> so that <benefit>.

## Acceptance criteria
- **AC-1** Given ... When ... Then ...
- **AC-2** ...

## Notes
Open questions, constraints, links to ADRs.
```

## Status lifecycle
`draft` → (a human reviews it) → `approved` → (implemented, with all ACs tested) → `implemented`

- Only a human sets `approved`.
- After a requirement is approved, its acceptance criteria may only change in a reviewed PR.
- Removing criteria or changing a status back is blocked by guard rails (NFR-007).

## Traceability
- Tests reference the requirement and criterion they verify, e.g. `REQ-002/AC-3`. See NFR-007 for how each test framework expresses this.
- `scripts/trace.mjs` generates `docs/traceability.md`.
- CI fails if an `approved` or `implemented` requirement has an acceptance criterion without a test.
