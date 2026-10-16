#!/usr/bin/env python3
"""Stop hook (NFR-007 AC-5): a session must not end with failing fast checks.

Runs only the checks affected by uncommitted changes, so it stays quick:
  - always: trace check and its tests
  - Java changes: backend unit tests (no containers; *QuarkusTest classes are excluded)
  - webui changes: lint, format check and Vitest
Exit code 2 sends the failure back to the agent instead of ending the session.
"""

import json
import os
import subprocess
import sys
from pathlib import Path

NVM = 'export NVM_DIR="$HOME/.nvm"; [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh" >/dev/null; '


def changed_files(root: Path) -> list[str]:
    out = subprocess.run(
        ["git", "status", "--porcelain", "--untracked-files=all"],
        cwd=root, capture_output=True, text=True,
    ).stdout
    return [line[3:].strip() for line in out.splitlines() if line.strip()]


def check(name: str, command: str, cwd: Path) -> str | None:
    result = subprocess.run(
        ["bash", "-c", NVM + command], cwd=cwd, capture_output=True, text=True, timeout=900
    )
    if result.returncode == 0:
        return None
    return f"--- {name} failed ---\n{(result.stdout + result.stderr)[-3000:]}"


def main() -> None:
    event = json.load(sys.stdin)
    if event.get("stop_hook_active"):
        return  # already reported once; avoid an endless loop
    root = Path(os.environ.get("CLAUDE_PROJECT_DIR") or event.get("cwd") or ".")
    changed = changed_files(root)
    if not changed:
        return

    failures = [
        check("trace check", "node --test scripts/*.test.mjs && node scripts/trace.mjs", root)
    ]
    if any(f.startswith("backend/") and f.endswith(".java") for f in changed):
        failures.append(
            check(
                "backend unit tests",
                "./mvnw -q -B -o test -Dtest='!*QuarkusTest' -Dsurefire.failIfNoSpecifiedTests=false"
                " -Djacoco.skip=true",
                root / "backend",
            )
        )
    if any(f.startswith("backend/src/main/webui/") for f in changed):
        failures.append(check("frontend checks", "npm run verify", root / "backend/src/main/webui"))

    failures = [f for f in failures if f]
    if failures:
        print("Fast checks fail; fix them before finishing (NFR-007 AC-5).", file=sys.stderr)
        print("\n".join(failures), file=sys.stderr)
        sys.exit(2)


if __name__ == "__main__":
    main()
