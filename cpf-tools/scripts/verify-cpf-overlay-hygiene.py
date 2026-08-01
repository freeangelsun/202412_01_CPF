#!/usr/bin/env python3
"""Fail-closed portable hygiene and high-confidence secret gate for the CPF overlay."""
from __future__ import annotations
import argparse
import re
import sys
from pathlib import Path

FORBIDDEN_DIRS = {"build", ".gradle", "node_modules", "dist", "coverage", "playwright-report", "test-results", "__pycache__"}
FORBIDDEN_SUFFIXES = {".pyc", ".log", ".tmp", ".bak", ".orig", ".rej", ".zip"}
SECRET_PATTERNS = {
    "private-key": re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
    "aws-access-key": re.compile(r"\bAKIA[0-9A-Z]{16}\b"),
    "github-token": re.compile(r"\bgh[pousr]_[A-Za-z0-9]{36,}\b"),
    "slack-token": re.compile(r"\bxox[baprs]-[A-Za-z0-9-]{20,}\b"),
}


def verify(root: Path) -> tuple[int, list[str]]:
    errors: list[str] = []
    files = 0
    for path in sorted(root.rglob("*")):
        rel = path.relative_to(root)
        if any(part in FORBIDDEN_DIRS for part in rel.parts):
            if path.is_file(): errors.append(f"forbidden generated directory content: {rel.as_posix()}")
            continue
        if not path.is_file():
            continue
        files += 1
        if path.suffix.lower() in FORBIDDEN_SUFFIXES or path.name.startswith(("npm-debug", "yarn-error", "hs_err_pid")):
            errors.append(f"forbidden generated file: {rel.as_posix()}")
            continue
        raw = path.read_bytes()
        if b"\x00" in raw and path.suffix.lower() not in {".png", ".jpg", ".jpeg", ".webp", ".ico"}:
            errors.append(f"unexpected binary/null content: {rel.as_posix()}")
            continue
        try:
            text = raw.decode("utf-8")
        except UnicodeDecodeError:
            continue
        for line_no, line in enumerate(text.splitlines(), start=1):
            if line.rstrip() != line:
                errors.append(f"trailing whitespace: {rel.as_posix()}:{line_no}")
                break
        for name, pattern in SECRET_PATTERNS.items():
            if pattern.search(text):
                errors.append(f"high-confidence secret pattern {name}: {rel.as_posix()}")
    return files, errors


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    args = parser.parse_args()
    files, errors = verify(args.root.resolve())
    if errors:
        for error in errors: print(f"[FAIL] {error}", file=sys.stderr)
        return 1
    print(f"[PASS] CPF overlay hygiene files={files} generatedArtifacts=0 highConfidenceSecrets=0 trailingWhitespace=0")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
