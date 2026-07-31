#!/usr/bin/env python3
"""QA33 repository-wide closure gate.

This gate is deliberately fail-closed. Overlay mode validates the shipped policy and
runner chain. Full mode additionally scans the complete repository for dependency
locks, removed legacy primaries, migration dual-primary implementations, unbounded
resource reads, browser token storage, generator parity, and observability identity
coverage.
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any

TEXT_EXTENSIONS = {
    ".java", ".kt", ".kts", ".groovy", ".gradle", ".ps1", ".py",
    ".ts", ".js", ".mjs", ".vue", ".sql", ".yml", ".yaml",
    ".json", ".properties", ".xml"
}
IGNORED_DIRS = {
    ".git", ".gradle", "build", "node_modules", "dist", "coverage",
    "playwright-report", "test-results", "__pycache__"
}


def read_json(path: Path) -> dict[str, Any]:
    value = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise ValueError(f"JSON object required: {path}")
    return value


def iter_text(root: Path):
    for path in root.rglob("*"):
        if not path.is_file() or path.suffix.lower() not in TEXT_EXTENSIONS:
            continue
        if any(part in IGNORED_DIRS for part in path.relative_to(root).parts):
            continue
        yield path


def run_python_gate(root: Path, relative: str) -> tuple[int, str]:
    result = subprocess.run(
        [sys.executable, str(root / relative), "--root", str(root)],
        cwd=root,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    combined = (result.stdout + "\n" + result.stderr).strip()
    return result.returncode, combined


def verify(root: Path, policy: dict[str, Any], overlay: bool) -> dict[str, Any]:
    failures: list[str] = []
    checks = 0

    if policy.get("schemaVersion") != 1:
        failures.append("unsupported policy schemaVersion")

    for relative in policy.get("requiredFiles", []):
        checks += 1
        if not (root / relative).is_file():
            failures.append(f"required file missing:{relative}")

    for relative, markers in policy.get("requiredMarkers", {}).items():
        path = root / relative
        text = path.read_text(encoding="utf-8", errors="replace") if path.is_file() else ""
        for marker in markers:
            checks += 1
            if marker not in text:
                failures.append(f"required marker missing:{relative}:{marker}")

    for relative in policy.get("forbiddenExistingPaths", []):
        checks += 1
        if (root / relative).exists():
            failures.append(f"legacy primary remains:{relative}")

    compiled_rules: list[tuple[str, re.Pattern[str], set[str]]] = []
    for rule in policy.get("forbiddenPatterns", []):
        try:
            compiled_rules.append((
                str(rule["id"]),
                re.compile(str(rule["regex"]), re.IGNORECASE | re.MULTILINE),
                {str(ext).lower() for ext in rule.get("extensions", [])},
            ))
        except (KeyError, re.error) as exc:
            failures.append(f"invalid forbidden pattern:{rule}:{exc}")

    allowlist = {
        str(key): {str(item) for item in values}
        for key, values in policy.get("patternAllowlist", {}).items()
    }
    for path in iter_text(root):
        relative = path.relative_to(root).as_posix()
        text = path.read_text(encoding="utf-8", errors="replace")
        for rule_id, pattern, extensions in compiled_rules:
            if extensions and path.suffix.lower() not in extensions:
                continue
            checks += 1
            if relative in allowlist.get(rule_id, set()):
                continue
            if pattern.search(text):
                failures.append(f"{rule_id}:{relative}")

    # The three Python gates are part of the release call graph and must remain executable.
    for gate in (
        "cpf-tools/scripts/verify-cpf-qa32-primary-engines.py",
        "cpf-tools/scripts/verify-cpf-qa32-repository-security.py",
        "cpf-tools/scripts/verify-cpf-qa32-generator.py",
    ):
        if not (root / gate).is_file():
            continue
        if overlay:
            # Nested QA32 gates require the complete repository (frontend manifests,
            # generator source, and the full DB migration history). Overlay mode validates
            # only the changed closure policy/runner chain and must not manufacture
            # failures from intentionally omitted unchanged source.
            continue
        checks += 1
        code, output = run_python_gate(root, gate)
        if code != 0:
            failures.append(f"nested gate failed:{gate}:{output[-1200:]}")

    if not overlay:
        full = policy.get("fullRepositoryRequirements", {})
        lockfiles = [
            path for path in root.rglob("gradle.lockfile")
            if not any(part in IGNORED_DIRS for part in path.relative_to(root).parts)
        ]
        checks += 1
        minimum_lockfiles = int(full.get("minimumGradleLockfiles", 1))
        if len(lockfiles) < minimum_lockfiles:
            failures.append(
                f"dependency lockfiles missing:required>={minimum_lockfiles},actual={len(lockfiles)}"
            )

        for relative in full.get("requiredGeneratorPaths", []):
            checks += 1
            if not (root / relative).exists():
                failures.append(f"generator closure path missing:{relative}")

        for relative in full.get("requiredRepositoryGatePaths", []):
            checks += 1
            if not (root / relative).is_file():
                failures.append(f"repository closure gate path missing:{relative}")

        migration_text = "\n".join(
            path.read_text(encoding="utf-8", errors="replace")
            for path in root.rglob("build.gradle*") if path.is_file()
        ).lower()
        for marker in full.get("requiredMigrationMarkers", []):
            checks += 1
            if str(marker).lower() not in migration_text:
                failures.append(f"Flyway primary marker missing:{marker}")

        source_text = "\n".join(
            path.read_text(encoding="utf-8", errors="replace")
            for path in iter_text(root)
            if path.suffix.lower() in {".java", ".kt", ".properties", ".yml", ".yaml"}
        )
        for token in full.get("requiredObservabilityTokens", []):
            checks += 1
            if str(token) not in source_text:
                failures.append(f"observability identity token missing:{token}")

    return {
        "schemaVersion": 1,
        "overlayMode": overlay,
        "checks": checks,
        "failures": failures,
        "status": "PASS" if not failures else "FAIL",
    }


def self_test(script: Path) -> int:
    with tempfile.TemporaryDirectory(prefix="cpf-qa33-closure-") as directory:
        root = Path(directory)
        (root / "cpf-tools/config").mkdir(parents=True)
        policy = {
            "schemaVersion": 1,
            "requiredFiles": ["build.gradle"],
            "requiredMarkers": {"build.gradle": ["lockAllConfigurations()"]},
            "forbiddenExistingPaths": ["legacy.txt"],
            "forbiddenPatterns": [{
                "id": "UNBOUNDED_READ_ALL_BYTES",
                "regex": r"\breadAllBytes\s*\(",
                "extensions": [".java"]
            }],
            "patternAllowlist": {"UNBOUNDED_READ_ALL_BYTES": []},
            "fullRepositoryRequirements": {}
        }
        (root / "build.gradle").write_text("lockAllConfigurations()\n", encoding="utf-8")
        report = verify(root, policy, overlay=True)
        if report["status"] != "PASS":
            print(json.dumps(report, ensure_ascii=False, indent=2))
            return 1
        (root / "Unsafe.java").write_text("class Unsafe { byte[] x() { return in.readAllBytes(); }}", encoding="utf-8")
        report = verify(root, policy, overlay=True)
        if not any("UNBOUNDED_READ_ALL_BYTES" in item for item in report["failures"]):
            print(json.dumps(report, ensure_ascii=False, indent=2))
            return 1
    print("[CPF][QA33][PASS] repository closure negative self-test")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument(
        "--policy",
        default="cpf-tools/config/qa33-repository-closure-policy.json",
    )
    parser.add_argument("--overlay", action="store_true")
    parser.add_argument("--json-report")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()

    if args.self_test:
        return self_test(Path(__file__))

    root = Path(args.root).resolve()
    try:
        policy = read_json(root / args.policy)
        report = verify(root, policy, args.overlay)
    except Exception as exc:
        report = {
            "schemaVersion": 1,
            "overlayMode": args.overlay,
            "checks": 0,
            "failures": [f"gate exception:{type(exc).__name__}:{exc}"],
            "status": "FAIL",
        }

    if args.json_report:
        output = root / args.json_report
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if report["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
