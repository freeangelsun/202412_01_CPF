-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=59_adm_local_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO ADM_IP_ALLOWLIST tgt
USING (SELECT '127.0.0.1' AS IP_PATTERN, '로컬 개발 PC' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.IP_PATTERN=src.IP_PATTERN)
WHEN MATCHED THEN UPDATE SET tgt.DESCRIPTION=src.DESCRIPTION, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.IP_PATTERN, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
INSERT INTO ADM_AUDIT_LOG (TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, REQUEST_BODY, CLIENT_IP, created_by, updated_by)
SELECT
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
    FROM ADM_AUDIT_LOG
    WHERE TRANSACTION_ID = '20260724120000000ADMadmUI010000001'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);
