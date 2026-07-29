-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=59_adm_local_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=admDB
INSERT INTO adm_ip_allowlist (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by) VALUES ('127.0.0.1', '로컬 개발 PC', 'Y', 'SYSTEM', 'SYSTEM') ON DUPLICATE KEY UPDATE DESCRIPTION = VALUES(DESCRIPTION), USE_YN = VALUES(USE_YN), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
INSERT INTO adm_audit_log (TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, REQUEST_BODY, CLIENT_IP, created_by, updated_by) SELECT
    '20260724120000000ADMadmUI010000001',
    '20260724120000000ADMadmUI010000001',
    'admin',
    'DASHBOARD',
    'SEED',
    'ADM',
    'INITIAL_DATA',
    'ADM 초기 데이터 등록',
    NULL,
    '127.0.0.1',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM adm_audit_log
    WHERE TRANSACTION_ID = '20260724120000000ADMadmUI010000001'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);
