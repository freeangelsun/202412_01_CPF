#!/usr/bin/env python3
"""Fail-closed Windows path compatibility gate for CPF source paths.

The historical 160-character relative-path budget remains a warning by default.
FullLocal additionally evaluates the real repository-root length and fails when a
managed source path exceeds the conservative full-path budget.
"""
from __future__ import annotations

import argparse
import re
from pathlib import Path

DEFAULT_RELATIVE_BUDGET = 160
DEFAULT_FULL_BUDGET = 240
FORBIDDEN_SEGMENT_PATTERNS = (
    re.compile(r"^REV[-_]?\d+$", re.I),
    re.compile(r"^SESSION[-_]?\d+$", re.I),
    re.compile(r"^\d{8}$"),
    re.compile(r"^FINAL_FINAL$", re.I),
)
EPHEMERAL_SEGMENTS = {".git", ".gradle", ".pytest_cache", "__pycache__", "node_modules", ".venv", ".cpf-python", "dist"}
VERSIONED_DIR_EXEMPT_PREFIXES = ("cpf-docs/deliverables/",)


def is_managed_source(path: Path, root: Path) -> bool:
    rel = path.relative_to(root)
    parts = rel.parts
    if any(part in EPHEMERAL_SEGMENTS for part in parts):
        return False
    # Ordinary module build output is generated, but cpf-tools/build/** is product source.
    if "build" in parts and not (len(parts) >= 2 and parts[0] == "cpf-tools" and parts[1] == "build"):
        return False
    return True


def projected_length(target_root: str, rel: str) -> int:
    root_text = target_root.rstrip("\\/")
    return len(root_text) + (1 if root_text else 0) + len(rel)


def is_versioned_dir_exempt(rel: str) -> bool:
    """Allow dated archival folders only under protected deliverables.

    This exemption applies to the naming rule only. Path-length budgets still
    cover every managed file under the protected path.
    """
    normalized = rel.replace("\\", "/")
    return any(normalized.startswith(prefix) for prefix in VERSIONED_DIR_EXEMPT_PREFIXES)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--target-root-text", default="", help="Windows repository root used only for path-length projection")
    ap.add_argument("--max-relative-path", type=int, default=DEFAULT_RELATIVE_BUDGET)
    ap.add_argument("--max-full-path", type=int, default=DEFAULT_FULL_BUDGET)
    ap.add_argument("--strict-relative-budget", action="store_true")
    ns = ap.parse_args()

    root = Path(ns.root).resolve()
    target_root = ns.target_root_text or str(root)
    failures: list[str] = []
    warnings: list[str] = []
    files = [p for p in root.rglob("*") if p.is_file() and is_managed_source(p, root)]
    max_seen = (0, "")
    max_full = (0, "")

    for path in files:
        rel = path.relative_to(root).as_posix()
        rel_len = len(rel)
        full_len = projected_length(target_root, rel)
        if rel_len > max_seen[0]:
            max_seen = (rel_len, rel)
        if full_len > max_full[0]:
            max_full = (full_len, rel)
        if rel_len > ns.max_relative_path:
            message = f"RELATIVE_BUDGET_EXCEEDED {rel_len} {rel}"
            if ns.strict_relative_budget:
                failures.append(message)
            else:
                warnings.append(message)
        if full_len > ns.max_full_path:
            failures.append(f"FULL_PATH_TOO_LONG {full_len} {rel}")
        if not is_versioned_dir_exempt(rel):
            for segment in Path(rel).parts[:-1]:
                if any(rx.match(segment) for rx in FORBIDDEN_SEGMENT_PATTERNS):
                    failures.append(f"FORBIDDEN_VERSIONED_DIR {segment} {rel}")

    print(f"WINDOWS_PATH_FILES={len(files)}")
    print(f"WINDOWS_PATH_MAX_RELATIVE={max_seen[0]} {max_seen[1]}")
    print(f"WINDOWS_PATH_MAX_FULL={max_full[0]} {max_full[1]}")
    print(f"WINDOWS_PATH_TARGET_ROOT={target_root}")
    print(f"WINDOWS_PATH_RELATIVE_BUDGET={ns.max_relative_path}")
    print(f"WINDOWS_PATH_FULL_BUDGET={ns.max_full_path}")
    print(f"WINDOWS_PATH_WARNINGS={len(warnings)}")
    for warning in warnings:
        print(warning)
    print(f"WINDOWS_PATH_FAILURES={len(failures)}")
    for failure in failures:
        print(failure)
    if failures:
        print("WINDOWS_PATH_COMPATIBILITY=FAIL")
        return 1
    print("WINDOWS_PATH_COMPATIBILITY=PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
