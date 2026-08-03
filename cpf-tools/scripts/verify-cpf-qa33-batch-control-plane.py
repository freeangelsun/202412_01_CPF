#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path


def require(condition: bool, message: str, failures: list[str]) -> None:
    if not condition:
        failures.append(message)


def read(root: Path, relative: str) -> str:
    path = root / relative
    if not path.is_file():
        raise FileNotFoundError(relative)
    return path.read_text(encoding="utf-8")


def verify_checksum(source: Path, manifest: Path, failures: list[str]) -> None:
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    entries = {}
    for line in manifest.read_text(encoding="utf-8").splitlines():
        match = re.match(r"^([0-9a-f]{64})\s+\*?(.+)$", line.strip())
        if match:
            entries[match.group(2)] = match.group(1)
    require(entries.get(source.name) == digest,
            f"checksum mismatch: {source.relative_to(manifest.parents[5])}", failures)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-report")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures: list[str] = []

    adapter = read(root, "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/JdbcBatchExecutionControlPlaneAdapter.java")
    resolver = read(root, "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/JdbcBatchApprovedLaunchRequestResolver.java")
    control = read(root, "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java")
    digest = read(root, "cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchCanonicalDigest.java")
    listener = read(root, "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchExecutionListener.java")

    for marker in (
        "claimLatestEpoch",
        "lockCurrentEpoch",
        "BATCH_STALE_FENCING_EPOCH",
        'sql.required("execution-control-reserve")',
        'sql.required("execution-control-assert-current")',
        'sql.required("execution-epoch-lock")',
        "BATCH_LINK_IMMUTABLE_FIELD_CONFLICT",
        "BATCH_CONTROL_BIND_FENCE_CONFLICT",
    ):
        require(marker in adapter, f"missing adapter marker: {marker}", failures)
    for marker in (
        'sql.required("execution-approved-launch-find-trigger")',
        'sql.required("execution-approved-launch-find-manual")',
    ):
        require(marker in resolver, f"missing approved-launch resolver marker: {marker}", failures)
    require("existing.fencingToken() == request.fencingToken()" not in adapter,
            "idempotency immutable comparison still couples operational fencing token", failures)

    reserve_sql = read(root, "cpf-tools/db/runtime-template/bat/repository/execution-control-reserve.sql.template").lower()
    assert_sql = read(root, "cpf-tools/db/runtime-template/bat/repository/execution-control-assert-current.sql.template").lower()
    lock_sql = read(root, "cpf-tools/db/runtime-template/bat/repository/execution-epoch-lock.sql.template").lower()
    approved_trigger_sql = read(root, "cpf-tools/db/runtime-template/bat/repository/execution-approved-launch-find-trigger.sql.template").lower()
    approved_manual_sql = read(root, "cpf-tools/db/runtime-template/bat/repository/execution-approved-launch-find-manual.sql.template").lower()
    for marker in ("cpf_batch_execution_control", "request_hash", "idempotency_scope", "fencing_token"):
        require(marker in reserve_sql, f"execution reserve SQL lacks {marker}", failures)
    for marker in ("cpf_batch_execution_control", "join cpf_batch_execution_epoch", "current_fencing_token", "control_status"):
        require(marker in assert_sql, f"assert-current SQL lacks {marker}", failures)
    require("cpf_batch_execution_epoch" in lock_sql and "for update" in lock_sql,
            "epoch lock SQL does not lock the current epoch through commit", failures)
    for name, statement in (("trigger", approved_trigger_sql), ("manual", approved_manual_sql)):
        require("cpf_batch_approved_launch" in statement and "approval_status = 'approved'" in statement,
                f"approved-launch {name} SQL is not fail-closed", failures)

    for marker in ("BATCH_START_RESPONSE_UNKNOWN", "reconcile(cpfExecutionId)", "JobRepository",
                   "RECONCILE_PAGE_SIZE", "while (true)", "start += instances.size()",
                   "getJobInstances", "getJobExecutions"):
        require(marker in control, f"missing unknown-result reconciliation marker: {marker}", failures)
    require("JobExplorer" not in control,
            "Spring Batch 6 reconciliation still depends on the removal-scheduled JobExplorer API", failures)
    require("operatorId\", request.operatorId()" not in digest,
            "request hash still includes operatorId", failures)
    require("fencingToken\", request.fencingToken()" not in digest,
            "request hash still includes fencingToken", failures)
    require("BATCH_JOB_START_LEDGER_OBSERVATION_FAILED" in listener,
            "beforeJob observation failure is not isolated", failures)
    require("BATCH_STEP_START_LEDGER_OBSERVATION_FAILED" in listener,
            "beforeStep observation failure is not isolated", failures)

    schema = json.loads(read(root, "cpf-tools/db/canonical/platform-schema.json"))
    tables = {table.get("name", "").lower(): table for table in schema.get("tables", [])}
    required_table_columns = {
        "cpf_batch_approved_launch": {
            "approval_id", "job_id", "definition_version", "definition_checksum",
            "approval_status", "launch_request_json", "row_version",
        },
        "cpf_batch_execution_control": {
            "cpf_execution_id", "job_id", "idempotency_scope", "idempotency_key",
            "request_hash", "fencing_token", "control_status", "control_version",
        },
        "cpf_batch_execution_link": {
            "cpf_execution_id", "link_key", "job_id", "definition_version",
            "spring_job_execution_id", "fencing_token",
        },
        "cpf_batch_execution_epoch": {
            "job_id", "current_fencing_token", "epoch_version", "updated_at",
        },
    }
    for table_name, required_columns in required_table_columns.items():
        require(table_name in tables, f"canonical schema lacks {table_name}", failures)
        if table_name in tables:
            columns = {column["name"] for column in tables[table_name].get("columns", [])}
            require(required_columns <= columns,
                    f"canonical {table_name} columns are incomplete: {sorted(required_columns - columns)}", failures)

    migrations = [
        ("mariadb", "cpf-tools/db/vendor/mariadb/migration/flyway/V89__batch_execution_idempotency_lifecycle.sql",
         "cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"),
        ("postgresql", "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V89__batch_execution_idempotency_lifecycle.sql",
         "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/checksums.sha256"),
        ("oracle", "cpf-tools/db/vendor/oracle/migration/flyway/batDB/V89__batch_execution_idempotency_lifecycle.sql",
         "cpf-tools/db/vendor/oracle/migration/flyway/batDB/checksums.sha256"),
    ]
    for vendor, migration_relative, manifest_relative in migrations:
        migration = read(root, migration_relative)
        require("requires empty CPF_BATCH_EXECUTION_CONTROL" not in migration,
                f"{vendor} V89 still rejects non-empty production data", failures)
        require("CPF_BATCH_EXECUTION_EPOCH" in migration,
                f"{vendor} V89 lacks latest fencing epoch ledger", failures)
        require("LEGACY_EXECUTION_REQUIRES_RECONCILIATION" in migration,
                f"{vendor} V89 lacks fail-closed legacy backfill", failures)
        verify_checksum(root / migration_relative, root / manifest_relative, failures)

    rollbacks = [
        "cpf-tools/db/vendor/mariadb/rollback/R89__batch_execution_idempotency_lifecycle.sql",
        "cpf-tools/db/vendor/postgresql/rollback/batDB/R89__batch_execution_idempotency_lifecycle.sql",
        "cpf-tools/db/vendor/oracle/rollback/batDB/R89__batch_execution_idempotency_lifecycle.sql",
    ]
    for rollback_relative in rollbacks:
        rollback = read(root, rollback_relative)
        require("CPF_BATCH_EXECUTION_EPOCH_R89_BAK" in rollback,
                f"rollback lacks epoch checkpoint: {rollback_relative}", failures)
        require("cannot restore global idempotency uniqueness" in rollback,
                f"rollback lacks duplicate guard: {rollback_relative}", failures)

    schema_standard_migrations = [
        ("mariadb", "cpf-tools/db/vendor/mariadb/migration/flyway/V95__batch_control_schema_standard.sql",
         "cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"),
        ("postgresql", "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V95__batch_control_schema_standard.sql",
         "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/checksums.sha256"),
        ("oracle", "cpf-tools/db/vendor/oracle/migration/flyway/batDB/V95__batch_control_schema_standard.sql",
         "cpf-tools/db/vendor/oracle/migration/flyway/batDB/checksums.sha256"),
    ]
    for vendor, migration_relative, manifest_relative in schema_standard_migrations:
        migration = read(root, migration_relative).lower()
        for marker in ("cpf_batch_execution_control", "cpf_batch_execution_link", "cpf_batch_approved_launch"):
            require(marker in migration, f"{vendor} V95 lacks canonical table marker: {marker}", failures)
        require("on delete cascade" in migration,
                f"{vendor} V95 lacks execution-link lifecycle cascade", failures)
        verify_checksum(root / migration_relative, root / manifest_relative, failures)

    result = {"gate": "qa33-batch-control-plane", "status": "PASS" if not failures else "FAIL", "failures": failures}
    if args.json_report:
        report = root / args.json_report
        report.parent.mkdir(parents=True, exist_ok=True)
        report.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if failures:
        for failure in failures:
            print(f"[CPF][QA33][FAIL] {failure}", file=sys.stderr)
        return 1
    print("[CPF][QA33][PASS] Batch Control Plane idempotency/fencing/unknown-result gate")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
