-- 기존 ADM DB에 거래 메타 스캔과 로그 정책 캐시 운영 버튼 권한을 비파괴 방식으로 보정합니다.
USE admDB;

INSERT INTO adm_menu (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('TRANSACTION_META', NULL, '거래 메타', '/adm#transactions', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY', NULL, '로그 정책', '/adm#log-policies', 115, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    MENU_NAME = VALUES(MENU_NAME),
    MENU_PATH = VALUES(MENU_PATH),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_button (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('TRANSACTION_META_READ', 'TRANSACTION_META', 'READ', '거래 메타 조회', 'GET', '/adm/api/transactions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_SCAN', 'TRANSACTION_META', 'SCAN', '거래 메타 스캔', 'POST', '/adm/api/transactions/scan', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_WRITE', 'TRANSACTION_META', 'WRITE', '거래 메타 비활성화', 'POST', '/adm/api/transactions/*/inactive', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_READ', 'LOG_POLICY', 'READ', '조회', 'GET', '/adm/api/log-policies/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_WRITE', 'LOG_POLICY', 'WRITE', '등록/수정', 'POST', '/adm/api/log-policies/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_OVERRIDE', 'LOG_POLICY', 'OVERRIDE', '임시 override', 'POST', '/adm/api/log-policies/overrides', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_REFRESH', 'LOG_POLICY', 'CACHE_REFRESH', '정책 캐시 새로고침', 'POST', '/adm/api/log-policies/cache/refresh', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_CLEAR', 'LOG_POLICY', 'CACHE_CLEAR', '정책 캐시 전체 삭제', 'POST', '/adm/api/log-policies/cache/clear', 50, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    MENU_ID = VALUES(MENU_ID),
    ACTION_CODE = VALUES(ACTION_CODE),
    BUTTON_NAME = VALUES(BUTTON_NAME),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATTERN = VALUES(API_PATTERN),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('TRANSACTION_META', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT ROLE_ID, MENU_ID, 'Y', 'Y', 'N', 'SYSTEM', 'SYSTEM'
FROM adm_role
CROSS JOIN adm_menu
WHERE ROLE_ID IN ('ADM_DEV_OPERATOR', 'ADM_OPERATOR')
  AND MENU_ID IN ('TRANSACTION_META', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT ROLE_ID, MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM adm_role
CROSS JOIN adm_menu
WHERE ROLE_ID IN ('ADM_BIZ_OPERATOR', 'ADM_VIEWER')
  AND MENU_ID IN ('TRANSACTION_META', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_button
WHERE BUTTON_ID IN (
    'TRANSACTION_META_READ',
    'TRANSACTION_META_SCAN',
    'TRANSACTION_META_WRITE',
    'LOG_POLICY_READ',
    'LOG_POLICY_WRITE',
    'LOG_POLICY_OVERRIDE',
    'LOG_POLICY_CACHE_REFRESH',
    'LOG_POLICY_CACHE_CLEAR'
)
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID, BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_role
CROSS JOIN adm_button
WHERE ROLE_ID IN ('ADM_DEV_OPERATOR', 'ADM_OPERATOR')
  AND BUTTON_ID IN (
      'TRANSACTION_META_READ',
      'TRANSACTION_META_SCAN',
      'TRANSACTION_META_WRITE',
      'LOG_POLICY_READ',
      'LOG_POLICY_WRITE',
      'LOG_POLICY_OVERRIDE',
      'LOG_POLICY_CACHE_REFRESH',
      'LOG_POLICY_CACHE_CLEAR'
  )
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID, BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_role
CROSS JOIN adm_button
WHERE ROLE_ID IN ('ADM_BIZ_OPERATOR', 'ADM_VIEWER')
  AND BUTTON_ID IN ('TRANSACTION_META_READ', 'LOG_POLICY_READ')
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
