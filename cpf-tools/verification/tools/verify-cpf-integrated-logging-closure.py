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
    logging_aspect_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/LoggingAspect.java"
    logging_aspect_test_path = "cpf-starters/platform-operations/observability/src/test/java/com/cpf/platform/operations/observability/internal/logging/LoggingAspectCanonicalContextTest.java"
    db_service_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/TransactionLogService.java"
    segment_service_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/segment/TransactionSegmentPersistenceService.java"
    db_listener_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/TransactionLogListener.java"
    db_adapter_path = "cpf-starters/data/persistence/mybatis/src/main/java/com/cpf/data/persistence/mybatis/logging/MyBatisTransactionLogPersistenceAdapter.java"
    timeline_path = "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/segment/CpfTransactionTimelineQueryFacade.java"
    adm_observability_path = "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmObservabilityController.java"
    adm_log_path = "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmLogController.java"
    file_smoke_path = "cpf-tools/runtime/tools/smoke-file-log-standard-runtime.ps1"
    policy_smoke_path = "cpf-tools/runtime/tools/smoke-log-policy-runtime.ps1"
    correlation_smoke_path = "cpf-tools/runtime/tools/smoke-integrated-log-correlation.ps1"
    full_local_path = "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"
    canonical_schema_path = "cpf-tools/db/canonical/platform-schema.json"

    canonical_schema_source = require_file(root, canonical_schema_path, failures)
    if canonical_schema_source:
        canonical_schema = json.loads(canonical_schema_source)
        segment_tables = [
            table for table in canonical_schema.get("tables", [])
            if table.get("name") == "CPF_TRANSACTION_SEGMENT"
        ]
        if len(segment_tables) != 1:
            failures.append(f"CANONICAL_TABLE_COUNT:{canonical_schema_path}:CPF_TRANSACTION_SEGMENT:{len(segment_tables)}")
        else:
            segment_table = segment_tables[0]
            segment_columns = {column.get("name") for column in segment_table.get("columns", [])}
            if "execution_id" not in segment_columns:
                failures.append(f"CANONICAL_COLUMN_MISSING:{canonical_schema_path}:CPF_TRANSACTION_SEGMENT.execution_id")
            segment_indexes = {
                index.get("name"): index.get("columns") for index in segment_table.get("indexes", [])
            }
            if segment_indexes.get("ix_cpf_transaction_segment_execution") != ["execution_id", "started_at"]:
                failures.append(f"CANONICAL_INDEX_MISMATCH:{canonical_schema_path}:ix_cpf_transaction_segment_execution")

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

    logging_aspect = require_file(root, logging_aspect_path, failures)
    require_tokens(logging_aspect, logging_aspect_path, (
        "boolean success = responseMetadata.httpStatus() < 400",
        'transactionSegment.fail("HTTP_" + responseMetadata.httpStatus()',
        'success ? "SUCCESS" : "FAILURE"',
        "canonicalErrorResponse(errorMetadata, transactionId)",
        "errorMetadata.httpStatus()",
        "errorMetadata.responseCode()",
        "errorMetadata.messageCode()",
        "CpfMaskingRuntime.mask",
    ), failures)

    logging_aspect_test = require_file(root, logging_aspect_test_path, failures)
    require_tokens(logging_aspect_test, logging_aspect_test_path, (
        "classifiesHandledHttpErrorAsFailureAndPersistsResponse",
        "persistsCanonicalErrorResponseWhenBusinessOperationThrows",
        'assertEquals("FAILURE"',
        "assertEquals(409",
        "assertNotNull(record.getResponse())",
    ), failures)

    segment_service = require_file(root, segment_service_path, failures)
    require_tokens(segment_service, segment_service_path, (
        'transactionManager = "cpfTransactionManager"',
        "Propagation.REQUIRES_NEW",
        "insertSegment",
        "updateSegment",
    ), failures)

    db_service = require_file(root, db_service_path, failures)
    require_tokens(db_service, db_service_path, (
        'transactionManager = "cpfTransactionManager"',
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

    canonical_mapper_namespace = (
        '<mapper namespace="com.cpf.data.persistence.mybatis.mapper.logging.TransactionLogMapper">'
    )
    canonical_segment_mapper_namespace = (
        '<mapper namespace="com.cpf.data.persistence.mybatis.mapper.logging.TransactionSegmentMapper">'
    )
    for vendor in ("mariadb", "postgresql", "oracle"):
        template_path = (
            f"cpf-tools/db/runtime-template/cpf/vendor/{vendor}/mybatis/logging/TransactionLogMapper.xml.template"
        )
        rendered_path = f"cpf-tools/db/vendor/{vendor}/runtime/cpf/mybatis/logging/TransactionLogMapper.xml"
        for mapper_path in (template_path, rendered_path):
            mapper = require_file(root, mapper_path, failures)
            require_tokens(mapper, mapper_path, (
                canonical_mapper_namespace,
                'id="existsRecoveryEvent"',
                'id="insertTransactionLog"',
                'id="insertTransactionLogDetail"',
                "CPF_TRANSACTION_LOG",
                "CPF_TRANSACTION_LOG_DETAIL",
                "CALLER_SYSTEM_CODE",
                "TARGET_SYSTEM_CODE",
                "ORIGINAL_SYSTEM_CODE",
                "SYSTEM_CODE",
                "#{callerSystemCode}",
                "#{targetSystemCode}",
                "#{originalSystemCode}",
                "#{systemCode}",
            ), failures)
            if "com.cpf.core.mapper.common.logging.TransactionLogMapper" in mapper:
                failures.append(f"{mapper_path}: retired TransactionLogMapper namespace remains")
            if "cpf_transaction_log" in mapper or "cpf_transaction_log_detail" in mapper:
                failures.append(f"{mapper_path}: noncanonical lowercase transaction-log table remains")

        segment_template_path = (
            f"cpf-tools/db/runtime-template/cpf/vendor/{vendor}/mybatis/logging/TransactionSegmentMapper.xml.template"
        )
        segment_rendered_path = (
            f"cpf-tools/db/vendor/{vendor}/runtime/cpf/mybatis/logging/TransactionSegmentMapper.xml"
        )
        for mapper_path in (segment_template_path, segment_rendered_path):
            mapper = require_file(root, mapper_path, failures)
            require_tokens(mapper, mapper_path, (
                canonical_segment_mapper_namespace,
                'id="insertSegment"',
                'id="updateSegmentEnd"',
                'id="countByTransactionSegmentId"',
                "CPF_TRANSACTION_SEGMENT",
                "execution_id",
                "#{executionId}",
                "system_code",
                "original_system_code",
                "caller_system_code",
                "target_system_code",
                "current_channel",
                "original_channel",
                "caller_channel",
                "target_channel",
                "#{systemCode}",
                "#{originalSystemCode}",
                "#{callerSystemCode}",
                "#{targetSystemCode}",
                "#{currentChannel}",
                "#{originalChannel}",
                "#{callerChannel}",
                "#{targetChannel}",
            ), failures)
            if "com.cpf.core.mapper.common.logging.TransactionSegmentMapper" in mapper:
                failures.append(f"{mapper_path}: retired TransactionSegmentMapper namespace remains")
            if "cpf_transaction_segment" in mapper:
                failures.append(f"{mapper_path}: noncanonical lowercase transaction-segment table remains")

    timeline = require_file(root, timeline_path, failures)
    require_tokens(timeline, timeline_path, (
        '"CPF_TRANSACTION_LOG"',
        '"CPF_TRANSACTION_SEGMENT"',
        "CPF_TRANSACTION_LINEAGE",
        "target_system_code AS remoteSystem",
        '"CPF_FILE_TRANSFER_HISTORY"',
        '"CPF_UNKNOWN_RESULT"',
        '"CPF_BROKER_OUTBOX"',
        '"CPF_BROKER_DLQ"',
        "transactionId",
    ), failures)
    if "remote_system AS remoteSystem" in timeline:
        failures.append(f"{timeline_path}: retired lineage remote_system column remains")
    for retired_table in (
        "cpf_transaction_log",
        "cpf_transaction_segment",
        "cpf_transaction_lineage",
        "cpf_file_transfer_history",
        "cpf_unknown_result",
    ):
        if retired_table in timeline:
            failures.append(f"{timeline_path}: noncanonical lowercase physical table remains: {retired_table}")

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
