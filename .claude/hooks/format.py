#!/usr/bin/env python3
"""PostToolUse formatter (NFR-007 AC-5): formats the file that was just edited.

Java → Spotless (google-java-format), web files → Prettier. Never blocks; problems are reported
to the agent so it can react, and the build's format check stays the hard gate.
"""

import json
import os
import subprocess
import sys
from pathlib import Path

PRETTIER_SUFFIXES = {".ts", ".mjs", ".js", ".html", ".scss", ".css", ".json", ".md"}
NVM = 'export NVM_DIR="$HOME/.nvm"; [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh" >/dev/null; '


def run(command: str, cwd: Path) -> None:
    result = subprocess.run(
        ["bash", "-c", NVM + command], cwd=cwd, capture_output=True, text=True, timeout=120
    )
    if result.returncode != 0:
        print(f"Formatter failed in {cwd}: {result.stdout[-800:]}{result.stderr[-800:]}", file=sys.stderr)


def main() -> None:
    event = json.load(sys.stdin)
    path = Path((event.get("tool_input") or {}).get("file_path", ""))
    root = Path(os.environ.get("CLAUDE_PROJECT_DIR") or event.get("cwd") or ".")
    if not path.is_file() or not path.is_relative_to(root):
        return
    rel = path.relative_to(root).as_posix()

    if rel.startswith("backend/src/") and path.suffix == ".java":
        pattern = ".*" + path.name.replace(".", "\\.")
        run(f"./mvnw -q -B -o spotless:apply -DspotlessFiles='{pattern}'", root / "backend")
    elif path.suffix in PRETTIER_SUFFIXES and not rel.startswith("docs/"):
        for project in ("backend/src/main/webui", "e2e"):
            if rel.startswith(project + "/"):
                run(f"npx --no-install prettier --write '{path}'", root / project)
                return


if __name__ == "__main__":
    main()
