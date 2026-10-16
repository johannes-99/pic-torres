# pic-torres: vision

## Goal
pic-torres is a private, self-hosted image gallery for a family. Family members sign in, upload their photos, and browse and view them in a fast, simple web UI. The photos stay on hardware the family controls.

## Users
| Role | Description |
|---|---|
| Family member | Signs in, uploads images, and browses and views all images in the gallery. |
| Administrator | Runs the server, manages user accounts in Keycloak, and handles backups. |

## Scope of v1
- Sign in and sign out via OIDC (REQ-001)
- Upload images (REQ-002)
- Browse the gallery as a thumbnail grid (REQ-003)
- View a single image full-size and download the original (REQ-004)

## Out of scope for v1 (backlog drafts)
- Deleting images (REQ-101)
- Albums (REQ-102)
- EXIF details, filtering and search (REQ-104)
- Video, mobile apps, public sharing links, face recognition

## Principles
- **Requirements first.** Every feature starts as a requirement file in `docs/requirements/` with acceptance criteria. Every acceptance criterion is covered by an automated test (see NFR-007).
- **Self-hosted and simple.** One app container plus PostgreSQL and Keycloak. No cloud dependencies.
- **Privacy.** Every image and API call requires authentication.

## Document map
- `docs/requirements/`: functional (`REQ-*`) and non-functional (`NFR-*`) requirements
- `docs/adr/`: architecture decision records
- `docs/backlog.md`: ordered milestones and the build process
