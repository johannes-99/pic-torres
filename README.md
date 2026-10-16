# pic-torres

A self-hosted family image gallery: a Quarkus backend with an Angular frontend.

Status: **M0 Foundation.** The skeleton, test infrastructure and CI are in place; features follow in M1–M3.

- [Vision and scope](docs/vision.md)
- [Requirements](docs/requirements/) ([format and process](docs/requirements/README.md)), [traceability matrix](docs/traceability.md)
- [Architecture decisions](docs/adr/)
- [Backlog and milestones](docs/backlog.md)
- [Working conventions for humans and AI agents](CLAUDE.md)

## Development setup

You need:

- **JDK 21**
- **Node LTS** via [nvm](https://github.com/nvm-sh/nvm): `nvm install` (reads `.nvmrc`)
- **A container runtime** for Quarkus Dev Services (Postgres, Keycloak) and the tests: Podman or Docker.

### Podman (rootless)

```bash
sudo apt install podman podman-docker
systemctl --user enable --now podman.socket
# add to ~/.bashrc:
export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock
export TESTCONTAINERS_RYUK_DISABLED=true
```

With Docker, no extra setup is needed.

### Run

```bash
cd backend
./mvnw quarkus:dev        # http://localhost:8080 – sign in as alice / alice
./mvnw verify             # all checks (backend, frontend, coverage, formatting)
cd ../e2e && npm ci && npx playwright install chromium && npx playwright test
```

The development realm has three users. Each user's password is the same as their username:

| User | Access |
|---|---|
| `alice` | Family member |
| `bob` | Family member |
| `carol` | Signed in, but no access (no `member` role) |
