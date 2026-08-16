-- ADM에서 CPF reliability capability를 조회하고 수동 처리하기 위한 메뉴·버튼·API 권한입니다.

USE admDB;

INSERT INTO adm_menu (
    MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by
) VALUES (
    'RELIABILITY', NULL, '신뢰성 처리 관제', '/adm#reliability', 52, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MENU_NAME = VALUES(MENU_NAME),
    MENU_PATH = VALUES(MENU_PATH),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_button (
    BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN,
    SORT_ORDER, USE_YN, created_by, updated_by
) VALUES
    ('RELIABILITY_READ', 'RELIABILITY', 'READ', '신뢰성 처리 조회', 'GET', '/adm/api/reliability/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_REPLAY', 'RELIABILITY', 'REPLAY', 'DLQ 재처리', 'POST', '/adm/api/reliability/broker/dlq/*/replay', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_RESOLVE', 'RELIABILITY', 'RESOLVE', '결과 미확정 수동 처리', 'POST', '/adm/api/reliability/unknown-results/*/resolve', 30, 'Y', 'SYSTEM', 'SYSTEM')
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

INSERT INTO adm_role_menu (
    ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by
)
SELECT ROLE_ID,
       'RELIABILITY',
       'Y',
       CASE WHEN ROLE_ID IN ('ADM_ADMIN', 'ADM_DEV_OPERATOR', 'ADM_OPERATOR') THEN 'Y' ELSE 'N' END,
       'N',
       'SYSTEM',
       'SYSTEM'
FROM adm_role
WHERE ROLE_ID IN ('ADM_ADMIN', 'ADM_DEV_OPERATOR', 'ADM_OPERATOR', 'ADM_BIZ_OPERATOR', 'ADM_VIEWER')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (
    ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by
)
SELECT role_source.ROLE_ID,
       button_source.BUTTON_ID,
       CASE
           WHEN button_source.BUTTON_ID = 'RELIABILITY_READ' THEN 'Y'
           WHEN role_source.ROLE_ID IN ('ADM_ADMIN', 'ADM_DEV_OPERATOR', 'ADM_OPERATOR') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM',
       'SYSTEM'
FROM (
    SELECT ROLE_ID
    FROM adm_role
    WHERE ROLE_ID IN ('ADM_ADMIN', 'ADM_DEV_OPERATOR', 'ADM_OPERATOR', 'ADM_BIZ_OPERATOR', 'ADM_VIEWER')
) role_source
CROSS JOIN (
    SELECT BUTTON_ID
    FROM adm_button
    WHERE BUTTON_ID IN ('RELIABILITY_READ', 'RELIABILITY_REPLAY', 'RELIABILITY_RESOLVE')
) button_source
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (
    API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME,
    PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by
)
SELECT CONCAT('API_', BUTTON_ID),
       MENU_ID,
       HTTP_METHOD,
       API_PATTERN,
       BUTTON_NAME,
       ACTION_CODE,
       MENU_ID,
       BUTTON_ID,
       USE_YN,
       'SYSTEM',
       'SYSTEM'
FROM adm_button
WHERE BUTTON_ID IN ('RELIABILITY_READ', 'RELIABILITY_REPLAY', 'RELIABILITY_RESOLVE')
ON DUPLICATE KEY UPDATE
    API_GROUP_CODE = VALUES(API_GROUP_CODE),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATH = VALUES(API_PATH),
    API_NAME = VALUES(API_NAME),
    PERMISSION_CODE = VALUES(PERMISSION_CODE),
    MENU_ID = VALUES(MENU_ID),
    BUTTON_ID = VALUES(BUTTON_ID),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_api_permission (
    ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by
)
SELECT rb.ROLE_ID, ap.API_PERMISSION_ID, rb.ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button rb
JOIN adm_api_permission ap ON ap.BUTTON_ID = rb.BUTTON_ID
WHERE rb.BUTTON_ID IN ('RELIABILITY_READ', 'RELIABILITY_REPLAY', 'RELIABILITY_RESOLVE')
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
