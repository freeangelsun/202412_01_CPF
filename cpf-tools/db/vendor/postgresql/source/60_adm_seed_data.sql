-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=60_adm_seed_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
INSERT INTO ADM_ROLE (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (ROLE_ID) DO UPDATE SET ROLE_NAME=EXCLUDED.ROLE_NAME, ROLE_TYPE=EXCLUDED.ROLE_TYPE, DESCRIPTION=EXCLUDED.DESCRIPTION, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_MENU (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('DASHBOARD', NULL, '대시보드', '/adm', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CAPABILITY_FLEET', NULL, 'CPF Capability', '/adm#capabilities', 15, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST', NULL, '온라인 거래 로그', '/adm#logs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('STANDARD_EXECUTION', NULL, '표준 실행 카탈로그', '/adm#standard-executions', 23, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY', NULL, '채널 정책', '/adm#channel-policy', 24, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG', NULL, '원격 로그 관리', '/adm#remote-logs', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META', NULL, '거래 메타', '/adm#transactions', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG', NULL, '감사 로그', '/adm#audit-logs', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH', NULL, '배치 관제', '/adm#batch', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY', NULL, '신뢰성 처리 관제', '/adm#reliability', 52, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', NULL, '알림 관리', '/adm#notifications', 55, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', NULL, '다운로드 감사', '/adm#downloads', 58, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE', NULL, '캐시 관리', '/adm#cache', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB', NULL, '대량파일 Job', '/adm#file-jobs', 61, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE', NULL, '메시지 관리', '/adm#messages', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE', NULL, '코드 관리', '/adm#codes', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE', NULL, '응답코드 관리', '/adm#response-codes', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG', NULL, '설정 관리', '/adm#configs', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG', NULL, '동적 로그 레벨', '/adm#log-level', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY', NULL, '로그 정책', '/adm#log-policies', 115, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD', NULL, '비밀번호 관리', '/adm#password', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY', NULL, '보안 운영', '/adm#security', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', NULL, '권한 관리', '/adm#permissions', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECRET', NULL, 'Secret / Key 관리', '/adm#secrets', 145, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR', NULL, '운영자 관리', '/adm#operators', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_DASHBOARD', NULL, 'Gateway 대시보드', '/adm#gateway-dashboard', 300, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_SERVERS', 'GATEWAY_DASHBOARD', 'Gateway 연동 서버', '/adm#gateway-servers', 301, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_GROUPS', 'GATEWAY_DASHBOARD', 'Gateway 서버 그룹', '/adm#gateway-groups', 302, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_ROUTES', 'GATEWAY_DASHBOARD', 'Gateway 경로·라우팅', '/adm#gateway-routes', 303, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_SECURITY', 'GATEWAY_DASHBOARD', 'Gateway 보안·제한', '/adm#gateway-security', 304, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_HEALTH', 'GATEWAY_DASHBOARD', 'Gateway Health·연결시험', '/adm#gateway-health', 305, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_TRANSACTIONS', 'GATEWAY_DASHBOARD', 'Gateway 거래 조회', '/adm#gateway-transactions', 306, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_LOG_POLICY', 'GATEWAY_DASHBOARD', 'Gateway 로그 정책', '/adm#gateway-log-policies', 307, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_APPLY_STATUS', 'GATEWAY_DASHBOARD', 'Gateway 적용 상태·이력', '/adm#gateway-apply-status', 308, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_OVERVIEW', 'BATCH', 'Batch Overview', '/adm#batch-overview', 501, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RUNTIME', 'BATCH', 'Runtime Topology', '/adm#batch-runtime', 502, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_INSTANCES', 'BATCH', 'Runtime Instances', '/adm#batch-instances', 503, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULER', 'BATCH', 'Scheduler HA', '/adm#batch-scheduler', 504, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_WORKER_POOLS', 'BATCH', 'Worker Pools', '/adm#batch-worker-pools', 505, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_CENTER_CUT', 'BATCH', 'Center-Cut', '/adm#batch-center-cut', 506, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_AGENTS', 'BATCH', 'Host Agents', '/adm#batch-agents', 507, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_JOB_PACKS', 'BATCH', 'Job Packs', '/adm#batch-job-packs', 508, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_EXECUTIONS', 'BATCH', 'Executions', '/adm#batch-executions', 509, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEPLOYMENT', 'BATCH', 'Deployment / Rollback', '/adm#batch-deployment', 510, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RECOVERY', 'BATCH', 'Recovery / Unknown', '/adm#batch-recovery', 511, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LEASES', 'BATCH', 'Lease / Fencing', '/adm#batch-leases', 512, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_ALERTS', 'BATCH', 'Batch Alerts', '/adm#batch-alerts', 513, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_AUDIT', 'BATCH', 'Audit / Evidence', '/adm#batch-audit', 514, 'Y', 'SYSTEM', 'SYSTEM'),
    ('APPROVAL', NULL, '위험조치 승인', '/adm#approvals', 524, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BREAK_GLASS', NULL, 'Break-glass', '/adm#breakGlass', 534, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BUSINESS_CALENDAR', NULL, '영업일 · 휴일', '/adm#businessCalendar', 544, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CAPACITY', NULL, 'Online Runtime Diagnostics', '/adm#capacity', 554, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FEATURE_FLAG', NULL, 'Feature Flag', '/adm#featureFlags', 564, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TOPOLOGY', NULL, '서비스 토폴로지', '/adm#topology', 574, 'Y', 'SYSTEM', 'SYSTEM'),
    ('INCIDENT', NULL, 'Error·Unknown Result', '/adm#incidents', 584, 'Y', 'SYSTEM', 'SYSTEM'),
    ('INTEGRATION_CLOSURE', NULL, '통합 운영 정정 승인', '/adm#integrationClosure', 594, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MAINTENANCE', NULL, '점검·Drain', '/adm#maintenance', 604, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPENAPI_OPERATIONS', NULL, 'OpenAPI 운영', '/adm#openApiOperations', 614, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS_GOVERNANCE', NULL, '운영 정책·SLO', '/adm#operations-governance', 624, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RECOVERY_CENTER', NULL, '복구 센터', '/adm#recoveryCenter', 634, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESILIENCE_POLICY', NULL, 'Resilience 정책', '/adm#resiliencePolicies', 644, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RUNTIME_CONTROL', NULL, 'Deployment·Promotion·Rollback', '/adm#runtimeControl', 654, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SERVICE_REGISTRY', NULL, '서비스 레지스트리', '/adm#serviceRegistry', 664, 'Y', 'SYSTEM', 'SYSTEM'),
    ('WORKER', NULL, 'Agent / Worker', '/adm#workers', 674, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (MENU_ID) DO UPDATE SET PARENT_MENU_ID=EXCLUDED.PARENT_MENU_ID, MENU_NAME=EXCLUDED.MENU_NAME, MENU_PATH=EXCLUDED.MENU_PATH, SORT_ORDER=EXCLUDED.SORT_ORDER, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('CAPABILITY_FLEET_READ', 'CAPABILITY_FLEET', 'READ', 'CPF Capability 조회', 'GET', '/adm/api/capability-management/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
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
    ('REMOTE_LOG_BUNDLE_DOWNLOAD', 'REMOTE_LOG', 'BUNDLE_DOWNLOAD', '동기 로그 ZIP 다운로드', 'POST', '/adm/api/remote-logs/bundles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_CREATE', 'REMOTE_LOG', 'CREATE', '비동기 로그 ZIP 작업 등록', 'POST', '/adm/api/remote-logs/bundle-jobs', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_TOKEN', 'REMOTE_LOG', 'ISSUE', '로그 ZIP 다운로드 token 발급', 'POST', '/adm/api/remote-logs/bundle-jobs/*/download-tokens', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_JOB_DOWNLOAD', 'REMOTE_LOG', 'JOB_DOWNLOAD', '비동기 로그 ZIP 다운로드', 'GET', '/adm/api/remote-logs/bundle-jobs/*/download', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_READ', 'TRANSACTION_META', 'READ', '거래 메타 조회', 'GET', '/adm/api/transactions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_WRITE', 'TRANSACTION_META', 'WRITE', '거래 메타 비활성화', 'POST', '/adm/api/transactions/*/inactive', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG_READ', 'AUDIT_LOG', 'READ', '조회', 'GET', '/adm/api/audit-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
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
    ('NOTIFICATION_RETRY', 'NOTIFICATION', 'RETRY', '알림 발송 재시도', 'POST', '/adm/api/notifications/delivery-logs/*/retry', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_CANCEL', 'NOTIFICATION', 'CANCEL', '알림 발송 취소', 'POST', '/adm/api/notifications/delivery-logs/*/cancel', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_READ', 'DOWNLOAD', 'READ', '다운로드 감사 조회', 'GET', '/adm/api/downloads/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_EXECUTE', 'DOWNLOAD', 'DOWNLOAD', 'CSV 다운로드', 'POST', '/adm/api/downloads/csv', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_READ', 'CACHE', 'READ', '조회', 'GET', '/adm/api/cache/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_REFRESH', 'CACHE', 'REFRESH', '캐시 갱신', 'POST', '/adm/api/cache/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_EVICT_KEY', 'CACHE', 'EVICT_KEY', '단일 Cache 제거', 'POST', '/adm/api/cache/evict-key', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_EVICT_NAMESPACE', 'CACHE', 'EVICT_NAMESPACE', 'Namespace Cache 제거', 'POST', '/adm/api/cache/evict-namespace', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_RECONCILE', 'CACHE', 'RECONCILE', 'Cache Durable 재조정', 'POST', '/adm/api/cache/reconcile', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_READ', 'FILE_JOB', 'READ', 'File Job 조회', 'GET', '/adm/api/file-jobs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_UPLOAD', 'FILE_JOB', 'UPLOAD', 'Upload 접수', 'POST', '/adm/api/file-jobs/uploads', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_APPLY', 'FILE_JOB', 'APPLY', '검증 Job 적용', 'POST', '/adm/api/file-jobs/*/apply', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_RETRY', 'FILE_JOB', 'RETRY', 'File Job 재시도', 'POST', '/adm/api/file-jobs/*/retry', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_CANCEL', 'FILE_JOB', 'CANCEL', 'File Job 취소', 'POST', '/adm/api/file-jobs/*/cancel', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_ROLLBACK', 'FILE_JOB', 'ROLLBACK', 'File Job Rollback', 'POST', '/adm/api/file-jobs/*/rollback', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_DOWNLOAD', 'FILE_JOB', 'DOWNLOAD', 'Artifact 다운로드', 'GET', '/adm/api/file-jobs/*/artifact', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_RESOLVE', 'FILE_JOB', 'RESOLVE', '결과 불명 확정', 'POST', '/adm/api/file-jobs/*/resolve-unknown', 80, 'Y', 'SYSTEM', 'SYSTEM'),
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
    ('OPERATOR_ROLE_UPDATE', 'OPERATOR', 'ROLE_UPDATE', '역할 부여', 'PUT', '/adm/api/operators/*/roles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_STATUS_UPDATE', 'OPERATOR', 'STATUS_UPDATE', '계정 상태 변경', 'PUT', '/adm/api/operators/*/status', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_CONTACT_UPDATE', 'OPERATOR', 'CONTACT_UPDATE', '연락처 변경', 'PUT', '/adm/api/operators/*/contacts', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_PII_RAW', 'OPERATOR', 'PII_RAW', '연락처 원문 조회', 'POST', '/adm/api/operators/*/contacts/raw', 60, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET MENU_ID=EXCLUDED.MENU_ID, ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, SORT_ORDER=EXCLUDED.SORT_ORDER, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_PASSWORD_POLICY (POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by)
VALUES (
    'DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (POLICY_ID) DO UPDATE SET MIN_LENGTH=EXCLUDED.MIN_LENGTH, REQUIRE_UPPER_YN=EXCLUDED.REQUIRE_UPPER_YN, REQUIRE_LOWER_YN=EXCLUDED.REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN=EXCLUDED.REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN=EXCLUDED.REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT=EXCLUDED.MAX_FAIL_COUNT, EXPIRE_DAYS=EXCLUDED.EXPIRE_DAYS, HISTORY_LIMIT=EXCLUDED.HISTORY_LIMIT, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM ADM_MENU
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('BATCH', 'DOWNLOAD', 'CACHE', 'FILE_JOB') THEN 'Y' ELSE 'N' END,
       'N',
       'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'CAPABILITY_FLEET', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', MENU_ID, READ_YN, WRITE_YN, DELETE_YN, 'SYSTEM', 'SYSTEM'
FROM ADM_ROLE_MENU
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', BUTTON_ID,
       CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', BUTTON_ID,
       CASE
           WHEN BUTTON_ID IN ('BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH', 'FILE_JOB_UPLOAD', 'FILE_JOB_APPLY', 'FILE_JOB_DOWNLOAD') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', BUTTON_ID,
       CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', BUTTON_ID, ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM ADM_ROLE_BUTTON
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
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
FROM (
    SELECT b.*,
           ROW_NUMBER() OVER (
               PARTITION BY COALESCE(HTTP_METHOD, 'ANY'), API_PATTERN
               ORDER BY SORT_ORDER, BUTTON_ID
           ) AS CPF_ROUTE_OWNER_RANK
    FROM ADM_BUTTON b
    WHERE API_PATTERN IS NOT NULL
) route_owner
WHERE CPF_ROUTE_OWNER_RANK = 1
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_GROUP_CODE=EXCLUDED.API_GROUP_CODE, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, MENU_ID=EXCLUDED.MENU_ID, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (
    'API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE',
    'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_GROUP_CODE=EXCLUDED.API_GROUP_CODE, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, MENU_ID=EXCLUDED.MENU_ID, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID,
       ap.API_PERMISSION_ID,
       CASE WHEN MAX(rb.ALLOW_YN) = 'Y' THEN 'Y' ELSE 'N' END,
       'SYSTEM',
       'SYSTEM'
FROM ADM_ROLE_BUTTON rb
JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID
JOIN ADM_API_PERMISSION ap
  ON ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')
 AND ap.API_PATH = b.API_PATTERN
WHERE b.API_PATTERN IS NOT NULL
GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('AUDIT_LOG_RETRY','AUDIT_LOG','WRITE','감사 전달 재처리','POST','/adm/api/audit-logs/deliveries/*/retry',20,'Y','SYSTEM','SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
UPDATE ADM_ROLE_MENU SET WRITE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP WHERE MENU_ID='AUDIT_LOG' AND ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR');
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID,'AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM' FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES ('API_AUDIT_LOG_RETRY','AUDIT_LOG','POST','/adm/api/audit-logs/deliveries/*/retry','감사 전달 재처리','WRITE','AUDIT_LOG','AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM')
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID,'API_AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM' FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('SECRET_READ','SECRET','READ','Secret Metadata 조회','GET','/adm/api/secrets/**',10,'Y','SYSTEM','SYSTEM'),
 ('SECRET_ROTATE','SECRET','ROTATE','Secret Rotation','POST','/adm/api/secrets/rotate',20,'Y','SYSTEM','SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','SECRET','Y','Y','N','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','SECRET','Y','N','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','SECRET','Y','N','N','SYSTEM','SYSTEM'),
 ('ADM_VIEWER','SECRET','N','N','N','SYSTEM','SYSTEM'),
 ('ADM_BIZ_OPERATOR','SECRET','N','N','N','SYSTEM','SYSTEM')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_ADMIN','SECRET_ROTATE','Y','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_DEV_OPERATOR','SECRET_ROTATE','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_OPERATOR','SECRET_ROTATE','N','SYSTEM','SYSTEM')
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES ('API_SECRET_READ','SECRET','GET','/adm/api/secrets/**','Secret Metadata 조회','READ','SECRET','SECRET_READ','Y','SYSTEM','SYSTEM'),
 ('API_SECRET_ROTATE','SECRET','POST','/adm/api/secrets/rotate','Secret Rotation','ROTATE','SECRET','SECRET_ROTATE','Y','SYSTEM','SYSTEM')
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_ADMIN','API_SECRET_ROTATE','Y','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_DEV_OPERATOR','API_SECRET_ROTATE','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_OPERATOR','API_SECRET_ROTATE','N','SYSTEM','SYSTEM')
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('BAT_RUNTIME_VIEW','BATCH_RUNTIME','RUNTIME_VIEW','Runtime 조회','GET','/adm/api/batch-runtime/**',10,'Y','SYSTEM','SYSTEM'),
 ('BAT_RUNTIME_OPERATE','BATCH_INSTANCES','RUNTIME_OPERATE','Runtime Start/Stop/Drain','POST','/adm/api/approvals/**',20,'Y','SYSTEM','SYSTEM'),
 ('BAT_JOB_OPERATE','BATCH_EXECUTIONS','JOB_OPERATE','Job 실행/중지/재처리','POST','/adm/api/batch/**',30,'Y','SYSTEM','SYSTEM'),
 ('BAT_SCHEDULE_OPERATE','BATCH_SCHEDULER','SCHEDULE_OPERATE','Scheduler 운영','POST','/adm/api/batch/**',40,'Y','SYSTEM','SYSTEM'),
 ('BAT_WORKER_OPERATE','BATCH_WORKER_POOLS','WORKER_OPERATE','Worker Pool 운영','POST','/adm/api/approvals/**',50,'Y','SYSTEM','SYSTEM'),
 ('BAT_CENTER_CUT_OPERATE','BATCH_CENTER_CUT','CENTER_CUT_OPERATE','Center-Cut 재처리/조정','POST','/adm/api/batch-runtime/**',60,'Y','SYSTEM','SYSTEM'),
 ('BAT_AGENT_OPERATE','BATCH_AGENTS','AGENT_OPERATE','Host Agent 운영','POST','/adm/api/approvals/**',70,'Y','SYSTEM','SYSTEM'),
 ('BAT_DEPLOY_PLAN','BATCH_DEPLOYMENT','DEPLOY_PLAN','Deployment Plan 생성','POST','/adm/api/batch-runtime/deployment-plans',80,'Y','SYSTEM','SYSTEM'),
 ('BAT_DEPLOY_APPROVE','BATCH_DEPLOYMENT','DEPLOY_APPROVE','Deployment 승인','POST','/adm/api/approvals/**',90,'Y','SYSTEM','SYSTEM'),
 ('BAT_DEPLOY_EXECUTE','BATCH_DEPLOYMENT','DEPLOY_EXECUTE','Deployment 실행','POST','/adm/api/approvals/**',100,'Y','SYSTEM','SYSTEM'),
 ('BAT_ROLLBACK_EXECUTE','BATCH_DEPLOYMENT','ROLLBACK_EXECUTE','Rollback 실행','POST','/adm/api/approvals/**',110,'Y','SYSTEM','SYSTEM'),
 ('BAT_RECOVERY_OPERATE','BATCH_RECOVERY','RECOVERY_OPERATE','UNKNOWN_RESULT 조정','POST','/adm/api/batch-runtime/**',120,'Y','SYSTEM','SYSTEM'),
 ('BAT_SECURITY_AUDIT','BATCH_AUDIT','SECURITY_AUDIT','BAT 보안·감사 조회','GET','/adm/api/batch-runtime/views/audit',130,'Y','SYSTEM','SYSTEM'),
 ('BAT_EVIDENCE_DOWNLOAD','BATCH_AUDIT','EVIDENCE_DOWNLOAD','BAT Evidence 다운로드','GET','/adm/api/downloads/**',140,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_VIEW','BATCH_RUNTIME','RETENTION_VIEW','Retention 조회','GET','/adm/api/batch-runtime/retention/**',150,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_PREVIEW','BATCH_RUNTIME','RETENTION_PREVIEW','Retention Preview','POST','/adm/api/batch-runtime/retention/preview',160,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_POLICY_REQUEST','BATCH_RUNTIME','RETENTION_POLICY_REQUEST','Retention 정책 변경 승인요청','POST','/adm/api/batch-runtime/retention/policies',170,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_RUN_REQUEST','BATCH_RUNTIME','RETENTION_RUN_REQUEST','Retention 수동 실행 승인요청','POST','/adm/api/batch-runtime/retention/policies/*/run',180,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_RUN_PAUSE','BATCH_RUNTIME','RETENTION_RUN_PAUSE','Retention Run 안전 일시정지','POST','/adm/api/batch-runtime/retention/runs/*/pause',190,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_RUN_RESUME','BATCH_RUNTIME','RETENTION_RUN_RESUME','Retention Run 재개 승인요청','POST','/adm/api/batch-runtime/retention/runs/*/resume',200,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_POLICY_PAUSE','BATCH_RUNTIME','RETENTION_POLICY_PAUSE','Retention 정책 안전 일시정지','POST','/adm/api/batch-runtime/retention/policies/*/pause',210,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_POLICY_RESUME','BATCH_RUNTIME','RETENTION_POLICY_RESUME','Retention 정책 재개 승인요청','POST','/adm/api/batch-runtime/retention/policies/*/resume',220,'Y','SYSTEM','SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET MENU_ID=EXCLUDED.MENU_ID, ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, SORT_ORDER=EXCLUDED.SORT_ORDER, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT r.ROLE_ID,m.MENU_ID,'Y',
       CASE WHEN r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR') THEN 'Y' ELSE 'N' END,
       'N','SYSTEM','SYSTEM'
FROM ADM_ROLE r JOIN ADM_MENU m ON m.PARENT_MENU_ID='BATCH'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT r.ROLE_ID,b.BUTTON_ID,
       CASE
         WHEN r.ROLE_ID='ADM_ADMIN' THEN 'Y'
         WHEN r.ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND b.BUTTON_ID NOT IN ('BAT_DEPLOY_APPROVE','BAT_DEPLOY_EXECUTE','BAT_ROLLBACK_EXECUTE') THEN 'Y'
         WHEN r.ROLE_ID='ADM_BIZ_OPERATOR' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_JOB_OPERATE','BAT_WORKER_OPERATE','BAT_CENTER_CUT_OPERATE','BAT_SECURITY_AUDIT','BAT_EVIDENCE_DOWNLOAD') THEN 'Y'
         WHEN r.ROLE_ID='ADM_VIEWER' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_SECURITY_AUDIT') THEN 'Y'
         ELSE 'N' END,
       'SYSTEM','SYSTEM'
FROM ADM_ROLE r JOIN ADM_BUTTON b ON b.BUTTON_ID LIKE 'BAT_%'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
SELECT
    CONCAT('API_', BUTTON_ID),
    MENU_ID,
    COALESCE(HTTP_METHOD, 'ANY'),
    API_PATTERN,
    BUTTON_NAME,
    ACTION_CODE,
    MENU_ID,
    BUTTON_ID,
    'Y',
    'SYSTEM',
    'SYSTEM'
FROM (
    SELECT b.*,
           ROW_NUMBER() OVER (
               PARTITION BY COALESCE(HTTP_METHOD, 'ANY'), API_PATTERN
               ORDER BY SORT_ORDER, BUTTON_ID
           ) AS CPF_ROUTE_OWNER_RANK
    FROM ADM_BUTTON b
    WHERE BUTTON_ID LIKE 'BAT_%'
      AND API_PATTERN IS NOT NULL
) route_owner
WHERE CPF_ROUTE_OWNER_RANK = 1
  AND NOT EXISTS (
      SELECT 1
      FROM ADM_API_PERMISSION existing
      WHERE existing.HTTP_METHOD = COALESCE(route_owner.HTTP_METHOD, 'ANY')
        AND existing.API_PATH = route_owner.API_PATTERN
  )
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_GROUP_CODE=EXCLUDED.API_GROUP_CODE, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, MENU_ID=EXCLUDED.MENU_ID, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID,
       ap.API_PERMISSION_ID,
       CASE WHEN MAX(rb.ALLOW_YN) = 'Y' THEN 'Y' ELSE 'N' END,
       'SYSTEM',
       'SYSTEM'
FROM ADM_ROLE_BUTTON rb
JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID
JOIN ADM_API_PERMISSION ap
  ON ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')
 AND ap.API_PATH = b.API_PATTERN
WHERE rb.BUTTON_ID LIKE 'BAT_%'
  AND b.API_PATTERN IS NOT NULL
GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
