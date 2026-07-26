-- cpf-tools/db/vendor/mariadb/source/59_adm_local_seed.sql
-- ============================================================================
-- ADM 로컬 개발/검증 전용 Optional Seed입니다.
-- 제품 필수 기준정보가 아니며 로컬 PC에서만 명시적으로 적용합니다.
-- 운영 배포에서는 IP allowlist와 감사 이벤트를 승인된 운영 절차로 등록합니다.

USE admDB;

INSERT INTO adm_ip_allowlist (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('127.0.0.1', '로컬 개발 PC', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_audit_log (
    TRANSACTION_ID,
    TRACE_ID,
    OPERATOR_ID,
    MENU_ID,
    ACTION_TYPE,
    TARGET_TYPE,
    TARGET_ID,
    REASON,
    REQUEST_BODY,
    CLIENT_IP,
    created_by,
    updated_by
) SELECT
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
