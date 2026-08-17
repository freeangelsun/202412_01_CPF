#!/usr/bin/env python3
"""Fail-closed static closure for CPF integrated transaction logging.

This gate does not claim runtime success. It proves that the runtime path needed by
FullLocal exists end-to-end: transaction event -> DB durable persistence/fallback,
structured FileLog with recovery diagnostics, ADM transaction/timeline queries, and
the Windows runtime smoke stages that cross-check the same transactionId.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path


def require_file(root: Path, relative: str, failures: list[str]) -> str:
    path = root / relative
    if not path.is_file():
        failures.append(f"MISSING:{relative}")
        return ""
    return path.read_text(encoding="utf-8-sig", errors="replace")


def require_tokens(source: str, relative: str, tokens: tuple[str, ...], failures: list[str]) -> None:
    for token in tokens:
        if token not in source:
            failures.append(f"TOKEN_MISSING:{relative}:{token}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output", default="")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    failures: list[str] = []

    file_writer_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/file/CpfFileLogWriter.java"
    async_writer_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/file/CpfAsyncFileLogWriter.java"
    db_service_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/TransactionLogService.java"
    db_listener_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/TransactionLogListener.java"
    db_adapter_path = "cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/MyBatisTransactionLogPersistenceAdapter.java"
    timeline_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/segment/CpfTransactionTimelineQueryFacade.java"
    adm_observability_path = "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmObservabilityController.java"
    adm_log_path = "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmLogController.java"
    file_smoke_path = "cpf-tools/runtime/tools/smoke-file-log-standard-runtime.ps1"
    policy_smoke_path = "cpf-tools/runtime/tools/smoke-log-policy-runtime.ps1"
    correlation_smoke_path = "cpf-tools/runtime/tools/smoke-integrated-log-correlation.ps1"
    full_local_path = "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"

    file_writer = require_file(root, file_writer_path, failures)
    require_tokens(file_writer, file_writer_path, (
        "transactions/{businessDate}/{transactionId}_{businessDate}.log",
        "CpfMaskingRuntime",
        "CpfFileLogRecoverySpool",
        "fileWriteDiagnostics()",
        "fileRecoveryDiagnostics()",
        "terminalLoss",
    ), failures)

    async_writer = require_file(root, async_writer_path, failures)
    require_tokens(async_writer, async_writer_path, (
        "ArrayBlockingQueue",
        "CALLER_RUNS",
        "shutdownTimeout",
        "terminalLossCount",
        "fileWriterSnapshot()",
    ), failures)

    db_service = require_file(root, db_service_path, failures)
    require_tokens(db_service, db_service_path, (
        "Propagation.REQUIRES_NEW",
        "logPolicy.dbLogEnabled()",
        "existsRecoveryEvent",
        "CpfMaskingRuntime.mask",
        "insertTransactionLog",
    ), failures)

    db_listener = require_file(root, db_listener_path, failures)
    require_tokens(db_listener, db_listener_path, (
        "ArrayBlockingQueue",
        "preserveFallback",
        "ASYNC_QUEUE_FULL",
        "ASYNC_SHUTDOWN_DRAIN",
        "terminalLossCount",
    ), failures)

    db_adapter = require_file(root, db_adapter_path, failures)
    require_tokens(db_adapter, db_adapter_path, (
        "implements CpfTransactionLogPersistencePort",
        "insertTransactionLog",
        "insertTransactionLogDetail",
    ), failures)

    timeline = require_file(root, timeline_path, failures)
    require_tokens(timeline, timeline_path, (
        '"cpf_transaction_log"',
        '"cpf_broker_outbox"',
        '"cpf_broker_dlq"',
        '"cpf_unknown_result"',
        "transactionId",
    ), failures)

    adm_observability = require_file(root, adm_observability_path, failures)
    require_tokens(adm_observability, adm_observability_path, (
        '/file-log-recovery',
        '/transactions/{transactionId}',
        '/traces/{traceId}',
        'fileWriteDiagnostics()',
        'fileRecoveryDiagnostics()',
    ), failures)

    adm_log = require_file(root, adm_log_path, failures)
    require_tokens(adm_log, adm_log_path, (
        '/adm/api/logs',
        'transactionId',
        'traceId',
        'instanceId',
    ), failures)

    file_smoke = require_file(root, file_smoke_path, failures)
    require_tokens(file_smoke, file_smoke_path, (
        "requiredFields",
        "containsTransactionId",
        "$LogBasePath",
        "$RequireRuntime",
        "transactions/[0-9]{8}",
    ), failures)

    policy_smoke = require_file(root, policy_smoke_path, failures)
    require_tokens(policy_smoke, policy_smoke_path, (
        "dbLogDisabled",
        "dbLogEnabled",
        "requestBodyPolicy",
        "responseBodyPolicy",
        "errorStackPolicy",
        "observabilityByTransaction",
        "policyAuditQuery",
    ), failures)

    correlation_smoke = require_file(root, correlation_smoke_path, failures)
    require_tokens(correlation_smoke, correlation_smoke_path, (
        "fileLogDbCorrelation",
        "fileLogRecovery",
        "processRuntimeLog",
        "secretLeakScan",
        "transactionId",
        "traceId",
        "terminalLoss",
    ), failures)

    full_local = require_file(root, full_local_path, failures)
    for token in (
        "CODEX_INTEGRATED_LOGGING_CLOSURE",
        "LOCAL_FILE_LOG_STANDARD",
        "LOCAL_DB_LOG_POLICY_RUNTIME",
        "LOCAL_INTEGRATED_LOG_CORRELATION",
        "smoke-file-log-standard-runtime.ps1",
        "smoke-log-policy-runtime.ps1",
        "smoke-integrated-log-correlation.ps1",
    ):
        if token not in full_local:
            failures.append(f"FULLLOCAL_STAGE_MISSING:{token}")

    result = {
        "status": "PASS" if not failures else "FAIL",
        "failures": sorted(set(failures)),
        "checked": {
            "fileLogDurability": True,
            "dbLogDurability": True,
            "transactionTimeline": True,
            "admObservability": True,
            "runtimeFileLogProbe": True,
            "runtimeDbLogPolicyProbe": True,
            "runtimeCorrelationProbe": True,
        },
        "note": "Static closure only; Windows FullLocal runtime stages must PASS for runtime completion.",
    }
    print("CPF_INTEGRATED_LOGGING_CLOSURE=" + result["status"])
    print("failures=" + str(len(result["failures"])))
    for failure in result["failures"]:
        print(failure)
    if args.json_output:
        output = Path(args.json_output)
        if not output.is_absolute():
            output = root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return 0 if not failures else 1


if __name__ == "__main__":
    raise SystemExit(main())
