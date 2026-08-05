#!/usr/bin/env python3
from __future__ import annotations
import argparse
import re
from pathlib import Path

REQUIRED = (
    "Generator ownership manifest contains a duplicate path",
    "Generator ownership manifest contains a non-canonical or rooted path",
    "Generator ownership manifest contains an unsafe path segment",
    "Generator ownership path escapes the generated domain root",
    "Generator ownership manifest contains an invalid SHA-256",
    "function Get-SafeOwnedRelativePath",
    "function Get-OwnedTargetPath",
    "function Assert-Sha256",
    "managed file missing:",
    "unmanaged target collision:",
    "managed file drift:",
    "obsolete managed file drift:",
    "retainedObsoleteOwnership",
    "managedCandidatePaths",
    "[StringComparer]::OrdinalIgnoreCase",
)
FORBIDDEN = (
    "$userOwnedPrefixes",
    "@('src/main/java/', 'src/test/java/', 'ui/')",
)
_SHA256 = re.compile(r"^[0-9A-Fa-f]{64}$")


def validate_owned_relative_path(value: str) -> list[str]:
    errors: list[str] = []
    if not value or not value.strip() or value != value.strip():
        return ["invalid relative path"]
    if (
        "\\" in value
        or value.startswith("/")
        or value.endswith("/")
        or "//" in value
        or ":" in value
        or "\x00" in value
    ):
        errors.append("non-canonical or rooted path")
    parts = value.split("/")
    if any(not part.strip() or part in {".", ".."} or part != part.strip() or part.endswith(".") for part in parts):
        errors.append("unsafe path segment")
    return errors


def validate_sha256(value: str) -> list[str]:
    return [] if _SHA256.fullmatch(value or "") else ["invalid SHA-256"]


def validate(path: Path) -> list[str]:
    text = path.read_text(encoding="utf-8-sig")
    errors = [f"missing token: {token}" for token in REQUIRED if token not in text]
    errors += [f"forbidden broad ownership token: {token}" for token in FORBIDDEN if token in text]
    user_owned = "$userOwnedFiles = @('README.md', 'config/cpf-approved-exceptions.csv')"
    if user_owned not in text:
        errors.append("explicit user-owned file allowlist is missing")
    if text.index("$ownedSha = if ($oldOwned.ContainsKey($relative))") > text.index("$currentSha = Get-Sha $target"):
        errors.append("ownership lookup must precede target mutation classification")
    if text.index("$oldOwned = @{}") > text.index("$tempRoot = Join-Path"):
        errors.append("ownership manifest validation must run before candidate generation")
    for unsafe_join in (
        "$target = Join-Path $domainRoot $relative",
        "$target = Join-Path $domainRoot $ownedPath",
        "$target = Join-Path $domainRoot $candidateRelative",
        "$target = Join-Path $domainRoot $obsoleteRelative",
    ):
        if unsafe_join in text:
            errors.append(f"owned target bypasses containment guard: {unsafe_join}")
    return errors


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    args = parser.parse_args()
    path = args.root / "cpf-tools/generator/upgrade-domain.ps1"
    if not path.is_file():
        print(f"FAIL missing {path}")
        return 1
    errors = validate(path)
    if errors:
        for error in errors:
            print(f"FAIL {error}")
        return 1
    print("PASS generator upgrade ownership is manifest-driven, path-contained, and fail-closed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
