---
id: NFR-006
title: Self-hosted deployment
type: non-functional
status: draft
priority: must
milestone: M4
---

## Rationale
The gallery runs on a home server or a small VPS, using one Compose file.

## Acceptance criteria
- **AC-1** `deploy/compose.yaml` defines the services:
  - `app` (the image from GHCR)
  - `postgres`
  - `keycloak`
  - `caddy` (reverse proxy with automatic HTTPS)
- **AC-2** Image files and database data live in named volumes, and survive container recreation.
- **AC-3** All secrets and host-specific values come from a `.env` file that is not committed. A `.env.example` with placeholders is committed. Values include the database password, the OIDC client secret and the domain.
- **AC-4** Given `podman compose up -d` or `docker compose up -d` on a fresh host with a valid `.env`, then the gallery is reachable over HTTPS on the configured domain, and REQ-001 to REQ-004 work.
- **AC-5** The app exposes `/q/health/live` and `/q/health/ready`, and Compose uses them as health checks.
- **AC-6** `deploy/README.md` documents:
  - the installation
  - updating to a new release
  - backing up and restoring the volumes and the database
  - adding a family member in Keycloak

## Notes
- The production runtime (Compose, or Podman Quadlet with systemd) will be decided before M4. These acceptance criteria may change then.
