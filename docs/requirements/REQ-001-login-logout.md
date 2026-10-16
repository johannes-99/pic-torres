---
id: REQ-001
title: Sign in and sign out
type: functional
status: approved
priority: must
milestone: M1
---

## User story
As a family member, I want to sign in with my account, so that only my family can see our photos.

## Acceptance criteria
- **AC-1** Given I am not signed in, when I open any page of the gallery, then I am redirected to the Keycloak sign-in page.
- **AC-2** Given I sign in with valid credentials and the role `member`, when the sign-in completes, then I land on the gallery page I originally requested.
- **AC-3** Given I am signed in, when the UI loads, then the header shows my display name, taken from `GET /api/me`, which returns my user ID and display name.
- **AC-4** Given I am signed in, when I click "Sign out", then my session ends in the app and in Keycloak, and I see a signed-out page with a "Sign in again" link.
- **AC-5** Given I am not signed in, when a client calls `/api/**` or requests an image URL, then the request is denied (401, or a redirect for browser navigation) and no image data is returned.
- **AC-6** Given I sign in with a user that does not have the role `member`, when I open the gallery, then I see an "Access denied" message, and the API returns 403.

## Notes
- Technical details: NFR-002.
