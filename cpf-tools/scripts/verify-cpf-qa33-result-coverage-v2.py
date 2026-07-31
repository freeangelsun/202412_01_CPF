#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any

ALLOWED = {"완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요"}
SHA_RE = re.compile(r"^[0-9a-f]{40}$")
HASH_RE = re.compile(r"^[0-9a-f]{64}$")
CATEGORIES = {"SOURCE_CONTRACT", "NEGATIVE_BOUNDARY", "RUNTIME_FAILURE_RECOVERY"}


def load_csv(path: Path, key: str) -> list[dict[str, str]]:
    if not path.is_file():
        raise FileNotFoundError(path)
    with path.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    values = [row.get(key, "").strip() for row in rows]
    if not values or not all(values):
        raise ValueError(f"blank or empty {key}:{path}")
    if len(values) != len(set(values)):
        raise ValueError(f"duplicate {key}:{path}")
    return rows


def git(root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", *args], cwd=root, capture_output=True, text=True, encoding="utf-8", errors="replace"
    )
    if result.returncode:
        raise RuntimeError(f"git {' '.join(args)} failed:{result.stderr.strip()}")
    return result.stdout.strip()


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def split_paths(value: str) -> list[str]:
    return [item.strip() for item in (value or "").split(";") if item.strip()]


def exact_relative_path(value: str) -> bool:
    return bool(value) and not Path(value).is_absolute() and not re.search(r"[*?\[\]{}]", value)


def validate_evidence(root: Path, relative: str, head: str, failures: list[str], label: str) -> None:
    if not exact_relative_path(relative):
        failures.append(f"invalid evidence path:{label}:{relative}")
        return
    path = root / relative
    if not path.is_file():
        failures.append(f"evidence missing:{label}:{relative}")
        return
    try:
        data: dict[str, Any] = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        failures.append(f"evidence invalid json:{label}:{exc}")
        return
    if data.get("sourceSha") != head:
        failures.append(f"evidence source SHA mismatch:{label}:{data.get('sourceSha')}!={head}")
    if data.get("exitCode") != 0 or data.get("sanitized") is not True:
        failures.append(f"evidence is not successful/sanitized:{label}")
    commands = data.get("commands") or data.get("command")
    if not commands:
        failures.append(f"evidence command missing:{label}")
    artifacts = data.get("artifacts", [])
    if not isinstance(artifacts, list) or not artifacts:
        failures.append(f"evidence artifact list missing:{label}")
        return
    for artifact in artifacts:
        if not isinstance(artifact, dict):
            failures.append(f"invalid evidence artifact:{label}")
            continue
        rel = artifact.get("path")
        expected = artifact.get("sha256")
        if not isinstance(rel, str) or not exact_relative_path(rel):
            failures.append(f"invalid artifact path:{label}:{rel}")
            continue
        target = root / rel
        if not target.is_file():
            failures.append(f"artifact missing:{label}:{rel}")
            continue
        if not isinstance(expected, str) or not HASH_RE.fullmatch(expected) or sha256(target) != expected:
            failures.append(f"artifact hash mismatch:{label}:{rel}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--release", action="store_true")
    parser.add_argument("--json-report")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures: list[str] = []

    try:
        requirements = load_csv(root / "cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv", "requirement_id")
        scenarios = load_csv(root / "cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv", "scenario_id")
        results = load_csv(root / "cpf-docs/quality/CPF_20260731_QA33_RESULT_MATRIX.csv", "record_key")
    except Exception as exc:
        report = {"status": "FAIL", "release": args.release, "failures": [str(exc)]}
        print(json.dumps(report, ensure_ascii=False, indent=2))
        return 1

    requirement_ids = {row["requirement_id"] for row in requirements}
    scenario_ids = {row["scenario_id"] for row in scenarios}
    expected_keys = {f"REQUIREMENT:{value}" for value in requirement_ids} | {
        f"SCENARIO:{value}" for value in scenario_ids
    }
    result_keys = {row["record_key"] for row in results}
    for missing in sorted(expected_keys - result_keys):
        failures.append(f"result row missing:{missing}")
    for extra in sorted(result_keys - expected_keys):
        failures.append(f"unexpected result row:{extra}")

    categories: dict[str, set[str]] = defaultdict(set)
    for scenario in scenarios:
        req = scenario["requirement_id"]
        category = scenario["category"]
        if req not in requirement_ids:
            failures.append(f"scenario requirement missing:{scenario['scenario_id']}:{req}")
        if category not in CATEGORIES:
            failures.append(f"invalid scenario category:{scenario['scenario_id']}:{category}")
        categories[req].add(category)
    for req in sorted(requirement_ids):
        if categories.get(req) != CATEGORIES:
            failures.append(f"scenario category coverage mismatch:{req}:{sorted(categories.get(req, set()))}")

    head = ""
    if args.release:
        try:
            head = git(root, "rev-parse", "HEAD")
            if not SHA_RE.fullmatch(head):
                failures.append(f"invalid HEAD:{head}")
            if git(root, "status", "--porcelain=v1"):
                failures.append("release gate requires clean Working Tree")
        except Exception as exc:
            failures.append(str(exc))

    status_counts: Counter[str] = Counter()
    for row in results:
        key = row["record_key"]
        record_type = row.get("record_type", "")
        record_id = row.get("record_id", "")
        if key != f"{record_type}:{record_id}":
            failures.append(f"record key mismatch:{key}:{record_type}:{record_id}")
        for field in ("development_status", "verification_status"):
            value = row.get(field, "")
            status_counts[f"{field}:{value}"] += 1
            if value not in ALLOWED:
                failures.append(f"invalid status:{key}:{field}:{value}")
        source_paths = split_paths(row.get("source_paths", ""))
        test_paths = split_paths(row.get("test_paths", ""))
        if row.get("development_status") == "완료":
            if not source_paths:
                failures.append(f"completed row has no exact source path:{key}")
            for rel in source_paths:
                if not exact_relative_path(rel):
                    failures.append(f"non-exact source path:{key}:{rel}")
                elif not (root / rel).exists():
                    failures.append(f"source path missing:{key}:{rel}")
        for rel in test_paths:
            if not exact_relative_path(rel):
                failures.append(f"non-exact test path:{key}:{rel}")
            elif not (root / rel).exists():
                failures.append(f"test path missing:{key}:{rel}")
        if args.release:
            if row.get("development_status") != "완료" or row.get("verification_status") != "완료":
                failures.append(f"release incomplete:{key}:{row.get('development_status')}:{row.get('verification_status')}")
            else:
                validate_evidence(root, row.get("evidence_path", ""), head, failures, key)

    report = {
        "schemaVersion": 2,
        "status": "PASS" if not failures else "FAIL",
        "release": args.release,
        "sourceSha": head or None,
        "requirements": len(requirements),
        "scenarios": len(scenarios),
        "resultRows": len(results),
        "statusCounts": dict(sorted(status_counts.items())),
        "failures": sorted(set(failures)),
    }
    if args.json_report:
        target = Path(args.json_report)
        if not target.is_absolute():
            target = root / target
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
