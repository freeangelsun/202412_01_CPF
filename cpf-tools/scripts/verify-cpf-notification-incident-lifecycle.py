#!/usr/bin/env python3
"""Fail-closed source/consumer/3DB contract for Notification·Incident lifecycle."""
from __future__ import annotations

import argparse
from pathlib import Path
import hashlib
import re
import sys

VENDORS = ("oracle", "postgresql", "mariadb")


def require(text: str, token: str, rel: str, errors: list[str]) -> None:
    if token not in text:
        errors.append(f"{rel}: required token missing: {token}")


def verify(root: Path) -> None:
    errors: list[str] = []
    rels = {
        "notification_controller": "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmNotificationController.java",
        "notification_service": "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationService.java",
        "notification_outbox": "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationOutboxService.java",
        "incident_controller": "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentLifecycleController.java",
        "incident_service": "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentLifecycleService.java",
        "incident_contracts": "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentContracts.java",
        "incident_conflict": "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentConflictException.java",
        "notification_methods": "cpf-admin/frontend/src/app/methods/referenceMethods.ts",
        "notification_page": "cpf-admin/frontend/src/features/notifications/NotificationsPage.vue",
        "incident_page": "cpf-admin/frontend/src/features/incidents/IncidentWorkbenchPage.vue",
        "incident_api": "cpf-admin/frontend/src/features/incidents/api.ts",
    }
    texts: dict[str, str] = {}
    for key, rel in rels.items():
        path = root / rel
        if not path.is_file():
            errors.append(f"{rel}: source missing")
        else:
            texts[key] = path.read_text(encoding="utf-8")
    if errors:
        raise ValueError("\n".join(errors))

    controller = texts["notification_controller"]
    for token in (
        'operationId = "admNotificationFindRules"',
        'operationId = "admNotificationSaveRule"',
        'operationId = "admNotificationUpdateRule"',
        'operationId = "admNotificationDisableRule"',
        'operationId = "admNotificationSendTest"',
        'operationId = "admNotificationRetryDelivery"',
        'operationId = "admNotificationCancelDelivery"',
        'operationId = "admNotificationFindDlq"',
        'notificationService.findDlq(limit)',
    ):
        require(controller, token, rels["notification_controller"], errors)
    if re.search(r"operator\(servletRequest,\s*(request\.|requestUser)", controller):
        errors.append(f"{rels['notification_controller']}: client-supplied operator is still trusted")

    service = texts["notification_service"]
    for token in (
        'findDeliveryLogsByStatus("DLQ", limit)',
        'auditLogService.record(',
        'NOTIFICATION_DELIVERY_RETRY',
        'NOTIFICATION_DELIVERY_CANCEL',
        'NOTIFICATION_RULE_CREATE',
        'NOTIFICATION_RULE_UPDATE',
    ):
        require(service, token, rels["notification_service"], errors)

    outbox = texts["notification_outbox"]
    for token in (
        'finalStatus = "DLQ";',
        "delivery_status IN ('DLQ', 'FAILED', 'UNKNOWN_RESULT', 'CANCELLED')",
        "delivery_status IN ('READY', 'RETRY', 'UNKNOWN_RESULT', 'DLQ')",
        "delivery_status = 'UNKNOWN_RESULT'",
        "delivery_status = 'PROCESSING'",
        "lease_owner = ?",
        "version = version + 1",
        "cpf_notification_delivery_attempt",
    ):
        require(outbox, token, rels["notification_outbox"], errors)
    if 'finalStatus = "FAILED";' in outbox:
        errors.append(f"{rels['notification_outbox']}: exhausted retry still terminates as generic FAILED")

    incident_controller = texts["incident_controller"]
    for token in (
        'operationId="admIncidentFindPolicies"',
        'operationId="admIncidentIngestSignal"',
        'operationId="admIncidentAcknowledge"',
        'operationId="admIncidentResolve"',
        'operationId="admIncidentReopen"',
        'operationId="admIncidentEscalate"',
        'operationId="admIncidentFindMaintenance"',
        'operationId="admIncidentCreateMaintenance"',
        'operationId="admIncidentUpdateMaintenance"',
        'operationId="admIncidentFindTimeline"',
    ):
        require(incident_controller, token, rels["incident_controller"], errors)

    for token in (
        "@ExceptionHandler(AdmIncidentConflictException.class)",
        "HttpStatus.NOT_FOUND",
        "HttpStatus.CONFLICT",
    ):
        require(incident_controller, token, rels["incident_controller"], errors)

    for token in (
        "INVALID_TRANSITION",
        "VERSION_CONFLICT",
        "ACTIVE_CONFLICT",
        "IDEMPOTENCY_CONFLICT",
        "COMMAND_IN_PROGRESS",
    ):
        require(texts["incident_conflict"], token, rels["incident_conflict"], errors)

    incident_service = texts["incident_service"]
    for token in (
        "threshold_count",
        "window_seconds",
        "escalation_minutes",
        "cpf_incident_timeline",
        "expectedVersion",
        "idempotencyKey",
        "SUPPRESSED_MAINTENANCE",
        "isMaintenance",
        "requireMutation",
        "requireTransition(before.status(), action)",
        "case \"ACKNOWLEDGE\" -> \"OPEN\".equals(before)",
        "case \"REOPEN\" -> \"RESOLVED\".equals(before)",
        "AdmIncidentConflictException.Type.VERSION_CONFLICT",
        "AdmIncidentConflictException.Type.ACTIVE_CONFLICT",
    ):
        require(incident_service, token, rels["incident_service"], errors)

    contracts = texts["incident_contracts"]
    for token in (
        "receiverGroup",
        "thresholdCount",
        "windowSeconds",
        "escalationMinutes",
        "approvalRequestId",
        "idempotencyKey",
        "MaintenanceSaveRequest",
        "TimelineResponse",
    ):
        require(contracts, token, rels["incident_contracts"], errors)

    methods = texts["notification_methods"]
    # The ADM frontend must use the generated OpenAPI client rather than a raw URL.
    for token in (
        'admNotificationFindDlq({ limit: 100 })',
        '["DLQ", "FAILED", "UNKNOWN_RESULT", "CANCELLED"]',
        '["READY", "RETRY", "UNKNOWN_RESULT", "DLQ"]',
    ):
        require(methods, token, rels["notification_methods"], errors)
    if '"/adm/api/notifications/delivery-logs/dlq' in methods or "'/adm/api/notifications/delivery-logs/dlq" in methods:
        errors.append(f"{rels['notification_methods']}: raw DLQ URL bypasses generated OpenAPI client")

    page = texts["notification_page"]
    for token in ('@click="loadNotificationDlq"', "delivery.deliveryStatus === 'DLQ'", "Provider Attempt 이력"):
        require(page, token, rels["notification_page"], errors)
    for token in ("act('escalate')", "tab.value==='maintenance'", "findTimeline", "savePolicy", "saveMaintenance", "submitPolicy", "submitMaintenance"):
        require(texts["incident_page"], token, rels["incident_page"], errors)
    for token in (
        "'/adm/api/incidents/maintenance-windows'",
        "`/adm/api/incidents/maintenance-windows/${id}`",
        "`/adm/api/incidents/${id}/acknowledge`",
        "`/adm/api/incidents/${id}/resolve`",
        "`/adm/api/incidents/${id}/reopen`",
        "`/adm/api/incidents/${id}/escalate`",
    ):
        require(texts["incident_api"], token, rels["incident_api"], errors)
    if "`/adm/api/incidents/${id}/${action}`" in texts["incident_api"]:
        errors.append(f"{rels['incident_api']}: generic dynamic incident action path bypasses explicit Operation contract")

    for vendor in VENDORS:
        rel = f"cpf-tools/db/vendor/{vendor}/source/10_cpf_schema.sql"
        path = root / rel
        if not path.is_file():
            errors.append(f"{rel}: source missing")
            continue
        ddl = path.read_text(encoding="utf-8")
        for table in (
            "cpf_notification_rule",
            "cpf_notification_delivery_log",
            "cpf_notification_delivery_attempt",
            "cpf_incident_policy",
            "cpf_incident_signal",
            "cpf_incident",
            "cpf_incident_timeline",
            "cpf_maintenance_window",
            "cpf_incident_command",
        ):
            require(ddl, table, rel, errors)
        for token in (
            "uk_cpf_incident_policy_code", "uk_cpf_incident_signal_idem",
            "uk_cpf_incident_active", "uk_cpf_incident_command_idem",
            "ix_cpf_incident_signal_window", "ix_cpf_incident_status",
            "ix_cpf_maintenance_active", "ck_cpf_maintenance_period",
        ):
            require(ddl, token, rel, errors)

        if vendor in ("oracle", "postgresql"):
            migration_rel = f"cpf-tools/db/vendor/{vendor}/migration/flyway/admDB/V92__adm_notification_incident_lifecycle.sql"
            checksum_rel = f"cpf-tools/db/vendor/{vendor}/migration/flyway/admDB/checksums.sha256"
        else:
            migration_rel = "cpf-tools/db/vendor/mariadb/migration/flyway/V92__adm_notification_incident_lifecycle.sql"
            checksum_rel = "cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"
        rollback_rel = f"cpf-tools/db/vendor/{vendor}/source/migration/rollback/R92__adm_notification_incident_lifecycle.sql"
        migration = root / migration_rel
        rollback = root / rollback_rel
        checksum = root / checksum_rel
        for required_path, required_rel in ((migration, migration_rel), (rollback, rollback_rel), (checksum, checksum_rel)):
            if not required_path.is_file(): errors.append(f"{required_rel}: source missing")
        if migration.is_file():
            migration_text = migration.read_text(encoding="utf-8")
            for table in ("cpf_incident_policy", "cpf_maintenance_window", "cpf_incident", "cpf_incident_signal", "cpf_incident_timeline", "cpf_incident_command"):
                require(migration_text, table, migration_rel, errors)
        if rollback.is_file():
            rollback_text = rollback.read_text(encoding="utf-8")
            for table in ("cpf_incident_command", "cpf_incident_timeline", "cpf_incident_signal", "cpf_incident", "cpf_maintenance_window", "cpf_incident_policy"):
                require(rollback_text, f"DROP TABLE" if table == "cpf_incident_command" else table, rollback_rel, errors)
                require(rollback_text, table, rollback_rel, errors)
        if migration.is_file() and checksum.is_file():
            digest = hashlib.sha256(migration.read_bytes()).hexdigest()
            line = f"{digest} *V92__adm_notification_incident_lifecycle.sql"
            if line not in checksum.read_text(encoding="utf-8"):
                errors.append(f"{checksum_rel}: V92 checksum mismatch or missing")

    maria_source_migration = root / "cpf-tools/db/vendor/mariadb/source/migration/flyway/V92__adm_notification_incident_lifecycle.sql"
    maria_source_checksum = root / "cpf-tools/db/vendor/mariadb/source/migration/flyway/checksums.sha256"
    if not maria_source_migration.is_file():
        errors.append("cpf-tools/db/vendor/mariadb/source/migration/flyway/V92__adm_notification_incident_lifecycle.sql: source missing")
    if not maria_source_checksum.is_file():
        errors.append("cpf-tools/db/vendor/mariadb/source/migration/flyway/checksums.sha256: source missing")
    if maria_source_migration.is_file() and maria_source_checksum.is_file():
        digest = hashlib.sha256(maria_source_migration.read_bytes()).hexdigest()
        if f"{digest} *V92__adm_notification_incident_lifecycle.sql" not in maria_source_checksum.read_text(encoding="utf-8"):
            errors.append("cpf-tools/db/vendor/mariadb/source/migration/flyway/checksums.sha256: V92 checksum mismatch or missing")

    if errors:
        raise ValueError("\n".join(errors))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    try:
        verify(Path(args.root).resolve())
    except ValueError as exc:
        print(f"[FAIL] CPF Notification·Incident lifecycle contract\n{exc}", file=sys.stderr)
        return 1
    print("[PASS] CPF Notification·Incident lifecycle rule=true threshold=true channel=true dlq=true escalation=true maintenance=true audit=true vendors=3 migration=V92 rollback=R92 checksum=true")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
