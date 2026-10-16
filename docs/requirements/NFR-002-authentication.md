---
id: NFR-002
title: Authentication with OIDC (Keycloak, web-app flow)
type: non-functional
status: draft
priority: must
milestone: M0
---

## Rationale
Family members sign in through a central identity provider. The backend uses the OIDC authorization code flow with a server-side session cookie (the BFF pattern), for two reasons:
- Tokens never reach the browser.
- Plain `<img src>` requests are authenticated automatically.

See ADR-0002.

## Acceptance criteria
- **AC-1** Given the backend, then authentication uses `quarkus-oidc` with `quarkus.oidc.application-type=web-app`, against a Keycloak realm named `pic-torres`.
- **AC-2** Given dev and test mode, when the app starts, then Quarkus Dev Services start Keycloak automatically. The realm is imported from `backend/src/main/resources/keycloak/realm.json` and includes the test users `alice` and `bob` with role `member`.
- **AC-3** Given production, then the OIDC server URL, client ID and client secret are read only from environment variables. No production secret is committed to the repository.
- **AC-4** Given any endpoint under `/api/**` or `/images/**`, when it is called without a valid session, then the response is 401 for API/XHR requests and a redirect to login for browser navigation.
- **AC-5** Given state-changing requests (POST/PUT/DELETE), then CSRF protection is active, and the Angular client sends the CSRF token automatically.
- **AC-6** Given the session cookie, then it is `HttpOnly`, `Secure` (in production), and `SameSite=Lax`.
- **AC-7** Given the realm, then users must have the role `member` to use the gallery. A user without the role receives 403.

## Notes
- User accounts are managed by the administrator in Keycloak. v1 has no self-registration.
