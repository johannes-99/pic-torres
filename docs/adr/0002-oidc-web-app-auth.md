# ADR-0002: OIDC web-app flow with session cookie (BFF) instead of SPA bearer tokens

- **Status:** proposed
- **Date:** 2026-10-16

## Context
Family members sign in via OIDC (Keycloak). The gallery displays many images through `<img src>` tags, and browsers cannot attach an `Authorization: Bearer` header to those requests.

## Decision
- Use `quarkus-oidc` with `application-type=web-app`:
  - The backend performs the authorization code flow.
  - It keeps the tokens server-side, in an encrypted session cookie.
- Angular uses no OIDC library. It calls `/api/me` to find out whether a user is signed in, and redirects to the login endpoint when it gets a 401.
- CSRF protection is applied to state-changing requests.

## Consequences
- **Positive:**
  - Images and API calls are protected by the same mechanism.
  - Tokens never reach JavaScript, which reduces the XSS impact.
  - The frontend code is simpler.
- **Negative:**
  - The UI and API must share one origin (guaranteed by Quinoa, ADR-0001).
  - CSRF handling is required.
- **Considered alternatives:**
  - SPA with PKCE and bearer tokens: images would need signed URLs, or blob fetching through JavaScript.
  - Basic/form login without an IdP: no SSO, and passwords live in the app.
