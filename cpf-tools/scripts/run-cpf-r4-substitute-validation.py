#!/usr/bin/env python3
"""Run every validation executable in the current Java21/Node/Python environment.

This wrapper deliberately excludes Java25 Gradle, real DB servers, and browser
E2E. Those are executed by the separate exact-head handoff script.  The source
snapshot SHA is nevertheless mandatory and must match the supplied baseline so
that substitute Evidence cannot be generated against an implicit stale commit.
"""
from __future__ import annotations

import argparse
import csv
import json
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

REVIEW_RELATIVE = Path("cpf-docs/work/review/development/DEV_EXEC_20001_END_QA25_R4")
EVIDENCE_RELATIVE = Path("cpf-docs/evidence/development/DEV_EXEC_20001_END_QA25_R4")
SHA_RE = re.compile(r"^[0-9a-f]{40}$")


def run(command: list[str], cwd: Path, log: Path, environment: dict[str, str] | None = None) -> tuple[int, str]:
    log.parent.mkdir(parents=True, exist_ok=True)
    process = subprocess.run(command, cwd=cwd, text=True, capture_output=True, env=environment)
    text = process.stdout + process.stderr
    log.write_text(text, encoding="utf-8")
    return process.returncode, text


def git_head(source_root: Path) -> str:
    process = subprocess.run(
        ["git", "-C", str(source_root), "rev-parse", "HEAD"],
        text=True,
        capture_output=True,
    )
    if process.returncode != 0:
        raise ValueError("source HEAD를 확인할 수 없습니다: " + process.stderr.strip())
    return process.stdout.strip()


def resolve_source_head(source_root: Path, expected: str, supplied: str | None) -> str:
    if not SHA_RE.fullmatch(expected):
        raise ValueError("baseline SHA는 40자리 소문자 hex여야 합니다.")
    actual = supplied or git_head(source_root)
    if not SHA_RE.fullmatch(actual):
        raise ValueError("source HEAD는 40자리 소문자 hex여야 합니다.")
    if actual != expected:
        raise ValueError(f"source HEAD mismatch expected={expected} actual={actual}")
    return actual


def write_summary(path: Path, rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=("test_name", "category", "exit_code", "result", "command", "log"))
        writer.writeheader()
        writer.writerows(rows)


