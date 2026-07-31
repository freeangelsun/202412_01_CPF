#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import subprocess
import sys
from datetime import datetime
from pathlib import Path
from typing import Any

COMPLETE = {"완료", "DEVELOPMENT_COMPLETE", "VERIFIED"}
SHA_RE = re.compile(r"^[0-9a-f]{40}$")
HASH_RE = re.compile(r"^[0-9a-f]{64}$")


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open(encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def ids(path: Path, key: str) -> list[str]:
    rows = read_csv(path)
    values = [row.get(key, "").strip() for row in rows]
    if not all(values):
        raise ValueError(f"blank {key}: {path}")
    if len(values) != len(set(values)):
        raise ValueError(f"duplicate {key}: {path}")
    return values


def git(root: Path, *args: str) -> str:
    result = subprocess.run(
        ["git", *args], cwd=root, capture_output=True, text=True, encoding="utf-8", errors="replace"
    )
    if result.returncode:
        raise RuntimeError(f"git {' '.join(args)} failed: {result.stderr.strip()}")
    return result.stdout.strip()


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def parse_time(value: Any, label: str, failures: list[str]) -> None:
    if not isinstance(value, str) or not value:
        failures.append(f"missing timestamp:{label}")
        return
    try:
        datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        failures.append(f"invalid timestamp:{label}:{value}")


def validate_evidence_record(
    root: Path,
    record: dict[str, Any],
    record_type: str,
    record_id: str,
    current_sha: str,
    failures: list[str],
) -> None:
    prefix = f"{record_type}:{record_id}"
    if record.get("sourceSha") != current_sha:
        failures.append(f"source SHA mismatch:{prefix}:{record.get('sourceSha')}")
    if record.get("exitCode") != 0:
        failures.append(f"nonzero evidence:{prefix}:{record.get('exitCode')}")
    if record.get("sanitized") is not True:
        failures.append(f"unsanitized evidence:{prefix}")
    parse_time(record.get("startedAt"), f"{prefix}:startedAt", failures)
    parse_time(record.get("finishedAt"), f"{prefix}:finishedAt", failures)

    commands = record.get("commands")
    if not isinstance(commands, list) or not commands or not all(isinstance(item, str) and item.strip() for item in commands):
        failures.append(f"commands missing:{prefix}")

    for field in ("sourcePaths", "testPaths"):
        paths = record.get(field)
        if not isinstance(paths, list) or not paths:
            failures.append(f"{field} missing:{prefix}")
            continue
        for value in paths:
            if not isinstance(value, str) or not value.strip() or not (root / value).exists():
                failures.append(f"invalid {field}:{prefix}:{value}")

    artifacts = record.get("artifacts")
    if not isinstance(artifacts, list) or not artifacts:
        failures.append(f"artifacts missing:{prefix}")
    else:
        for artifact in artifacts:
            if not isinstance(artifact, dict):
                failures.append(f"invalid artifact entry:{prefix}")
                continue
            rel = artifact.get("path")
            expected_hash = artifact.get("sha256")
            path = root / rel if isinstance(rel, str) else None
            if path is None or not path.is_file():
                failures.append(f"artifact missing:{prefix}:{rel}")
                continue
            if not isinstance(expected_hash, str) or not HASH_RE.fullmatch(expected_hash):
                failures.append(f"artifact hash invalid:{prefix}:{rel}")
                continue
            if sha256(path) != expected_hash:
                failures.append(f"artifact hash mismatch:{prefix}:{rel}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--release", action="store_true")
    parser.add_argument("--json-report")
    parser.add_argument(
        "--evidence-index",
        default="cpf-docs/evidence/current/CPF_20260730_QA32_REQUIREMENT_EVIDENCE.sanitized.json",
    )
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures: list[str] = []

    try:
        current_sha = git(root, "rev-parse", "HEAD")
        if not SHA_RE.fullmatch(current_sha):
            failures.append(f"invalid HEAD SHA:{current_sha}")
        if args.release and git(root, "status", "--porcelain=v1"):
            failures.append("release gate requires clean Working Tree")
    except Exception as exc:  # gate must fail closed
        current_sha = ""
        failures.append(str(exc))

    try:
        requirements = ids(root / "cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv", "requirement_id")
        defects = ids(root / "cpf-docs/quality/CPF_20260730_QA32_DEFECT_REGISTER.csv", "defect_id")
        scenarios = ids(root / "cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv", "scenario_id")
    except Exception as exc:
        requirements, defects, scenarios = [], [], []
        failures.append(str(exc))

    result_path = root / "cpf-docs/quality/CPF_20260730_QA32_RESULT_MATRIX.csv"
    rows = read_csv(result_path) if result_path.is_file() else []
    if not rows:
        failures.append("result matrix missing or empty")
    by_key = {(row.get("record_type", "").strip(), row.get("record_id", "").strip()): row for row in rows}

    expected = (("REQUIREMENT", requirements), ("DEFECT", defects), ("SCENARIO", scenarios))
    for record_type, values in expected:
        for record_id in values:
            row = by_key.get((record_type, record_id))
            if row is None:
                failures.append(f"missing result row:{record_type}:{record_id}")
                continue
            if row.get("development_status") not in COMPLETE:
                failures.append(
                    f"development incomplete:{record_type}:{record_id}:{row.get('development_status')}"
                )
            if args.release and row.get("verification_status") not in COMPLETE:
                failures.append(
                    f"verification incomplete:{record_type}:{record_id}:{row.get('verification_status')}"
                )

    evidence_records: dict[tuple[str, str], dict[str, Any]] = {}
    evidence_path = root / args.evidence_index
    if args.release:
        if not evidence_path.is_file():
            failures.append(f"evidence index missing:{args.evidence_index}")
        else:
            try:
                evidence = json.loads(evidence_path.read_text(encoding="utf-8"))
                if evidence.get("sourceSha") != current_sha:
                    failures.append("evidence index source SHA mismatch")
                if evidence.get("sanitized") is not True:
                    failures.append("evidence index is not sanitized")
                records = evidence.get("records")
                if not isinstance(records, list):
                    failures.append("evidence index records missing")
                else:
                    for record in records:
                        if not isinstance(record, dict):
                            failures.append("invalid evidence record")
                            continue
                        key = (str(record.get("recordType", "")), str(record.get("recordId", "")))
                        if key in evidence_records:
                            failures.append(f"duplicate evidence record:{key[0]}:{key[1]}")
                        evidence_records[key] = record
            except Exception as exc:
                failures.append(f"evidence index invalid:{exc}")

        for record_type, values in expected:
            for record_id in values:
                record = evidence_records.get((record_type, record_id))
                if record is None:
                    failures.append(f"evidence missing:{record_type}:{record_id}")
                    continue
                validate_evidence_record(root, record, record_type, record_id, current_sha, failures)

    primary_gate = subprocess.run(
        [sys.executable, str(root / "cpf-tools/scripts/verify-cpf-qa32-primary-engines.py"), "--root", str(root)],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if primary_gate.returncode:
        failures.append("primary engine gate failed")

    report = {
        "schemaVersion": 2,
        "sourceSha": current_sha,
        "requirements": len(requirements),
        "defects": len(defects),
        "scenarios": len(scenarios),
        "releaseMode": args.release,
        "evidenceIndex": args.evidence_index if args.release else None,
        "failures": failures,
        "status": "PASS" if not failures else "FAIL",
    }
    if args.json_report:
        output = root / args.json_report
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
