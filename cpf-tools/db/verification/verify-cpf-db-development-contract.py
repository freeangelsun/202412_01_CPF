#!/usr/bin/env python3
"""Static verification for CPF database development contracts.

This verifier is intentionally dependency-free so it can run when PowerShell and
all three database clients are unavailable. It does not claim runtime execution;
it verifies syntax structure, security controls, contract parity, and ownership.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path

VENDORS = ["mariadb", "postgresql", "oracle"]
DB_SCRIPTS = [
    "cpf-tools/db/tools/backup-cpf-database.ps1",
    "cpf-tools/db/tools/restore-cpf-database.ps1",
    "cpf-tools/db/tools/set-cpf-backup-legal-hold.ps1",
    "cpf-tools/db/tools/invoke-cpf-backup-retention.ps1",
    "cpf-tools/db/tools/replicate-cpf-backup-artifact.ps1",
    "cpf-tools/db/verification/verify-dr-restore.ps1",
    "cpf-tools/db/tools/invoke-cpf-pitr-restore.ps1",
    "cpf-tools/testing/tools/prepare-cpf-test-data.ps1",
    "cpf-tools/db/tools/invoke-official-db-vendor-sql.ps1",
    "cpf-tools/db/tools/invoke-platform-database-migration.ps1",
    "cpf-tools/db/tools/run-db-vendor-lifecycle.ps1",
    "cpf-tools/db/tools/invoke-db-lifecycle-docker-client.ps1",
    "cpf-tools/db/tools/database-profile-common.ps1",
    "cpf-tools/db/verification/check-database-profile-standard.ps1",
    "cpf-tools/db/tools/cpf-backup-lifecycle-common.ps1",
    "cpf-tools/verification/tools/invoke-cpf-data-retention.ps1",
    "cpf-tools/db/tools/invoke-cpf-db-performance-gate.ps1",
    "cpf-tools/runtime/tools/invoke-cpf-datasource-runtime-gate.ps1",
    "cpf-tools/verification/tools/invoke-cpf-data-observability-gate.ps1",
    "cpf-tools/db/tools/invoke-cpf-db-operability-gate.ps1",
]


@dataclass(frozen=True)
class StaticFailure:
    path: str
    message: str


def load_json(path: Path) -> dict:
    with path.open(encoding="utf-8-sig") as handle:
        value = json.load(handle)
    if not isinstance(value, dict):
        raise ValueError(f"JSON root must be object: {path}")
    return value


def strip_powershell_non_code(text: str) -> str:
    """Replace strings/comments/here-strings with spaces while preserving lines."""
    out = list(text)
    i = 0
    n = len(text)
    state = "code"
    while i < n:
        ch = text[i]
        nxt = text[i + 1] if i + 1 < n else ""
        if state == "code":
            if ch == "#":
                state = "line_comment"
                out[i] = " "
            elif ch == "<" and nxt == "#":
                state = "block_comment"
                out[i] = out[i + 1] = " "
                i += 1
            elif ch == "'":
                state = "single"
                out[i] = " "
            elif ch == '"':
                state = "double"
                out[i] = " "
            elif ch == "@" and nxt in {"'", '"'} and (i == 0 or text[i - 1] in "\r\n"):
                quote = nxt
                state = "here_single" if quote == "'" else "here_double"
                out[i] = out[i + 1] = " "
                i += 1
        elif state == "line_comment":
            if ch in "\r\n":
                state = "code"
            else:
                out[i] = " "
        elif state == "block_comment":
            out[i] = " "
            if ch == "#" and nxt == ">":
                out[i + 1] = " "
                i += 1
                state = "code"
        elif state == "single":
            out[i] = " "
            if ch == "'":
                if nxt == "'":
                    out[i + 1] = " "
                    i += 1
                else:
                    state = "code"
        elif state == "double":
            out[i] = " "
            if ch == "`" and nxt:
                out[i + 1] = " "
                i += 1
            elif ch == '"':
                state = "code"
        elif state in {"here_single", "here_double"}:
            out[i] = " "
            marker = "'@" if state == "here_single" else '"@'
            if ch in "\r\n":
                line_start = i + 1
                while line_start < n and text[line_start] in "\r\n":
                    out[line_start] = text[line_start]
                    line_start += 1
                if text.startswith(marker, line_start):
                    out[line_start : line_start + 2] = [" ", " "]
                    i = line_start + 1
                    state = "code"
        i += 1
    if state in {"single", "double", "block_comment", "here_single", "here_double"}:
        raise ValueError(f"unterminated PowerShell token: {state}")
    return "".join(out)


def check_balanced_powershell(path: Path) -> list[StaticFailure]:
    text = path.read_text(encoding="utf-8-sig")
    failures: list[StaticFailure] = []
    try:
        code = strip_powershell_non_code(text)
    except ValueError as exc:
        return [StaticFailure(str(path), str(exc))]
    pairs = {"(": ")", "[": "]", "{": "}"}
    closers = {value: key for key, value in pairs.items()}
    stack: list[tuple[str, int]] = []
    for pos, ch in enumerate(code):
        if ch in pairs:
            stack.append((ch, pos))
        elif ch in closers:
            if not stack or stack[-1][0] != closers[ch]:
                failures.append(StaticFailure(str(path), f"unmatched closer {ch} at offset {pos}"))
                break
            stack.pop()
    if stack:
        ch, pos = stack[-1]
        failures.append(StaticFailure(str(path), f"unclosed delimiter {ch} at offset {pos}"))
    return failures


def verify(root: Path) -> tuple[list[StaticFailure], dict]:
    failures: list[StaticFailure] = []
    db = root / "cpf-tools" / "db"
    scripts = {Path(relative).name: root / relative for relative in DB_SCRIPTS}
    backup = load_json(db / "cpf-backup-lifecycle-contract.json")
    pitr = load_json(db / "cpf-pitr-lifecycle-contract.json")
    lifecycle = load_json(db / "cpf-db-lifecycle-contract.json")
    test_data = load_json(db / "cpf-test-data-policy.json")
    retention = load_json(db / "cpf-data-retention-policy.json")
    performance = load_json(db / "cpf-db-performance-policy.json")
    datasource_runtime = load_json(db / "cpf-datasource-runtime-policy.json")
    data_observability = load_json(db / "cpf-data-observability-policy.json")
    operability = load_json(db / "cpf-db-operability-contract.json")

    for name, contract in [("backup", backup), ("pitr", pitr), ("lifecycle", lifecycle), ("test-data", test_data), ("data-retention", retention), ("performance", performance), ("datasource-runtime", datasource_runtime), ("data-observability", data_observability), ("operability", operability)]:
        if contract.get("officialVendors") != VENDORS:
            failures.append(StaticFailure(name, f"officialVendors must equal {VENDORS}"))

    stages = lifecycle.get("orderedStages", [])
    expected_tail = ["backup-restore", "point-in-time-recovery"]
    if stages[-2:] != expected_tail:
        failures.append(StaticFailure("cpf-db-lifecycle-contract.json", f"stage tail must be {expected_tail}"))

    for relative in DB_SCRIPTS:
        path = root / relative
        if not path.is_file():
            failures.append(StaticFailure(str(path), "missing DB script"))
            continue
        failures.extend(check_balanced_powershell(path))

    replica_text = scripts["replicate-cpf-backup-artifact.ps1"].read_text(encoding="utf-8-sig")
    for token in ["ConfirmReplication", "artifactSha256", "REPLICATE", "TargetRegion", "ApprovalReference", "Assert-CpfManifestHash", ".cpf-replica-stage-"]:
        if token not in replica_text:
            failures.append(StaticFailure("replicate-cpf-backup-artifact.ps1", f"missing cross-region control: {token}"))

    backup_retention_text = scripts["invoke-cpf-backup-retention.ps1"].read_text(encoding="utf-8-sig")
    for token in ["ExpectedPlanSha256", "REPLICA_REQUIRED", "minimumVerifiedReplicaCountBeforePrimaryPurge", "$deletionCommitted", "reconcileRequired", "PlanSha256 $planSha"]:
        if token not in backup_retention_text:
            failures.append(StaticFailure("invoke-cpf-backup-retention.ps1", f"missing backup retention safety control: {token}"))

    pitr_text = scripts["invoke-cpf-pitr-restore.ps1"].read_text(encoding="utf-8-sig")
    for token in ["pg_is_in_recovery", "CATALOG START WITH", "ExpectedPlanSha256", "$result.status='UNKNOWN'"]:
        if token not in pitr_text:
            failures.append(StaticFailure("invoke-cpf-pitr-restore.ps1", f"missing control: {token}"))

    sqlplus_text = scripts["invoke-official-db-vendor-sql.ps1"].read_text(encoding="utf-8-sig")
    for token in ["SET ECHO OFF", "SET VERIFY OFF", "SET DEFINE OFF", "SensitiveValues", "Assert-CpfSqlPlusScalar"]:
        if token not in sqlplus_text:
            failures.append(StaticFailure("invoke-official-db-vendor-sql.ps1", f"missing secret control: {token}"))

    profile_common_text = scripts["database-profile-common.ps1"].read_text(encoding="utf-8-sig")
    if 'Join-Path $PSScriptRoot "../vendor-pack-manifest.json"' not in profile_common_text:
        failures.append(
            StaticFailure(
                "database-profile-common.ps1",
                "Vendor manifest must resolve from cpf-tools/db/tools to cpf-tools/db/vendor-pack-manifest.json",
            )
        )
    if '../db/vendor-pack-manifest.json' in profile_common_text:
        failures.append(StaticFailure("database-profile-common.ps1", "stale duplicated db/db manifest path remains"))

    profile_gate_text = scripts["check-database-profile-standard.ps1"].read_text(encoding="utf-8-sig")
    expected_profile_owner = 'Join-Path $Root "cpf-tools/db/tools/database-profile-common.ps1"'
    if expected_profile_owner not in profile_gate_text:
        failures.append(
            StaticFailure(
                "check-database-profile-standard.ps1",
                "Profile gate must dot-source the central DB tool owner from repository Root",
            )
        )

    retention_text = scripts["invoke-cpf-data-retention.ps1"].read_text(encoding="utf-8-sig")
    for token in ["ConfirmArchiveBeforePurge", "ExpectedPlanSha256", "$result.status='UNKNOWN'", "generate-cpf-data-retention-sql.py"]:
        if token not in retention_text:
            failures.append(StaticFailure("invoke-cpf-data-retention.ps1", f"missing data retention control: {token}"))

    datasource_text = scripts["invoke-cpf-datasource-runtime-gate.ps1"].read_text(encoding="utf-8-sig")
    for token in ["ExpectedEvidenceSha256", "$Operator -eq $ApprovedBy", "ConfirmSanitizedEvidence", "$LASTEXITCODE -ne 0"]:
        if token not in datasource_text:
            failures.append(StaticFailure("invoke-cpf-datasource-runtime-gate.ps1", f"missing datasource runtime control: {token}"))

    observability_text = scripts["invoke-cpf-data-observability-gate.ps1"].read_text(encoding="utf-8-sig")
    for token in ["ExpectedEvidenceSha256", "$Operator -eq $ApprovedBy", "ConfirmSanitizedEvidence", "$LASTEXITCODE -ne 0"]:
        if token not in observability_text:
            failures.append(StaticFailure("invoke-cpf-data-observability-gate.ps1", f"missing data observability control: {token}"))

    operability_text = scripts["invoke-cpf-db-operability-gate.ps1"].read_text(encoding="utf-8-sig")
    for token in ["ExpectedEvidenceSha256", "Get-FileHash", "ConfirmSanitizedEvidence", "independent operator and approver", "Evidence approvalReference mismatch", "$LASTEXITCODE -ne 0"]:
        if token not in operability_text:
            failures.append(StaticFailure("invoke-cpf-db-operability-gate.ps1", f"missing DB operability control: {token}"))

    capability_ids = [item.get("id") for item in operability.get("capabilities", [])]
    expected_capabilities = ["DB-OWNERSHIP", "DB-INSTALL", "DB-FRESH", "DB-MIGRATION", "DB-ROLLBACK", "DB-BACKUP", "DB-MULTI-VENDOR", "DB-SQL", "DB-PERF", "DB-MULTI", "DATA-LINEAGE", "DATA-RETENTION"]
    if capability_ids != expected_capabilities:
        failures.append(StaticFailure("cpf-db-operability-contract.json", "canonical capability order or membership mismatch"))
    generated_openapi = db / "generated" / "cpf-db-operations.openapi.json"
    if not generated_openapi.is_file():
        failures.append(StaticFailure(str(generated_openapi), "missing generated DB operations OpenAPI"))
    else:
        spec = load_json(generated_openapi)
        if spec.get("x-cpf-route-consumer-status") != "CROSS_SESSION_REQUIRED":
            failures.append(StaticFailure(str(generated_openapi), "route consumer boundary must remain explicit"))

    migration_text = scripts["invoke-platform-database-migration.ps1"].read_text(encoding="utf-8-sig")
    for token in ["reconcileRequired", "UNKNOWN", "artifactSha256", "Assert-CpfBackupManifest"]:
        if token not in migration_text:
            failures.append(StaticFailure("invoke-platform-database-migration.ps1", f"missing migration safety token: {token}"))

    forbidden_argument_patterns = [
        re.compile(r"ArgumentList[^\n]*(?:--password|-p\$|/as sysdba)", re.I),
        re.compile(r"ProcessStartInfo[^\n]*(?:password|secret)", re.I),
    ]
    secret_exceptions = {"backup-cpf-database.ps1": ["NO_PASSWORD_ARGUMENT"]}
    for relative in DB_SCRIPTS:
        text = (root / relative).read_text(encoding="utf-8-sig")
        for pattern in forbidden_argument_patterns:
            match = pattern.search(text)
            if match and not any(marker in text for marker in secret_exceptions.get(Path(relative).name, [])):
                failures.append(StaticFailure(relative, f"possible secret-bearing process argument: {match.group(0)[:120]}"))

    summary = {
        "schemaVersion": 1,
        "officialVendors": VENDORS,
        "checkedPowerShellFiles": len(DB_SCRIPTS),
        "lifecycleStages": stages,
        "failureCount": len(failures),
        "status": "PASS" if not failures else "FAIL",
        "runtimeClaim": "STATIC_SUBSTITUTE_ONLY",
    }
    return failures, summary


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures, summary = verify(root)
    if args.json_output:
        out = Path(args.json_output)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps({**summary, "failures": [f.__dict__ for f in failures]}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False, sort_keys=True))
    for failure in failures:
        print(f"FAIL {failure.path}: {failure.message}", file=sys.stderr)
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
