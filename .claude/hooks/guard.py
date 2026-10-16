#!/usr/bin/env python3
"""PreToolUse guard (NFR-007 AC-5).

Blocks changes that would let an agent weaken the quality gates:
  - disabling or focusing tests (@Disabled, .skip(, .only(, .fixme()
  - deleting test files
  - removing acceptance criteria from approved/implemented requirements
  - moving a requirement back to draft, or setting "approved" (only a human approves)

Exit code 2 blocks the tool call; the message on stderr is shown to the agent.
"""

import json
import re
import shlex
import sys
from pathlib import Path

TEST_PATH = re.compile(
    r"(^|/)(backend/src/test/|e2e/tests/)|\.spec\.ts$|\.test\.mjs$|(^|/)src/test/"
)
REQUIREMENT_PATH = re.compile(r"(^|/)docs/requirements/(REQ|NFR)-\d{3}[^/]*\.md$")
WEAKENING = re.compile(r"@Disabled\b|\b(?:it|test|describe)\.(?:skip|only|fixme)\(|\bxit\(|\bfit\(")
STATUS = re.compile(r"^status:\s*(\S+)", re.MULTILINE)
CRITERION = re.compile(r"^\s*-\s+\*\*(AC-\d+)\*\*", re.MULTILINE)


def block(message: str) -> None:
    print(f"Blocked by guard rail (NFR-007 AC-5): {message}", file=sys.stderr)
    print("If this change is really intended, ask the human to make it.", file=sys.stderr)
    sys.exit(2)


def read(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except (FileNotFoundError, IsADirectoryError, UnicodeDecodeError):
        return ""


def resulting_content(tool: str, args: dict, current: str) -> str:
    """Content of the file after the edit, as far as it can be derived from the tool input."""
    if tool == "Write":
        return args.get("content", "")
    edits = args.get("edits") if tool == "MultiEdit" else [args]
    result = current
    for edit in edits or []:
        old, new = edit.get("old_string", ""), edit.get("new_string", "")
        if edit.get("replace_all"):
            result = result.replace(old, new)
        else:
            result = result.replace(old, new, 1)
    return result


def check_test_file(rel: str, before: str, after: str) -> None:
    added = len(WEAKENING.findall(after)) - len(WEAKENING.findall(before))
    if added > 0:
        block(f"{rel}: tests must not be disabled, skipped or focused.")


def check_requirement(rel: str, before: str, after: str) -> None:
    if not before:
        return  # new requirement files are fine
    old_status = (STATUS.search(before) or [None, "draft"])[1]
    new_status = (STATUS.search(after) or [None, "draft"])[1]
    if new_status == "approved" and old_status != "approved":
        block(f"{rel}: only a human may set a requirement to approved.")
    if old_status in ("approved", "implemented"):
        if new_status in ("draft", "rejected"):
            block(f"{rel}: status {old_status} must not be changed to {new_status}.")
        removed = sorted(set(CRITERION.findall(before)) - set(CRITERION.findall(after)))
        if removed:
            block(f"{rel}: acceptance criteria {', '.join(removed)} must not be removed.")


def check_bash(command: str) -> None:
    for segment in re.split(r"&&|\|\||[;|\n]", command):
        try:
            words = shlex.split(segment)
        except ValueError:
            words = segment.split()
        deleting = words[:1] == ["rm"] or words[:2] == ["git", "rm"] or words[:1] == ["unlink"]
        if deleting and any(TEST_PATH.search(word) for word in words[1:]):
            block("test files must not be deleted.")
    if re.search(r"\bgit\s+push\b.*(--force|\s-f\b|\bmain\b|\bHEAD:main\b)", command):
        block("no force pushes and no direct pushes to main; open a pull request instead.")


def main() -> None:
    event = json.load(sys.stdin)
    tool = event.get("tool_name", "")
    args = event.get("tool_input", {}) or {}
    root = Path(event.get("cwd") or ".")

    if tool == "Bash":
        check_bash(args.get("command", ""))
        return
    if tool not in ("Edit", "Write", "MultiEdit"):
        return

    path = Path(args.get("file_path", ""))
    rel = str(path.relative_to(root)) if path.is_absolute() and path.is_relative_to(root) else str(path)
    before = read(path)
    after = resulting_content(tool, args, before)

    if TEST_PATH.search(rel):
        check_test_file(rel, before, after)
    if REQUIREMENT_PATH.search(rel):
        check_requirement(rel, before, after)


if __name__ == "__main__":
    main()
