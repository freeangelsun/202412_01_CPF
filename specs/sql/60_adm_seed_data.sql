-- ADM 초기 역할, 메뉴, 버튼 권한, 보안 정책, 로컬 계정 데이터입니다.
-- 대상 DB: admDB

USE admDB;

INSERT INTO adm_role (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES
    ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    ROLE_NAME = VALUES(ROLE_NAME),
    ROLE_TYPE = VALUES(ROLE_TYPE),
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_menu (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('DASHBOARD', NULL, '대시보드', '/adm', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST', NULL, '온라인 거래 로그', '/adm#logs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('STANDARD_EXECUTION', NULL, '표준 실행 카탈로그', '/adm#standard-executions', 23, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY', NULL, '채널 정책', '/adm#channel-policy', 24, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG', NULL, '원격 로그 관리', '/adm#remote-logs', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META', NULL, '거래 메타', '/adm#transactions', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG', NULL, '감사 로그', '/adm#audit-logs', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER', NULL, '회원 관리', '/adm#members', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH', NULL, '배치 관제', '/adm#batch', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY', NULL, '신뢰성 처리 관제', '/adm#reliability', 52, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', NULL, '알림 관리', '/adm#notifications', 55, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', NULL, '다운로드 감사', '/adm#downloads', 58, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE', NULL, '캐시 관리', '/adm#cache', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE', NULL, '메시지 관리', '/adm#messages', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE', NULL, '코드 관리', '/adm#codes', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE', NULL, '응답코드 관리', '/adm#response-codes', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG', NULL, '설정 관리', '/adm#configs', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG', NULL, '동적 로그 레벨', '/adm#log-level', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY', NULL, '로그 정책', '/adm#log-policies', 115, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD', NULL, '비밀번호 관리', '/adm#password', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY', NULL, '보안 운영', '/adm#security', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', NULL, '권한 관리', '/adm#permissions', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR', NULL, '운영자 관리', '/adm#operators', 150, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    PARENT_MENU_ID = VALUES(PARENT_MENU_ID),
    MENU_NAME = VALUES(MENU_NAME),
    MENU_PATH = VALUES(MENU_PATH),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_button (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('LOG_LIST_READ', 'LOG_LIST', 'READ', '조회', 'GET', '/adm/api/logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST_DETAIL', 'LOG_LIST', 'DETAIL', '상세 조회', 'GET', '/adm/api/logs/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST_DOWNLOAD', 'LOG_LIST', 'DOWNLOAD', '다운로드', 'GET', '/adm/api/logs/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('STANDARD_EXECUTION_READ', 'STANDARD_EXECUTION', 'READ', '표준 실행 조회', 'GET', '/adm/api/standard-executions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_READ', 'CHANNEL_POLICY', 'READ', '채널 정책 조회', 'GET', '/adm/api/channels/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_WRITE', 'CHANNEL_POLICY', 'WRITE', '채널·거래 정책 변경', 'PUT', '/adm/api/channels/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_REFRESH', 'CHANNEL_POLICY', 'REFRESH', '채널 정책 스냅샷 갱신', 'POST', '/adm/api/channels/refresh', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_IMPORT', 'CHANNEL_POLICY', 'IMPORT', '채널 정책 패키지 반입', 'POST', '/adm/api/channels/package/import', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_READ', 'REMOTE_LOG', 'READ', '로그 아티팩트 조회', 'GET', '/adm/api/remote-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_DOWNLOAD', 'REMOTE_LOG', 'DOWNLOAD', '로그 아티팩트 다운로드', 'GET', '/adm/api/remote-logs/*/download', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_DOWNLOAD', 'REMOTE_LOG', 'DOWNLOAD', '동기 로그 ZIP 다운로드', 'POST', '/adm/api/remote-logs/bundles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_CREATE', 'REMOTE_LOG', 'CREATE', '비동기 로그 ZIP 작업 등록', 'POST', '/adm/api/remote-logs/bundle-jobs', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_TOKEN', 'REMOTE_LOG', 'ISSUE', '로그 ZIP 다운로드 token 발급', 'POST', '/adm/api/remote-logs/bundle-jobs/*/download-tokens', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_JOB_DOWNLOAD', 'REMOTE_LOG', 'DOWNLOAD', '비동기 로그 ZIP 다운로드', 'GET', '/adm/api/remote-logs/bundle-jobs/*/download', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_READ', 'TRANSACTION_META', 'READ', '거래 메타 조회', 'GET', '/adm/api/transactions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_SCAN', 'TRANSACTION_META', 'SCAN', '거래 메타 스캔', 'POST', '/adm/api/transactions/scan', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_WRITE', 'TRANSACTION_META', 'WRITE', '거래 메타 비활성화', 'POST', '/adm/api/transactions/*/inactive', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG_READ', 'AUDIT_LOG', 'READ', '조회', 'GET', '/adm/api/audit-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_READ', 'MEMBER', 'READ', '회원 조회', 'GET', '/adm/api/members/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_CREATE', 'MEMBER', 'CREATE', '회원 등록', 'POST', '/adm/api/members', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_UPDATE', 'MEMBER', 'UPDATE', '회원 수정', 'PUT', '/adm/api/members/*', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_STATUS', 'MEMBER', 'STATUS', '회원 상태 변경', 'PUT', '/adm/api/members/*/status', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_ROLE_GRANT', 'MEMBER', 'ROLE_GRANT', '회원 권한 부여', 'POST', '/adm/api/members/*/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_ROLE_REVOKE', 'MEMBER', 'ROLE_REVOKE', '회원 권한 회수', 'DELETE', '/adm/api/members/*/roles/*', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_READ', 'BATCH', 'READ', '조회', 'GET', '/adm/api/batch/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_REGISTER', 'BATCH', 'REGISTER', '배치 등록', 'POST', '/adm/api/batch/jobs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_EXECUTE', 'BATCH', 'EXECUTE', '수동 실행', 'POST', '/adm/api/batch/*/run', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RETRY', 'BATCH', 'RETRY', '실패 재수행', 'POST', '/adm/api/batch/executions/*/retry', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_STOP', 'BATCH', 'STOP', '실행 중지', 'POST', '/adm/api/batch/executions/*/stop', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULE', 'BATCH', 'SCHEDULE', '스케줄 변경', 'POST', '/adm/api/batch/schedules/**', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_CALENDAR_SAVE', 'BATCH', 'CALENDAR_SAVE', '영업일 저장', 'POST', '/adm/api/batch/calendar', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SIMULATION', 'BATCH', 'SIMULATION', '수행 시뮬레이션', 'GET', '/adm/api/batch/schedules/*/simulation', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RELATION_READ', 'BATCH', 'RELATION_READ', '배치 관계 조회', 'GET', '/adm/api/batch/relations', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_TARGET_READ', 'BATCH', 'TARGET_READ', '수행 대상 조회', 'GET', '/adm/api/batch/execution-targets', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULER_RUN', 'BATCH', 'SCHEDULER_RUN', '스케줄러 1회 실행', 'POST', '/adm/api/batch/scheduler/run-once', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_JOB_DETAIL', 'BATCH', 'DETAIL', 'Job 상세 조회', 'GET', '/adm/api/batch/jobs/*', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_STEP_READ', 'BATCH', 'STEP_READ', 'Step 이력 조회', 'GET', '/adm/api/batch/steps', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_WORKER_READ', 'BATCH', 'WORKER_READ', 'Worker 상태 조회', 'GET', '/adm/api/batch/workers', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LOCK_READ', 'BATCH', 'LOCK_READ', 'Lock 조회', 'GET', '/adm/api/batch/locks', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LOCK_RELEASE', 'BATCH', 'LOCK_RELEASE', 'Lock 강제 해제', 'POST', '/adm/api/batch/locks/release', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_GHOST_READ', 'BATCH', 'GHOST_READ', 'Ghost 후보 조회', 'GET', '/adm/api/batch/ghost-candidates', 170, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_GHOST_ACTION', 'BATCH', 'GHOST_ACTION', 'Ghost 조치', 'POST', '/adm/api/batch/ghost-candidates/*/actions', 180, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_OPERATION_READ', 'BATCH', 'OPERATION_READ', '운영 작업 로그 조회', 'GET', '/adm/api/batch/operations', 190, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_READ', 'RELIABILITY', 'READ', '신뢰성 처리 조회', 'GET', '/adm/api/reliability/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_REPLAY', 'RELIABILITY', 'REPLAY', 'DLQ 재처리', 'POST', '/adm/api/reliability/broker/dlq/*/replay', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_RESOLVE', 'RELIABILITY', 'RESOLVE', '결과 미확정 수동 처리', 'POST', '/adm/api/reliability/unknown-results/*/resolve', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_RECOVERY_RUN', 'RELIABILITY', 'RECOVERY_RUN', 'DB 거래 로그 복구 실행', 'POST', '/adm/api/reliability/transaction-log-recovery/run', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_READ', 'NOTIFICATION', 'READ', '알림 조회', 'GET', '/adm/api/notifications/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_WRITE', 'NOTIFICATION', 'WRITE', '알림 등록/수정', 'POST', '/adm/api/notifications/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_DISABLE', 'NOTIFICATION', 'DISABLE', '알림 비활성화', 'PUT', '/adm/api/notifications/rules/*/disable', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_TEST_SEND', 'NOTIFICATION', 'TEST_SEND', '알림 테스트 발송', 'POST', '/adm/api/notifications/rules/*/test-send', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_READ', 'DOWNLOAD', 'READ', '다운로드 감사 조회', 'GET', '/adm/api/downloads/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_EXECUTE', 'DOWNLOAD', 'DOWNLOAD', 'CSV 다운로드', 'POST', '/adm/api/downloads/csv', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_READ', 'CACHE', 'READ', '조회', 'GET', '/adm/api/cache/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_REFRESH', 'CACHE', 'REFRESH', '캐시 갱신', 'POST', '/adm/api/cache/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_READ', 'MESSAGE', 'READ', '조회', 'GET', '/adm/api/messages/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_WRITE', 'MESSAGE', 'WRITE', '등록/수정', 'POST', '/adm/api/messages/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_DISABLE', 'MESSAGE', 'DISABLE', '비활성', 'DELETE', '/adm/api/messages/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_READ', 'CODE', 'READ', '조회', 'GET', '/adm/api/codes/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_WRITE', 'CODE', 'WRITE', '등록/수정', 'POST', '/adm/api/codes/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_DISABLE', 'CODE', 'DISABLE', '비활성', 'DELETE', '/adm/api/codes/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE_READ', 'RESPONSE_CODE', 'READ', '조회', 'GET', '/adm/api/response-codes/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE_WRITE', 'RESPONSE_CODE', 'WRITE', '등록/수정', 'POST', '/adm/api/response-codes/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG_READ', 'CONFIG', 'READ', '조회', 'GET', '/adm/api/configs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG_WRITE', 'CONFIG', 'WRITE', '수정', 'POST', '/adm/api/configs/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG_READ', 'DYNAMIC_LOG', 'READ', '조회', 'GET', '/adm/api/log-level/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG_WRITE', 'DYNAMIC_LOG', 'WRITE', '적용', 'POST', '/adm/api/log-level/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_READ', 'LOG_POLICY', 'READ', '조회', 'GET', '/adm/api/log-policies/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_WRITE', 'LOG_POLICY', 'WRITE', '등록/수정', 'POST', '/adm/api/log-policies/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_OVERRIDE', 'LOG_POLICY', 'OVERRIDE', '임시 override', 'POST', '/adm/api/log-policies/overrides', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_REFRESH', 'LOG_POLICY', 'CACHE_REFRESH', '정책 캐시 새로고침', 'POST', '/adm/api/log-policies/cache/refresh', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_CLEAR', 'LOG_POLICY', 'CACHE_CLEAR', '정책 캐시 전체 삭제', 'POST', '/adm/api/log-policies/cache/clear', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_READ', 'PASSWORD', 'READ', '정책 조회', 'GET', '/adm/api/operators/password-policy/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_RESET', 'PASSWORD', 'RESET_PASSWORD', '비밀번호 초기화', 'POST', '/adm/api/operators/*/password/reset', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_UNLOCK', 'PASSWORD', 'UNLOCK', '잠금 해제', 'POST', '/adm/api/operators/*/unlock', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_SESSION_REVOKE', 'PASSWORD', 'REVOKE_SESSION', '세션 강제 종료', 'POST', '/adm/api/operators/sessions/*/revoke', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_READ', 'SECURITY', 'READ', '조회', 'GET', '/adm/api/security/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_WRITE', 'SECURITY', 'WRITE', '보안 설정 변경', 'POST', '/adm/api/security/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION_READ', 'PERMISSION', 'READ', '조회', 'GET', '/adm/api/permissions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION_WRITE', 'PERMISSION', 'WRITE', '권한 변경', 'POST', '/adm/api/permissions/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_READ', 'OPERATOR', 'READ', '조회', 'GET', '/adm/api/operators/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_CREATE', 'OPERATOR', 'CREATE', '운영자 등록', 'POST', '/adm/api/operators', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_ROLE_UPDATE', 'OPERATOR', 'ROLE_UPDATE', '역할 부여', 'PUT', '/adm/api/operators/*/roles', 30, 'Y', 'SYSTEM', 'SYSTEM')
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

INSERT INTO adm_password_policy (
    POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN,
    REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by
) VALUES (
    'DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MIN_LENGTH = VALUES(MIN_LENGTH),
    REQUIRE_UPPER_YN = VALUES(REQUIRE_UPPER_YN),
    REQUIRE_LOWER_YN = VALUES(REQUIRE_LOWER_YN),
    REQUIRE_DIGIT_YN = VALUES(REQUIRE_DIGIT_YN),
    REQUIRE_SPECIAL_YN = VALUES(REQUIRE_SPECIAL_YN),
    MAX_FAIL_COUNT = VALUES(MAX_FAIL_COUNT),
    EXPIRE_DAYS = VALUES(EXPIRE_DAYS),
    HISTORY_LIMIT = VALUES(HISTORY_LIMIT),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

-- 최초 운영자는 CPF_ADM_BOOTSTRAP_* 환경변수를 명시한 애플리케이션 bootstrap에서만 생성합니다.
-- SQL seed에는 재사용 가능한 비밀번호 해시나 자동 활성 관리자 계정을 두지 않습니다.

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_menu
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('MEMBER', 'BATCH', 'DOWNLOAD', 'CACHE') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID = 'MEMBER' THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', MENU_ID, READ_YN, WRITE_YN, DELETE_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_menu
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', BUTTON_ID,
       CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', BUTTON_ID,
       CASE
           WHEN BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_UPDATE', 'MEMBER_STATUS', 'MEMBER_ROLE_GRANT', 'MEMBER_ROLE_REVOKE', 'BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', BUTTON_ID,
       CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', BUTTON_ID, ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

-- ADM API 권한은 버튼/행위 권한을 실제 Controller path와 연결하기 위한 서버 권한검사 메타입니다.
INSERT INTO adm_api_permission (
    API_PERMISSION_ID,
    API_GROUP_CODE,
    HTTP_METHOD,
    API_PATH,
    API_NAME,
    PERMISSION_CODE,
    MENU_ID,
    BUTTON_ID,
    USE_YN,
    created_by,
    updated_by
)
SELECT
    CONCAT('API_', BUTTON_ID),
    MENU_ID,
    COALESCE(HTTP_METHOD, 'ANY'),
    API_PATTERN,
    BUTTON_NAME,
    ACTION_CODE,
    MENU_ID,
    BUTTON_ID,
    USE_YN,
    'SYSTEM',
    'SYSTEM'
FROM adm_button
WHERE API_PATTERN IS NOT NULL
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

INSERT INTO adm_api_permission (
    API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
    MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by
) VALUES (
    'API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE',
    'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM'
)
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

INSERT INTO adm_role_api_permission (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID, ap.API_PERMISSION_ID, rb.ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button rb
JOIN adm_api_permission ap ON ap.BUTTON_ID = rb.BUTTON_ID
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

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
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
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
    WHERE TRANSACTION_ID = 'INITIAL_SQL_SEED'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);
