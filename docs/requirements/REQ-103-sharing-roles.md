---
id: REQ-103
title: Sharing and roles
type: functional
status: draft
priority: could
milestone: backlog
---

## User story
As an administrator, I want to give family members different rights, and restrict some albums to certain people, so that private photos stay private.

## Acceptance criteria (draft)
- **AC-1** The roles are `admin`, `member` (upload and view) and `viewer` (view only), managed in Keycloak.
- **AC-2** An album can be restricted to selected users. Images that exist only in restricted albums are hidden from other users everywhere, including the main gallery and direct URLs.
- **AC-3** A viewer cannot upload: the upload button is hidden, and the API returns 403.

## Open questions
- Do we need public share links for people outside the family? (Currently out of scope.)