def read_summary(path: Path) -> list[dict[str, str]]:
    if not path.is_file():
        return []
    with path.open(encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-root", required=True)
    parser.add_argument("--artifact-root", required=True)
    parser.add_argument("--datasets-root", required=True)
    parser.add_argument("--baseline-sha", required=True)
    parser.add_argument(
        "--source-head",
        help="exact fetched snapshot처럼 .git이 없는 경우 검증할 실제 source SHA",
    )
    parser.add_argument("--work-root")
    parser.add_argument("--phase", choices=("all", "runtime", "traceability"), default="all")
    args = parser.parse_args()
    source_root = Path(args.source_root).resolve()
    artifact_root = Path(args.artifact_root).resolve()
    datasets_root = Path(args.datasets_root).resolve()
    try:
        source_head = resolve_source_head(source_root, args.baseline_sha, args.source_head)
    except ValueError as failure:
        parser.error(str(failure))
    review = artifact_root / REVIEW_RELATIVE
    evidence = artifact_root / EVIDENCE_RELATIVE
    review.mkdir(parents=True, exist_ok=True)
    evidence.mkdir(parents=True, exist_ok=True)
    summary_path = evidence / "R4_RUNTIME_EXIT_SUMMARY_FINAL.csv"
    rows: list[dict[str, str]] = read_summary(summary_path) if args.phase == "traceability" else []
    failed = any(row.get("exit_code") != "0" for row in rows)
    work_root: Path | None = None

    def task(name: str, category: str, command: list[str], log_name: str, cwd: Path = source_root, env: dict[str, str] | None = None) -> None:
        nonlocal failed
        log = evidence / log_name
        exit_code, output = run(command, cwd, log, env)
        result_line = next((line.strip() for line in output.splitlines() if line.strip()), "")
        rows[:] = [row for row in rows if row.get("test_name") != name]
        rows.append({
            "test_name": name,
            "category": category,
            "exit_code": str(exit_code),
            "result": result_line[:1000],
            "command": subprocess.list2cmdline(command),
            "log": log.relative_to(artifact_root).as_posix(),
        })
        write_summary(summary_path, rows)
        print(f"[{name}] exit={exit_code} {result_line[:300]}", flush=True)
        if exit_code != 0:
            failed = True

    python = sys.executable
    if args.phase in ("all", "runtime"):
        task(
            "PYTHON_GATE_TESTS",
            "gate-unit",
            [python, "-m", "pytest", "-q", str(source_root / "cpf-tools/scripts/tests")],
            "R4_PYTHON_GATE_TESTS_FINAL.log",
        )
        task(
            "PYTHON_COMPILEALL",
            "compile",
            [python, "-m", "compileall", "-q", str(source_root / "cpf-tools/scripts"), str(source_root / "cpf-tools/verification")],
            "R4_PYTHON_COMPILEALL_FINAL.log",
        )
        task(
            "FRONTEND_FULL_COMPILE",
            "frontend-compile",
            [python, str(source_root / "cpf-tools/verification/frontend-full-compile/run-frontend-full-compile.py"), "--root", str(source_root)],
            "R4_FRONTEND_TSC_BZA_OWNER_FINAL.log",
        )
        task(
            "FRONTEND_API_RUNTIME",
            "frontend-runtime",
            [python, str(source_root / "cpf-tools/verification/frontend-api-runtime/run-frontend-api-runtime-harness.py")],
            "R4_FRONTEND_API_BZA_OWNER_FINAL.log",
        )
        task(
            "FRONTEND_WORKFLOW_RUNTIME",
            "frontend-runtime",
            [python, str(source_root / "cpf-tools/verification/frontend-workflow-runtime/run-frontend-workflow-runtime-harness.py"), "--root", str(source_root)],
            "R4_FRONTEND_WORKFLOW_RUNTIME_FINAL.log",
        )
        task("JAVA21_CONTROLLER", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/controller-runtime/run-controller-runtime-harness.py")], "R4_JAVA21_CONTROLLER_FINAL.log")
        task("JAVA21_DB_LESS", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/db-less-runtime/run-db-less-runtime-harness.py")], "R4_JAVA21_DB_LESS_FINAL.log")
        task("JAVA21_NETWORK", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/network-runtime/run-network-runtime-harness.py")], "R4_JAVA21_NETWORK_FINAL.log")
        task("JAVA21_PERSISTENCE", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/persistence-runtime/run-persistence-runtime-harness.py")], "R4_JAVA21_PERSISTENCE_FINAL.log")
        task("JAVA21_TRANSACTION", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/transaction-runtime/run-transaction-runtime-harness.py")], "R4_JAVA21_TRANSACTION_FINAL.log")
        task("JAVA21_RUNTIME_COMMAND", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/runtime-command-runtime/run-runtime-command-harness.py"), "--root", str(source_root)], "R4_JAVA21_RUNTIME_COMMAND_FINAL.log")
        task("JAVA21_BATCH_ABANDON", "java21-runtime", [python, str(source_root / "cpf-tools/verification/java21/batch-abandon-runtime/run-batch-abandon-harness.py"), "--root", str(source_root)], "R4_JAVA21_BATCH_ABANDON_FINAL.log")

        work_root = Path(args.work_root).resolve() if args.work_root else Path(tempfile.mkdtemp(prefix="cpf-r4-audit-wrapper-"))
        audit_work = work_root / "audit-runtime"
        task(
            "JAVA21_AUDIT_MULTI_PROCESS",
            "java21-multi-process",
            [python, str(source_root / "cpf-tools/verification/java21/audit-runtime/run-audit-runtime-harness.py"), "--work-dir", str(audit_work), "--source-head", source_head],
            "R4_JAVA21_AUDIT_FINAL.log",
        )
        task(
            "DB_VENDOR_SEMANTIC_PARITY",
            "db-static-runtime",
            [python, str(source_root / "cpf-tools/scripts/verify-cpf-db-vendor-semantic-parity.py"), "--root", str(source_root), "--json-output", str(evidence / "gates/db_vendor_semantic_final.json")],
            "gates/db_vendor_semantic_final.log",
        )
        task(
            "DB_LESS_FAIL_CLOSED",
            "spring-static-runtime",
            [python, str(source_root / "cpf-tools/scripts/verify-cpf-db-less-fail-closed.py"), "--root", str(source_root), "--json-output", str(evidence / "gates/db_less_after_fix.json")],
            "gates/db_less_after_fix.log",
        )
        task(
            "FRONTEND_CONSUMER_CLOSURE",
            "frontend-gate",
            [python, str(source_root / "cpf-tools/scripts/verify-cpf-frontend-consumer-closure.py"), "--root", str(source_root), "--json-output", str(evidence / "gates/frontend_closure_final.json")],
            "gates/frontend_closure_final.log",
        )
        task(
            "NETWORK_POLICY_CONSUMERS",
            "network-gate",
            [python, str(source_root / "cpf-tools/scripts/verify-cpf-network-policy-consumers.py"), "--root", str(source_root), "--json-output", str(evidence / "gates/network_consumers_final.json")],
            "gates/network_consumers_final.log",
        )
        task(
            "OPERATOR_TRUST_BOUNDARY",
            "security-gate",
            [python, str(source_root / "cpf-tools/scripts/verify-cpf-operator-trust-boundary.py"), "--root", str(source_root), "--json-output", str(evidence / "gates/operator_trust_final.json")],
            "gates/operator_trust_rerun.log",
        )
        task(
            "TRANSACTION_ID_STANDARD",
            "transaction-gate",
            [python, str(source_root / "cpf-tools/scripts/verify-cpf-transaction-id-standard.py"), "--root", str(source_root), "--json-output", str(evidence / "gates/transaction_id_final.json")],
            "gates/transaction_id.log",
        )

    if args.phase in ("all", "traceability"):
        execution_glob = str(datasets_root / "CPF_EXECUTION_SEQUENCE_PART_*.csv")
        requirement_glob = str(datasets_root / "CPF_REQUIREMENT_MASTER_PART_*.csv")
        scenario_glob = str(datasets_root / "CPF_SCENARIO_MASTER_PART_*.csv")
        task(
            "WORK_PACKAGE_SOURCE_REVIEW",
            "traceability",
            [
                python, str(source_root / "cpf-tools/scripts/build-cpf-work-package-source-review.py"),
                "--execution-glob", execution_glob,
                "--requirement-glob", requirement_glob,
                "--scenario-glob", scenario_glob,
                "--source-root", str(source_root),
                "--output", str(review / "WORK_PACKAGE_SOURCE_REVIEW.csv"),
                "--summary-output", str(review / "WORK_PACKAGE_SOURCE_REVIEW_SUMMARY.json"),
                "--start-row", "20001", "--expected-requirements", "10558", "--expected-work-packages", "291",
                "--baseline-sha", source_head,
            ],
            "R4_WORK_PACKAGE_SOURCE_REVIEW_FINAL.log",
        )
        task(
            "REQUIREMENT_TRACEABILITY_BUILD",
            "traceability",
            [
                python, str(source_root / "cpf-tools/scripts/build-cpf-requirement-development-traceability.py"),
                "--execution-glob", execution_glob,
                "--requirement-glob", requirement_glob,
                "--scenario-glob", scenario_glob,
                "--work-package-review", str(review / "WORK_PACKAGE_SOURCE_REVIEW.csv"),
                "--output-dir", str(review),
                "--start-row", "20001", "--expected-count", "10558", "--expected-work-packages", "291",
                "--baseline-sha", source_head,
            ],
            "R4_REQUIREMENT_TRACEABILITY_FINAL.log",
        )
        task(
            "REQUIREMENT_TRACEABILITY_CLOSURE",
            "traceability-gate",
            [
                python, str(source_root / "cpf-tools/scripts/verify-cpf-development-traceability-closure.py"),
                "--root", str(artifact_root),
                "--requirement-status", str(review / "REQUIREMENT_STATUS.csv"),
                "--work-package-status", str(review / "WORK_PACKAGE_STATUS.csv"),
                "--source-review", str(review / "WORK_PACKAGE_SOURCE_REVIEW.csv"),
                "--expected-requirements", "10558", "--expected-work-packages", "291",
                "--expected-sha", source_head,
                "--json-output", str(evidence / "R4_REQUIREMENT_TRACEABILITY_CLOSURE_FINAL.json"),
            ],
            "R4_REQUIREMENT_TRACEABILITY_CLOSURE_FINAL.log",
            cwd=artifact_root,
        )

    write_summary(summary_path, rows)
    result = {
        "status": "PASS" if not failed else "FAIL",
        "meaning": "Current-environment substitute validation only; exact Git/Java25/real DB/browser validation is separate",
        "baselineSha": args.baseline_sha,
        "sourceHead": source_head,
        "phase": args.phase,
        "completeTaskSet": len(rows) == 22,
        "testCount": len(rows),
        "passed": sum(row["exit_code"] == "0" for row in rows),
        "failed": sum(row["exit_code"] != "0" for row in rows),
        "summary": summary_path.relative_to(artifact_root).as_posix(),
    }
    (evidence / "R4_SUBSTITUTE_VALIDATION_FINAL.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    if work_root is not None and args.work_root is None:
        shutil.rmtree(work_root, ignore_errors=True)
    return 0 if not failed else 1


if __name__ == "__main__":
    raise SystemExit(main())
