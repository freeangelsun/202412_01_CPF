from __future__ import annotations
import importlib.util
from pathlib import Path
import hashlib
import tempfile
import unittest

SCRIPT = Path(__file__).parents[1] / "verify-cpf-notification-incident-lifecycle.py"
spec = importlib.util.spec_from_file_location("notification_incident", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class NotificationIncidentLifecycleTest(unittest.TestCase):
    def fixture(self) -> Path:
        root = Path(tempfile.mkdtemp())
        files = {
            "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmNotificationController.java": '''
            operationId = "admNotificationFindRules"; operationId = "admNotificationSaveRule";
            operationId = "admNotificationUpdateRule"; operationId = "admNotificationDisableRule";
            operationId = "admNotificationSendTest"; operationId = "admNotificationRetryDelivery";
            operationId = "admNotificationCancelDelivery"; operationId = "admNotificationFindDlq";
            operator(servletRequest, null); notificationService.findDlq(limit);
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationService.java": '''
            findDeliveryLogsByStatus("DLQ", limit); auditLogService.record(
            NOTIFICATION_DELIVERY_RETRY NOTIFICATION_DELIVERY_CANCEL NOTIFICATION_RULE_CREATE NOTIFICATION_RULE_UPDATE
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationOutboxService.java": '''
            finalStatus = "DLQ"; delivery_status IN ('DLQ', 'FAILED', 'UNKNOWN_RESULT', 'CANCELLED')
            delivery_status IN ('READY', 'RETRY', 'UNKNOWN_RESULT', 'DLQ')
            delivery_status = 'UNKNOWN_RESULT' delivery_status = 'PROCESSING' lease_owner = ?
            version = version + 1 cpf_notification_delivery_attempt
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentLifecycleController.java": '''
            @ExceptionHandler(AdmIncidentConflictException.class) HttpStatus.NOT_FOUND HttpStatus.CONFLICT
            operationId="admIncidentFindPolicies" operationId="admIncidentIngestSignal"
            operationId="admIncidentAcknowledge" operationId="admIncidentResolve"
            operationId="admIncidentReopen" operationId="admIncidentEscalate"
            operationId="admIncidentFindMaintenance" operationId="admIncidentCreateMaintenance"
            operationId="admIncidentUpdateMaintenance" operationId="admIncidentFindTimeline"
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentLifecycleService.java": '''
            threshold_count window_seconds escalation_minutes cpf_incident_timeline expectedVersion
            idempotencyKey SUPPRESSED_MAINTENANCE isMaintenance requireMutation
            requireTransition(before.status(), action) case "ACKNOWLEDGE" -> "OPEN".equals(before)
            case "REOPEN" -> "RESOLVED".equals(before) AdmIncidentConflictException.Type.VERSION_CONFLICT
            AdmIncidentConflictException.Type.ACTIVE_CONFLICT
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentConflictException.java": '''
            INVALID_TRANSITION VERSION_CONFLICT ACTIVE_CONFLICT IDEMPOTENCY_CONFLICT COMMAND_IN_PROGRESS
            ''',
            "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentContracts.java": '''
            receiverGroup thresholdCount windowSeconds escalationMinutes approvalRequestId idempotencyKey
            MaintenanceSaveRequest TimelineResponse
            ''',
            "cpf-admin/frontend/src/app/methods/referenceMethods.ts": '''
            admNotificationFindDlq({ limit: 100 })
            ["DLQ", "FAILED", "UNKNOWN_RESULT", "CANCELLED"]
            ["READY", "RETRY", "UNKNOWN_RESULT", "DLQ"]
            ''',
            "cpf-admin/frontend/src/features/notifications/NotificationsPage.vue": '''
            @click="loadNotificationDlq" delivery.deliveryStatus === 'DLQ' Provider Attempt 이력
            ''',
            "cpf-admin/frontend/src/features/incidents/IncidentWorkbenchPage.vue": "act('escalate') tab.value==='maintenance' findTimeline savePolicy saveMaintenance submitPolicy submitMaintenance",
            "cpf-admin/frontend/src/features/incidents/api.ts": "'/adm/api/incidents/maintenance-windows' `/adm/api/incidents/maintenance-windows/${id}` `/adm/api/incidents/${id}/acknowledge` `/adm/api/incidents/${id}/resolve` `/adm/api/incidents/${id}/reopen` `/adm/api/incidents/${id}/escalate`",
        }
        ddl = " ".join((
            "cpf_notification_rule", "cpf_notification_delivery_log", "cpf_notification_delivery_attempt",
            "cpf_incident_policy", "cpf_incident_signal", "cpf_incident", "cpf_incident_timeline",
            "cpf_maintenance_window", "cpf_incident_command",
            "uk_cpf_incident_policy_code", "uk_cpf_incident_signal_idem", "uk_cpf_incident_active",
            "uk_cpf_incident_command_idem", "ix_cpf_incident_signal_window", "ix_cpf_incident_status",
            "ix_cpf_maintenance_active", "ck_cpf_maintenance_period",
        ))
        rollback = "DROP TABLE cpf_incident_command cpf_incident_timeline cpf_incident_signal cpf_incident cpf_maintenance_window cpf_incident_policy"
        for vendor in module.VENDORS:
            files[f"cpf-tools/db/vendor/{vendor}/source/10_cpf_schema.sql"] = ddl
            if vendor in ("oracle", "postgresql"):
                migration_rel = f"cpf-tools/db/vendor/{vendor}/migration/flyway/admDB/V92__adm_notification_incident_lifecycle.sql"
                checksum_rel = f"cpf-tools/db/vendor/{vendor}/migration/flyway/admDB/checksums.sha256"
            else:
                migration_rel = "cpf-tools/db/vendor/mariadb/migration/flyway/V92__adm_notification_incident_lifecycle.sql"
                checksum_rel = "cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"
            files[migration_rel] = ddl
            digest = hashlib.sha256(ddl.encode("utf-8")).hexdigest()
            files[checksum_rel] = f"{digest} *V92__adm_notification_incident_lifecycle.sql\n"
            files[f"cpf-tools/db/vendor/{vendor}/source/migration/rollback/R92__adm_notification_incident_lifecycle.sql"] = rollback
        files["cpf-tools/db/vendor/mariadb/source/migration/flyway/V92__adm_notification_incident_lifecycle.sql"] = ddl
        files["cpf-tools/db/vendor/mariadb/source/migration/flyway/checksums.sha256"] = f"{hashlib.sha256(ddl.encode('utf-8')).hexdigest()} *V92__adm_notification_incident_lifecycle.sql\n"
        for rel, text in files.items():
            path = root / rel
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8")
        return root

    def test_valid(self):
        module.verify(self.fixture())

    def test_raw_dlq_url_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/frontend/src/app/methods/referenceMethods.ts"
        p.write_text(p.read_text(encoding="utf-8") + '\n"/adm/api/notifications/delivery-logs/dlq?limit=100"', encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_generic_failed_terminal_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationOutboxService.java"
        p.write_text(p.read_text(encoding="utf-8").replace('finalStatus = "DLQ";', 'finalStatus = "FAILED";'), encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_client_actor_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmNotificationController.java"
        p.write_text(p.read_text(encoding="utf-8").replace("operator(servletRequest, null)", "operator(servletRequest, request.requestUser())"), encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_generic_dynamic_incident_action_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/frontend/src/features/incidents/api.ts"
        p.write_text(p.read_text(encoding="utf-8") + " `/adm/api/incidents/${id}/${action}`", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_missing_transition_guard_rejected(self):
        root = self.fixture()
        p = root / "cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentLifecycleService.java"
        p.write_text(p.read_text(encoding="utf-8").replace("requireTransition(before.status(), action)", ""), encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)

    def test_missing_vendor_table_rejected(self):
        root = self.fixture()
        p = root / "cpf-tools/db/vendor/oracle/source/10_cpf_schema.sql"
        p.write_text(p.read_text(encoding="utf-8").replace("cpf_incident_command", ""), encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root)


if __name__ == "__main__":
    unittest.main()
