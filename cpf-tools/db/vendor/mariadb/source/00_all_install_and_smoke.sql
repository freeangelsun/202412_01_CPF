-- CPF generated SQL bundle: 00_all_install_and_smoke.sql
-- 목적: 제품 Object 설치, 제품 Seed 반영, read-only Verify(Provision/Optional/Test/Reset 제외)
-- 정본은 database-source-plan.json의 mariadb.sourceRoot 아래 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File cpf-tools/db/tools/build-all-install-sql.ps1 로 재생성합니다.
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=mariadb
-- DO NOT EDIT generated DDL directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
CREATE TABLE IF NOT EXISTS ADM_APPROVAL_CAPABILITY_NONCE (
    NONCE_HASH CHAR(64) NOT NULL COMMENT 'single-use nonce SHA-256',
    APPROVAL_REFERENCE VARCHAR(240) NOT NULL COMMENT '승인 실행 reference',
    EXPIRES_AT DATETIME(3) NOT NULL COMMENT 'capability 만료시각',
    CONSUMED_AT DATETIME(3) NULL COMMENT 'single-use 소비시각',
    CONSUMED_BY VARCHAR(80) NULL COMMENT 'capability 소비 경계',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_CAPABILITY_NONCE PRIMARY KEY (NONCE_HASH),
    CONSTRAINT ck_adm_approval_cap_nonce_hash CHECK (CHAR_LENGTH(NONCE_HASH) = 64),
    INDEX ix_adm_approval_cap_nonce_expiry (EXPIRES_AT, CONSUMED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 승인 실행 HMAC capability cluster-safe single-use nonce ledger';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_POLICY (
    POLICY_CODE VARCHAR(80) NOT NULL COMMENT '위험조치 승인 정책 코드',
    POLICY_VERSION INT NOT NULL COMMENT '정책 버전',
    POLICY_NAME VARCHAR(150) NOT NULL COMMENT '정책명',
    ACTION_TYPE VARCHAR(80) NOT NULL COMMENT '대상 위험조치 유형',
    EFFECTIVE_FROM DATETIME(3) NOT NULL COMMENT '시행 시작시각',
    EFFECTIVE_TO DATETIME(3) NULL COMMENT '시행 종료시각',
    ENABLED_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    SELF_APPROVAL_ALLOWED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '자기승인 허용 여부',
    BREAK_GLASS_ALLOWED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '긴급 우회 허용 여부',
    DESCRIPTION VARCHAR(1000) NULL COMMENT '정책 설명',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_POLICY PRIMARY KEY (POLICY_CODE, POLICY_VERSION),
    CONSTRAINT ck_adm_approval_policy_version CHECK (POLICY_VERSION > 0),
    CONSTRAINT ck_adm_approval_policy_flags CHECK (ENABLED_YN IN ('Y','N') AND SELF_APPROVAL_ALLOWED_YN IN ('Y','N') AND BREAK_GLASS_ALLOWED_YN IN ('Y','N')),
    CONSTRAINT ck_adm_approval_policy_effective CHECK (EFFECTIVE_TO IS NULL OR EFFECTIVE_TO > EFFECTIVE_FROM),
    INDEX ix_adm_approval_policy_action (ACTION_TYPE, ENABLED_YN, EFFECTIVE_FROM, EFFECTIVE_TO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 위험조치 승인 정책 Version';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_POLICY_LOCK (
    LOCK_BUCKET INT NOT NULL COMMENT '승인 정책 actionType 직렬화 lock bucket (0..63)',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_POLICY_LOCK PRIMARY KEY (LOCK_BUCKET),
    CONSTRAINT ck_adm_approval_policy_lock_bucket CHECK (LOCK_BUCKET BETWEEN 0 AND 63)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 승인 정책 활성 유효기간 동시 생성 직렬화 lock bucket';

CREATE TABLE IF NOT EXISTS ADM_AUDIT_DELIVERY (
    DELIVERY_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Delivery identifier',
    TRANSACTION_ID CHAR(34) NOT NULL COMMENT 'CPF 전역 transactionId',
    TRACE_ID VARCHAR(64) NULL COMMENT 'Trace identifier',
    OPERATOR_ID VARCHAR(100) NOT NULL COMMENT 'Operator identifier',
    ACTION_TYPE VARCHAR(100) NOT NULL COMMENT 'Action type',
    TARGET_TYPE VARCHAR(100) NULL COMMENT 'Target type',
    TARGET_ID VARCHAR(255) NULL COMMENT 'Target identifier',
    REASON VARCHAR(1000) NOT NULL COMMENT 'Reason',
    BEFORE_DATA LONGTEXT NULL COMMENT 'Before data',
    AFTER_DATA LONGTEXT NULL COMMENT 'After data',
    DIFF_DATA LONGTEXT NULL COMMENT 'Change data',
    CLIENT_IP VARCHAR(64) NULL COMMENT 'Client IP',
    OPERATION_STATUS VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT 'Operation status',
    DELIVERY_STATUS VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Delivery status',
    ATTEMPT_COUNT INT NOT NULL DEFAULT 0 COMMENT 'Attempt count',
    MAX_ATTEMPTS INT NOT NULL DEFAULT 10 COMMENT 'Max attempts',
    NEXT_ATTEMPT_AT DATETIME(3) NULL COMMENT 'Next attempt time',
    LAST_ERROR VARCHAR(1000) NULL COMMENT 'Last error',
    AUDIT_ID BIGINT NULL COMMENT 'Audit identifier',
    REQUESTED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Request time',
    DELIVERED_AT DATETIME(3) NULL COMMENT 'Delivery time',
    CREATED_BY VARCHAR(100) NOT NULL COMMENT 'Creator',
    UPDATED_BY VARCHAR(100) NOT NULL COMMENT 'Last updater',
    CREATED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    UPDATED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    CONSTRAINT pk_ADM_AUDIT_DELIVERY PRIMARY KEY (DELIVERY_ID),
    CONSTRAINT ck_adm_audit_delivery_operation CHECK (OPERATION_STATUS IN ('REQUESTED','SUCCEEDED','FAILED','UNKNOWN')),
    CONSTRAINT ck_adm_audit_delivery_status CHECK (DELIVERY_STATUS IN ('PENDING','RETRY','FAILED','DELIVERED')),
    INDEX ix_adm_audit_delivery_status (DELIVERY_STATUS, OPERATION_STATUS, NEXT_ATTEMPT_AT),
    INDEX ix_adm_audit_delivery_tx (TRANSACTION_ID),
    INDEX ix_adm_audit_delivery_operator (OPERATOR_ID, REQUESTED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 필수 감사 Delivery 원장';

CREATE TABLE IF NOT EXISTS ADM_AUDIT_LOG (
    AUDIT_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '감사 로그 순번',
    TRANSACTION_ID CHAR(34) NULL COMMENT 'CPF 전역 transactionId',
    TRACE_ID VARCHAR(80) NULL COMMENT '분산 추적 ID',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 ID',
    BUTTON_ID VARCHAR(80) NULL COMMENT '버튼/행위 ID',
    ACTION_TYPE VARCHAR(30) NOT NULL COMMENT '행위 유형',
    TARGET_TYPE VARCHAR(50) NULL COMMENT '대상 유형',
    TARGET_ID VARCHAR(100) NULL COMMENT '대상 ID',
    REASON VARCHAR(500) NOT NULL COMMENT '감사 사유',
    BEFORE_DATA LONGTEXT NULL COMMENT '변경 전 데이터',
    AFTER_DATA LONGTEXT NULL COMMENT '변경 후 데이터',
    DIFF_DATA LONGTEXT NULL COMMENT '변경 차이 데이터',
    REQUEST_BODY LONGTEXT NULL COMMENT '요청 본문',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    RETENTION_UNTIL DATE NULL COMMENT '보존 만료 기준일',
    IMMUTABLE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '삭제 불가 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_AUDIT_LOG PRIMARY KEY (AUDIT_ID),
    INDEX ix_adm_audit_log_tx (TRANSACTION_ID),
    INDEX ix_adm_audit_log_operator_time (OPERATOR_ID, created_at),
    INDEX ix_adm_audit_log_action_time (ACTION_TYPE, created_at),
    INDEX ix_adm_audit_log_target_time (TARGET_TYPE, TARGET_ID, created_at),
    INDEX ix_adm_audit_log_retention (RETENTION_UNTIL, IMMUTABLE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 감사 로그';

CREATE TABLE IF NOT EXISTS ADM_BREAK_GLASS_SESSION (
    session_id CHAR(36) NOT NULL COMMENT '비상 권한 세션 UUID',
    operator_id VARCHAR(100) NOT NULL COMMENT '세션 소유 운영자',
    scope_type VARCHAR(60) NOT NULL COMMENT 'SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY 등 좁은 Scope 종류',
    scope_value VARCHAR(200) NOT NULL COMMENT 'Scope 대상 식별자',
    reason VARCHAR(1000) NOT NULL COMMENT '발급 사유',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED/EXPIRED',
    expires_at DATETIME(3) NOT NULL COMMENT '강제 만료시각',
    closed_at DATETIME(3) NULL COMMENT '종료/만료시각',
    close_reason VARCHAR(1000) NULL COMMENT '종료/만료 사유',
    post_review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    reviewed_by VARCHAR(100) NULL COMMENT '사후검토자',
    reviewed_at DATETIME(3) NULL COMMENT '사후검토시각',
    review_reason VARCHAR(1000) NULL COMMENT '사후검토 의견',
    created_by VARCHAR(100) NOT NULL COMMENT 'Creator',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL COMMENT 'Last updater',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    CONSTRAINT pk_ADM_BREAK_GLASS_SESSION PRIMARY KEY (session_id),
    CONSTRAINT ck_adm_break_glass_status CHECK (status IN ('ACTIVE','CLOSED','EXPIRED')),
    CONSTRAINT ck_adm_break_glass_review CHECK (post_review_status IN ('PENDING','APPROVED','REJECTED')),
    INDEX ix_adm_break_glass_operator (operator_id, status, expires_at),
    INDEX ix_adm_break_glass_scope (scope_type, scope_value, status),
    INDEX ix_adm_break_glass_review (post_review_status, closed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 범위/TTL 제한 Break-glass 세션';

CREATE TABLE IF NOT EXISTS ADM_DOWNLOAD_AUDIT_LOG (
    DOWNLOAD_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '다운로드 감사 로그 순번',
    ADMIN_ID VARCHAR(50) NOT NULL COMMENT '요청 운영자 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 ID',
    SCREEN_ID VARCHAR(100) NULL COMMENT '화면 ID',
    DOWNLOAD_TYPE VARCHAR(50) NOT NULL COMMENT '다운로드 유형',
    TARGET_TYPE VARCHAR(50) NULL COMMENT '대상 유형',
    SEARCH_CONDITION_SUMMARY LONGTEXT NULL COMMENT '검색 조건 요약',
    ROW_COUNT INT NOT NULL DEFAULT 0 COMMENT '다운로드 행 수',
    MASKED_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '마스킹 적용 여부',
    INCLUDE_SENSITIVE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감정보 포함 요청 여부',
    REASON VARCHAR(500) NOT NULL COMMENT '다운로드 사유',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    USER_AGENT VARCHAR(500) NULL COMMENT 'User-Agent',
    CSV_POLICY_VERSION VARCHAR(30) NOT NULL DEFAULT 'CPF-CSV-1' COMMENT 'CSV spreadsheet injection protection policy version',
    REQUESTED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '요청 일시',
    COMPLETED_AT DATETIME NULL COMMENT '완료 일시',
    STATUS VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '처리 상태',
    FAILURE_REASON VARCHAR(1000) NULL COMMENT '실패 사유',
    FILE_NAME VARCHAR(300) NULL COMMENT '파일명',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_DOWNLOAD_AUDIT_LOG PRIMARY KEY (DOWNLOAD_ID),
    INDEX ix_adm_download_audit_log_admin_time (ADMIN_ID, REQUESTED_AT),
    INDEX ix_adm_download_audit_log_type_time (DOWNLOAD_TYPE, REQUESTED_AT),
    INDEX ix_adm_download_audit_log_status_time (STATUS, REQUESTED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 다운로드 감사 로그';

CREATE TABLE IF NOT EXISTS ADM_DYNAMIC_LOG_LEVEL_RULE (
    RULE_ID VARCHAR(80) NOT NULL COMMENT '동적 로그 레벨 규칙 ID',
    TRANSACTION_ID CHAR(34) NULL COMMENT '프레임워크 거래 ID',
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL COMMENT '업무 거래 ID',
    MODULE_ID VARCHAR(10) NULL COMMENT '모듈 ID',
    LOG_LEVEL VARCHAR(10) NOT NULL COMMENT '적용 로그 레벨',
    EXPIRE_AT DATETIME NOT NULL COMMENT '만료일시',
    REASON VARCHAR(500) NULL COMMENT '적용 사유',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_DYNAMIC_LOG_LEVEL_RULE PRIMARY KEY (RULE_ID),
    INDEX ix_adm_dynamic_log_level_rule_biz_tx (BUSINESS_TRANSACTION_ID, EXPIRE_AT),
    INDEX ix_adm_dynamic_log_level_rule_tx (TRANSACTION_ID, EXPIRE_AT),
    INDEX ix_adm_dynamic_log_level_rule_active (USE_YN, EXPIRE_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 동적 로그 레벨 규칙';

CREATE TABLE IF NOT EXISTS ADM_FILE_JOB (
    job_id VARCHAR(36) NOT NULL COMMENT 'File Job ID',
    operation_id VARCHAR(100) NOT NULL COMMENT '멱등 Operation ID',
    request_hash VARCHAR(64) NOT NULL COMMENT '요청 SHA-256',
    job_type VARCHAR(20) NOT NULL COMMENT 'UPLOAD 또는 DOWNLOAD',
    template_code VARCHAR(100) NOT NULL COMMENT 'Template Code',
    template_version INT NOT NULL COMMENT 'Template Version',
    file_format VARCHAR(10) NOT NULL COMMENT 'CSV 또는 XLSX',
    job_state VARCHAR(30) NOT NULL COMMENT 'Job 상태',
    dry_run CHAR(1) NOT NULL COMMENT 'Dry-run 여부',
    rollback_supported CHAR(1) NOT NULL COMMENT 'Rollback 지원 여부',
    source_path VARCHAR(1000) NULL COMMENT 'Source Artifact 경로',
    result_path VARCHAR(1000) NULL COMMENT 'Result Artifact 경로',
    source_sha256 VARCHAR(64) NULL COMMENT 'Source SHA-256',
    result_sha256 VARCHAR(64) NULL COMMENT 'Result SHA-256',
    total_rows BIGINT NOT NULL DEFAULT 0 COMMENT '전체 행 수',
    success_rows BIGINT NOT NULL DEFAULT 0 COMMENT '성공 행 수',
    failed_rows BIGINT NOT NULL DEFAULT 0 COMMENT '실패 행 수',
    lease_owner VARCHAR(100) NULL COMMENT 'Lease 소유자',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Fencing Token',
    lease_until DATETIME(6) NULL COMMENT 'Lease 만료',
    retention_until DATETIME(6) NOT NULL COMMENT 'Artifact 보존 만료',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청 운영자',
    reason VARCHAR(500) NOT NULL COMMENT '요청 사유',
    client_ip VARCHAR(64) NULL COMMENT '요청 Client IP',
    error_code VARCHAR(80) NULL COMMENT '오류 코드',
    error_message VARCHAR(1000) NULL COMMENT '마스킹된 오류 메시지',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    approval_id VARCHAR(120) NULL COMMENT '승인 ID',
    applied_by VARCHAR(100) NULL COMMENT '적용 운영자',
    resolved_by VARCHAR(100) NULL COMMENT '결과 불명 확정 운영자',
    control_by VARCHAR(100) NULL COMMENT '최근 제어 운영자',
    control_reason VARCHAR(500) NULL COMMENT '최근 제어 사유',
    control_updated_at DATETIME(6) NULL COMMENT '최근 제어 시각',
    CONSTRAINT pk_ADM_FILE_JOB PRIMARY KEY (job_id),
    CONSTRAINT uk_adm_file_job_operation UNIQUE (operation_id),
    INDEX ix_adm_file_job_claim (job_state, lease_until, created_at),
    INDEX ix_adm_file_job_retention (retention_until, job_state),
    INDEX ix_adm_file_job_approval (approval_id, job_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 비동기 대량 File Job 원장';

CREATE TABLE IF NOT EXISTS ADM_INCIDENT (
    incident_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Incident identifier',
    incident_no VARCHAR(64) NOT NULL COMMENT 'Incident number',
    severity VARCHAR(16) NOT NULL COMMENT 'Severity',
    title VARCHAR(300) NOT NULL COMMENT 'Title',
    summary VARCHAR(2000) NULL COMMENT 'Summary',
    source_type VARCHAR(40) NOT NULL DEFAULT 'MANUAL' COMMENT 'Source type',
    source_id VARCHAR(200) NULL COMMENT 'Source identifier',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN' COMMENT 'Status',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Detection time',
    acknowledged_at DATETIME(3) NULL COMMENT 'Acknowledgement time',
    mitigated_at DATETIME(3) NULL COMMENT 'Mitigation time',
    resolved_at DATETIME(3) NULL COMMENT 'Resolution time',
    reason VARCHAR(1000) NOT NULL COMMENT 'Reason',
    version BIGINT NOT NULL DEFAULT 0 COMMENT 'Version',
    created_by VARCHAR(100) NOT NULL COMMENT 'Creator',
    updated_by VARCHAR(100) NOT NULL COMMENT 'Last updater',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    CONSTRAINT pk_ADM_INCIDENT PRIMARY KEY (incident_id),
    CONSTRAINT uk_adm_incident_no UNIQUE (incident_no),
    INDEX idx_adm_incident_status (status, severity, detected_at),
    INDEX idx_adm_incident_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Incident Lifecycle';

CREATE TABLE IF NOT EXISTS ADM_INCIDENT_COMMAND (
    command_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Incident Command ID',
    command_type VARCHAR(80) NOT NULL COMMENT 'Command 유형',
    idempotency_key VARCHAR(160) NOT NULL COMMENT '멱등 키',
    request_hash VARCHAR(64) NOT NULL COMMENT '요청 SHA-256',
    status VARCHAR(30) NOT NULL COMMENT 'Command 상태',
    result_ref VARCHAR(200) NULL COMMENT '결과 참조',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_INCIDENT_COMMAND PRIMARY KEY (command_id),
    CONSTRAINT uk_adm_incident_command_idem UNIQUE (idempotency_key),
    CONSTRAINT ck_adm_incident_command_status CHECK (status IN ('RUNNING','DONE')),
    INDEX ix_adm_incident_command_status (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Notification Incident 멱등 Command 원장';

CREATE TABLE IF NOT EXISTS ADM_INCIDENT_POLICY (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Incident 정책 ID',
    policy_code VARCHAR(100) NOT NULL COMMENT 'Incident 정책 코드',
    event_type VARCHAR(80) NOT NULL COMMENT '이벤트 유형',
    event_sub_type VARCHAR(80) NULL COMMENT '이벤트 상세 유형',
    severity VARCHAR(20) NOT NULL COMMENT '심각도',
    threshold_count INT NOT NULL COMMENT '임계 건수',
    window_seconds INT NOT NULL COMMENT '평가 구간(초)',
    escalation_minutes INT NOT NULL COMMENT 'Escalation 기준(분)',
    receiver_group VARCHAR(100) NOT NULL COMMENT '수신 그룹',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_INCIDENT_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT uk_adm_incident_policy_code UNIQUE (policy_code),
    CONSTRAINT ck_adm_incident_policy_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_adm_incident_policy_threshold CHECK (threshold_count > 0),
    CONSTRAINT ck_adm_incident_policy_window CHECK (window_seconds > 0),
    CONSTRAINT ck_adm_incident_policy_escalation CHECK (escalation_minutes > 0),
    INDEX ix_adm_incident_policy_event (event_type, event_sub_type, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Notification Incident 정책';

CREATE TABLE IF NOT EXISTS ADM_IP_ALLOWLIST (
    ALLOW_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'IP 허용 목록 순번',
    IP_PATTERN VARCHAR(100) NOT NULL COMMENT '허용 IP 또는 CIDR 패턴',
    DESCRIPTION VARCHAR(500) NULL COMMENT '허용 사유',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_IP_ALLOWLIST PRIMARY KEY (ALLOW_ID),
    CONSTRAINT uk_adm_ip_allowlist_pattern UNIQUE (IP_PATTERN),
    INDEX ix_adm_ip_allowlist_use (USE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 관리자 IP 허용 목록';

CREATE TABLE IF NOT EXISTS ADM_LOG_EXPORT_ARTIFACT (
    export_id VARCHAR(64) NOT NULL COMMENT 'Export ID',
    owner_operator_id VARCHAR(100) NOT NULL COMMENT '소유 운영자',
    file_name VARCHAR(255) NOT NULL COMMENT '다운로드 파일명',
    content_type VARCHAR(100) NOT NULL COMMENT 'Content Type',
    artifact_content LONGBLOB NOT NULL COMMENT '마스킹된 Artifact 본문',
    content_length BIGINT NOT NULL COMMENT '본문 길이',
    created_at DATETIME(3) NOT NULL COMMENT '생성 시각',
    expires_at DATETIME(3) NOT NULL COMMENT '만료 시각',
    status_code VARCHAR(20) NOT NULL COMMENT 'READY/REVOKED',
    downloaded_at DATETIME(3) NULL COMMENT '마지막 다운로드 시각',
    download_count BIGINT NOT NULL DEFAULT 0 COMMENT '다운로드 횟수',
    CONSTRAINT pk_ADM_LOG_EXPORT_ARTIFACT PRIMARY KEY (export_id),
    CONSTRAINT ck_adm_log_export_status CHECK (status_code IN ('READY','REVOKED')),
    INDEX ix_adm_log_export_expiry (expires_at),
    INDEX ix_adm_log_export_owner (owner_operator_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 다중 인스턴스 공용 로그 Export Artifact';

CREATE TABLE IF NOT EXISTS ADM_MAINTENANCE_ACTION (
    action_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Action identifier',
    service_id VARCHAR(100) NOT NULL COMMENT 'Service identifier',
    endpoint_code VARCHAR(100) NOT NULL COMMENT 'Endpoint code',
    instance_id VARCHAR(150) NOT NULL COMMENT 'Instance identifier',
    action_type VARCHAR(20) NOT NULL COMMENT 'Action type',
    before_status VARCHAR(40) NULL COMMENT 'Before status',
    after_status VARCHAR(40) NULL COMMENT 'After status',
    result_status VARCHAR(20) NOT NULL COMMENT 'Result status',
    reason VARCHAR(1000) NOT NULL COMMENT 'Reason',
    requested_by VARCHAR(100) NOT NULL COMMENT 'Requester',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Request time',
    result_detail TEXT NULL COMMENT 'Result detail',
    CONSTRAINT pk_ADM_MAINTENANCE_ACTION PRIMARY KEY (action_id),
    INDEX idx_adm_maintenance_target (service_id, endpoint_code, instance_id, requested_at),
    INDEX idx_adm_maintenance_result (result_status, requested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Maintenance Command Audit';

CREATE TABLE IF NOT EXISTS ADM_MAINTENANCE_WINDOW (
    maintenance_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Maintenance Window ID',
    maintenance_code VARCHAR(100) NOT NULL COMMENT 'Maintenance 코드',
    target_type VARCHAR(80) NOT NULL COMMENT '대상 유형',
    target_id VARCHAR(160) NOT NULL COMMENT '대상 ID',
    starts_at DATETIME(3) NOT NULL COMMENT '시작일시',
    ends_at DATETIME(3) NOT NULL COMMENT '종료일시',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_MAINTENANCE_WINDOW PRIMARY KEY (maintenance_id),
    CONSTRAINT uk_adm_maintenance_code UNIQUE (maintenance_code),
    CONSTRAINT ck_adm_maintenance_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_adm_maintenance_period CHECK (ends_at > starts_at),
    INDEX ix_adm_maintenance_active (use_yn, starts_at, ends_at),
    INDEX ix_adm_maintenance_target (target_type, target_id, starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Maintenance Window';

CREATE TABLE IF NOT EXISTS ADM_MENU (
    MENU_ID VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    PARENT_MENU_ID VARCHAR(50) NULL COMMENT '상위 메뉴 ID',
    MENU_NAME VARCHAR(100) NOT NULL COMMENT '메뉴명',
    MENU_PATH VARCHAR(200) NOT NULL COMMENT '메뉴 경로',
    SORT_ORDER INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_MENU PRIMARY KEY (MENU_ID),
    CONSTRAINT fk_adm_menu_parent FOREIGN KEY (PARENT_MENU_ID) REFERENCES ADM_MENU (MENU_ID) ON DELETE SET NULL,
    INDEX ix_adm_menu_parent (PARENT_MENU_ID, SORT_ORDER)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 메뉴';

CREATE TABLE IF NOT EXISTS ADM_OPERATOR (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    OPERATOR_NAME VARCHAR(100) NOT NULL COMMENT '운영자명',
    PASSWORD_HASH VARCHAR(512) NOT NULL COMMENT '비밀번호 해시',
    ACCOUNT_STATUS VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION' COMMENT '계정 상태: PENDING_ACTIVATION/ACTIVE/LOCKED/SUSPENDED/DISABLED',
    VERSION_NO BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    CREATE_OPERATION_ID VARCHAR(100) NULL COMMENT '운영자 생성 멱등 Operation ID',
    LOCKED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 여부',
    FAIL_COUNT INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    PASSWORD_CHANGED_AT DATETIME NULL COMMENT '비밀번호 변경일시',
    PASSWORD_EXPIRE_AT DATETIME NULL COMMENT '비밀번호 만료일시',
    PASSWORD_CHANGE_REQUIRED_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '비밀번호 변경 필요 여부',
    LAST_LOGIN_AT DATETIME NULL COMMENT '마지막 로그인 일시',
    LAST_LOGIN_IP VARCHAR(50) NULL COMMENT '마지막 로그인 IP',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_OPERATOR PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT uk_adm_operator_create_operation UNIQUE (CREATE_OPERATION_ID),
    CONSTRAINT ck_adm_operator_status CHECK (ACCOUNT_STATUS IN ('PENDING_ACTIVATION','ACTIVE','LOCKED','SUSPENDED','DISABLED')),
    INDEX ix_adm_operator_use (USE_YN),
    INDEX ix_adm_operator_status (ACCOUNT_STATUS, USE_YN),
    INDEX ix_adm_operator_lock (LOCKED_YN, FAIL_COUNT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자';

CREATE TABLE IF NOT EXISTS ADM_OPERATOR_SESSION (
    SESSION_ID VARCHAR(80) NOT NULL COMMENT '세션 ID',
    TOKEN_HASH VARCHAR(512) NOT NULL COMMENT '토큰 해시',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    ROLE_IDS VARCHAR(1000) NULL COMMENT '역할 ID 목록',
    ISSUED_AT DATETIME NOT NULL COMMENT '발급일시',
    EXPIRE_AT DATETIME NOT NULL COMMENT '만료일시',
    REVOKED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '폐기 여부',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    USER_AGENT VARCHAR(500) NULL COMMENT 'User-Agent',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_OPERATOR_SESSION PRIMARY KEY (SESSION_ID),
    INDEX ix_adm_operator_session_token (TOKEN_HASH),
    INDEX ix_adm_operator_session_user (OPERATOR_ID, EXPIRE_AT),
    INDEX ix_adm_operator_session_active (REVOKED_YN, EXPIRE_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 세션';

CREATE TABLE IF NOT EXISTS ADM_PASSWORD_POLICY (
    POLICY_ID VARCHAR(50) NOT NULL COMMENT '비밀번호 정책 ID',
    MIN_LENGTH INT NOT NULL DEFAULT 12 COMMENT '최소 길이',
    REQUIRE_UPPER_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '대문자 필수 여부',
    REQUIRE_LOWER_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '소문자 필수 여부',
    REQUIRE_DIGIT_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '숫자 필수 여부',
    REQUIRE_SPECIAL_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '특수문자 필수 여부',
    MAX_FAIL_COUNT INT NOT NULL DEFAULT 5 COMMENT '최대 실패 횟수',
    EXPIRE_DAYS INT NOT NULL DEFAULT 90 COMMENT '만료 일수',
    HISTORY_LIMIT INT NOT NULL DEFAULT 5 COMMENT '재사용 금지 이력 수',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_PASSWORD_POLICY PRIMARY KEY (POLICY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 비밀번호 정책';

CREATE TABLE IF NOT EXISTS ADM_ROLE (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    ROLE_NAME VARCHAR(100) NOT NULL COMMENT '역할명',
    ROLE_TYPE VARCHAR(30) NOT NULL DEFAULT 'BUSINESS_OPERATOR' COMMENT '역할 유형',
    DESCRIPTION VARCHAR(500) NULL COMMENT '역할 설명',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_ROLE PRIMARY KEY (ROLE_ID),
    INDEX ix_adm_role_type (ROLE_TYPE, USE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할';

CREATE TABLE IF NOT EXISTS BAT_APPROVED_LAUNCH (
    approval_id VARCHAR(120) NOT NULL COMMENT '승인된 Launch Request 식별자',
    job_id VARCHAR(80) NOT NULL COMMENT '승인 대상 Batch Job ID',
    definition_version BIGINT NOT NULL COMMENT '승인 대상 불변 정의 Version',
    definition_checksum CHAR(64) NOT NULL COMMENT '승인 대상 정의 SHA-256',
    approval_status VARCHAR(20) NOT NULL COMMENT '승인 Lifecycle 상태',
    launch_request_json LONGTEXT NOT NULL COMMENT '서명·검증된 불변 Launch Request JSON',
    effective_from DATETIME(6) NOT NULL COMMENT '승인 효력 시작 시각',
    effective_until DATETIME(6) NULL COMMENT '승인 효력 종료 시각',
    approved_by VARCHAR(120) NOT NULL COMMENT '승인자 식별자',
    approved_at DATETIME(6) NOT NULL COMMENT '승인 시각',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '승인 상태 낙관적 잠금 Version',
    CONSTRAINT pk_BAT_APPROVED_LAUNCH PRIMARY KEY (approval_id),
    CONSTRAINT uk_cpf_bat_approved_def UNIQUE (job_id, definition_version, definition_checksum),
    CONSTRAINT ck_cpf_bat_approval_status CHECK (approval_status IN ('APPROVED', 'REVOKED', 'EXPIRED')),
    CONSTRAINT ck_cpf_bat_approval_version CHECK (row_version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사전 승인된 불변 Spring Batch Launch Request';

CREATE TABLE IF NOT EXISTS BAT_DEPLOYMENT_CELL (
    cell_id VARCHAR(120) NULL COMMENT 'Deployment cell identifier',
    environment_id VARCHAR(80) NOT NULL COMMENT 'Target environment identifier',
    runtime_role VARCHAR(40) NOT NULL COMMENT 'Target runtime role',
    service_id VARCHAR(120) NOT NULL COMMENT 'Target service identifier',
    manifest_version VARCHAR(80) NOT NULL COMMENT 'Desired manifest version',
    manifest_hash VARCHAR(128) NOT NULL COMMENT 'Desired manifest checksum',
    desired_state VARCHAR(32) NOT NULL COMMENT 'Desired cell state',
    desired_instance_count INT NOT NULL DEFAULT 1 COMMENT 'Desired runtime instance count',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Cell registration time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last desired-state update time',
    CONSTRAINT pk_BAT_DEPLOYMENT_CELL PRIMARY KEY (cell_id),
    CONSTRAINT ck_bat_deployment_runtime_role CHECK (BINARY runtime_role IN ('CONTROL_PLANE','SCHEDULER','WORKER','CENTER_CUT_RUNNER','AGENT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment cell desired state';

CREATE TABLE IF NOT EXISTS BAT_DEPLOYMENT_LOCK (
    cell_id VARCHAR(120) NULL COMMENT 'Locked deployment cell identifier',
    owner_deployment_id VARCHAR(80) NOT NULL COMMENT 'Lock owner deployment identifier',
    fencing_token BIGINT NOT NULL COMMENT 'Monotonic deployment fencing token',
    locked_at DATETIME(6) NOT NULL COMMENT 'Lock acquisition time',
    expires_at DATETIME(6) NOT NULL COMMENT 'Lock expiry time',
    CONSTRAINT pk_BAT_DEPLOYMENT_LOCK PRIMARY KEY (cell_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment cell lease lock';

CREATE TABLE IF NOT EXISTS BAT_DEPLOYMENT_PLAN (
    plan_id VARCHAR(80) NULL COMMENT 'Deployment plan identifier',
    cell_id VARCHAR(120) NOT NULL COMMENT 'Target deployment cell identifier',
    manifest_json LONGTEXT NOT NULL COMMENT 'Immutable deployment manifest snapshot',
    manifest_hash VARCHAR(128) NOT NULL COMMENT 'Deployment manifest checksum',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Plan requester',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory deployment reason',
    plan_state VARCHAR(40) NOT NULL COMMENT 'Deployment plan lifecycle state',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Plan request time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last plan state update time',
    CONSTRAINT pk_BAT_DEPLOYMENT_PLAN PRIMARY KEY (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment plan';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_CONTROL (
    cpf_execution_id VARCHAR(80) NOT NULL COMMENT 'CPF Batch 실행 식별자',
    job_id VARCHAR(80) NOT NULL COMMENT 'Batch Job ID',
    definition_version BIGINT NOT NULL COMMENT '실행에 고정된 정의 Version',
    approval_id VARCHAR(120) NOT NULL COMMENT '실행 승인 식별자',
    operator_id VARCHAR(120) NOT NULL COMMENT '실행 요청 운영자',
    reason VARCHAR(500) NOT NULL COMMENT '승인된 실행 사유',
    idempotency_key VARCHAR(200) NOT NULL COMMENT 'Scope 내부 실행 멱등 Key',
    fencing_token BIGINT NOT NULL COMMENT 'Control Plane Fencing Token',
    job_instance_id BIGINT NULL COMMENT 'Spring Batch JobInstance ID',
    job_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    control_status VARCHAR(40) NOT NULL COMMENT 'CPF 실행 Control 상태',
    unknown_reason VARCHAR(100) NULL COMMENT 'UNKNOWN_RESULT 판정 사유 Code',
    unknown_detail VARCHAR(4000) NULL COMMENT 'Masking된 UNKNOWN_RESULT 상세',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '실행 예약 생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '마지막 Control 상태 변경 시각',
    idempotency_scope VARCHAR(400) NOT NULL COMMENT '실행 멱등성 격리 Scope',
    request_hash CHAR(64) NOT NULL COMMENT 'Canonical Launch Request SHA-256',
    plan_checksum CHAR(64) NOT NULL COMMENT '검증된 실행 Plan SHA-256',
    control_version BIGINT NOT NULL DEFAULT 1 COMMENT 'Control 상태 CAS Version',
    reconcile_attempts INT NOT NULL DEFAULT 0 COMMENT 'UNKNOWN_RESULT 대사 시도 횟수',
    reconcile_after DATETIME(6) NULL COMMENT '다음 대사 가능 시각',
    last_error_code VARCHAR(100) NULL COMMENT '마지막 표준 오류 Code',
    last_error_detail VARCHAR(4000) NULL COMMENT 'Masking된 마지막 오류 상세',
    CONSTRAINT pk_BAT_EXECUTION_CONTROL PRIMARY KEY (cpf_execution_id),
    CONSTRAINT uk_cpf_bat_exec_idem_scope UNIQUE (idempotency_scope, idempotency_key),
    CONSTRAINT ck_cpf_bat_fencing_pos CHECK (fencing_token > 0),
    CONSTRAINT ck_cpf_bat_request_hash CHECK (request_hash REGEXP '^[0-9a-f]{64}$'),
    CONSTRAINT ck_cpf_bat_plan_hash CHECK (plan_checksum REGEXP '^[0-9a-f]{64}$'),
    CONSTRAINT ck_cpf_bat_control_version CHECK (control_version > 0),
    CONSTRAINT ck_cpf_bat_reconcile_attempt CHECK (reconcile_attempts >= 0),
    CONSTRAINT ck_cpf_bat_control_status CHECK (control_status IN ('RESERVED', 'STARTING', 'STARTED', 'STOPPING', 'STOPPED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT', 'ABANDONING', 'ABANDONED', 'REJECTED')),
    INDEX ix_cpf_bat_exec_job (job_id, definition_version, created_at),
    INDEX ix_cpf_bat_exec_sb (job_execution_id),
    INDEX ix_cpf_bat_exec_reconcile (control_status, reconcile_after, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 승인·멱등·Fencing 기반 Spring Batch 실행 Control Ledger';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_EPOCH (
    job_id VARCHAR(80) NOT NULL COMMENT 'Batch Job ID별 최신 Fencing Epoch 식별자',
    current_fencing_token BIGINT NOT NULL COMMENT '현재 유효한 최신 Fencing Token',
    epoch_version BIGINT NOT NULL DEFAULT 1 COMMENT 'Epoch 낙관적 잠금 버전',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '최신 Epoch 변경 시각',
    CONSTRAINT pk_BAT_EXECUTION_EPOCH PRIMARY KEY (job_id),
    CONSTRAINT ck_cpf_batch_execution_epoch_token CHECK (current_fencing_token > 0),
    CONSTRAINT ck_cpf_batch_execution_epoch_version CHECK (epoch_version > 0),
    INDEX ix_cpf_batch_execution_epoch_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Batch Job별 최신 Fencing Epoch Ledger';

CREATE TABLE IF NOT EXISTS BAT_INSTANCE (
    instance_id VARCHAR(100) NOT NULL COMMENT '배치 인스턴스 ID',
    instance_name VARCHAR(150) NOT NULL COMMENT '배치 인스턴스명',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    server_port INT NULL COMMENT '서버 포트',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    description VARCHAR(500) NULL COMMENT '인스턴스 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_INSTANCE PRIMARY KEY (instance_id),
    INDEX ix_bat_instance_active (active_yn, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 서버 인스턴스';

CREATE TABLE IF NOT EXISTS BAT_JOB_DEFINITION_AUDIT (
    audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '감사 ID',
    job_id VARCHAR(100) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    action_code VARCHAR(40) NOT NULL COMMENT '행위',
    from_state VARCHAR(20) NULL COMMENT '이전 상태',
    to_state VARCHAR(20) NULL COMMENT '다음 상태',
    reason VARCHAR(1000) NOT NULL COMMENT '사유',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발생시각',
    requested_by VARCHAR(100) NULL COMMENT '승인 대상 변경 요청자',
    approval_request_id VARCHAR(120) NULL COMMENT '검증된 승인 요청 식별자',
    transaction_id CHAR(34) NULL COMMENT '운영 명령 Transaction ID',
    trace_id VARCHAR(64) NULL COMMENT '분산 추적 Trace ID',
    before_json MEDIUMTEXT NULL COMMENT '마스킹된 변경 전 Definition',
    after_json MEDIUMTEXT NULL COMMENT '마스킹된 변경 후 Definition',
    CONSTRAINT pk_BAT_JOB_DEFINITION_AUDIT PRIMARY KEY (audit_id),
    INDEX idx_bat_job_def_audit (job_id, definition_version, created_at),
    INDEX ix_bat_job_definition_audit_approval (approval_request_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Job Definition 승인·상태 감사';

CREATE TABLE IF NOT EXISTS BAT_JOB_DEFINITION_VERSION (
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    definition_version BIGINT NOT NULL COMMENT '불변 Definition Version',
    job_name VARCHAR(200) NOT NULL COMMENT '배치 Job 이름',
    executor_type VARCHAR(40) NOT NULL COMMENT 'Executor 유형',
    definition_state VARCHAR(20) NOT NULL COMMENT 'Definition 상태',
    owner_domain VARCHAR(80) NOT NULL COMMENT '소유 업무영역',
    description VARCHAR(1000) NULL COMMENT '설명',
    trigger_type VARCHAR(30) NOT NULL COMMENT 'Trigger 유형',
    trigger_expression VARCHAR(500) NULL COMMENT 'Trigger 조건',
    timezone_id VARCHAR(60) NOT NULL DEFAULT 'Asia/Seoul' COMMENT 'Timezone',
    misfire_policy VARCHAR(30) NOT NULL COMMENT 'Misfire 정책',
    agent_pool VARCHAR(100) NOT NULL COMMENT 'Agent Pool',
    zone_id VARCHAR(80) NULL COMMENT '실행 Zone',
    max_concurrency INT NOT NULL DEFAULT 1 COMMENT '최대 동시 실행',
    timeout_seconds BIGINT NOT NULL DEFAULT 3600 COMMENT 'Timeout 초',
    restartable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재시작 가능 여부',
    max_attempts INT NOT NULL DEFAULT 1 COMMENT '최대 시도',
    initial_backoff_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '초기 Backoff',
    backoff_multiplier DECIMAL(10,4) NOT NULL DEFAULT 1 COMMENT 'Backoff 배수',
    max_backoff_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '최대 Backoff',
    skip_limit INT NOT NULL DEFAULT 0 COMMENT 'Skip 허용',
    unknown_result_policy VARCHAR(30) NOT NULL COMMENT '결과 불명 처리 정책',
    compensation_reference VARCHAR(200) NULL COMMENT '보상 처리 참조',
    alert_delay_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '지연 알림',
    sla_seconds BIGINT NOT NULL DEFAULT 0 COMMENT 'SLA',
    notify_failure_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '실패 알림',
    notify_missed_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '미실행 알림',
    executor_reference VARCHAR(300) NOT NULL COMMENT 'Executor 참조',
    definition_json TEXT NOT NULL COMMENT 'Definition JSON',
    checksum VARCHAR(128) NULL COMMENT 'Checksum',
    effective_from DATETIME NULL COMMENT '시행 시작',
    effective_until DATETIME NULL COMMENT '시행 종료',
    row_version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_JOB_DEFINITION_VERSION PRIMARY KEY (job_id, definition_version),
    CONSTRAINT ck_bat_job_def_state CHECK (definition_state IN ('DRAFT','VALIDATED','APPROVAL','PUBLISHED','RETIRED')),
    INDEX idx_bat_job_def_state (definition_state, updated_at),
    INDEX idx_bat_job_def_owner (owner_domain, job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Versioned Job Definition 정본';

CREATE TABLE IF NOT EXISTS BAT_JOB_PACK (
    job_pack_id VARCHAR(120) NOT NULL COMMENT 'Job-pack identifier',
    owner_domain VARCHAR(20) NOT NULL COMMENT 'Owning domain SystemCode',
    artifact_coordinate VARCHAR(240) NOT NULL COMMENT 'Job-pack artifact coordinate',
    artifact_version VARCHAR(80) NOT NULL COMMENT 'Job-pack artifact version',
    artifact_checksum VARCHAR(128) NULL COMMENT 'Job-pack artifact checksum',
    signature_present_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Artifact signature presence flag',
    platform_range VARCHAR(120) NULL COMMENT 'Compatible CPF platform range',
    manifest_json LONGTEXT NOT NULL COMMENT 'Validated job-pack manifest',
    last_registered_at DATETIME(6) NOT NULL COMMENT 'Last catalog registration time',
    CONSTRAINT pk_BAT_JOB_PACK PRIMARY KEY (job_pack_id),
    INDEX ix_bat_job_pack_owner (owner_domain, artifact_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT external job-pack catalog';

CREATE TABLE IF NOT EXISTS BAT_LOCK (
    lock_key VARCHAR(200) NOT NULL COMMENT '배치 잠금 키',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_parameters_hash VARCHAR(128) NOT NULL COMMENT 'Job 파라미터 해시',
    owner_id VARCHAR(100) NOT NULL COMMENT '잠금 소유자',
    locked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '잠금 획득 일시',
    expire_at DATETIME(3) NOT NULL COMMENT '잠금 만료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '운영 위험조치 낙관적 잠금 Version',
    CONSTRAINT pk_BAT_LOCK PRIMARY KEY (lock_key),
    INDEX ix_bat_lock_job (job_id, job_parameters_hash),
    INDEX ix_bat_lock_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 중복 실행 방지 잠금';

CREATE TABLE IF NOT EXISTS BAT_ON_DEMAND_REQUEST (
    execution_request_id VARCHAR(36) NOT NULL COMMENT '온라인 접수 실행 요청 ID',
    standard_batch_id CHAR(10) NOT NULL COMMENT 'B 유형 10자리 표준 배치 ID',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 접수 방지 멱등 키',
    transaction_id CHAR(34) NOT NULL COMMENT '온라인 접수 거래 ID',
    business_date CHAR(8) NOT NULL COMMENT '배치 업무 기준일 YYYYMMDD',
    request_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' COMMENT 'REQUESTED, RUNNING, COMPLETED, FAILED, RESTARTED, STOPPING 등 접수 상태',
    parameters_json LONGTEXT NULL COMMENT '검증된 배치 업무 파라미터 JSON',
    request_reason VARCHAR(500) NOT NULL COMMENT '실행 감사 사유',
    request_user VARCHAR(100) NOT NULL COMMENT '실행 요청자',
    cpf_execution_id BIGINT NULL COMMENT 'BAT 배치 실행 메타 ID',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    result_json LONGTEXT NULL COMMENT '마스킹된 실행 결과 JSON',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '민감정보가 제거된 실패 메시지',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '접수일시',
    completed_at DATETIME(3) NULL COMMENT '완료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_ON_DEMAND_REQUEST PRIMARY KEY (execution_request_id),
    CONSTRAINT uk_bat_on_demand_idempotency UNIQUE (standard_batch_id, idempotency_key),
    CONSTRAINT ck_bat_on_demand_id CHECK (standard_batch_id REGEXP '^B[A-Z]{3}[A-Z0-9]{2}[0-9]{4}$' AND RIGHT(standard_batch_id, 4) <> '0000'),
    CONSTRAINT ck_bat_on_demand_status CHECK (request_status IN ('REQUESTED', 'RUNNING', 'COMPLETED', 'FAILED', 'RESTARTED', 'RESTART_FAILED', 'RESTART_NOT_AVAILABLE', 'STOPPING', 'STOPPED', 'SKIPPED_LOCKED')),
    INDEX ix_bat_on_demand_status (request_status, requested_at),
    INDEX ix_bat_on_demand_transaction (transaction_id),
    INDEX ix_bat_on_demand_spring (spring_batch_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 온디맨드 배치 온라인 접수';

CREATE TABLE IF NOT EXISTS BAT_OPERATION_LOG (
    operation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 운영 로그 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    operation_type VARCHAR(30) NOT NULL COMMENT '운영 작업 유형',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '운영 사유',
    before_data LONGTEXT NULL COMMENT '작업 전 데이터',
    after_data LONGTEXT NULL COMMENT '작업 후 데이터',
    result_type CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    result_message VARCHAR(1000) NULL COMMENT '결과 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_OPERATION_LOG PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_job_time (job_id, created_at),
    INDEX ix_bat_operation_execution (execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 운영 작업 로그';

CREATE TABLE IF NOT EXISTS BAT_OPERATION_LOG_ARCHIVE (
    operation_id BIGINT NOT NULL COMMENT '원본 배치 운영 로그 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    operation_type VARCHAR(30) NOT NULL COMMENT '운영 작업 유형',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '운영 사유',
    before_data LONGTEXT NULL COMMENT '작업 전 데이터',
    after_data LONGTEXT NULL COMMENT '작업 후 데이터',
    result_type CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    result_message VARCHAR(1000) NULL COMMENT '결과 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '원본 등록자',
    created_at DATETIME NOT NULL COMMENT '원본 등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '원본 수정자',
    updated_at DATETIME NOT NULL COMMENT '원본 수정일시',
    archived_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '보관 일시',
    archived_by VARCHAR(100) NOT NULL COMMENT '보관 수행자',
    archive_reason VARCHAR(500) NOT NULL COMMENT '보관 사유',
    CONSTRAINT pk_BAT_OPERATION_LOG_ARCHIVE PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_archive_job_time (job_id, created_at),
    INDEX ix_bat_operation_archive_archived (archived_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 운영 로그 보관소';

CREATE TABLE IF NOT EXISTS BAT_OPERATION_REQUEST (
    idempotency_key VARCHAR(120) NOT NULL COMMENT 'BAT 위험조치 멱등 키',
    request_hash CHAR(64) NOT NULL COMMENT 'Canonical 위험조치 SHA-256',
    operation_type VARCHAR(80) NOT NULL COMMENT 'BAT Owner operation',
    target_type VARCHAR(80) NOT NULL COMMENT '위험조치 대상 유형',
    target_id VARCHAR(200) NOT NULL COMMENT '위험조치 대상 ID',
    action_type VARCHAR(100) NOT NULL COMMENT '감사 Action 유형',
    approval_request_id VARCHAR(120) NOT NULL COMMENT 'ADM 승인 요청 식별자',
    requested_by VARCHAR(50) NOT NULL COMMENT '검증된 실행 운영자',
    expected_version BIGINT NULL COMMENT '대상 낙관적 잠금 Version',
    request_state VARCHAR(30) NOT NULL DEFAULT 'RESERVED' COMMENT 'RESERVED/COMPLETED/FAILED/UNKNOWN',
    result_payload TEXT NULL COMMENT '멱등 Replay용 마스킹 결과',
    failure_code VARCHAR(80) NULL COMMENT '실패/결과불명 코드',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실패/결과불명 메시지',
    completed_at DATETIME(3) NULL COMMENT '최종 상태 확정 일시',
    created_by VARCHAR(50) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_BAT_OPERATION_REQUEST PRIMARY KEY (idempotency_key),
    CONSTRAINT ck_bat_operation_request_state CHECK (request_state IN ('RESERVED','COMPLETED','FAILED','UNKNOWN')),
    CONSTRAINT ck_bat_operation_request_hash CHECK (CHAR_LENGTH(request_hash) = 64),
    INDEX ix_bat_operation_request_target (target_type, target_id, created_at),
    INDEX ix_bat_operation_request_state (request_state, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 위험조치 승인·멱등·UNKNOWN 복구 Ledger';

CREATE TABLE IF NOT EXISTS BAT_RECONCILIATION_AUDIT (
    reconciliation_audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Reconciliation audit sequence',
    request_id VARCHAR(100) NOT NULL COMMENT 'Approved reconciliation request identifier',
    entity_type VARCHAR(40) NOT NULL COMMENT 'Reconciled entity type',
    entity_key VARCHAR(300) NOT NULL COMMENT 'Canonical reconciled entity identity',
    from_status VARCHAR(40) NOT NULL COMMENT 'State observed before reconciliation',
    to_status VARCHAR(40) NOT NULL COMMENT 'State established by reconciliation',
    requester_id VARCHAR(120) NOT NULL COMMENT 'Verified reconciliation requester',
    approver_id VARCHAR(120) NOT NULL COMMENT 'Verified independent approver',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Required operator reason',
    idempotency_key VARCHAR(200) NOT NULL COMMENT 'Single reconciliation outcome key',
    expected_attempt INTEGER NULL COMMENT 'Expected trigger execution attempt',
    expected_version BIGINT NULL COMMENT 'Expected entity optimistic version',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Immutable audit creation time',
    CONSTRAINT pk_BAT_RECONCILIATION_AUDIT PRIMARY KEY (reconciliation_audit_id),
    CONSTRAINT uq_bat_reconcile_idem UNIQUE (idempotency_key),
    CONSTRAINT ck_bat_reconcile_separation CHECK (requester_id <> approver_id),
    INDEX ix_bat_reconcile_entity (entity_type, entity_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Immutable BAT UNKNOWN-result reconciliation approval audit';

CREATE TABLE IF NOT EXISTS BAT_RUNTIME_COMMAND (
    command_id VARCHAR(80) NULL COMMENT 'Runtime command identifier',
    idempotency_key VARCHAR(160) NOT NULL COMMENT 'Command idempotency key',
    command_type VARCHAR(80) NOT NULL COMMENT 'Approved command type',
    target_type VARCHAR(40) NOT NULL COMMENT 'Command target type',
    target_snapshot LONGTEXT NULL COMMENT 'Command target snapshot JSON',
    target_snapshot_hash VARCHAR(128) NULL COMMENT 'Target snapshot checksum',
    expected_version BIGINT NULL COMMENT 'Expected target version',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Command requester',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory command reason',
    approval_policy_version VARCHAR(80) NULL COMMENT 'Approval policy version',
    approval_request_id VARCHAR(80) NULL COMMENT 'ADM approval request identifier',
    approved_by VARCHAR(120) NULL COMMENT 'Command approver',
    command_state VARCHAR(40) NOT NULL COMMENT 'Command lifecycle state',
    execution_attempt INT NOT NULL DEFAULT 0 COMMENT 'Execution attempt count',
    result_text LONGTEXT NULL COMMENT 'Command result detail',
    failure_stage VARCHAR(80) NULL COMMENT 'Last failed stage',
    before_state LONGTEXT NULL COMMENT 'State before operation',
    after_state LONGTEXT NULL COMMENT 'State after operation',
    result_code VARCHAR(80) NULL COMMENT 'Command result code',
    requested_at DATETIME(6) NOT NULL COMMENT 'Command request time',
    expires_at DATETIME(6) NULL COMMENT 'Command expiry time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last command state update time',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    evidence_ref VARCHAR(500) NULL COMMENT 'Audit evidence reference',
    CONSTRAINT pk_BAT_RUNTIME_COMMAND PRIMARY KEY (command_id),
    CONSTRAINT idempotency_key UNIQUE (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT approved runtime command';

CREATE TABLE IF NOT EXISTS BAT_RUNTIME_INSTANCE (
    instance_id VARCHAR(160) NULL COMMENT 'Runtime instance identifier',
    runtime_role VARCHAR(40) NOT NULL COMMENT 'Standalone runtime role',
    service_id VARCHAR(120) NOT NULL COMMENT 'Runtime service identifier',
    was_id VARCHAR(120) NULL COMMENT 'WAS identifier',
    host_alias VARCHAR(160) NULL COMMENT 'Registered host alias',
    zone_id VARCHAR(80) NULL COMMENT 'Availability zone identifier',
    pool_id VARCHAR(80) NULL COMMENT 'Runtime pool identifier',
    artifact_version VARCHAR(80) NOT NULL COMMENT 'Running artifact version',
    git_sha VARCHAR(64) NULL COMMENT 'Running source commit SHA',
    artifact_checksum VARCHAR(128) NULL COMMENT 'Running artifact checksum',
    profile_name VARCHAR(80) NULL COMMENT 'Active runtime profile',
    desired_state VARCHAR(32) NOT NULL DEFAULT 'RUNNING' COMMENT 'Control-plane desired state',
    actual_state VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'Last observed runtime state',
    config_version VARCHAR(80) NULL COMMENT 'Applied configuration version',
    schema_compatibility VARCHAR(120) NULL COMMENT 'Supported schema version range',
    started_at DATETIME(6) NULL COMMENT 'Runtime start time',
    last_heartbeat_at DATETIME(6) NULL COMMENT 'Last heartbeat time',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Monotonic instance fencing token',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Registration time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last state update time',
    CONSTRAINT pk_BAT_RUNTIME_INSTANCE PRIMARY KEY (instance_id),
    CONSTRAINT ck_bat_runtime_instance_role CHECK (BINARY runtime_role IN ('CONTROL_PLANE','SCHEDULER','WORKER','CENTER_CUT_RUNNER','AGENT')),
    INDEX ix_bat_runtime_instance_service (service_id, actual_state),
    INDEX ix_bat_runtime_instance_heartbeat (last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT standalone runtime instance registry';

CREATE TABLE IF NOT EXISTS BAT_SB_JOB_INSTANCE (
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_NAME VARCHAR(100) NOT NULL COMMENT 'Spring Batch Job 이름',
    JOB_KEY VARCHAR(32) NOT NULL COMMENT 'Job 파라미터 식별 키',
    CONSTRAINT pk_BAT_SB_JOB_INSTANCE PRIMARY KEY (JOB_INSTANCE_ID),
    CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobInstance 저장소';

CREATE TABLE IF NOT EXISTS BAT_SCHEDULER_LEASE (
    scheduler_key VARCHAR(100) NULL COMMENT 'Scheduler leadership key',
    owner_instance_id VARCHAR(160) NOT NULL COMMENT 'Current leader instance identifier',
    fencing_token BIGINT NOT NULL COMMENT 'Monotonic leadership fencing token',
    lease_until DATETIME(6) NOT NULL COMMENT 'Leadership lease expiry time',
    last_heartbeat_at DATETIME(6) NOT NULL COMMENT 'Leader heartbeat time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last lease update time',
    CONSTRAINT pk_BAT_SCHEDULER_LEASE PRIMARY KEY (scheduler_key),
    INDEX ix_bat_scheduler_lease_expire (lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT scheduler leader lease';

CREATE TABLE IF NOT EXISTS BAT_VERSION_COMPATIBILITY (
    compatibility_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Compatibility rule identifier',
    environment_id VARCHAR(80) NOT NULL DEFAULT '*' COMMENT 'Applicable environment identifier',
    provider_coordinate VARCHAR(200) NOT NULL COMMENT 'Provider artifact coordinate',
    consumer_coordinate VARCHAR(200) NOT NULL DEFAULT '*' COMMENT 'Consumer artifact coordinate',
    min_version VARCHAR(80) NULL COMMENT 'Minimum compatible version',
    max_version VARCHAR(80) NULL COMMENT 'Maximum compatible version',
    schema_range VARCHAR(120) NULL COMMENT 'Compatible schema version range',
    required_capability VARCHAR(80) NULL COMMENT 'Required runtime capability',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Rule enabled flag',
    CONSTRAINT pk_BAT_VERSION_COMPATIBILITY PRIMARY KEY (compatibility_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT artifact and schema compatibility contract';

CREATE TABLE IF NOT EXISTS BAT_WORKER (
    worker_id VARCHAR(160) NOT NULL COMMENT '배치 worker ID',
    instance_id VARCHAR(160) NOT NULL COMMENT '서버 인스턴스 ID',
    host_name VARCHAR(150) NULL COMMENT '호스트명',
    process_id VARCHAR(80) NULL COMMENT '프로세스 ID',
    thread_name VARCHAR(160) NULL COMMENT '스레드명',
    worker_version VARCHAR(80) NOT NULL DEFAULT 'unknown' COMMENT 'worker 배포 버전',
    capabilities_json LONGTEXT NULL COMMENT 'worker 지원 Job 및 capability JSON',
    max_concurrency INT NOT NULL DEFAULT 1 COMMENT 'worker 최대 동시 실행 수',
    queue_capacity INT NOT NULL DEFAULT 1 COMMENT 'worker 내부 대기열 허용 수',
    control_status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING, DRAINING, STOPPED 제어 상태',
    worker_status VARCHAR(30) NOT NULL DEFAULT 'IDLE' COMMENT 'worker 상태',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    current_job_id VARCHAR(100) NULL COMMENT '현재 실행 Job ID',
    current_execution_id BIGINT NULL COMMENT '현재 BAT 배치 실행 순번',
    description VARCHAR(500) NULL COMMENT 'worker 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_WORKER PRIMARY KEY (worker_id),
    INDEX ix_bat_worker_instance (instance_id, active_yn),
    INDEX ix_bat_worker_status (worker_status, last_heartbeat_at),
    INDEX ix_bat_worker_control (control_status, active_yn, last_heartbeat_at),
    INDEX ix_bat_worker_current_job (current_job_id, current_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker heartbeat';

CREATE TABLE IF NOT EXISTS CMN_BUSINESS_CALENDAR_DAY (
    calendar_id VARCHAR(50) NOT NULL COMMENT 'Calendar 식별자',
    business_date DATE NOT NULL COMMENT '기준 일자',
    business_day_yn CHAR(1) NOT NULL COMMENT '영업일 여부',
    day_type VARCHAR(30) NOT NULL DEFAULT 'BUSINESS' COMMENT 'BUSINESS/HOLIDAY/SPECIAL 등 일자 유형',
    institution_code VARCHAR(50) NULL COMMENT '선택 기관/시장 코드',
    reason VARCHAR(500) NULL COMMENT '휴일/예외 사유',
    version_no BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 시각',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정 시각',
    CONSTRAINT pk_CMN_BUSINESS_CALENDAR_DAY PRIMARY KEY (calendar_id, business_date),
    CONSTRAINT ck_cmn_business_calendar_day_yn CHECK (business_day_yn IN ('Y','N')),
    CONSTRAINT ck_cmn_business_calendar_version CHECK (version_no > 0),
    INDEX ix_cmn_business_calendar_date (business_date, calendar_id),
    INDEX ix_cmn_business_calendar_institution (institution_code, business_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 영업일/휴일 Override 제품 정본';

CREATE TABLE IF NOT EXISTS CMN_CACHE_REFRESH_CHECKPOINT (
    consumer_id VARCHAR(120) NOT NULL COMMENT 'Durable consumer identifier',
    last_event_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Last processed event identifier',
    last_applied_at DATETIME(3) NULL COMMENT 'Last applied event time',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_CMN_CACHE_REFRESH_CHECKPOINT PRIMARY KEY (consumer_id),
    INDEX ix_cpf_cache_refresh_checkpoint_event (last_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cache durable replay consumer cursor';

CREATE TABLE IF NOT EXISTS CMN_CACHE_REFRESH_EVENT (
    event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '캐시 갱신 이벤트 순번',
    cache_name VARCHAR(50) NOT NULL COMMENT '캐시 이름',
    event_type VARCHAR(30) NOT NULL COMMENT '이벤트 유형',
    event_key VARCHAR(200) NULL COMMENT '이벤트 대상 키',
    source_was_id VARCHAR(50) NULL COMMENT '이벤트 발행 WAS ID',
    published_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '발행자',
    published_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발행일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CMN_CACHE_REFRESH_EVENT PRIMARY KEY (event_id),
    INDEX ix_cpf_cache_refresh_event_cache_id (cache_name, event_id),
    INDEX ix_cpf_cache_refresh_event_time (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Common 캐시 갱신 DB fallback 이벤트';

CREATE TABLE IF NOT EXISTS CMN_CODE (
    code_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '코드 순번',
    parent_id BIGINT NULL COMMENT '상위 코드 순번',
    code_key VARCHAR(80) NOT NULL COMMENT '코드 그룹 키',
    code_value VARCHAR(120) NOT NULL COMMENT '코드 값',
    description VARCHAR(500) NULL COMMENT '코드 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CMN_CODE PRIMARY KEY (code_id),
    CONSTRAINT uk_cpf_code_key_value UNIQUE (code_key, code_value),
    CONSTRAINT fk_cpf_code_parent FOREIGN KEY (parent_id) REFERENCES CMN_CODE (code_id) ON DELETE SET NULL,
    INDEX ix_cpf_code_parent (parent_id),
    INDEX ix_cpf_code_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 시스템 공통 코드';

CREATE TABLE IF NOT EXISTS CMN_MESSAGE (
    message_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '메시지 순번',
    message_code VARCHAR(20) NOT NULL COMMENT '메시지 코드',
    locale VARCHAR(10) NOT NULL DEFAULT 'ko' COMMENT '언어 코드',
    message_format_type VARCHAR(20) NOT NULL DEFAULT 'FIXED' COMMENT '메시지 포맷 유형',
    external_message VARCHAR(2000) NOT NULL COMMENT '외부 노출 메시지',
    internal_message VARCHAR(4000) NOT NULL COMMENT '내부 진단 메시지',
    parameter_count INT NOT NULL DEFAULT 0 COMMENT '파라미터 개수',
    parameter_sample VARCHAR(1000) NULL COMMENT '파라미터 예시',
    description VARCHAR(500) NULL COMMENT '메시지 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    parameter_schema_json TEXT NULL COMMENT '메시지 파라미터 이름/타입/민감도 schema',
    escape_html_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '외부 렌더링 HTML escape 여부',
    mask_arguments_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '민감 파라미터 마스킹 여부',
    effective_from DATETIME NULL COMMENT '적용 시작 일시',
    effective_to DATETIME NULL COMMENT '적용 종료 일시',
    catalog_version BIGINT NOT NULL DEFAULT 1 COMMENT '다중 인스턴스 cache version fence',
    CONSTRAINT pk_CMN_MESSAGE PRIMARY KEY (message_id),
    CONSTRAINT uk_cpf_message_code_locale UNIQUE (message_code, locale),
    CONSTRAINT ck_cmn_message_escape CHECK (escape_html_yn IN ('Y','N')),
    CONSTRAINT ck_cmn_message_mask CHECK (mask_arguments_yn IN ('Y','N')),
    INDEX ix_cpf_message_code_use (message_code, use_yn),
    INDEX ix_cpf_message_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 시스템 메시지';

CREATE TABLE IF NOT EXISTS CMN_PARAMETER (
    config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '설정 순번',
    config_key VARCHAR(150) NOT NULL COMMENT '설정 키',
    config_value VARCHAR(2000) NOT NULL COMMENT '설정 값',
    config_type VARCHAR(30) NOT NULL DEFAULT 'STRING' COMMENT '설정 값 유형',
    description VARCHAR(500) NULL COMMENT '설정 설명',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CMN_PARAMETER PRIMARY KEY (config_id),
    CONSTRAINT uk_cpf_config_key UNIQUE (config_key),
    INDEX ix_cpf_config_use (use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 시스템 설정';

CREATE TABLE IF NOT EXISTS CMN_RESPONSE_CODE (
    response_code VARCHAR(20) NOT NULL COMMENT 'CPF 응답 코드',
    message_code VARCHAR(20) NOT NULL COMMENT '연결 메시지 코드',
    result_type CHAR(1) NOT NULL COMMENT '결과 유형',
    module_id VARCHAR(3) NOT NULL COMMENT '모듈 ID',
    response_group VARCHAR(2) NOT NULL COMMENT '응답 그룹',
    sequence_no VARCHAR(4) NOT NULL COMMENT '응답 일련번호',
    http_status INT NOT NULL COMMENT 'HTTP 상태 코드',
    description VARCHAR(500) NULL COMMENT '응답 코드 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    category VARCHAR(32) NOT NULL DEFAULT 'BUSINESS' COMMENT '기술중립 오류 분류',
    retry_disposition VARCHAR(16) NOT NULL DEFAULT 'NEVER' COMMENT 'NEVER/SAFE/RECONCILE/UNKNOWN',
    exposure VARCHAR(32) NOT NULL DEFAULT 'SAFE_MESSAGE_ONLY' COMMENT '외부 메시지 노출 정책',
    effective_from DATETIME NULL COMMENT '적용 시작 일시',
    effective_to DATETIME NULL COMMENT '적용 종료 일시',
    catalog_version BIGINT NOT NULL DEFAULT 1 COMMENT '다중 인스턴스 cache version fence',
    CONSTRAINT pk_CMN_RESPONSE_CODE PRIMARY KEY (response_code),
    CONSTRAINT ck_cmn_response_code_category CHECK (category IN ('VALIDATION','NOT_FOUND','CONFLICT','RATE_LIMIT','AUTHENTICATION','AUTHORIZATION','BUSINESS','EXTERNAL','INFRASTRUCTURE','INTERNAL')),
    CONSTRAINT ck_cmn_response_code_retry CHECK (retry_disposition IN ('NEVER','SAFE','RECONCILE','UNKNOWN')),
    CONSTRAINT ck_cmn_response_code_exposure CHECK (exposure IN ('SAFE_MESSAGE_ONLY','GENERIC_MESSAGE_ONLY')),
    INDEX ix_cpf_response_code_message (message_code),
    INDEX ix_cpf_response_code_module (module_id, result_type, response_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 응답 코드';

CREATE TABLE IF NOT EXISTS CMN_TEMPLATE_DEFINITION (
    TEMPLATE_CODE VARCHAR(100) NOT NULL COMMENT 'Template 식별 코드',
    TEMPLATE_VERSION BIGINT NOT NULL COMMENT '불변 Template 버전',
    CHANNEL_CODE VARCHAR(30) NOT NULL COMMENT 'EMAIL/SMS/PUSH/DOCUMENT 등 채널 코드',
    TEMPLATE_BODY TEXT NOT NULL COMMENT 'Fail-closed token renderer 입력 본문',
    ALLOWED_VARIABLES VARCHAR(2000) NOT NULL DEFAULT '-' COMMENT '쉼표 구분 허용 변수 Schema',
    STATUS_CODE VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/APPROVED/RETIRED 상태',
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '현재 활성 승인본 여부',
    REVISION_NO BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 Revision',
    APPROVED_BY VARCHAR(100) NULL COMMENT '승인자',
    APPROVED_AT DATETIME(3) NULL COMMENT '승인 일시',
    CREATED_BY VARCHAR(100) NOT NULL COMMENT '등록자',
    CREATED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 일시',
    UPDATED_BY VARCHAR(100) NOT NULL COMMENT '최종 수정자',
    UPDATED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '최종 수정 일시',
    CONSTRAINT pk_CMN_TEMPLATE_DEFINITION PRIMARY KEY (TEMPLATE_CODE, TEMPLATE_VERSION, CHANNEL_CODE),
    CONSTRAINT CK_CMN_TEMPLATE_VERSION CHECK (TEMPLATE_VERSION > 0),
    CONSTRAINT CK_CMN_TEMPLATE_STATUS CHECK (STATUS_CODE IN ('DRAFT','APPROVED','RETIRED')),
    CONSTRAINT CK_CMN_TEMPLATE_ACTIVE CHECK (ACTIVE_YN IN ('Y','N')),
    CONSTRAINT CK_CMN_TEMPLATE_REVISION CHECK (REVISION_NO >= 0),
    INDEX IX_CMN_TEMPLATE_ACTIVE (TEMPLATE_CODE, CHANNEL_CODE, STATUS_CODE, ACTIVE_YN, TEMPLATE_VERSION)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN 알림·문서 Template 버전·승인·활성·감사 제품 정본';

CREATE TABLE IF NOT EXISTS CPF_BROKER_DLQ (
    dlq_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'DLQ 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    topic VARCHAR(160) NOT NULL COMMENT 'Broker topic 또는 queue',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    failure_reason VARCHAR(1000) NULL COMMENT 'DLQ 이동 사유',
    replay_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '재처리 상태',
    replay_count INT NOT NULL DEFAULT 0 COMMENT '재처리 요청 횟수',
    replay_requested_at DATETIME(3) NULL COMMENT '재처리 요청 일시',
    replay_completed_at DATETIME(3) NULL COMMENT '재처리 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_BROKER_DLQ PRIMARY KEY (dlq_id),
    CONSTRAINT uk_cpf_broker_dlq_message UNIQUE (message_id),
    INDEX ix_cpf_broker_dlq_status (replay_status, created_at),
    INDEX ix_cpf_broker_dlq_topic (topic, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Broker DLQ';

CREATE TABLE IF NOT EXISTS CPF_BROKER_INBOX (
    inbox_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Inbox 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    consumer_identity VARCHAR(120) NOT NULL DEFAULT 'default' COMMENT 'Consumer 논리 식별자',
    idempotency_key VARCHAR(160) NULL COMMENT '중복 처리 키',
    inbox_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED' COMMENT 'Inbox 처리 상태',
    result_detail VARCHAR(1000) NULL COMMENT '소비 처리 결과 상세',
    lease_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Claim/CAS Version',
    received_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수신 일시',
    consumed_at DATETIME(3) NULL COMMENT '소비 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_BROKER_INBOX PRIMARY KEY (inbox_id),
    CONSTRAINT uk_cpf_broker_inbox_consumer_message UNIQUE (consumer_identity, message_id),
    INDEX ix_cpf_broker_inbox_idempotency (idempotency_key),
    INDEX ix_cpf_broker_inbox_status (inbox_status, received_at),
    INDEX ix_cpf_broker_inbox_retention (inbox_status, updated_at, inbox_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Broker Inbox';

CREATE TABLE IF NOT EXISTS CPF_BROKER_OUTBOX (
    outbox_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Outbox 내부 순번',
    message_id VARCHAR(120) NOT NULL COMMENT '메시지 ID',
    topic VARCHAR(160) NOT NULL COMMENT 'Broker topic 또는 queue',
    message_key VARCHAR(200) NULL COMMENT 'Broker partition key',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    producer_module VARCHAR(20) NULL COMMENT '생산 모듈',
    consumer_module VARCHAR(20) NULL COMMENT '소비 모듈',
    idempotency_key VARCHAR(160) NULL COMMENT '중복 처리 키',
    payload LONGBLOB NULL COMMENT '메시지 payload',
    content_type VARCHAR(100) NULL COMMENT '메시지 content type',
    header_json MEDIUMTEXT NULL COMMENT '메시지 header 직렬화 값',
    attribute_json MEDIUMTEXT NULL COMMENT '메시지 속성 직렬화 값',
    outbox_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'Outbox 처리 상태',
    worker_id VARCHAR(120) NULL COMMENT '처리 worker ID',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '발행 시도 횟수',
    max_attempts INT NOT NULL DEFAULT 5 COMMENT '최대 발행 시도 횟수',
    next_attempt_at DATETIME(3) NULL COMMENT '다음 발행 가능 일시',
    lease_until DATETIME(3) NULL COMMENT 'worker 점유 만료 일시',
    broker_name VARCHAR(80) NULL COMMENT '전송 대상 broker 이름',
    partition_key VARCHAR(200) NULL COMMENT '전송 partition key',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '업무 이벤트 발생 일시',
    claimed_at DATETIME(3) NULL COMMENT 'worker 점유 일시',
    published_at DATETIME(3) NULL COMMENT '발행 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_BROKER_OUTBOX PRIMARY KEY (outbox_id),
    CONSTRAINT uk_cpf_broker_outbox_message UNIQUE (message_id),
    INDEX ix_cpf_broker_outbox_status (outbox_status, outbox_id),
    INDEX ix_cpf_broker_outbox_ready (outbox_status, next_attempt_at, outbox_id),
    INDEX ix_cpf_broker_outbox_lease (outbox_status, lease_until),
    INDEX ix_cpf_broker_outbox_tx (transaction_id, segment_id),
    INDEX ix_cpf_broker_outbox_topic (topic, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Broker Outbox';

CREATE TABLE IF NOT EXISTS CPF_CACHE_INVALIDATION_CHECKPOINT (
    consumer_id VARCHAR(120) NOT NULL COMMENT 'Consumer 식별자',
    last_event_id BIGINT NOT NULL DEFAULT 0 COMMENT '최종 처리 Event 순번',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP COMMENT 'Checkpoint 갱신 일시',
    CONSTRAINT pk_CPF_CACHE_INVALIDATION_CHECKPOINT PRIMARY KEY (consumer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cache 무효화 Consumer Checkpoint';

CREATE TABLE IF NOT EXISTS CPF_CACHE_INVALIDATION_EVENT (
    event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Cache 무효화 Event 순번',
    event_key VARCHAR(100) NOT NULL COMMENT '멱등 Event Key',
    tenant_id VARCHAR(80) NOT NULL COMMENT 'Tenant 식별자',
    namespace_cd VARCHAR(80) NOT NULL COMMENT 'Cache Namespace',
    cache_key VARCHAR(512) NULL COMMENT '단일 Cache Key, Namespace 전체는 빈 값',
    event_version BIGINT NOT NULL DEFAULT 0 COMMENT '발행 Version',
    reason VARCHAR(500) NOT NULL COMMENT '무효화 사유',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청 운영자',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '발행 일시',
    CONSTRAINT pk_CPF_CACHE_INVALIDATION_EVENT PRIMARY KEY (event_id),
    CONSTRAINT uk_cpf_cache_inv_event_key UNIQUE (event_key),
    INDEX ix_cpf_cache_inv_scope (tenant_id, namespace_cd, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Durable Cache 무효화 Event 원장';

CREATE TABLE IF NOT EXISTS CPF_DATA_QUALITY_QUARANTINE (
    QUARANTINE_ID VARCHAR(64) NOT NULL COMMENT '격리 ID',
    RECORD_ID VARCHAR(64) NOT NULL COMMENT '업무 레코드 ID',
    ORIGINAL_PAYLOAD LONGBLOB NOT NULL COMMENT '원본 JSON payload',
    CORRECTED_PAYLOAD LONGBLOB NULL COMMENT '보정 JSON payload',
    QUARANTINE_STATE VARCHAR(64) NOT NULL COMMENT 'QUARANTINED/CORRECTED/REPLAYED',
    VIOLATION_SUMMARY VARCHAR(1000) NOT NULL COMMENT '마스킹 위반 요약',
    ROW_VERSION BIGINT NOT NULL COMMENT 'CAS row version',
    UPDATED_BY VARCHAR(64) NULL COMMENT '수정자',
    UPDATE_REASON VARCHAR(1000) NULL COMMENT '수정 사유',
    UPDATED_AT TIMESTAMP(6) NOT NULL COMMENT '수정 시각',
    VIOLATION_PAYLOAD LONGBLOB NULL COMMENT 'V106 structured violation JSON',
    CONSTRAINT pk_CPF_DATA_QUALITY_QUARANTINE PRIMARY KEY (QUARANTINE_ID),
    INDEX IX_CPF_DQ_QUARANTINE_STATE (QUARANTINE_STATE, UPDATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Persistent data-quality quarantine';

CREATE TABLE IF NOT EXISTS CPF_DATA_QUALITY_RULE (
    RULE_ID VARCHAR(64) NOT NULL COMMENT 'DQ rule ID',
    RULE_VERSION BIGINT NOT NULL COMMENT 'Append-only rule version',
    FIELD_NAME VARCHAR(64) NOT NULL COMMENT '검사 대상 필드',
    EXPRESSION VARCHAR(1000) NOT NULL COMMENT 'Canonical rule expression',
    SEVERITY VARCHAR(64) NOT NULL COMMENT 'Rule severity',
    RULE_STATE VARCHAR(64) NOT NULL COMMENT 'Rule state',
    UPDATED_BY VARCHAR(64) NOT NULL COMMENT '수정자',
    UPDATED_AT TIMESTAMP(6) NOT NULL COMMENT '수정 시각',
    PARAMETERS_PAYLOAD LONGBLOB NULL COMMENT 'V106 rule parameter JSON payload',
    CONSTRAINT pk_CPF_DATA_QUALITY_RULE PRIMARY KEY (RULE_ID, RULE_VERSION)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Persistent data-quality rule versions';

CREATE TABLE IF NOT EXISTS CPF_FIELD_ENCRYPTION_LEDGER (
    RECORD_ID VARCHAR(64) NOT NULL COMMENT '암호화 대상 레코드 ID',
    FIELD_NAME VARCHAR(64) NOT NULL COMMENT '암호화 필드명',
    CLASSIFICATION VARCHAR(64) NOT NULL COMMENT '데이터 분류',
    KEY_VERSION VARCHAR(64) NOT NULL COMMENT '암호화 키 버전',
    SEARCH_TOKEN VARCHAR(1000) NULL COMMENT '검색 토큰',
    ENVELOPE_PAYLOAD LONGBLOB NOT NULL COMMENT 'Envelope 암호문',
    MASKED_PREVIEW VARCHAR(1000) NOT NULL COMMENT '마스킹 미리보기',
    ROW_VERSION BIGINT NOT NULL COMMENT 'CAS row version',
    UPDATED_AT TIMESTAMP(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT pk_CPF_FIELD_ENCRYPTION_LEDGER PRIMARY KEY (RECORD_ID, FIELD_NAME),
    INDEX IX_CPF_FIELD_ENC_TOKEN (FIELD_NAME, SEARCH_TOKEN(64))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Integration Closure field encryption ledger';

CREATE TABLE IF NOT EXISTS CPF_FILE_TRANSFER_HISTORY (
    history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '파일 전송 이력 내부 순번',
    transfer_id VARCHAR(260) NOT NULL COMMENT '파일 전송 ID',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '파일 전송 endpoint 코드',
    transfer_operation VARCHAR(30) NOT NULL COMMENT '전송 작업 유형',
    local_path VARCHAR(1000) NULL COMMENT '로컬 파일 경로',
    remote_path VARCHAR(1000) NULL COMMENT '원격 파일 경로',
    checksum VARCHAR(128) NULL COMMENT '파일 checksum',
    file_size BIGINT NOT NULL DEFAULT 0 COMMENT '파일 크기',
    duplicate_key VARCHAR(1200) NOT NULL COMMENT '중복 방지 키',
    transfer_status VARCHAR(30) NOT NULL COMMENT '전송 상태',
    result_detail VARCHAR(1000) NULL COMMENT '전송 결과 상세',
    completed_at DATETIME(3) NULL COMMENT '전송 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_FILE_TRANSFER_HISTORY PRIMARY KEY (history_id),
    CONSTRAINT uk_cpf_file_transfer_history_id UNIQUE (transfer_id),
    INDEX ix_cpf_file_transfer_duplicate (endpoint_code, duplicate_key(255), checksum),
    INDEX ix_cpf_file_transfer_tx (transaction_id, segment_id),
    INDEX ix_cpf_file_transfer_status (transfer_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 파일 전송 이력';

CREATE TABLE IF NOT EXISTS CPF_IDEMPOTENCY_RECORD (
    idempotency_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '중복 처리 내부 순번',
    scope VARCHAR(40) NOT NULL COMMENT '중복 처리 적용 범위',
    idempotency_key VARCHAR(160) NOT NULL COMMENT '중복 처리 키',
    request_hash VARCHAR(128) NULL COMMENT '요청 본문 해시',
    payload_hash VARCHAR(128) NULL COMMENT '처리 대상 payload 해시',
    record_status VARCHAR(30) NOT NULL DEFAULT 'PROCESSING' COMMENT '중복 처리 상태',
    stored_response MEDIUMTEXT NULL COMMENT '재응답용 저장 응답',
    retry_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '재요청 허용 여부',
    completed_at DATETIME(3) NULL COMMENT '처리 완료 일시',
    expires_at DATETIME(3) NULL COMMENT '만료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_IDEMPOTENCY_RECORD PRIMARY KEY (idempotency_seq),
    CONSTRAINT uk_cpf_idempotency_record_key UNIQUE (scope, idempotency_key),
    INDEX ix_cpf_idempotency_record_status (record_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 중복 처리 기록';

CREATE TABLE IF NOT EXISTS CPF_INTEGRATION_CLOSURE_AUDIT (
    AUDIT_ID VARCHAR(64) NOT NULL COMMENT 'Audit ID',
    CATEGORY VARCHAR(64) NOT NULL COMMENT 'Audit category',
    TARGET_ID VARCHAR(64) NOT NULL COMMENT 'Target ID',
    ACTION_NAME VARCHAR(64) NOT NULL COMMENT 'Action name',
    ACTOR_ID VARCHAR(64) NOT NULL COMMENT 'Actor ID',
    ACTION_REASON VARCHAR(1000) NOT NULL COMMENT 'Action reason',
    RESULT_STATE VARCHAR(64) NOT NULL COMMENT 'Result state',
    RESULT_DETAIL VARCHAR(1000) NULL COMMENT 'Masked result detail',
    OCCURRED_AT TIMESTAMP(6) NOT NULL COMMENT 'Occurred at',
    CONSTRAINT pk_CPF_INTEGRATION_CLOSURE_AUDIT PRIMARY KEY (AUDIT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Integration Closure audit ledger';

CREATE TABLE IF NOT EXISTS CPF_MASKING_POLICY_COMMAND (
    command_id_hash CHAR(64) NOT NULL COMMENT '명령 식별자 SHA-256(원문 미보관)',
    command_hash CHAR(64) NOT NULL COMMENT '명령 본문 SHA-256',
    result_version BIGINT NOT NULL COMMENT '명령 결과 정책 버전',
    result_sensitive_keys_csv VARCHAR(4000) NOT NULL COMMENT '결과 민감 키 목록(CSV)',
    result_max_length INT NOT NULL COMMENT '결과 최대 길이',
    result_mask_bearer_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '결과 Bearer 마스킹 여부',
    result_value_rules_csv VARCHAR(1000) NOT NULL COMMENT '결과 값 규칙 목록(CSV)',
    result_updated_at DATETIME(6) NOT NULL COMMENT '결과 반영 시각',
    result_updated_by VARCHAR(128) NOT NULL COMMENT '결과 반영 운영자',
    result_reason VARCHAR(1000) NULL COMMENT '결과 사유',
    recorded_at DATETIME(6) NOT NULL COMMENT '명령 기록 시각(TTL 정리 기준)',
    CONSTRAINT pk_CPF_MASKING_POLICY_COMMAND PRIMARY KEY (command_id_hash),
    CONSTRAINT ck_cpf_masking_policy_command_bearer CHECK (result_mask_bearer_flag IN ('Y','N')),
    INDEX ix_cpf_masking_policy_command_recorded (recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='마스킹 정책 명령 중복 제거 원장';

CREATE TABLE IF NOT EXISTS CPF_MASKING_POLICY_HEAD (
    singleton_id INT NOT NULL COMMENT '단일 head 행 식별자(항상 1)',
    active_version BIGINT NOT NULL COMMENT '현재 활성 마스킹 정책 버전',
    CONSTRAINT pk_CPF_MASKING_POLICY_HEAD PRIMARY KEY (singleton_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='현재 활성 마스킹 정책 버전 포인터';

CREATE TABLE IF NOT EXISTS CPF_MASKING_POLICY_SHARD (
    shard_id INT NOT NULL COMMENT '마스킹 정책 제어 shard 식별자(단일 shard=0)',
    CONSTRAINT pk_CPF_MASKING_POLICY_SHARD PRIMARY KEY (shard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='마스킹 정책 변경을 직렬화하는 제어 shard';

CREATE TABLE IF NOT EXISTS CPF_MASKING_POLICY_VERSION (
    policy_version BIGINT NOT NULL COMMENT '마스킹 정책 버전',
    sensitive_keys_csv VARCHAR(4000) NOT NULL COMMENT '운영자가 지정한 민감 키 목록(CSV)',
    max_length INT NOT NULL COMMENT '마스킹 후 최대 길이',
    mask_bearer_flag CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Bearer 토큰 마스킹 여부',
    value_rules_csv VARCHAR(1000) NOT NULL COMMENT '운영자가 선택한 값 규칙 목록(CSV)',
    updated_at DATETIME(6) NOT NULL COMMENT '정책 반영 시각',
    updated_by VARCHAR(128) NOT NULL COMMENT '정책을 변경한 운영자',
    update_reason VARCHAR(1000) NULL COMMENT '변경 사유',
    CONSTRAINT pk_CPF_MASKING_POLICY_VERSION PRIMARY KEY (policy_version),
    CONSTRAINT ck_cpf_masking_policy_version_bearer CHECK (mask_bearer_flag IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='마스킹 정책 버전 이력';

CREATE TABLE IF NOT EXISTS CPF_NOTIFICATION_RULE (
    rule_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    event_sub_type VARCHAR(80) NULL COMMENT '알림 이벤트 세부 유형',
    channel_code VARCHAR(30) NOT NULL DEFAULT 'ADM' COMMENT '알림 채널 코드',
    template_code VARCHAR(80) NULL COMMENT '알림 템플릿 코드',
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '알림 심각도',
    receiver_group VARCHAR(100) NULL COMMENT '수신자 그룹',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_NOTIFICATION_RULE PRIMARY KEY (rule_id),
    CONSTRAINT uk_cpf_notification_rule UNIQUE (event_type, event_sub_type, channel_code),
    INDEX ix_cpf_notification_rule_use (use_yn, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 운영 알림 규칙';

CREATE TABLE IF NOT EXISTS CPF_SAGA_EXECUTION (
    saga_id VARCHAR(100) NOT NULL COMMENT 'Saga identifier',
    saga_type VARCHAR(100) NOT NULL COMMENT 'Saga type',
    business_key VARCHAR(200) NULL COMMENT 'Business key',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    saga_status VARCHAR(40) NOT NULL COMMENT 'Saga status',
    version INT NOT NULL DEFAULT 0 COMMENT 'Version',
    error_message VARCHAR(2000) NULL COMMENT 'Error message',
    started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Start time',
    completed_at DATETIME(3) NULL COMMENT 'Completion time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    CONSTRAINT pk_CPF_SAGA_EXECUTION PRIMARY KEY (saga_id),
    INDEX idx_cpf_saga_status (saga_status, updated_at),
    INDEX idx_cpf_saga_business (saga_type, business_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Saga 실행 원장';

CREATE TABLE IF NOT EXISTS CPF_STANDARD_EXECUTION (
    standard_execution_id CHAR(10) NOT NULL COMMENT 'CPF O·S·B 10자리 표준 실행 ID',
    execution_name VARCHAR(150) NOT NULL COMMENT '표준 실행명',
    execution_type VARCHAR(20) NOT NULL COMMENT '실행 유형 ONLINE, SHARED 또는 BATCH',
    owner_domain VARCHAR(20) NOT NULL COMMENT '실행 소유 주제영역',
    source_module VARCHAR(20) NOT NULL COMMENT '발견 소스 모듈',
    source_class VARCHAR(255) NOT NULL COMMENT '선언 클래스명',
    source_method VARCHAR(150) NOT NULL COMMENT '선언 메서드명',
    http_method VARCHAR(10) NULL COMMENT 'HTTP 진입 method',
    endpoint VARCHAR(500) NULL COMMENT '연결 API 또는 배치 endpoint',
    operation_id VARCHAR(150) NULL COMMENT '연결 OpenAPI operation ID',
    description VARCHAR(1000) NULL COMMENT '실행 기능 설명',
    required_permission VARCHAR(150) NULL COMMENT '필수 실행 권한 코드',
    audit_reason_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '감사 사유 필수 여부',
    visibility VARCHAR(20) NOT NULL DEFAULT 'INTERNAL' COMMENT 'PUBLIC 또는 INTERNAL 노출 범위',
    direct_allowed_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '업무 URL 직접 호출 허용 여부',
    gateway_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '공개 CPF Gateway 호출 허용 여부',
    source_version VARCHAR(100) NOT NULL COMMENT '소스 버전 또는 빌드 식별자',
    registration_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED' COMMENT '등록 상태',
    first_registered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 등록일시',
    last_discovered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 발견일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_STANDARD_EXECUTION PRIMARY KEY (standard_execution_id),
    CONSTRAINT ck_cpf_standard_execution_id CHECK (standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$' AND RIGHT(standard_execution_id, 4) <> '0000'),
    CONSTRAINT ck_cpf_standard_execution_type CHECK (execution_type IN ('ONLINE', 'SHARED', 'BATCH')),
    INDEX ix_cpf_standard_execution_type (execution_type, registration_status),
    INDEX ix_cpf_standard_execution_owner (owner_domain, source_module),
    INDEX ix_cpf_standard_execution_source (source_class, source_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF O·S·B 표준 실행 카탈로그';

CREATE TABLE IF NOT EXISTS CPF_STANDARD_EXECUTION_ALIAS (
    legacy_execution_id VARCHAR(32) NOT NULL COMMENT '조회 호환용 구형 실행 ID',
    standard_execution_id CHAR(10) NOT NULL COMMENT '현재 10자리 표준 실행 ID',
    migration_reason VARCHAR(300) NOT NULL COMMENT 'ID 전환 사유',
    retired_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '구형 ID 사용 종료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_STANDARD_EXECUTION_ALIAS PRIMARY KEY (legacy_execution_id),
    CONSTRAINT uk_cpf_standard_execution_alias_current UNIQUE (standard_execution_id, legacy_execution_id),
    CONSTRAINT ck_cpf_standard_execution_alias_current CHECK (standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 구형 실행 ID 조회 호환 이력';

CREATE TABLE IF NOT EXISTS CPF_TRANSACTION_LINEAGE (
    lineage_id VARCHAR(64) NOT NULL COMMENT '정규화 lineage 이벤트 ID',
    transaction_id CHAR(34) NOT NULL COMMENT 'CPF End-to-End 거래 ID',
    segment_id VARCHAR(128) NOT NULL COMMENT '거래 구간 ID',
    parent_segment_id VARCHAR(128) NULL COMMENT '상위 거래 구간 ID',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT '재시도 순번',
    trace_id VARCHAR(128) NULL COMMENT '분산 trace ID',
    span_id VARCHAR(128) NULL COMMENT '분산 span ID',
    request_id VARCHAR(128) NULL COMMENT '요청 ID',
    idempotency_key VARCHAR(160) NULL COMMENT '멱등성 키',
    tenant_id VARCHAR(128) NULL COMMENT '테넌트 ID',
    system_code VARCHAR(64) NULL COMMENT '현재 처리 Logical System 코드',
    target_system_code VARCHAR(128) NULL COMMENT '원격 Target Logical System 코드',
    current_channel VARCHAR(64) NULL COMMENT '현재 처리 Channel 코드(선택 Context)',
    actor_id_masked VARCHAR(256) NULL COMMENT '마스킹된 행위자 ID',
    instance_id VARCHAR(128) NULL COMMENT '실행 인스턴스 ID',
    was_id VARCHAR(128) NULL COMMENT 'WAS ID',
    agent_id VARCHAR(128) NULL COMMENT 'Agent ID',
    worker_id VARCHAR(128) NULL COMMENT 'Worker ID',
    target_channel VARCHAR(128) NULL COMMENT '원격 Target Channel 코드(선택 Context)',
    operation_id VARCHAR(160) NULL COMMENT '업무/호출 Operation ID',
    message_id VARCHAR(160) NULL COMMENT '메시지 ID',
    consumer_group VARCHAR(160) NULL COMMENT '메시지 Consumer Group',
    dlq_id VARCHAR(160) NULL COMMENT 'DLQ ID',
    batch_job_instance_id VARCHAR(128) NULL COMMENT 'Batch Job Instance ID',
    batch_job_execution_id VARCHAR(128) NULL COMMENT 'Batch Job Execution ID',
    batch_step_execution_id VARCHAR(128) NULL COMMENT 'Batch Step Execution ID',
    partition_id VARCHAR(128) NULL COMMENT 'Batch/Message Partition ID',
    file_id VARCHAR(160) NULL COMMENT 'File 거래 ID',
    source_type VARCHAR(32) NOT NULL COMMENT 'lineage 원천 유형',
    source_ref_id VARCHAR(256) NULL COMMENT '원천 레코드 참조 ID',
    lifecycle_state VARCHAR(32) NOT NULL COMMENT '원천 lifecycle 상태',
    failure_stage VARCHAR(128) NULL COMMENT '실패 단계',
    unknown_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과 미확정 여부',
    reconcile_state VARCHAR(32) NULL COMMENT 'Reconcile 상태',
    occurred_at DATETIME(6) NOT NULL COMMENT '이벤트 발생 시각/파티션 키',
    freshness_at DATETIME(6) NOT NULL COMMENT 'lineage 갱신/관측 시각',
    payload_hash CHAR(64) NOT NULL COMMENT '민감 원문 미포함 정규화 payload hash',
    archived_at DATETIME(6) NULL COMMENT 'Archive 완료 시각',
    CONSTRAINT pk_CPF_TRANSACTION_LINEAGE PRIMARY KEY (lineage_id, occurred_at),
    CONSTRAINT uk_cpf_tx_lineage_event UNIQUE (transaction_id, segment_id, attempt_no, source_type, payload_hash, occurred_at),
    CONSTRAINT ck_cpf_tx_lineage_unknown CHECK (unknown_yn IN ('Y','N')),
    INDEX idx_cpf_tx_lineage_tx_time (transaction_id, occurred_at, segment_id, attempt_no),
    INDEX idx_cpf_tx_lineage_trace (trace_id, span_id, occurred_at),
    INDEX idx_cpf_tx_lineage_request (request_id, idempotency_key, occurred_at),
    INDEX idx_cpf_tx_lineage_message (message_id, consumer_group, dlq_id, occurred_at),
    INDEX idx_cpf_tx_lineage_batch (batch_job_instance_id, batch_job_execution_id, batch_step_execution_id, occurred_at),
    INDEX idx_cpf_tx_lineage_file (file_id, source_type, source_ref_id, occurred_at),
    INDEX idx_cpf_tx_lineage_retention (archived_at, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF End-to-End 거래의 정규화 operational lineage projection/index. 원천 domain store를 대체하지 않음.';

CREATE TABLE IF NOT EXISTS CPF_TRANSACTION_LINEAGE_ARCHIVE (
    lineage_id VARCHAR(64) NOT NULL COMMENT '정규화 lineage 이벤트 ID',
    transaction_id CHAR(34) NOT NULL COMMENT 'CPF End-to-End 거래 ID',
    segment_id VARCHAR(128) NOT NULL COMMENT '거래 구간 ID',
    parent_segment_id VARCHAR(128) NULL COMMENT '상위 거래 구간 ID',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT '재시도 순번',
    trace_id VARCHAR(128) NULL COMMENT '분산 trace ID',
    span_id VARCHAR(128) NULL COMMENT '분산 span ID',
    request_id VARCHAR(128) NULL COMMENT '요청 ID',
    idempotency_key VARCHAR(160) NULL COMMENT '멱등성 키',
    tenant_id VARCHAR(128) NULL COMMENT '테넌트 ID',
    system_code VARCHAR(64) NULL COMMENT '현재 처리 Logical System 코드',
    target_system_code VARCHAR(128) NULL COMMENT '원격 Target Logical System 코드',
    current_channel VARCHAR(64) NULL COMMENT '현재 처리 Channel 코드(선택 Context)',
    actor_id_masked VARCHAR(256) NULL COMMENT '마스킹된 행위자 ID',
    instance_id VARCHAR(128) NULL COMMENT '실행 인스턴스 ID',
    was_id VARCHAR(128) NULL COMMENT 'WAS ID',
    agent_id VARCHAR(128) NULL COMMENT 'Agent ID',
    worker_id VARCHAR(128) NULL COMMENT 'Worker ID',
    target_channel VARCHAR(128) NULL COMMENT '원격 Target Channel 코드(선택 Context)',
    operation_id VARCHAR(160) NULL COMMENT '업무/호출 Operation ID',
    message_id VARCHAR(160) NULL COMMENT '메시지 ID',
    consumer_group VARCHAR(160) NULL COMMENT '메시지 Consumer Group',
    dlq_id VARCHAR(160) NULL COMMENT 'DLQ ID',
    batch_job_instance_id VARCHAR(128) NULL COMMENT 'Batch Job Instance ID',
    batch_job_execution_id VARCHAR(128) NULL COMMENT 'Batch Job Execution ID',
    batch_step_execution_id VARCHAR(128) NULL COMMENT 'Batch Step Execution ID',
    partition_id VARCHAR(128) NULL COMMENT 'Batch/Message Partition ID',
    file_id VARCHAR(160) NULL COMMENT 'File 거래 ID',
    source_type VARCHAR(32) NOT NULL COMMENT 'lineage 원천 유형',
    source_ref_id VARCHAR(256) NULL COMMENT '원천 레코드 참조 ID',
    lifecycle_state VARCHAR(32) NOT NULL COMMENT '원천 lifecycle 상태',
    failure_stage VARCHAR(128) NULL COMMENT '실패 단계',
    unknown_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과 미확정 여부',
    reconcile_state VARCHAR(32) NULL COMMENT 'Reconcile 상태',
    occurred_at DATETIME(6) NOT NULL COMMENT '이벤트 발생 시각/파티션 키',
    freshness_at DATETIME(6) NOT NULL COMMENT 'lineage 갱신/관측 시각',
    payload_hash CHAR(64) NOT NULL COMMENT '민감 원문 미포함 정규화 payload hash',
    archived_at DATETIME(6) NULL COMMENT 'Archive 완료 시각',
    archive_reason VARCHAR(64) NOT NULL COMMENT 'Archive 사유',
    archived_by VARCHAR(128) NOT NULL COMMENT 'Archive 수행 주체',
    CONSTRAINT pk_CPF_TRANSACTION_LINEAGE_ARCHIVE PRIMARY KEY (lineage_id, occurred_at),
    CONSTRAINT ck_cpf_tx_lineage_arch_unknown CHECK (unknown_yn IN ('Y','N')),
    INDEX idx_cpf_tx_lineage_arch_tx (transaction_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF transaction lineage 보존/감사 Archive';

CREATE TABLE IF NOT EXISTS CPF_TRANSACTION_LOG (
    LOG_DATE DATE NOT NULL COMMENT '로그 기준일',
    LOG_IDX BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 로그 순번',
    RECOVERY_EVENT_ID VARCHAR(64) NULL COMMENT 'DB 로그 복구 이벤트 중복 방지 ID',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    TRACE_ID VARCHAR(100) NULL COMMENT '분산 추적 ID',
    SPAN_ID VARCHAR(100) NULL COMMENT '현재 span ID',
    PARENT_SPAN_ID VARCHAR(100) NULL COMMENT '상위 span ID',
    SEQUENCE_NO INT NULL DEFAULT 1 COMMENT '거래 내부 로그 순번',
    MODULE_ID VARCHAR(20) NULL DEFAULT 'N/A' COMMENT '모듈 ID',
    MENU_ID VARCHAR(50) NULL COMMENT '메뉴 또는 화면 ID',
    BUSINESS_TRANSACTION_ID VARCHAR(20) NULL COMMENT '업무 거래 ID',
    BUSINESS_TRANSACTION_NAME VARCHAR(150) NULL COMMENT '업무 거래명',
    LOG_TYPE VARCHAR(20) NULL DEFAULT 'N/A' COMMENT '로그 유형',
    API_VERSION VARCHAR(20) NULL COMMENT '호출 API 버전',
    CLIENT_ID VARCHAR(80) NULL COMMENT '클라이언트/Application 식별자',
    CLIENT_VERSION VARCHAR(50) NULL COMMENT '클라이언트 앱 또는 SDK 버전',
    CALLER_SYSTEM_CODE VARCHAR(120) NULL COMMENT '바로 직전 호출 Logical System 코드',
    TARGET_SYSTEM_CODE VARCHAR(32) NULL COMMENT '현재 호출 대상 Logical System 코드',
    ORIGINAL_SYSTEM_CODE VARCHAR(20) NULL COMMENT 'X-Transaction-Id 최초 발급 Logical System 코드',
    SYSTEM_CODE VARCHAR(20) NULL COMMENT '현재 요청을 실제 처리하는 Logical System 코드',
    CALLER_CHANNEL VARCHAR(120) NULL COMMENT '직전 호출 Channel 코드(선택 Context)',
    TARGET_CHANNEL VARCHAR(32) NULL COMMENT '호출 대상 Channel 코드(선택 Context)',
    TARGET_OPERATION_ID VARCHAR(160) NULL COMMENT '호출 대상 Canonical operationId',
    CALLER_INSTANCE_ID VARCHAR(120) NULL COMMENT '호출 인스턴스 ID',
    CORRELATION_ID VARCHAR(120) NULL COMMENT '내부 연계 상관관계 ID',
    IDEMPOTENCY_KEY VARCHAR(120) NULL COMMENT '중복 처리 방지 멱등키',
    LOCALE VARCHAR(20) NULL COMMENT '클라이언트 locale',
    TIMEZONE VARCHAR(50) NULL COMMENT '클라이언트 시간대',
    REQUEST_TYPE VARCHAR(20) NULL COMMENT '요청 유형',
    ORIGINAL_CHANNEL VARCHAR(20) NULL COMMENT '최초 유입 Channel 코드(선택 Context)',
    CURRENT_CHANNEL VARCHAR(20) NULL COMMENT '현재 처리 Channel 코드(선택 Context)',
    MEMBER_NO VARCHAR(50) NULL COMMENT '회원 번호',
    CUSTOMER_NO VARCHAR(50) NULL COMMENT '고객 번호',
    SCREEN_ID VARCHAR(50) NULL COMMENT '화면 ID',
    DEVICE_ID VARCHAR(100) NULL COMMENT '디바이스 ID',
    CLIENT_REQUEST_TIME VARCHAR(30) NULL COMMENT '클라이언트 요청 생성 시각',
    WAS_ID VARCHAR(50) NULL COMMENT '처리 WAS ID',
    INSTANCE_ID VARCHAR(160) NULL COMMENT '처리 서버 인스턴스 ID',
    HOST_NAME VARCHAR(120) NULL COMMENT '처리 서버 호스트명',
    HOST_IP VARCHAR(128) NULL COMMENT 'Runtime host IP',
    PROCESS_ID VARCHAR(80) NULL COMMENT '처리 서버 프로세스 ID',
    THREAD_NAME VARCHAR(160) NULL COMMENT '처리 스레드명',
    RESERVED_FIELD_1 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 1',
    RESERVED_FIELD_2 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 2',
    RESERVED_FIELD_3 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 3',
    RESERVED_FIELD_4 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 4',
    RESERVED_FIELD_5 VARCHAR(255) NULL COMMENT '업무 확장 예약 필드 5',
    HTTP_METHOD VARCHAR(10) NULL COMMENT 'HTTP 메서드',
    URI VARCHAR(500) NULL DEFAULT 'N/A' COMMENT '요청 URI',
    CONTROLLER VARCHAR(255) NULL COMMENT 'Controller 요약',
    EXECUTION_PACKAGE VARCHAR(255) NULL COMMENT '실행 패키지명',
    EXECUTION_CLASS VARCHAR(255) NULL COMMENT '실행 클래스명',
    EXECUTION_METHOD VARCHAR(100) NULL COMMENT '실행 메서드명',
    EXECUTION_SIGNATURE VARCHAR(1000) NULL COMMENT '실행 시그니처',
    WORKFLOW_ID VARCHAR(50) NULL COMMENT '워크플로우 ID',
    WORKFLOW_NAME VARCHAR(100) NULL COMMENT '워크플로우명',
    WORKFLOW_INSTANCE_ID VARCHAR(100) NULL COMMENT '워크플로우 인스턴스 ID',
    WORKFLOW_STEP_ID VARCHAR(50) NULL COMMENT '워크플로우 단계 ID',
    WORKFLOW_STEP_NAME VARCHAR(100) NULL COMMENT '워크플로우 단계명',
    WORKFLOW_STATUS VARCHAR(30) NULL COMMENT '워크플로우 상태',
    WORKFLOW_FAILURE_POLICY VARCHAR(30) NULL COMMENT '워크플로우 실패 정책',
    COMPENSATION_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '보상 거래 여부',
    COMPENSATION_TRANSACTION_ID VARCHAR(20) NULL COMMENT '보상 거래 ID',
    COMPENSATION_TARGET_TRANSACTION_ID VARCHAR(20) NULL COMMENT '보상 대상 거래 ID',
    COMPENSATION_STATUS VARCHAR(30) NULL COMMENT '보상 처리 상태',
    PARAMETERS MEDIUMTEXT NULL COMMENT '마스킹된 요청 파라미터',
    REQUEST_BODY MEDIUMTEXT NULL COMMENT '마스킹된 요청 본문',
    RESPONSE MEDIUMTEXT NULL COMMENT '마스킹된 응답 본문',
    HTTP_STATUS INT NULL COMMENT 'HTTP 상태 코드',
    RESPONSE_CODE VARCHAR(20) NULL COMMENT 'CPF 응답 코드',
    MESSAGE_CODE VARCHAR(20) NULL COMMENT '메시지 코드',
    MESSAGE_CONTENT VARCHAR(1000) NULL COMMENT '외부 호출 메시지',
    ERROR_MESSAGE MEDIUMTEXT NULL COMMENT '마스킹된 오류 메시지',
    ERROR_CODE VARCHAR(100) NULL COMMENT '내부 오류 코드',
    EXTERNAL_MESSAGE VARCHAR(1000) NULL COMMENT '외부 표시 메시지',
    INTERNAL_MESSAGE MEDIUMTEXT NULL COMMENT '내부 진단 메시지',
    EXEC_USER VARCHAR(100) NOT NULL DEFAULT 'N/A' COMMENT '실행 사용자',
    CLIENT_IP VARCHAR(100) NULL COMMENT '클라이언트 IP',
    USER_AGENT VARCHAR(500) NULL COMMENT 'User-Agent',
    START_TIME DATETIME(3) NULL COMMENT '처리 시작 시각',
    END_TIME DATETIME(3) NULL COMMENT '처리 종료 시각',
    DURATION_MS BIGINT NULL COMMENT '처리 시간 밀리초',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_TRANSACTION_LOG PRIMARY KEY (LOG_IDX),
    CONSTRAINT uk_cpf_transaction_log_recovery_event UNIQUE (RECOVERY_EVENT_ID),
    INDEX ix_cpf_transaction_log_date (LOG_DATE),
    INDEX ix_cpf_transaction_log_transaction_time (TRANSACTION_ID, START_TIME, LOG_IDX),
    INDEX ix_cpf_transaction_log_trace_id (TRACE_ID),
    INDEX ix_cpf_transaction_log_business_time (BUSINESS_TRANSACTION_ID, START_TIME),
    INDEX ix_cpf_transaction_log_client (CLIENT_ID, START_TIME),
    INDEX ix_cpf_transaction_log_correlation (CORRELATION_ID, START_TIME),
    INDEX ix_cpf_transaction_log_idempotency (IDEMPOTENCY_KEY),
    INDEX ix_cpf_transaction_log_member_time (MEMBER_NO, START_TIME),
    INDEX ix_cpf_transaction_log_customer_time (CUSTOMER_NO, START_TIME),
    INDEX ix_cpf_transaction_log_system_time (SYSTEM_CODE, START_TIME),
    INDEX ix_cpf_transaction_log_module_time (MODULE_ID, START_TIME),
    INDEX ix_cpf_transaction_log_instance_time (INSTANCE_ID, START_TIME),
    INDEX ix_cpf_transaction_log_was_time (WAS_ID, START_TIME),
    INDEX ix_cpf_transaction_log_module_instance_time (MODULE_ID, INSTANCE_ID, START_TIME),
    INDEX ix_cpf_transaction_log_status_time (LOG_TYPE, RESPONSE_CODE, START_TIME),
    INDEX ix_cpf_transaction_log_http_status_time (HTTP_STATUS, START_TIME),
    INDEX ix_cpf_transaction_log_target_operation (TARGET_SYSTEM_CODE, TARGET_OPERATION_ID, START_TIME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 거래 요약 로그';

CREATE TABLE IF NOT EXISTS CPF_TRANSACTION_SEGMENT (
    segment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 구간 내부 순번',
    transaction_segment_id VARCHAR(120) NOT NULL COMMENT '거래 구간 ID',
    transaction_id CHAR(34) NOT NULL COMMENT 'CPF transactionId',
    execution_id VARCHAR(160) NULL COMMENT 'CPF 실행 인스턴스 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '상위 거래 구간 ID',
    transaction_role VARCHAR(40) NOT NULL COMMENT '구간 역할',
    module_code VARCHAR(20) NOT NULL COMMENT '현재 처리 모듈 코드',
    source_module_code VARCHAR(20) NULL COMMENT '호출 출발 모듈 코드',
    target_module_code VARCHAR(20) NULL COMMENT '호출 대상 모듈 코드',
    direction VARCHAR(20) NOT NULL COMMENT '구간 처리 방향',
    call_depth INT NOT NULL DEFAULT 0 COMMENT '호출 깊이',
    sequence_no INT NOT NULL DEFAULT 1 COMMENT '거래 내 구간 순번',
    api_path VARCHAR(500) NULL COMMENT '처리 API 경로',
    transaction_name VARCHAR(200) NULL COMMENT '거래 구간명',
    started_at DATETIME(6) NOT NULL COMMENT '구간 시작 일시',
    ended_at DATETIME(6) NULL COMMENT '구간 종료 일시',
    duration_ms BIGINT NULL COMMENT '구간 수행시간 밀리초',
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' COMMENT '구간 처리 상태',
    failure_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실패 여부',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message_masked VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지',
    request_header_snapshot_masked MEDIUMTEXT NULL COMMENT '마스킹된 요청 헤더 snapshot',
    response_header_snapshot_masked MEDIUMTEXT NULL COMMENT '마스킹된 응답 헤더 snapshot',
    extension_header_snapshot_masked MEDIUMTEXT NULL COMMENT '마스킹된 확장 헤더 snapshot',
    customer_no_masked VARCHAR(80) NULL COMMENT '마스킹된 고객번호',
    member_no_masked VARCHAR(80) NULL COMMENT '마스킹된 회원번호',
    user_id_masked VARCHAR(80) NULL COMMENT '마스킹된 사용자 ID',
    operator_id_masked VARCHAR(80) NULL COMMENT '마스킹된 운영자 ID',
    system_code VARCHAR(30) NULL COMMENT '현재 요청을 실제 처리하는 Logical System 코드',
    original_system_code VARCHAR(30) NULL COMMENT 'X-Transaction-Id 최초 발급 Logical System 코드',
    caller_system_code VARCHAR(100) NULL COMMENT '바로 직전 호출 Logical System 코드',
    target_system_code VARCHAR(32) NULL COMMENT '현재 호출 대상 Logical System 코드',
    current_channel VARCHAR(30) NULL COMMENT '현재 처리 Channel 코드(선택 Context)',
    original_channel VARCHAR(30) NULL COMMENT '최초 유입 Channel 코드(선택 Context)',
    client_id VARCHAR(100) NULL COMMENT '클라이언트/Application 식별자',
    caller_channel VARCHAR(100) NULL COMMENT '직전 호출 Channel 코드(선택 Context)',
    target_channel VARCHAR(32) NULL COMMENT '호출 대상 Channel 코드(선택 Context)',
    target_operation_id VARCHAR(160) NULL COMMENT '호출 대상 Canonical operationId',
    external_institution_code VARCHAR(50) NULL COMMENT '외부기관 코드',
    external_transaction_id VARCHAR(120) NULL COMMENT '외부기관 거래 ID',
    selected_instance_id VARCHAR(100) NULL COMMENT '선택된 하위 서비스 인스턴스 ID',
    attempt_no INT NULL COMMENT '서비스 호출 시도 순번',
    retry_yn CHAR(1) NULL COMMENT '재시도 여부',
    failover_yn CHAR(1) NULL COMMENT '다른 인스턴스로 전환한 여부',
    circuit_state VARCHAR(20) NULL COMMENT '호출 시점 circuit 상태',
    downstream_http_status INT NULL COMMENT '하위 서비스 HTTP 상태',
    result_state VARCHAR(30) NULL COMMENT '호출 결과 상태',
    unknown_result_id VARCHAR(100) NULL COMMENT '결과 미확정 관리 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    CONSTRAINT pk_CPF_TRANSACTION_SEGMENT PRIMARY KEY (segment_id),
    CONSTRAINT uk_cpf_transaction_segment_id UNIQUE (transaction_segment_id),
    INDEX ix_cpf_transaction_segment_execution (execution_id, started_at),
    INDEX ix_cpf_transaction_segment_transaction (transaction_id, started_at, segment_id),
    INDEX ix_cpf_transaction_segment_parent (parent_segment_id),
    INDEX ix_cpf_transaction_segment_module (module_code, started_at),
    INDEX ix_cpf_transaction_segment_role (transaction_role, direction),
    INDEX ix_cpf_transaction_segment_status (failure_yn, status, started_at),
    INDEX ix_cpf_transaction_segment_duration (duration_ms),
    INDEX ix_cpf_transaction_segment_customer (customer_no_masked, started_at),
    INDEX ix_cpf_transaction_segment_member (member_no_masked, started_at),
    INDEX ix_cpf_transaction_segment_user (user_id_masked, started_at),
    INDEX ix_cpf_transaction_segment_operator (operator_id_masked, started_at),
    INDEX ix_cpf_transaction_segment_client_system (client_id, caller_system_code, started_at),
    INDEX ix_cpf_transaction_segment_external (external_institution_code, external_transaction_id),
    INDEX ix_cpf_transaction_segment_instance (selected_instance_id, started_at),
    INDEX ix_cpf_transaction_segment_attempt (transaction_id, attempt_no),
    INDEX ix_cpf_transaction_segment_unknown (unknown_result_id),
    INDEX ix_cpf_transaction_segment_target_operation (target_system_code, target_operation_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 복합 거래 구간 로그';

CREATE TABLE IF NOT EXISTS CPF_UNKNOWN_RESULT (
    unknown_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Unknown result 내부 순번',
    unknown_id VARCHAR(120) NOT NULL COMMENT 'Unknown result ID',
    unknown_type VARCHAR(40) NOT NULL COMMENT 'Unknown result 유형',
    unknown_status VARCHAR(40) NOT NULL DEFAULT 'CHECK_PENDING' COMMENT 'Unknown result 상태',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    segment_id VARCHAR(120) NULL COMMENT '거래 구간 ID',
    external_key VARCHAR(200) NULL COMMENT '외부 시스템 또는 메시지 키',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    next_action VARCHAR(100) NULL COMMENT '다음 조치',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '감지 일시',
    resolved_at DATETIME(3) NULL COMMENT '해결 일시',
    resolved_by VARCHAR(100) NULL COMMENT '해결 운영자',
    audit_reason VARCHAR(500) NULL COMMENT '수동 처리 감사 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '자동 결과확인 시도 횟수',
    next_check_at DATETIME(3) NULL COMMENT '다음 자동 확인 예정 일시',
    lease_owner VARCHAR(120) NULL COMMENT 'Reconciliation claim 소유자',
    lease_until DATETIME(3) NULL COMMENT 'Reconciliation claim 만료 일시',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
    CONSTRAINT pk_CPF_UNKNOWN_RESULT PRIMARY KEY (unknown_seq),
    CONSTRAINT uk_cpf_unknown_result_id UNIQUE (unknown_id),
    INDEX ix_cpf_unknown_result_status (unknown_type, unknown_status, detected_at),
    INDEX ix_cpf_unknown_result_tx (transaction_id, segment_id),
    INDEX ix_cpf_unknown_result_external (external_key),
    INDEX ix_cpf_unknown_result_claim (unknown_status, next_check_at, lease_until, detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF Unknown result 및 reconciliation 이력';

CREATE TABLE IF NOT EXISTS CPF_WEBHOOK_DELIVERY (
    DELIVERY_ID VARCHAR(64) NOT NULL COMMENT 'Delivery ID',
    ENDPOINT_ID VARCHAR(64) NOT NULL COMMENT 'Endpoint ID',
    EVENT_ID VARCHAR(64) NOT NULL COMMENT 'Event ID',
    EVENT_TYPE VARCHAR(64) NOT NULL COMMENT 'Event type',
    IDEMPOTENCY_KEY VARCHAR(64) NOT NULL COMMENT 'Idempotency key',
    DELIVERY_SEQUENCE BIGINT NOT NULL COMMENT 'Ordered sequence',
    DELIVERY_STATE VARCHAR(64) NOT NULL COMMENT 'Delivery state',
    ATTEMPT_COUNT BIGINT NOT NULL COMMENT 'Attempt count',
    NEXT_ATTEMPT_AT TIMESTAMP(6) NOT NULL COMMENT 'Next retry time',
    LAST_ERROR VARCHAR(1000) NULL COMMENT 'Last masked error',
    ROW_VERSION BIGINT NOT NULL COMMENT 'CAS row version',
    UPDATED_AT TIMESTAMP(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT pk_CPF_WEBHOOK_DELIVERY PRIMARY KEY (DELIVERY_ID),
    CONSTRAINT UK_CPF_WEBHOOK_IDEMP UNIQUE (ENDPOINT_ID, IDEMPOTENCY_KEY),
    INDEX IX_CPF_WEBHOOK_DUE (ENDPOINT_ID, DELIVERY_STATE, NEXT_ATTEMPT_AT, DELIVERY_SEQUENCE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook durable delivery/retry ledger';

CREATE TABLE IF NOT EXISTS CPF_WEBHOOK_ENDPOINT (
    ENDPOINT_ID VARCHAR(64) NOT NULL COMMENT 'Webhook endpoint ID',
    CALLBACK_URI VARCHAR(1000) NOT NULL COMMENT 'Callback URI',
    SECRET_VERSION VARCHAR(64) NOT NULL COMMENT 'Secret version reference',
    ALLOWED_EVENT_TYPES VARCHAR(1000) NOT NULL COMMENT '허용 이벤트 목록',
    ENABLED SMALLINT NOT NULL COMMENT '논리 enabled; vendor boolean mapping',
    ROW_VERSION BIGINT NOT NULL COMMENT 'CAS row version',
    UPDATED_BY VARCHAR(64) NOT NULL COMMENT '수정자',
    UPDATE_REASON VARCHAR(1000) NOT NULL COMMENT '수정 사유',
    UPDATED_AT TIMESTAMP(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT pk_CPF_WEBHOOK_ENDPOINT PRIMARY KEY (ENDPOINT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Integration Closure webhook endpoint';

CREATE TABLE IF NOT EXISTS GW_CONTROL_NONCE (
    audience VARCHAR(160) NOT NULL COMMENT '대상 Gateway Instance/Audience',
    key_id VARCHAR(80) NOT NULL COMMENT '서명 Key ID',
    caller_id VARCHAR(80) NOT NULL COMMENT '호출 Service ID',
    nonce VARCHAR(160) NOT NULL COMMENT '일회성 요청 Nonce',
    claimed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Claim 시각',
    expires_at DATETIME(3) NOT NULL COMMENT 'Replay 차단 만료 시각',
    CONSTRAINT pk_GW_CONTROL_NONCE PRIMARY KEY (audience, key_id, caller_id, nonce),
    INDEX ix_cpf_gateway_control_nonce_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Control API 다중 Instance Replay 방지 원장';

CREATE TABLE IF NOT EXISTS GW_CONTROL_SECURITY_AUDIT (
    event_id VARCHAR(64) NOT NULL COMMENT '보안 감사 Event ID',
    occurred_at DATETIME(3) NOT NULL COMMENT '발생 시각',
    audience VARCHAR(160) NULL COMMENT '대상 Audience',
    key_id VARCHAR(80) NULL COMMENT '서명 Key ID',
    caller_service VARCHAR(80) NULL COMMENT '호출 Service',
    operator_id VARCHAR(100) NULL COMMENT '인증 운영자',
    http_method VARCHAR(16) NULL COMMENT 'HTTP Method',
    request_target VARCHAR(1000) NULL COMMENT '요청 Target',
    remote_address VARCHAR(128) NULL COMMENT '원격 주소',
    result_code VARCHAR(80) NOT NULL COMMENT '거부 결과 코드',
    safe_message VARCHAR(1000) NULL COMMENT '민감정보 제거 메시지',
    CONSTRAINT pk_GW_CONTROL_SECURITY_AUDIT PRIMARY KEY (event_id),
    INDEX ix_cpf_gw_ctl_sec_audit_time (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Control 보안 실패 불변 감사 원장';

CREATE TABLE IF NOT EXISTS GW_OPERATION_IDEMPOTENCY (
    operation_id VARCHAR(100) NOT NULL COMMENT 'Operation ID',
    operation_type VARCHAR(50) NOT NULL COMMENT 'Operation 유형',
    resource_id VARCHAR(100) NOT NULL COMMENT '대상 ID',
    payload_hash VARCHAR(64) NOT NULL COMMENT 'Payload SHA-256',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 상태',
    result_payload LONGTEXT NULL COMMENT '마스킹된 결과',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    expires_at DATETIME NOT NULL COMMENT '멱등 보존 만료',
    CONSTRAINT pk_GW_OPERATION_IDEMPOTENCY PRIMARY KEY (operation_id),
    INDEX ix_cpf_gwy_operation_resource (operation_type, resource_id, created_at),
    INDEX ix_cpf_gwy_operation_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Command 멱등성 원장';

CREATE TABLE IF NOT EXISTS GW_RATE_LIMIT_COUNTER (
    counter_key VARCHAR(300) NOT NULL COMMENT 'Rate Limit scope 식별 키',
    policy_version BIGINT NOT NULL COMMENT 'Rate Limit 정책 버전',
    window_start_ms BIGINT NOT NULL COMMENT 'Window 시작 epoch millisecond',
    reset_at_ms BIGINT NOT NULL COMMENT 'Window reset epoch millisecond',
    used_units BIGINT NOT NULL DEFAULT 0 COMMENT '현재 소비 Unit',
    rejected_count INT NOT NULL DEFAULT 0 COMMENT '거절 누적 횟수',
    blocked_until_ms BIGINT NOT NULL DEFAULT 0 COMMENT 'Block 종료 epoch millisecond',
    version BIGINT NOT NULL DEFAULT 0 COMMENT 'CAS 낙관적 잠금 버전',
    CONSTRAINT pk_GW_RATE_LIMIT_COUNTER PRIMARY KEY (counter_key, policy_version, window_start_ms),
    INDEX ix_gw_rate_limit_active_block (counter_key, policy_version, blocked_until_ms),
    INDEX ix_gw_rate_limit_reset (reset_at_ms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway 다중 인스턴스 공유 Rate Limit Counter';

CREATE TABLE IF NOT EXISTS GW_SERVER_GROUP (
    server_group_id VARCHAR(100) NOT NULL COMMENT '서버 그룹 ID',
    group_name VARCHAR(200) NOT NULL COMMENT '서버 그룹명',
    environment_code VARCHAR(50) NOT NULL COMMENT '환경 코드',
    service_id VARCHAR(100) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(100) NOT NULL COMMENT 'Endpoint 코드',
    target_protocol VARCHAR(30) NOT NULL COMMENT 'Target Protocol',
    load_balance_policy VARCHAR(50) NOT NULL COMMENT 'Load Balance 정책',
    hash_key_source VARCHAR(200) NULL COMMENT 'Hash Key Source',
    health_policy_id VARCHAR(100) NULL COMMENT 'Health 정책 ID',
    failover_group_id VARCHAR(100) NULL COMMENT 'Failover 그룹 ID',
    group_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '그룹 상태',
    direct_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '직접 호출 허용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금 버전',
    CONSTRAINT pk_GW_SERVER_GROUP PRIMARY KEY (server_group_id),
    CONSTRAINT ck_cpf_gwy_group_direct CHECK (direct_allowed_yn IN ('Y','N')),
    INDEX ix_cpf_gwy_group_service (environment_code, service_id, group_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Server Group';

CREATE TABLE IF NOT EXISTS GW_SPOOL_CHECKPOINT (
    gateway_instance_id VARCHAR(100) NOT NULL COMMENT 'Gateway Instance ID',
    spool_name VARCHAR(100) NOT NULL COMMENT 'Spool 이름',
    last_written_sequence BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 기록 Sequence',
    last_ingested_sequence BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 적재 Sequence',
    backlog_count BIGINT NOT NULL DEFAULT 0 COMMENT '적체 건수',
    backlog_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '적체 용량',
    last_error_code VARCHAR(100) NULL COMMENT '마지막 오류',
    last_error_at DATETIME NULL COMMENT '마지막 오류 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '갱신 시각',
    CONSTRAINT pk_GW_SPOOL_CHECKPOINT PRIMARY KEY (gateway_instance_id, spool_name),
    INDEX ix_cpf_gwy_spool_backlog (backlog_count, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Durable Spool 관제 Checkpoint';

CREATE TABLE IF NOT EXISTS OPS_ASYNC_OPERATION (
    execution_id VARCHAR(160) NOT NULL COMMENT 'CPF async executionId',
    operation_id VARCHAR(160) NOT NULL COMMENT 'Async handler Canonical operationId',
    transaction_id CHAR(34) NOT NULL COMMENT 'End-to-end transactionId',
    idempotency_key VARCHAR(256) NOT NULL COMMENT '중복 submit 방지 business key',
    command_type VARCHAR(300) NOT NULL COMMENT '등록 Async Handler command type',
    command_payload TEXT NOT NULL COMMENT '마스킹/암호화 정책 적용 대상 command payload',
    context_payload TEXT NOT NULL COMMENT 'CPF Context snapshot',
    result_type VARCHAR(300) NOT NULL COMMENT 'typed result class',
    result_payload TEXT NULL COMMENT '성공 결과 payload',
    state VARCHAR(30) NOT NULL DEFAULT 'ACCEPTED' COMMENT 'Async lifecycle state',
    result_status VARCHAR(30) NULL COMMENT 'CPF Boundary outcome',
    error_code VARCHAR(120) NULL COMMENT '실패/UNKNOWN code',
    error_message VARCHAR(2000) NULL COMMENT '마스킹된 실패 설명',
    recovery_id VARCHAR(160) NULL COMMENT 'UNKNOWN recovery correlation',
    recovery_action VARCHAR(120) NULL COMMENT 'Probe/Reconcile next action',
    submitted_at DATETIME(3) NOT NULL COMMENT '접수시각',
    started_at DATETIME(3) NULL COMMENT '실행시작',
    updated_at DATETIME(3) NOT NULL COMMENT '최종변경',
    completed_at DATETIME(3) NULL COMMENT '최종완료',
    expires_at DATETIME(3) NOT NULL COMMENT '실행만료',
    heartbeat_at DATETIME(3) NULL COMMENT 'Worker heartbeat',
    lease_owner VARCHAR(160) NULL COMMENT '현재 Runtime instanceId',
    lease_until DATETIME(3) NULL COMMENT 'Worker lease expiry',
    cancellation_reason VARCHAR(500) NULL COMMENT 'cooperative cancel reason',
    version BIGINT NOT NULL DEFAULT 1 COMMENT 'optimistic/fencing version',
    CONSTRAINT pk_OPS_ASYNC_OPERATION PRIMARY KEY (execution_id),
    CONSTRAINT uk_ops_async_operation_idempotency UNIQUE (operation_id, idempotency_key),
    CONSTRAINT ck_ops_async_operation_state CHECK (state IN ('ACCEPTED','RUNNING','SUCCEEDED','FAILED','UNKNOWN','CANCEL_REQUESTED','CANCELLED','EXPIRED')),
    CONSTRAINT ck_ops_async_operation_result CHECK (result_status IS NULL OR result_status IN ('SUCCESS','BUSINESS_FAILURE','TECHNICAL_FAILURE','UNKNOWN','CANCELLED')),
    INDEX ix_ops_async_operation_state (state, submitted_at),
    INDEX ix_ops_async_operation_tx (transaction_id, submitted_at),
    INDEX ix_ops_async_operation_lease (state, lease_until),
    INDEX ix_ops_async_operation_expiry (expires_at, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 범용 Async Operation durable execution state';

CREATE TABLE IF NOT EXISTS OPS_CHANNEL_EXECUTION_POLICY (
    policy_key VARCHAR(100) NOT NULL COMMENT '채널 실행 정책 불변 키',
    operation_id VARCHAR(160) NOT NULL COMMENT 'Canonical 업무 operationId 또는 전체 Operation *',
    caller_channel VARCHAR(16) NOT NULL COMMENT '직전 Hop의 Canonical Caller Channel 또는 * wildcard',
    allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실행 허용 여부',
    authentication_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '정책별 인증 필수 여부',
    signature_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '정책별 요청 서명 필수 여부',
    max_tps INT NOT NULL DEFAULT 0 COMMENT '0이면 제한하지 않는 최대 초당 요청 수',
    effective_from DATETIME(3) NULL COMMENT '정책 적용 시작일시',
    effective_to DATETIME(3) NULL COMMENT '정책 적용 종료일시',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '정책 사용 여부',
    policy_version BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 적용 정책 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_CHANNEL_EXECUTION_POLICY PRIMARY KEY (policy_key),
    CONSTRAINT uk_ops_channel_execution_policy_operation_caller UNIQUE (operation_id, caller_channel),
    CONSTRAINT ck_ops_channel_execution_policy_operation CHECK (operation_id = '*' OR operation_id REGEXP '^[A-Za-z][A-Za-z0-9_.:-]{2,159}$'),
    CONSTRAINT ck_cpf_channel_execution_policy_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_auth CHECK (authentication_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_signature CHECK (signature_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_active CHECK (active_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_period CHECK (effective_from IS NULL OR effective_to IS NULL OR effective_from <= effective_to),
    INDEX ix_ops_channel_execution_policy_lookup (operation_id, caller_channel, active_yn),
    INDEX ix_ops_channel_execution_policy_effective (active_yn, effective_from, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 업무 Operation + Caller Channel Canonical 허용 정책';

CREATE TABLE IF NOT EXISTS OPS_CHANNEL_POLICY_VERSION (
    version_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '채널 정책 스냅샷 버전',
    change_type VARCHAR(30) NOT NULL COMMENT 'CHANNEL 또는 EXECUTION_POLICY 변경 유형',
    target_key VARCHAR(100) NOT NULL COMMENT '변경 대상 채널 또는 정책 키',
    change_reason VARCHAR(500) NOT NULL COMMENT '운영 변경 사유',
    applied_by VARCHAR(100) NOT NULL COMMENT '적용 운영자',
    applied_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '적용일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_CHANNEL_POLICY_VERSION PRIMARY KEY (version_id),
    INDEX ix_cpf_channel_policy_version_target (change_type, target_key, version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 채널 정책 변경 버전';

CREATE TABLE IF NOT EXISTS OPS_CHANNEL_REGISTRY (
    channel_code VARCHAR(30) NOT NULL COMMENT 'CPF 통합 채널 코드',
    channel_name VARCHAR(100) NOT NULL COMMENT '채널명',
    channel_type VARCHAR(30) NOT NULL COMMENT 'CLIENT, OPERATOR 또는 SYSTEM 유형',
    trust_level VARCHAR(30) NOT NULL COMMENT 'EXTERNAL 또는 INTERNAL 신뢰 수준',
    client_channel_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '최초 유입 클라이언트 채널 여부',
    internal_channel_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '내부 호출 채널 여부',
    authentication_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '인증 필수 여부',
    signature_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '요청 서명 필수 여부',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '채널 사용 여부',
    description VARCHAR(500) NULL COMMENT '채널 설명',
    policy_version BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 적용 정책 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_CHANNEL_REGISTRY PRIMARY KEY (channel_code),
    CONSTRAINT ck_cpf_channel_registry_client CHECK (client_channel_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_internal CHECK (internal_channel_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_auth CHECK (authentication_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_signature CHECK (signature_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_active CHECK (active_yn IN ('Y', 'N')),
    INDEX ix_cpf_channel_registry_active (active_yn, channel_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 통합 채널 레지스트리';

CREATE TABLE IF NOT EXISTS OPS_CONTROL_OPERATION (
    operation_id VARCHAR(100) NOT NULL COMMENT 'Idempotent operation identifier',
    command_type VARCHAR(80) NOT NULL COMMENT 'Operation command type',
    request_hash VARCHAR(64) NOT NULL COMMENT 'Request fingerprint checksum',
    entity_id VARCHAR(100) NULL COMMENT 'Target entity identifier',
    result_state VARCHAR(30) NOT NULL DEFAULT 'PROCESSING' COMMENT 'Operation result state',
    result_json LONGTEXT NULL COMMENT 'Operation result JSON',
    expires_at DATETIME(3) NULL COMMENT 'Expiry time',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_CONTROL_OPERATION PRIMARY KEY (operation_id),
    CONSTRAINT ck_cpf_control_operation_state CHECK (result_state IN ('PROCESSING','SUCCESS','FAILED','UNKNOWN','EXPIRED','CANCELLED')),
    INDEX ix_cpf_control_operation_expiry (result_state, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='운영 명령 operationId fingerprint 및 결과 복구 ledger';

CREATE TABLE IF NOT EXISTS OPS_FEATURE_FLAG_AUDIT (
    audit_id VARCHAR(64) NOT NULL COMMENT 'audit_id',
    event_type VARCHAR(80) NOT NULL COMMENT 'event_type',
    flag_key VARCHAR(200) NOT NULL COMMENT 'flag_key',
    actor_id VARCHAR(100) NULL COMMENT 'actor_id',
    reason_code VARCHAR(500) NULL COMMENT 'reason_code',
    sanitized_attributes VARCHAR(2000) NULL COMMENT 'sanitized_attributes',
    occurred_at DATETIME(3) NOT NULL COMMENT 'occurred_at',
    CONSTRAINT pk_OPS_FEATURE_FLAG_AUDIT PRIMARY KEY (audit_id),
    INDEX ix_cpf_ffaudit_key_time (flag_key, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Feature Flag 감사';

CREATE TABLE IF NOT EXISTS OPS_FEATURE_FLAG_KILL_SWITCH (
    flag_key VARCHAR(200) NOT NULL COMMENT 'flag_key',
    enabled_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'enabled_flag',
    revision BIGINT NOT NULL COMMENT 'revision',
    updated_by VARCHAR(100) NOT NULL COMMENT 'updated_by',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'updated_at',
    CONSTRAINT pk_OPS_FEATURE_FLAG_KILL_SWITCH PRIMARY KEY (flag_key),
    CONSTRAINT ck_cpf_ffkill_enabled CHECK (enabled_flag IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Feature Flag Kill Switch';

CREATE TABLE IF NOT EXISTS OPS_FEATURE_FLAG_OVERRIDE (
    override_id VARCHAR(64) NOT NULL COMMENT 'override_id',
    flag_key VARCHAR(200) NOT NULL COMMENT 'flag_key',
    value_type VARCHAR(20) NOT NULL COMMENT 'value_type',
    value_text VARCHAR(2000) NOT NULL COMMENT 'value_text',
    expires_at DATETIME(3) NOT NULL COMMENT 'expires_at',
    override_status VARCHAR(20) NOT NULL COMMENT 'override_status',
    revision BIGINT NOT NULL COMMENT 'revision',
    active_flag_key VARCHAR(200) NULL COMMENT 'active_flag_key',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 일시',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'updated_at',
    CONSTRAINT pk_OPS_FEATURE_FLAG_OVERRIDE PRIMARY KEY (override_id),
    CONSTRAINT uk_cpf_ffovr_active UNIQUE (active_flag_key),
    CONSTRAINT ck_cpf_ffovr_status CHECK (override_status IN ('ACTIVE','SUPERSEDED','REVOKED','EXPIRED')),
    INDEX ix_cpf_ffovr_key (flag_key, override_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Feature Flag 활성 Override';

CREATE TABLE IF NOT EXISTS OPS_FEATURE_FLAG_OVERRIDE_REQUEST (
    request_id VARCHAR(64) NOT NULL COMMENT 'request_id',
    flag_key VARCHAR(200) NOT NULL COMMENT 'flag_key',
    value_type VARCHAR(20) NOT NULL COMMENT 'value_type',
    value_text VARCHAR(2000) NOT NULL COMMENT 'value_text',
    expires_at DATETIME(3) NOT NULL COMMENT 'expires_at',
    requester_id VARCHAR(100) NOT NULL COMMENT 'requester_id',
    request_reason VARCHAR(500) NOT NULL COMMENT 'request_reason',
    request_status VARCHAR(20) NOT NULL COMMENT 'request_status',
    approver_id VARCHAR(100) NULL COMMENT 'approver_id',
    approval_reason VARCHAR(500) NULL COMMENT 'approval_reason',
    active_flag_key VARCHAR(200) NULL COMMENT 'active_flag_key',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 일시',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'updated_at',
    CONSTRAINT pk_OPS_FEATURE_FLAG_OVERRIDE_REQUEST PRIMARY KEY (request_id),
    CONSTRAINT uk_cpf_ffreq_pending UNIQUE (active_flag_key),
    CONSTRAINT ck_cpf_ffreq_status CHECK (request_status IN ('PENDING','APPROVED','REJECTED')),
    INDEX ix_cpf_ffreq_status (request_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Feature Flag Override 승인 요청';

CREATE TABLE IF NOT EXISTS OPS_FEATURE_FLAG_REVISION (
    singleton_id BIGINT NOT NULL COMMENT 'singleton_id',
    revision BIGINT NOT NULL COMMENT 'revision',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'updated_at',
    CONSTRAINT pk_OPS_FEATURE_FLAG_REVISION PRIMARY KEY (singleton_id),
    CONSTRAINT ck_cpf_ffrev_single CHECK (singleton_id = 1),
    CONSTRAINT ck_cpf_ffrev_value CHECK (revision >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Feature Flag 다중 인스턴스 Revision';

CREATE TABLE IF NOT EXISTS OPS_LOG_POLICY (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 순번',
    policy_key VARCHAR(120) NOT NULL COMMENT '로그 정책 키',
    policy_name VARCHAR(150) NOT NULL COMMENT '로그 정책명',
    target_type VARCHAR(30) NOT NULL COMMENT '정책 대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT '정책 대상 ID',
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '기본 로그 레벨',
    db_log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'DB 로그 적재 여부',
    file_log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '파일 로그 출력 여부',
    policy_schema_version INT NOT NULL DEFAULT 2 COMMENT '로그 정책 Schema Version',
    query_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE' COMMENT 'Query Capture Mode',
    request_header_capture_mode VARCHAR(30) NOT NULL DEFAULT 'ALLOWLIST' COMMENT '요청 Header Capture Mode',
    response_header_capture_mode VARCHAR(30) NOT NULL DEFAULT 'ALLOWLIST' COMMENT '응답 Header Capture Mode',
    request_body_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE' COMMENT '요청 Body Capture Mode',
    response_body_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE' COMMENT '응답 Body Capture Mode',
    error_stack_capture_mode VARCHAR(30) NOT NULL DEFAULT 'SUMMARY' COMMENT '오류 Stack Capture Mode',
    query_allowlist VARCHAR(2000) NULL COMMENT 'Query 허용 목록',
    header_allowlist VARCHAR(2000) NULL COMMENT 'Header 허용 목록',
    field_allowlist VARCHAR(2000) NULL COMMENT 'JSONPath/XPath/Fixed Field 허용 목록',
    max_query_bytes INT NOT NULL DEFAULT 4096 COMMENT 'Query 최대 Byte',
    max_header_bytes INT NOT NULL DEFAULT 8192 COMMENT 'Header 최대 Byte',
    max_request_body_bytes INT NOT NULL DEFAULT 65536 COMMENT '요청 Body 최대 Byte',
    max_response_body_bytes INT NOT NULL DEFAULT 65536 COMMENT '응답 Body 최대 Byte',
    max_stack_bytes INT NOT NULL DEFAULT 32768 COMMENT '오류 Stack 최대 Byte',
    policy_checksum VARCHAR(64) NULL COMMENT '정책 SHA-256 Checksum',
    request_body_log_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '요청 본문 로그 여부',
    response_body_log_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '응답 본문 로그 여부',
    error_stack_log_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '오류 stack 로그 여부',
    masking_policy_key VARCHAR(120) NOT NULL DEFAULT 'DEFAULT' COMMENT '마스킹 정책 키',
    retention_days INT NOT NULL DEFAULT 90 COMMENT '보존 일수',
    sampling_rate DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '샘플링 비율',
    priority INT NOT NULL DEFAULT 100 COMMENT '정책 우선순위',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    description VARCHAR(500) NULL COMMENT '정책 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_LOG_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT uk_cpf_log_policy_key UNIQUE (policy_key),
    CONSTRAINT uk_cpf_log_policy_target UNIQUE (target_type, target_id),
    INDEX ix_cpf_log_policy_active (active_yn, target_type, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 로그 정책';

CREATE TABLE IF NOT EXISTS OPS_MANAGED_SERVER (
    managed_server_id VARCHAR(80) NOT NULL COMMENT 'Stable managed server identifier',
    server_name VARCHAR(150) NOT NULL COMMENT 'Canonical server name',
    display_name VARCHAR(200) NOT NULL COMMENT 'Operator display name',
    hostname VARCHAR(200) NULL COMMENT 'Observed or registered hostname; not a primary identity',
    management_identity VARCHAR(200) NULL COMMENT 'Stable authenticated management identity when available',
    environment_code VARCHAR(40) NOT NULL DEFAULT 'default' COMMENT 'Canonical environment',
    server_group VARCHAR(100) NULL COMMENT 'Shared policy group',
    zone_code VARCHAR(60) NULL COMMENT 'Availability zone',
    location VARCHAR(200) NULL COMMENT 'Logical location or site',
    description VARCHAR(500) NULL COMMENT 'Operator description',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Management enabled flag',
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED' COMMENT 'Managed server lifecycle status',
    tags_json LONGTEXT NULL COMMENT 'Operator tags JSON',
    registered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Registration time',
    registered_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Registrar',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater',
    CONSTRAINT pk_OPS_MANAGED_SERVER PRIMARY KEY (managed_server_id),
    CONSTRAINT uk_ops_managed_server_management_identity UNIQUE (management_identity),
    CONSTRAINT ck_ops_managed_server_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT ck_ops_managed_server_status CHECK (status IN ('PENDING','REGISTERED','ACTIVE','DEGRADED','DISABLED','DECOMMISSIONED','UNKNOWN','MAINTENANCE')),
    INDEX ix_ops_managed_server_name (server_name),
    INDEX ix_ops_managed_server_env_status (environment_code, status, enabled_yn),
    INDEX ix_ops_managed_server_group (server_group, environment_code),
    INDEX ix_ops_managed_server_hostname (hostname, environment_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Central managed server master; runtime instances reference this stable identity';

CREATE TABLE IF NOT EXISTS OPS_RESILIENCE_AUDIT (
    audit_id VARCHAR(64) NOT NULL COMMENT 'audit_id',
    event_type VARCHAR(80) NOT NULL COMMENT 'event_type',
    operation_id VARCHAR(200) NOT NULL COMMENT 'operation_id',
    actor_id VARCHAR(100) NULL COMMENT 'actor_id',
    reason_code VARCHAR(256) NULL COMMENT 'reason_code',
    sanitized_attributes VARCHAR(2000) NULL COMMENT 'sanitized_attributes',
    occurred_at DATETIME(3) NOT NULL COMMENT 'occurred_at',
    CONSTRAINT pk_OPS_RESILIENCE_AUDIT PRIMARY KEY (audit_id),
    INDEX ix_cpf_raud_op_time (operation_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Resilience 감사';

CREATE TABLE IF NOT EXISTS OPS_RESILIENCE_POLICY (
    policy_id VARCHAR(64) NOT NULL COMMENT 'policy_id',
    operation_id VARCHAR(200) NOT NULL COMMENT 'operation_id',
    revision BIGINT NOT NULL COMMENT 'revision',
    timeout_ms BIGINT NOT NULL COMMENT 'timeout_ms',
    max_attempts BIGINT NOT NULL COMMENT 'max_attempts',
    retry_backoff_ms BIGINT NOT NULL COMMENT 'retry_backoff_ms',
    circuit_failure_threshold BIGINT NOT NULL COMMENT 'circuit_failure_threshold',
    circuit_open_ms BIGINT NOT NULL COMMENT 'circuit_open_ms',
    bulkhead_max_concurrent BIGINT NOT NULL COMMENT 'bulkhead_max_concurrent',
    rate_limit_permits BIGINT NOT NULL COMMENT 'rate_limit_permits',
    rate_limit_window_ms BIGINT NOT NULL COMMENT 'rate_limit_window_ms',
    idempotent_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'idempotent_flag',
    reconcile_flag CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'reconcile_flag',
    policy_status VARCHAR(20) NOT NULL COMMENT 'policy_status',
    active_operation_key VARCHAR(200) NULL COMMENT 'active_operation_key',
    updated_by VARCHAR(100) NOT NULL COMMENT 'updated_by',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'updated_at',
    CONSTRAINT pk_OPS_RESILIENCE_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT uk_cpf_rpol_active UNIQUE (active_operation_key),
    CONSTRAINT uk_cpf_rpol_rev UNIQUE (operation_id, revision),
    CONSTRAINT ck_cpf_rpol_idem CHECK (idempotent_flag IN ('Y','N')),
    CONSTRAINT ck_cpf_rpol_recon CHECK (reconcile_flag IN ('Y','N')),
    INDEX ix_cpf_rpol_status (policy_status, operation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Resilience 활성 정책';

CREATE TABLE IF NOT EXISTS OPS_RESILIENCE_POLICY_REQUEST (
    request_id VARCHAR(64) NOT NULL COMMENT 'request_id',
    operation_id VARCHAR(200) NOT NULL COMMENT 'operation_id',
    requested_revision BIGINT NOT NULL COMMENT 'requested_revision',
    policy_payload VARCHAR(2000) NOT NULL COMMENT 'policy_payload',
    requester_id VARCHAR(100) NOT NULL COMMENT 'requester_id',
    request_reason VARCHAR(500) NOT NULL COMMENT 'request_reason',
    request_status VARCHAR(20) NOT NULL COMMENT 'request_status',
    approver_id VARCHAR(100) NULL COMMENT 'approver_id',
    approval_reason VARCHAR(500) NULL COMMENT 'approval_reason',
    active_operation_key VARCHAR(200) NULL COMMENT 'active_operation_key',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록 일시',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'updated_at',
    CONSTRAINT pk_OPS_RESILIENCE_POLICY_REQUEST PRIMARY KEY (request_id),
    CONSTRAINT uk_cpf_rpreq_pending UNIQUE (active_operation_key),
    CONSTRAINT ck_cpf_rpreq_status CHECK (request_status IN ('PENDING','APPROVED','REJECTED')),
    INDEX ix_cpf_rpreq_status (request_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Resilience 정책 승인 요청';

CREATE TABLE IF NOT EXISTS OPS_RETENTION_CONTROL_AUDIT (
    audit_id VARCHAR(64) NOT NULL COMMENT 'Retention control audit identity',
    operation_type VARCHAR(40) NOT NULL COMMENT 'POLICY_SAVE/RUN_NOW/RUN_PAUSE/RUN_RESUME/POLICY_PAUSE/POLICY_RESUME',
    target_type VARCHAR(20) NOT NULL COMMENT 'POLICY or RUN',
    target_id VARCHAR(80) NOT NULL COMMENT 'Retention policy/run identity',
    requested_by VARCHAR(100) NOT NULL COMMENT 'Verified request operator',
    approved_by VARCHAR(100) NULL COMMENT 'Verified independent approver',
    approval_request_id VARCHAR(120) NULL COMMENT 'ADM approval request identity',
    reason_text VARCHAR(500) NOT NULL COMMENT 'Operator reason',
    expected_version BIGINT NULL COMMENT 'Approved optimistic policy version',
    result_state VARCHAR(20) NOT NULL DEFAULT 'SUCCEEDED' COMMENT 'Control result state',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Audit creation time',
    CONSTRAINT pk_OPS_RETENTION_CONTROL_AUDIT PRIMARY KEY (audit_id),
    CONSTRAINT ck_ops_retention_audit_result CHECK (result_state IN ('SUCCEEDED','FAILED','UNKNOWN')),
    INDEX ix_ops_retention_audit_target (target_type, target_id, created_at),
    INDEX ix_ops_retention_audit_approval (approval_request_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Immutable operator/approval audit for retention control actions';

CREATE TABLE IF NOT EXISTS OPS_RETENTION_POLICY (
    policy_id VARCHAR(80) NOT NULL COMMENT 'Retention policy identity',
    target_name VARCHAR(80) NOT NULL COMMENT 'Data family/handler target',
    action_name VARCHAR(16) NOT NULL DEFAULT 'KEEP' COMMENT 'KEEP/ARCHIVE/PURGE',
    retention_days INT NOT NULL DEFAULT 90 COMMENT 'Cutoff age in days',
    schedule_expression VARCHAR(100) NULL COMMENT 'Spring cron expression in UTC',
    maintenance_start VARCHAR(8) NULL COMMENT 'UTC HH:mm[:ss] window start',
    maintenance_end VARCHAR(8) NULL COMMENT 'UTC HH:mm[:ss] window end',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Scheduling/execution enabled',
    paused_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Policy scheduling paused',
    legal_hold_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Legal hold disables destructive work',
    chunk_size INT NOT NULL DEFAULT 1000 COMMENT 'Rows per committed chunk',
    throttle_millis BIGINT NOT NULL DEFAULT 0 COMMENT 'Sleep between committed chunks',
    max_rows_per_run BIGINT NOT NULL DEFAULT 100000 COMMENT 'Per-run row processing limit',
    max_runtime_seconds BIGINT NOT NULL DEFAULT 300 COMMENT 'Per-run wall clock limit',
    lease_seconds INT NOT NULL DEFAULT 60 COMMENT 'Single executor lease duration',
    policy_version BIGINT NOT NULL DEFAULT 1 COMMENT 'Operator policy version',
    next_run_at DATETIME(3) NULL COMMENT 'Next scheduler due time',
    last_run_at DATETIME(3) NULL COMMENT 'Last execution completion/release time',
    lease_owner VARCHAR(128) NULL COMMENT 'Current executor runtime instance',
    lease_until DATETIME(3) NULL COMMENT 'Executor lease expiry',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Monotonic executor fencing token',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic metadata version',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last updated time',
    CONSTRAINT pk_OPS_RETENTION_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT ck_ops_retention_policy_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_paused CHECK (paused_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_hold CHECK (legal_hold_yn IN ('Y','N')),
    CONSTRAINT ck_ops_retention_policy_action CHECK (action_name IN ('KEEP','ARCHIVE','PURGE')),
    CONSTRAINT ck_ops_retention_policy_chunk CHECK (chunk_size >= 1 AND chunk_size <= 100000),
    CONSTRAINT ck_ops_retention_policy_limits CHECK (max_rows_per_run >= 1 AND max_runtime_seconds >= 1 AND lease_seconds >= 5),
    INDEX ix_ops_retention_policy_due (enabled_yn, paused_yn, next_run_at),
    INDEX ix_ops_retention_policy_lease (lease_until, lease_owner),
    INDEX ix_ops_retention_policy_target (target_name, action_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Shared retention policy, schedule and single-executor lease state';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_CONTROLLER_LEASE (
    lease_key VARCHAR(60) NOT NULL COMMENT 'Controller lease key',
    holder_id VARCHAR(120) NOT NULL COMMENT 'Lease holder identifier',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Monotonic fencing token',
    lease_until DATETIME(3) NOT NULL COMMENT 'Lease expiry time',
    last_reconciled_at DATETIME(3) NULL COMMENT 'Last reconciliation time',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_CONTROLLER_LEASE PRIMARY KEY (lease_key),
    INDEX ix_cpf_runtime_controller_lease_until (lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Control Plane controller leader lease/fencing';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_INSTANCE_GROUP (
    group_id VARCHAR(80) NOT NULL COMMENT '인스턴스 그룹 ID',
    group_name VARCHAR(150) NOT NULL COMMENT '그룹명',
    parent_group_id VARCHAR(80) NULL COMMENT '상위 그룹 ID',
    environment_code VARCHAR(40) NULL COMMENT 'Target environment code',
    description VARCHAR(500) NULL COMMENT 'Description',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Active flag',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_INSTANCE_GROUP PRIMARY KEY (group_id),
    CONSTRAINT ck_cpf_runtime_group_active CHECK (active_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_runtime_group_parent FOREIGN KEY (parent_group_id) REFERENCES OPS_RUNTIME_INSTANCE_GROUP (group_id),
    INDEX ix_cpf_runtime_group_parent (parent_group_id, active_yn),
    INDEX ix_cpf_runtime_group_env (environment_code, active_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Instance Group/Cell/Zone logical group';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_LOCK (
    lock_key VARCHAR(200) NOT NULL COMMENT '잠금 키',
    owner_id VARCHAR(200) NOT NULL COMMENT '현재 owner',
    request_id VARCHAR(200) NOT NULL COMMENT '획득 요청 ID',
    fencing_token BIGINT NOT NULL COMMENT '단조 증가 fencing token',
    owner_epoch BIGINT NOT NULL COMMENT 'owner takeover epoch',
    version_no BIGINT NOT NULL COMMENT 'optimistic version',
    acquired_at DATETIME(6) NOT NULL COMMENT '획득 시각',
    lease_until DATETIME(6) NOT NULL COMMENT 'lease 만료 시각',
    lock_state VARCHAR(32) NOT NULL COMMENT 'ACTIVE/RELEASED/EXPIRED/FORCE_RELEASED',
    last_reason VARCHAR(1000) NULL COMMENT '마지막 상태 변경 사유',
    updated_at DATETIME(6) NOT NULL COMMENT '최종 변경 시각',
    CONSTRAINT pk_OPS_RUNTIME_LOCK PRIMARY KEY (lock_key),
    CONSTRAINT ck_cpf_runtime_lock_state CHECK (lock_state IN ('ACTIVE','RELEASED','EXPIRED','FORCE_RELEASED')),
    CONSTRAINT ck_cpf_runtime_lock_token CHECK (fencing_token >= 1 AND owner_epoch >= 1 AND version_no >= 1),
    INDEX idx_cpf_runtime_lock_lease (lock_state, lease_until, lock_key),
    INDEX idx_cpf_runtime_lock_owner (owner_id, request_id, lock_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF topology-independent lock 계약의 JDBC durable provider 상태. lease/fencing/owner epoch/stale writer 차단을 지원.';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_POLICY_EVENT (
    event_id VARCHAR(64) NOT NULL COMMENT '내구성 정책 이벤트 ID',
    event_type VARCHAR(50) NOT NULL COMMENT '정책 이벤트 유형',
    aggregate_type VARCHAR(80) NOT NULL COMMENT '정책 대상 유형',
    aggregate_id VARCHAR(200) NOT NULL COMMENT '정책 대상 ID',
    aggregate_version BIGINT NOT NULL COMMENT '정책 버전',
    action_code VARCHAR(50) NOT NULL COMMENT '정책 조치 코드',
    payload_checksum VARCHAR(128) NULL COMMENT '정책 Snapshot Checksum',
    metadata_text TEXT NULL COMMENT '민감정보를 제외한 전달 Metadata',
    reason VARCHAR(1000) NOT NULL COMMENT '운영 변경 사유',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청 운영자',
    occurred_at DATETIME(3) NOT NULL COMMENT '정책 변경 시각',
    event_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '이벤트 상태',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    CONSTRAINT pk_OPS_RUNTIME_POLICY_EVENT PRIMARY KEY (event_id),
    CONSTRAINT ck_cpf_runtime_policy_event_status CHECK (event_status IN ('PENDING', 'RETIRED')),
    INDEX ix_cpf_runtime_policy_event_pending (event_status, event_type, occurred_at, event_id),
    INDEX ix_cpf_runtime_policy_event_aggregate (aggregate_type, aggregate_id, aggregate_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Policy Durable Event';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_RATE_BUCKET (
    bucket_key VARCHAR(180) NOT NULL COMMENT 'Rate bucket identifier',
    subject_id VARCHAR(120) NOT NULL COMMENT 'Rate-limit subject identifier',
    window_start DATETIME(3) NOT NULL COMMENT 'Rate-window start time',
    request_count INT NOT NULL DEFAULT 0 COMMENT 'Rate-window request count',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_RATE_BUCKET PRIMARY KEY (bucket_key),
    CONSTRAINT ck_cpf_runtime_rate_count CHECK (request_count >= 0),
    INDEX ix_cpf_runtime_rate_subject (subject_id, window_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Control Plane distributed per-minute rate bucket';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_VERSION (
    version_key VARCHAR(40) NOT NULL COMMENT '버전 키',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '현재 전역 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_VERSION PRIMARY KEY (version_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Control Plane 단조 증가 버전';

CREATE TABLE IF NOT EXISTS OPS_SCHEMA_INSTALLATION (
    schema_name VARCHAR(64) NOT NULL COMMENT 'CPF 소유 Schema 이름',
    system_code VARCHAR(20) NOT NULL COMMENT '공식 SystemCode',
    database_vendor VARCHAR(20) NOT NULL COMMENT '설치 DB Vendor',
    product_version VARCHAR(50) NOT NULL COMMENT 'CPF 제품 버전',
    baseline_key VARCHAR(100) NOT NULL COMMENT 'Empty Install Baseline 식별자',
    install_state VARCHAR(30) NOT NULL COMMENT '설치 상태',
    installed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 설치 완료 시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF_INSTALLER' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF_INSTALLER' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_OPS_SCHEMA_INSTALLATION PRIMARY KEY (schema_name),
    CONSTRAINT ck_cpf_schema_installation_vendor CHECK (database_vendor IN ('MARIADB', 'POSTGRESQL', 'ORACLE')),
    CONSTRAINT ck_cpf_schema_installation_state CHECK (install_state IN ('PRODUCT_SEEDED')),
    INDEX ix_cpf_schema_installation_system (system_code),
    INDEX ix_cpf_schema_installation_version (product_version, install_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 공식 Empty Install 및 Product Seed Baseline';

CREATE TABLE IF NOT EXISTS OPS_SERVICE (
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    service_name VARCHAR(150) NOT NULL COMMENT '서비스명',
    service_type VARCHAR(30) NOT NULL DEFAULT 'INTERNAL' COMMENT '서비스 유형',
    owner_module_code VARCHAR(20) NOT NULL COMMENT '소유 모듈 코드',
    description VARCHAR(500) NULL COMMENT '서비스 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 row version',
    CONSTRAINT pk_OPS_SERVICE PRIMARY KEY (service_id),
    INDEX ix_cpf_service_owner (owner_module_code, use_yn),
    INDEX ix_cpf_service_type (service_type, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 레지스트리';

CREATE TABLE IF NOT EXISTS OPS_SYSTEM_REGISTRY (
    system_code VARCHAR(32) NOT NULL COMMENT 'CPF System 코드',
    system_name VARCHAR(120) NOT NULL COMMENT 'System 명',
    domain_code VARCHAR(50) NULL COMMENT '소유 Domain 코드',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '등록 System 활성 여부',
    description VARCHAR(500) NULL COMMENT 'System 설명',
    policy_version BIGINT NOT NULL DEFAULT 1 COMMENT 'System 정책 버전',
    first_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 Runtime 발견 시각',
    last_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 Runtime 발견 시각',
    last_instance_id VARCHAR(160) NULL COMMENT '최근 발견 Runtime instanceId',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '최종 수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    CONSTRAINT pk_OPS_SYSTEM_REGISTRY PRIMARY KEY (system_code),
    CONSTRAINT ck_ops_system_registry_enabled CHECK (enabled_yn IN ('Y', 'N')),
    INDEX ix_ops_system_registry_domain (domain_code, enabled_yn),
    INDEX ix_ops_system_registry_seen (last_seen_at, enabled_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 업무 System Registry';

CREATE TABLE IF NOT EXISTS OPS_TRANSACTION_SUBJECT (
    transaction_id VARCHAR(128) NOT NULL COMMENT 'Subject와 연결되는 Canonical transactionId',
    subject_role VARCHAR(32) NOT NULL DEFAULT 'ACTOR' COMMENT 'ACTOR/RELATED/BENEFICIARY/OWNER/TARGET',
    subject_type VARCHAR(32) NOT NULL COMMENT 'CUSTOMER_NO/CUSTOMER_ID/MEMBER_NO/LOGIN_ID',
    subject_search_key VARCHAR(64) NOT NULL COMMENT 'CPF Crypto deterministic protected search token; raw identifier 저장 금지',
    subject_masked_value VARCHAR(256) NOT NULL COMMENT 'ADM 표시용 마스킹 식별값',
    source_type VARCHAR(40) NOT NULL COMMENT 'Subject identity source boundary',
    trust_level VARCHAR(20) NOT NULL DEFAULT 'CLAIMED' COMMENT 'UNVERIFIED/CLAIMED/TRUSTED/VERIFIED',
    search_key_version VARCHAR(64) NOT NULL COMMENT '검색 Token key version',
    first_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이 transaction에서 Subject 최초 확인 시각',
    last_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 동일 Subject 확인 시각',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    CONSTRAINT pk_OPS_TRANSACTION_SUBJECT PRIMARY KEY (transaction_id, subject_role, subject_type, subject_search_key),
    CONSTRAINT ck_ops_transaction_subject_role CHECK (subject_role IN ('ACTOR','RELATED','BENEFICIARY','OWNER','TARGET')),
    CONSTRAINT ck_ops_transaction_subject_type CHECK (subject_type IN ('CUSTOMER_NO','CUSTOMER_ID','MEMBER_NO','LOGIN_ID')),
    CONSTRAINT ck_ops_transaction_subject_trust CHECK (trust_level IN ('UNVERIFIED','CLAIMED','TRUSTED','VERIFIED')),
    INDEX ix_ops_transaction_subject_search (subject_role, subject_type, subject_search_key, first_seen_at),
    INDEX ix_ops_transaction_subject_transaction (transaction_id, first_seen_at),
    INDEX ix_ops_transaction_subject_retention (last_seen_at, transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Protected Subject identifier와 transactionId의 Canonical 검색 관계';

CREATE TABLE IF NOT EXISTS SEC_BFF_CREDENTIAL_VAULT (
    handle_id VARCHAR(64) NOT NULL COMMENT 'Session에 저장되는 난수 Credential Handle',
    key_id VARCHAR(100) NOT NULL COMMENT '암호화 Key 식별자',
    access_iv VARBINARY(32) NOT NULL COMMENT 'Access Token AES-GCM IV',
    access_cipher_text LONGBLOB NOT NULL COMMENT 'Access Token AES-GCM Ciphertext',
    refresh_iv VARBINARY(32) NULL DEFAULT NULL COMMENT 'Refresh Token AES-GCM IV',
    refresh_cipher_text LONGBLOB NULL DEFAULT NULL COMMENT 'Refresh Token AES-GCM Ciphertext',
    access_expires_at DATETIME(6) NOT NULL COMMENT 'Access Token 만료 일시',
    refresh_expires_at DATETIME(6) NOT NULL COMMENT 'Refresh Token 만료 일시',
    created_at DATETIME(6) NOT NULL COMMENT 'Credential 생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT 'Credential 최종 회전 일시',
    version_no BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 회전 버전',
    CONSTRAINT pk_SEC_BFF_CREDENTIAL_VAULT PRIMARY KEY (handle_id),
    CONSTRAINT ck_cpf_bff_credential_version CHECK (version_no > 0),
    CONSTRAINT ck_cpf_bff_credential_expiry CHECK (refresh_expires_at >= access_expires_at),
    INDEX idx_cpf_bff_credential_expiry (refresh_expires_at),
    INDEX idx_cpf_bff_credential_key (key_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM/Backoffice Web BFF Access/Refresh Token 암호화 Vault';

CREATE TABLE IF NOT EXISTS SEC_JWT_KEY (
    KEY_ID VARCHAR(80) NOT NULL COMMENT 'JWT key ID',
    ISSUER VARCHAR(100) NOT NULL COMMENT '토큰 발급자',
    ALGORITHM VARCHAR(20) NOT NULL DEFAULT 'HS256' COMMENT '서명 알고리즘',
    SECRET_REF VARCHAR(500) NOT NULL COMMENT 'Vault/KMS/환경변수 secret 참조',
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    EXPIRE_AT DATETIME NULL COMMENT '만료일시',
    created_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_SEC_JWT_KEY PRIMARY KEY (KEY_ID),
    INDEX ix_cpf_security_jwt_key_issuer (ISSUER, ACTIVE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF JWT key 메타';

CREATE TABLE IF NOT EXISTS SEC_TOKEN_AUDIT_LOG (
    TOKEN_AUDIT_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '토큰 감사 로그 순번',
    TRANSACTION_ID CHAR(34) NULL COMMENT 'CPF transactionId',
    TRACE_ID VARCHAR(80) NULL COMMENT '분산 추적 ID',
    TOKEN_HASH VARCHAR(512) NULL COMMENT '토큰 해시',
    TOKEN_TYPE VARCHAR(30) NOT NULL DEFAULT 'Bearer' COMMENT '토큰 유형',
    ISSUER VARCHAR(100) NULL COMMENT '토큰 발급자',
    SUBJECT VARCHAR(200) NULL COMMENT '토큰 주체',
    AUDIENCE VARCHAR(200) NULL COMMENT '토큰 대상',
    ACTIVE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '활성 여부',
    EXPIRE_AT DATETIME NULL COMMENT '만료일시',
    FAILURE_REASON VARCHAR(1000) NULL COMMENT '검증 실패 사유',
    CLIENT_IP VARCHAR(50) NULL COMMENT '클라이언트 IP',
    created_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_SEC_TOKEN_AUDIT_LOG PRIMARY KEY (TOKEN_AUDIT_ID),
    INDEX ix_cpf_security_token_tx (TRANSACTION_ID),
    INDEX ix_cpf_security_token_hash (TOKEN_HASH),
    INDEX ix_cpf_security_token_subject_time (SUBJECT, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 보안 토큰 감사 로그';

CREATE TABLE IF NOT EXISTS SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL COMMENT '세션 기본 식별자',
    SESSION_ID CHAR(36) NOT NULL COMMENT '세션 ID',
    CREATION_TIME BIGINT NOT NULL COMMENT '생성 시각(epoch millis)',
    LAST_ACCESS_TIME BIGINT NOT NULL COMMENT '마지막 접근 시각(epoch millis)',
    MAX_INACTIVE_INTERVAL INT NOT NULL COMMENT '최대 비활성 유지 시간(초)',
    EXPIRY_TIME BIGINT NOT NULL COMMENT '만료 시각(epoch millis)',
    PRINCIPAL_NAME VARCHAR(100) NULL COMMENT '세션 주체 이름',
    CONSTRAINT pk_SPRING_SESSION PRIMARY KEY (PRIMARY_ID),
    CONSTRAINT SPRING_SESSION_IX1 UNIQUE (SESSION_ID),
    INDEX SPRING_SESSION_IX2 (EXPIRY_TIME),
    INDEX SPRING_SESSION_IX3 (PRINCIPAL_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서버 세션 저장소(Spring Session JDBC 표준 규격)';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_POLICY_STEP (
    POLICY_CODE VARCHAR(80) NOT NULL COMMENT '승인 정책 코드',
    POLICY_VERSION INT NOT NULL COMMENT '승인 정책 버전',
    STEP_NO INT NOT NULL COMMENT '승인 단계',
    STEP_TYPE VARCHAR(30) NOT NULL DEFAULT 'APPROVAL' COMMENT 'APPROVAL/REVIEW',
    TARGET_TYPE VARCHAR(30) NOT NULL COMMENT 'OPERATOR/ROLE/ORGANIZATION/ORG_MANAGER',
    TARGET_CODE VARCHAR(100) NOT NULL COMMENT '대상 운영자/역할/조직 코드',
    DECISION_RULE VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY/N_OF_M',
    REQUIRED_COUNT INT NULL COMMENT 'N_OF_M 최소 승인 수',
    REQUIRED_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 단계 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_POLICY_STEP PRIMARY KEY (POLICY_CODE, POLICY_VERSION, STEP_NO, TARGET_TYPE, TARGET_CODE),
    CONSTRAINT ck_adm_approval_policy_step_no CHECK (STEP_NO >= 1),
    CONSTRAINT ck_adm_approval_policy_step_type CHECK (STEP_TYPE IN ('APPROVAL','REVIEW')),
    CONSTRAINT ck_adm_approval_policy_step_target CHECK (TARGET_TYPE IN ('OPERATOR','ROLE','ORGANIZATION','ORG_MANAGER')),
    CONSTRAINT ck_adm_approval_policy_step_rule CHECK (DECISION_RULE IN ('ALL','ANY','N_OF_M')),
    CONSTRAINT ck_adm_approval_policy_step_required CHECK (REQUIRED_YN IN ('Y','N') AND ( (DECISION_RULE = 'N_OF_M' AND REQUIRED_COUNT IS NOT NULL AND REQUIRED_COUNT > 0) OR (DECISION_RULE <> 'N_OF_M' AND REQUIRED_COUNT IS NULL) )),
    CONSTRAINT fk_adm_approval_policy_step_policy FOREIGN KEY (POLICY_CODE, POLICY_VERSION) REFERENCES ADM_APPROVAL_POLICY (POLICY_CODE, POLICY_VERSION) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 승인 정책 단계';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_REQUEST (
    APPROVAL_REQUEST_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '위험조치 승인 요청 순번',
    REQUEST_KEY VARCHAR(120) NOT NULL COMMENT '멱등 승인 요청 키',
    POLICY_CODE VARCHAR(80) NOT NULL COMMENT '적용 정책 코드',
    POLICY_VERSION INT NOT NULL COMMENT '적용 정책 버전 Snapshot',
    ACTION_TYPE VARCHAR(80) NOT NULL COMMENT '위험조치 유형',
    OWNER_MODULE VARCHAR(30) NOT NULL COMMENT '실제 Command Owner Module',
    OWNER_COMMAND VARCHAR(120) NOT NULL COMMENT '실행할 Owner Command',
    TARGET_TYPE VARCHAR(80) NOT NULL COMMENT '위험조치 대상 유형',
    TARGET_ID VARCHAR(200) NOT NULL COMMENT '위험조치 대상 ID',
    REQUESTED_BY VARCHAR(50) NOT NULL COMMENT '요청 운영자',
    REQUEST_REASON VARCHAR(1000) NOT NULL COMMENT '요청 사유',
    COMMAND_PAYLOAD_HASH CHAR(64) NOT NULL COMMENT '승인 대상 Command payload SHA-256',
    COMMAND_PAYLOAD_SNAPSHOT LONGTEXT NULL COMMENT '마스킹된 승인 대상 Command Snapshot',
    APPROVAL_STATUS VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED/EXPIRED/EXECUTING/COMPLETED/FAILED/UNKNOWN',
    CURRENT_STEP_NO INT NOT NULL DEFAULT 1 COMMENT '현재 승인 단계',
    EXPIRE_AT DATETIME(3) NULL COMMENT '승인 만료시각',
    TRANSACTION_ID CHAR(34) NULL COMMENT 'CPF transactionId',
    VERSION_NO BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_REQUEST PRIMARY KEY (APPROVAL_REQUEST_ID),
    CONSTRAINT uk_adm_approval_request_key UNIQUE (REQUEST_KEY),
    CONSTRAINT ck_adm_approval_request_status CHECK (APPROVAL_STATUS IN ('PENDING','APPROVED','REJECTED','CANCELLED','EXPIRED','EXECUTING','COMPLETED','FAILED','UNKNOWN')),
    CONSTRAINT ck_adm_approval_request_version CHECK (VERSION_NO >= 0),
    CONSTRAINT ck_adm_approval_request_step CHECK (CURRENT_STEP_NO >= 1),
    CONSTRAINT ck_adm_approval_request_hash CHECK (CHAR_LENGTH(COMMAND_PAYLOAD_HASH) = 64),
    CONSTRAINT fk_adm_approval_request_policy FOREIGN KEY (POLICY_CODE, POLICY_VERSION) REFERENCES ADM_APPROVAL_POLICY (POLICY_CODE, POLICY_VERSION),
    INDEX ix_adm_approval_request_status (APPROVAL_STATUS, EXPIRE_AT, APPROVAL_REQUEST_ID),
    INDEX ix_adm_approval_request_actor (REQUESTED_BY, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 위험조치 승인 요청';

CREATE TABLE IF NOT EXISTS ADM_BUTTON (
    BUTTON_ID VARCHAR(80) NOT NULL COMMENT '버튼/행위 ID',
    MENU_ID VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    ACTION_CODE VARCHAR(50) NOT NULL COMMENT '행위 코드',
    BUTTON_NAME VARCHAR(100) NOT NULL COMMENT '버튼/행위명',
    HTTP_METHOD VARCHAR(10) NULL COMMENT '대상 HTTP 메서드',
    API_PATTERN VARCHAR(300) NULL COMMENT '대상 API 경로 패턴',
    SORT_ORDER INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_BUTTON PRIMARY KEY (BUTTON_ID),
    CONSTRAINT uk_adm_button_menu_action UNIQUE (MENU_ID, ACTION_CODE),
    CONSTRAINT fk_adm_button_menu FOREIGN KEY (MENU_ID) REFERENCES ADM_MENU (MENU_ID) ON DELETE CASCADE,
    INDEX ix_adm_button_menu (MENU_ID, SORT_ORDER)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 메뉴별 버튼/행위';

CREATE TABLE IF NOT EXISTS ADM_FILE_JOB_ROW (
    job_id VARCHAR(36) NOT NULL COMMENT 'File Job ID',
    row_no BIGINT NOT NULL COMMENT '행 번호',
    row_state VARCHAR(30) NOT NULL COMMENT '행 처리 상태',
    business_key VARCHAR(200) NULL COMMENT '업무 Key',
    payload_json LONGTEXT NOT NULL COMMENT '보호된 행 Payload',
    error_code VARCHAR(80) NULL COMMENT '행 오류 코드',
    error_message VARCHAR(1000) NULL COMMENT '마스킹된 행 오류',
    rollback_token VARCHAR(1000) NULL COMMENT 'Rollback Token',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    CONSTRAINT pk_ADM_FILE_JOB_ROW PRIMARY KEY (job_id, row_no),
    CONSTRAINT fk_adm_file_job_row_job FOREIGN KEY (job_id) REFERENCES ADM_FILE_JOB (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM File Job 행별 결과 원장';

CREATE TABLE IF NOT EXISTS ADM_INCIDENT_LIFECYCLE (
    incident_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Incident ID',
    policy_id BIGINT NOT NULL COMMENT 'Incident 정책 ID',
    policy_code VARCHAR(100) NOT NULL COMMENT 'Incident 정책 코드',
    severity VARCHAR(20) NOT NULL COMMENT '심각도',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN' COMMENT 'Incident 상태',
    title VARCHAR(300) NOT NULL COMMENT '제목',
    summary VARCHAR(2000) NULL COMMENT '요약',
    source_type VARCHAR(80) NOT NULL COMMENT '발생 소스 유형',
    source_id VARCHAR(160) NOT NULL COMMENT '발생 소스 ID',
    correlation_id VARCHAR(100) NULL COMMENT '상관관계 ID',
    transaction_id VARCHAR(100) NULL COMMENT 'CPF transactionId',
    occurrence_count INT NOT NULL DEFAULT 1 COMMENT '발생 건수',
    escalation_level INT NOT NULL DEFAULT 0 COMMENT 'Escalation 단계',
    first_occurred_at DATETIME(3) NOT NULL COMMENT '최초 발생일시',
    last_occurred_at DATETIME(3) NOT NULL COMMENT '최근 발생일시',
    acknowledged_at DATETIME(3) NULL COMMENT '접수일시',
    resolved_at DATETIME(3) NULL COMMENT '해결일시',
    owner_id VARCHAR(100) NULL COMMENT '담당자 ID',
    active_key VARCHAR(400) NULL COMMENT '활성 Incident 중복 방지 키',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_INCIDENT_LIFECYCLE PRIMARY KEY (incident_id),
    CONSTRAINT uk_adm_incident_lifecycle_active UNIQUE (active_key),
    CONSTRAINT ck_adm_incident_lifecycle_status CHECK (status IN ('OPEN','ACKNOWLEDGED','RESOLVED')),
    CONSTRAINT ck_adm_incident_lifecycle_occurrence CHECK (occurrence_count > 0),
    CONSTRAINT ck_adm_incident_lifecycle_escalation CHECK (escalation_level >= 0),
    CONSTRAINT fk_adm_incident_lifecycle_policy FOREIGN KEY (policy_id) REFERENCES ADM_INCIDENT_POLICY (policy_id),
    INDEX ix_adm_incident_lifecycle_status (status, last_occurred_at, incident_id),
    INDEX ix_adm_incident_lifecycle_source (source_type, source_id, status),
    INDEX ix_adm_incident_lifecycle_trace (correlation_id, transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Notification Incident 원장';

CREATE TABLE IF NOT EXISTS ADM_INCIDENT_SIGNAL (
    signal_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Incident Signal ID',
    policy_id BIGINT NOT NULL COMMENT 'Incident 정책 ID',
    source_type VARCHAR(80) NOT NULL COMMENT '발생 소스 유형',
    source_id VARCHAR(160) NOT NULL COMMENT '발생 소스 ID',
    correlation_id VARCHAR(100) NULL COMMENT '상관관계 ID',
    transaction_id VARCHAR(100) NULL COMMENT 'CPF transactionId',
    title VARCHAR(300) NOT NULL COMMENT '제목',
    summary VARCHAR(2000) NULL COMMENT '요약',
    occurred_at DATETIME(3) NOT NULL COMMENT '발생일시',
    suppressed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Maintenance 억제 여부',
    idempotency_key VARCHAR(160) NOT NULL COMMENT '멱등 키',
    created_by VARCHAR(100) NOT NULL COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    CONSTRAINT pk_ADM_INCIDENT_SIGNAL PRIMARY KEY (signal_id),
    CONSTRAINT uk_adm_incident_signal_idem UNIQUE (idempotency_key),
    CONSTRAINT ck_adm_incident_signal_suppressed CHECK (suppressed_yn IN ('Y','N')),
    CONSTRAINT fk_adm_incident_signal_policy FOREIGN KEY (policy_id) REFERENCES ADM_INCIDENT_POLICY (policy_id),
    INDEX ix_adm_incident_signal_window (policy_id, source_type, source_id, occurred_at),
    INDEX ix_adm_incident_signal_trace (correlation_id, transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Notification Incident Signal 원장';

CREATE TABLE IF NOT EXISTS ADM_MFA_OTP_SECRET (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    SECRET_REF VARCHAR(500) NOT NULL COMMENT 'OTP secret 참조',
    ENABLED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'MFA 사용 여부',
    VERIFIED_AT DATETIME NULL COMMENT 'MFA 검증일시',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_MFA_OTP_SECRET PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT fk_adm_mfa_otp_secret_operator FOREIGN KEY (OPERATOR_ID) REFERENCES ADM_OPERATOR (OPERATOR_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 MFA OTP secret 메타';

CREATE TABLE IF NOT EXISTS ADM_OPERATOR_ROLE (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_OPERATOR_ROLE PRIMARY KEY (OPERATOR_ID, ROLE_ID),
    CONSTRAINT fk_adm_operator_role_operator FOREIGN KEY (OPERATOR_ID) REFERENCES ADM_OPERATOR (OPERATOR_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_operator_role_role FOREIGN KEY (ROLE_ID) REFERENCES ADM_ROLE (ROLE_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 역할 매핑';

CREATE TABLE IF NOT EXISTS ADM_ORGANIZATION (
    ORGANIZATION_CODE VARCHAR(50) NOT NULL COMMENT '운영 조직 코드',
    PARENT_ORGANIZATION_CODE VARCHAR(50) NULL COMMENT '상위 조직 코드',
    ORGANIZATION_NAME VARCHAR(120) NOT NULL COMMENT '운영 조직명',
    ORGANIZATION_TYPE VARCHAR(30) NOT NULL DEFAULT 'DEPARTMENT' COMMENT '조직 유형',
    MANAGER_OPERATOR_ID VARCHAR(50) NULL COMMENT '기본 DB Directory Adapter의 조직 책임자 운영자 ID',
    EFFECTIVE_FROM DATE NULL COMMENT '적용 시작일',
    EFFECTIVE_TO DATE NULL COMMENT '적용 종료일',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_ORGANIZATION PRIMARY KEY (ORGANIZATION_CODE),
    CONSTRAINT ck_adm_organization_use CHECK (USE_YN IN ('Y','N')),
    CONSTRAINT ck_adm_organization_effective CHECK (EFFECTIVE_TO IS NULL OR EFFECTIVE_FROM IS NULL OR EFFECTIVE_TO > EFFECTIVE_FROM),
    CONSTRAINT fk_adm_organization_parent FOREIGN KEY (PARENT_ORGANIZATION_CODE) REFERENCES ADM_ORGANIZATION (ORGANIZATION_CODE) ON DELETE SET NULL,
    CONSTRAINT fk_adm_organization_manager FOREIGN KEY (MANAGER_OPERATOR_ID) REFERENCES ADM_OPERATOR (OPERATOR_ID) ON DELETE SET NULL,
    INDEX ix_adm_organization_parent (PARENT_ORGANIZATION_CODE, USE_YN)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영 조직 Directory 기본 Adapter';

CREATE TABLE IF NOT EXISTS ADM_PASSWORD_HISTORY (
    HISTORY_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '비밀번호 이력 순번',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    PASSWORD_HASH VARCHAR(512) NOT NULL COMMENT '이전 비밀번호 해시',
    CHANGED_REASON VARCHAR(500) NULL COMMENT '변경 사유',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_PASSWORD_HISTORY PRIMARY KEY (HISTORY_ID),
    CONSTRAINT fk_adm_password_history_operator FOREIGN KEY (OPERATOR_ID) REFERENCES ADM_OPERATOR (OPERATOR_ID) ON DELETE CASCADE,
    INDEX ix_adm_password_history_operator_time (OPERATOR_ID, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 비밀번호 변경 이력';

CREATE TABLE IF NOT EXISTS ADM_ROLE_MENU (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    MENU_ID VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    READ_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '조회 권한 여부',
    WRITE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '등록/수정 권한 여부',
    DELETE_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제 권한 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_ROLE_MENU PRIMARY KEY (ROLE_ID, MENU_ID),
    CONSTRAINT fk_adm_role_menu_role FOREIGN KEY (ROLE_ID) REFERENCES ADM_ROLE (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_menu_menu FOREIGN KEY (MENU_ID) REFERENCES ADM_MENU (MENU_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할별 메뉴 권한';

CREATE TABLE IF NOT EXISTS BAT_DEPLOYMENT_EXECUTION (
    deployment_id VARCHAR(80) NOT NULL COMMENT 'Deployment execution identifier',
    cell_id VARCHAR(120) NOT NULL COMMENT 'Target deployment cell identifier',
    idempotency_key VARCHAR(160) NOT NULL COMMENT 'Deployment idempotency key',
    from_version VARCHAR(80) NULL COMMENT 'Previous artifact version',
    to_version VARCHAR(80) NOT NULL COMMENT 'Target artifact version',
    strategy_code VARCHAR(32) NOT NULL COMMENT 'ROLLING/CANARY/BLUE_GREEN strategy',
    execution_state VARCHAR(40) NOT NULL COMMENT 'Deployment execution state',
    failure_stage VARCHAR(80) NULL COMMENT 'Failed deployment stage',
    result_message VARCHAR(4000) NULL COMMENT 'Deployment result detail',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Deployment requester',
    approved_by VARCHAR(120) NOT NULL COMMENT 'Deployment approver',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory deployment reason',
    started_at DATETIME(6) NULL COMMENT 'Deployment start time',
    finished_at DATETIME(6) NULL COMMENT 'Deployment finish time',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Deployment record time',
    idempotency_scope VARCHAR(120) NOT NULL COMMENT 'Cell-scoped deployment idempotency scope',
    request_hash CHAR(64) NOT NULL COMMENT 'Canonical approved deployment request SHA-256',
    expected_version BIGINT NULL COMMENT 'Expected deployment plan version',
    approval_request_id VARCHAR(120) NULL COMMENT 'Approval request identifier',
    reconcile_requested_by VARCHAR(120) NULL COMMENT 'Reconciliation requester',
    reconcile_approved_by VARCHAR(120) NULL COMMENT 'Reconciliation approver',
    reconcile_approval_request_id VARCHAR(120) NULL COMMENT 'Reconciliation approval request identifier',
    reconcile_reason VARCHAR(1000) NULL COMMENT 'Mandatory reconciliation reason',
    reconciled_at DATETIME(6) NULL COMMENT 'Reconciliation completion time',
    CONSTRAINT pk_BAT_DEPLOYMENT_EXECUTION PRIMARY KEY (deployment_id),
    CONSTRAINT uk_bat_deploy_exec_scope_idem UNIQUE (idempotency_scope, idempotency_key),
    CONSTRAINT ck_bat_deploy_exec_request_hash CHECK (request_hash REGEXP '^[0-9a-f]{64}$'),
    CONSTRAINT fk_bat_deployment_execution_cell FOREIGN KEY (cell_id) REFERENCES BAT_DEPLOYMENT_CELL (cell_id),
    INDEX ix_bat_deployment_execution_cell_state (cell_id, execution_state),
    INDEX ix_bat_deploy_exec_request_hash (request_hash),
    INDEX ix_bat_deploy_exec_reconciled (execution_state, reconciled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT approved deployment execution';

CREATE TABLE IF NOT EXISTS BAT_DEPLOYMENT_INSTANCE (
    cell_id VARCHAR(120) NOT NULL COMMENT 'Deployment cell identifier',
    instance_id VARCHAR(160) NOT NULL COMMENT 'Runtime instance identifier',
    host_alias VARCHAR(160) NOT NULL COMMENT 'Target host alias',
    port_no INT NOT NULL COMMENT 'Runtime service port',
    profile_name VARCHAR(80) NOT NULL COMMENT 'Runtime profile name',
    zone_id VARCHAR(80) NULL COMMENT 'Availability zone identifier',
    pool_id VARCHAR(80) NULL COMMENT 'Runtime pool identifier',
    agent_base_url VARCHAR(500) NOT NULL COMMENT 'Approved host-agent base URL',
    config_ref VARCHAR(1000) NULL COMMENT 'External configuration reference',
    desired_state VARCHAR(32) NOT NULL COMMENT 'Desired instance state',
    CONSTRAINT pk_BAT_DEPLOYMENT_INSTANCE PRIMARY KEY (cell_id, instance_id),
    CONSTRAINT uk_bat_deployment_instance_id UNIQUE (instance_id),
    CONSTRAINT fk_bat_deployment_instance_cell FOREIGN KEY (cell_id) REFERENCES BAT_DEPLOYMENT_CELL (cell_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT deployment cell instance projection';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_LINK (
    cpf_execution_id VARCHAR(80) NOT NULL COMMENT 'CPF Batch 실행 식별자',
    link_key VARCHAR(80) NOT NULL COMMENT 'Job/Step 실행 Link 식별 Key',
    job_id VARCHAR(80) NOT NULL COMMENT 'Batch Job ID',
    definition_version BIGINT NOT NULL COMMENT '실행에 고정된 정의 Version',
    spring_job_instance_id BIGINT NOT NULL COMMENT 'Spring Batch JobInstance ID',
    spring_job_execution_id BIGINT NOT NULL COMMENT 'Spring Batch JobExecution ID',
    spring_step_execution_id BIGINT NULL COMMENT 'Spring Batch StepExecution ID',
    spring_status VARCHAR(40) NOT NULL COMMENT 'Spring Batch 실행 상태',
    fencing_token BIGINT NOT NULL COMMENT 'Link 생성 시 검증된 Fencing Token',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Link 생성 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Link 마지막 동기화 시각',
    CONSTRAINT pk_BAT_EXECUTION_LINK PRIMARY KEY (cpf_execution_id, link_key),
    CONSTRAINT ck_cpf_bat_link_fencing CHECK (fencing_token > 0),
    CONSTRAINT fk_cpf_bat_exec_link FOREIGN KEY (cpf_execution_id) REFERENCES BAT_EXECUTION_CONTROL (cpf_execution_id) ON DELETE CASCADE,
    INDEX ix_cpf_bat_link_sb (spring_job_execution_id, spring_step_execution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 실행과 Spring Batch Job/Step Metadata 연결 Projection';

CREATE TABLE IF NOT EXISTS BAT_JOB (
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    job_name VARCHAR(150) NOT NULL COMMENT '배치 Job 이름',
    job_type VARCHAR(30) NOT NULL DEFAULT 'TASKLET' COMMENT '배치 Job 유형',
    description VARCHAR(500) NULL COMMENT '배치 설명',
    restartable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재시작 가능 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    published_definition_version BIGINT NULL COMMENT '현재 Runtime에 고정 반영된 Job Definition Version',
    published_definition_checksum VARCHAR(128) NULL COMMENT 'Published Definition 무결성 Checksum',
    executor_reference VARCHAR(300) NULL COMMENT '검증된 Executor Catalog Reference',
    definition_published_at DATETIME(3) NULL COMMENT 'Published Definition Runtime 반영 시각',
    CONSTRAINT pk_BAT_JOB PRIMARY KEY (job_id),
    CONSTRAINT fk_bat_job_published_definition FOREIGN KEY (job_id, published_definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version),
    INDEX ix_bat_job_use (use_yn, job_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Job 기준';

CREATE TABLE IF NOT EXISTS BAT_JOB_DEPENDENCY (
    job_id VARCHAR(100) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    related_job_id VARCHAR(80) NOT NULL COMMENT '선행 Job',
    condition_code VARCHAR(40) NOT NULL COMMENT '의존 조건',
    timeout_seconds BIGINT NOT NULL DEFAULT 0 COMMENT '대기 Timeout',
    required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 여부',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬',
    CONSTRAINT pk_BAT_JOB_DEPENDENCY PRIMARY KEY (job_id, definition_version, related_job_id),
    CONSTRAINT ck_bat_job_dep_self CHECK (job_id <> related_job_id),
    CONSTRAINT fk_bat_job_dep_def FOREIGN KEY (job_id, definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Versioned Job Dependency';

CREATE TABLE IF NOT EXISTS BAT_JOB_PACK_JOB (
    job_pack_id VARCHAR(120) NOT NULL COMMENT 'Owning job-pack identifier',
    job_id VARCHAR(100) NOT NULL COMMENT 'Published job identifier',
    restartable_yn CHAR(1) NOT NULL COMMENT 'Job restartability flag',
    center_cut_provider_key VARCHAR(100) NULL COMMENT 'Center-cut target provider key',
    center_cut_handler_key VARCHAR(100) NULL COMMENT 'Center-cut item handler key',
    CONSTRAINT pk_BAT_JOB_PACK_JOB PRIMARY KEY (job_pack_id, job_id),
    CONSTRAINT fk_bat_job_pack_job_pack FOREIGN KEY (job_pack_id) REFERENCES BAT_JOB_PACK (job_pack_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT job-pack job projection';

CREATE TABLE IF NOT EXISTS BAT_JOB_PARAMETER_DEFINITION (
    job_id VARCHAR(100) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    parameter_name VARCHAR(100) NOT NULL COMMENT 'Parameter 이름',
    parameter_type VARCHAR(40) NOT NULL COMMENT 'Parameter 유형',
    label_text VARCHAR(200) NULL COMMENT 'UI Label',
    description_text VARCHAR(1000) NULL COMMENT '설명',
    required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '필수 여부',
    sensitive_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감정보 여부',
    default_value VARCHAR(1000) NULL COMMENT '기본값',
    allowed_values TEXT NULL COMMENT '허용값',
    validation_pattern VARCHAR(1000) NULL COMMENT '검증 Pattern',
    min_value DECIMAL(38,10) NULL COMMENT '최솟값',
    max_value DECIMAL(38,10) NULL COMMENT '최댓값',
    min_length INT NULL COMMENT '최소 길이',
    max_length INT NULL COMMENT '최대 길이',
    reference_type VARCHAR(80) NULL COMMENT '참조 유형',
    alias_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Alias 강제',
    runtime_override_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '실행 Override',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬',
    CONSTRAINT pk_BAT_JOB_PARAMETER_DEFINITION PRIMARY KEY (job_id, definition_version, parameter_name),
    CONSTRAINT fk_bat_job_param_def FOREIGN KEY (job_id, definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT Typed Parameter Schema';

CREATE TABLE IF NOT EXISTS BAT_JOB_RUNTIME_PROJECTION (
    job_id VARCHAR(100) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Published Definition Version',
    definition_checksum VARCHAR(64) NOT NULL COMMENT 'Definition Checksum',
    projection_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Projection 상태',
    executor_type VARCHAR(40) NOT NULL COMMENT 'Executor 유형',
    executor_reference VARCHAR(300) NOT NULL COMMENT 'Executor Reference',
    trigger_type VARCHAR(30) NOT NULL COMMENT 'Trigger 유형',
    trigger_expression VARCHAR(500) NULL COMMENT 'Trigger 표현식',
    timezone_id VARCHAR(100) NOT NULL COMMENT 'Timezone',
    projection_json LONGTEXT NOT NULL COMMENT '불변 Runtime Projection JSON',
    projection_hash VARCHAR(64) NOT NULL COMMENT 'Projection SHA-256',
    effective_from DATETIME NULL COMMENT '유효 시작',
    effective_until DATETIME NULL COMMENT '유효 종료',
    published_by VARCHAR(100) NOT NULL COMMENT 'Publish 운영자',
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Publish 시각',
    retired_at DATETIME NULL COMMENT 'Retire 시각',
    row_version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 버전',
    CONSTRAINT pk_BAT_JOB_RUNTIME_PROJECTION PRIMARY KEY (job_id, definition_version),
    CONSTRAINT fk_bat_job_projection_definition FOREIGN KEY (job_id, definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version),
    INDEX ix_bat_job_projection_status (projection_status, effective_from, effective_until),
    UNIQUE INDEX ix_bat_job_projection_hash (projection_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Published Batch Definition Runtime 정본';

CREATE TABLE IF NOT EXISTS BAT_JOB_RUNTIME_PROJECTION_OUTBOX (
    outbox_id VARCHAR(100) NOT NULL COMMENT 'Outbox ID',
    job_id VARCHAR(100) NOT NULL COMMENT 'Job ID',
    definition_version BIGINT NOT NULL COMMENT 'Definition Version',
    event_type VARCHAR(40) NOT NULL COMMENT 'PUBLISH/RETIRE',
    payload_hash VARCHAR(64) NOT NULL COMMENT 'Payload Hash',
    event_payload LONGTEXT NOT NULL COMMENT 'Event Payload',
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'Delivery 상태',
    lease_owner VARCHAR(100) NULL COMMENT 'Lease Owner',
    lease_until DATETIME NULL COMMENT 'Lease 만료',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Fencing Token',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '시도 횟수',
    next_attempt_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '다음 시도',
    last_error_code VARCHAR(100) NULL COMMENT '마지막 오류 코드',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    delivered_at DATETIME NULL COMMENT '전달 시각',
    CONSTRAINT pk_BAT_JOB_RUNTIME_PROJECTION_OUTBOX PRIMARY KEY (outbox_id),
    CONSTRAINT fk_bat_projection_outbox_definition FOREIGN KEY (job_id, definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version),
    INDEX ix_bat_projection_outbox_claim (delivery_status, next_attempt_at, lease_until),
    INDEX ix_bat_projection_outbox_job (job_id, definition_version, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Batch Runtime Projection Durable Outbox';

CREATE TABLE IF NOT EXISTS BAT_RUNTIME_COMMAND_ATTEMPT (
    attempt_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Command attempt identifier',
    command_id VARCHAR(80) NOT NULL COMMENT 'Runtime command identifier',
    attempt_no INT NOT NULL COMMENT 'Command attempt number',
    instance_id VARCHAR(160) NULL COMMENT 'Target runtime instance identifier',
    stage_code VARCHAR(80) NOT NULL COMMENT 'Attempt execution stage',
    attempt_state VARCHAR(40) NOT NULL COMMENT 'Attempt result state',
    result_message VARCHAR(4000) NULL COMMENT 'Attempt result detail',
    started_at DATETIME(6) NOT NULL COMMENT 'Attempt start time',
    finished_at DATETIME(6) NULL COMMENT 'Attempt finish time',
    CONSTRAINT pk_BAT_RUNTIME_COMMAND_ATTEMPT PRIMARY KEY (attempt_id),
    CONSTRAINT uk_bat_runtime_command_attempt UNIQUE (command_id, attempt_no, instance_id, stage_code),
    CONSTRAINT fk_bat_runtime_command_attempt_command FOREIGN KEY (command_id) REFERENCES BAT_RUNTIME_COMMAND (command_id) ON DELETE CASCADE,
    INDEX ix_bat_runtime_command_attempt_instance (instance_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT runtime command execution attempt';

CREATE TABLE IF NOT EXISTS BAT_SB_JOB_EXECUTION (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    VERSION BIGINT NULL COMMENT '낙관적 잠금 버전',
    JOB_INSTANCE_ID BIGINT NOT NULL COMMENT 'Spring Batch JobInstance 순번',
    CREATE_TIME DATETIME(6) NOT NULL COMMENT '실행 생성 일시',
    START_TIME DATETIME(6) NULL DEFAULT NULL COMMENT '실행 시작 일시',
    END_TIME DATETIME(6) NULL DEFAULT NULL COMMENT '실행 종료 일시',
    STATUS VARCHAR(10) NULL COMMENT '실행 상태',
    EXIT_CODE VARCHAR(2500) NULL COMMENT '종료 코드',
    EXIT_MESSAGE VARCHAR(2500) NULL COMMENT '종료 메시지',
    LAST_UPDATED DATETIME(6) NULL COMMENT '마지막 수정 일시',
    CONSTRAINT pk_BAT_SB_JOB_EXECUTION PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID) REFERENCES BAT_SB_JOB_INSTANCE (JOB_INSTANCE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 JobExecution 저장소';

CREATE TABLE IF NOT EXISTS CMN_TEMPLATE_AUDIT (
    AUDIT_ID VARCHAR(64) NOT NULL COMMENT '감사 이벤트 ID',
    TEMPLATE_CODE VARCHAR(100) NOT NULL COMMENT 'Template 식별 코드',
    TEMPLATE_VERSION BIGINT NOT NULL COMMENT 'Template 버전',
    CHANNEL_CODE VARCHAR(30) NOT NULL COMMENT '채널 코드',
    ACTION_TYPE VARCHAR(30) NOT NULL COMMENT 'CREATE_DRAFT/APPROVE/SUPERSEDE/RETIRE',
    REQUEST_USER VARCHAR(100) NOT NULL COMMENT '요청자',
    REQUEST_REASON VARCHAR(500) NOT NULL COMMENT '변경 사유',
    BEFORE_STATUS VARCHAR(30) NULL COMMENT '변경 전 상태',
    AFTER_STATUS VARCHAR(30) NOT NULL COMMENT '변경 후 상태',
    REVISION_NO BIGINT NOT NULL COMMENT '변경 후 Revision',
    OCCURRED_AT DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발생 시각',
    CONSTRAINT pk_CMN_TEMPLATE_AUDIT PRIMARY KEY (AUDIT_ID),
    CONSTRAINT CK_CMN_TEMPLATE_AUDIT_ACTION CHECK (ACTION_TYPE IN ('CREATE_DRAFT','APPROVE','SUPERSEDE','RETIRE')),
    CONSTRAINT CK_CMN_TEMPLATE_AUDIT_REVISION CHECK (REVISION_NO >= 0),
    CONSTRAINT FK_CMN_TEMPLATE_AUDIT_DEFINITION FOREIGN KEY (TEMPLATE_CODE, TEMPLATE_VERSION, CHANNEL_CODE) REFERENCES CMN_TEMPLATE_DEFINITION (TEMPLATE_CODE, TEMPLATE_VERSION, CHANNEL_CODE),
    INDEX IX_CMN_TEMPLATE_AUDIT_LOOKUP (TEMPLATE_CODE, CHANNEL_CODE, TEMPLATE_VERSION, OCCURRED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CMN Template 변경 append-only 감사 원장';

CREATE TABLE IF NOT EXISTS CPF_DATA_QUALITY_OPERATION (
    OPERATION_ID VARCHAR(160) NOT NULL COMMENT 'Replay/reconcile idempotency operation ID',
    OPERATION_TYPE VARCHAR(64) NOT NULL COMMENT 'Operation type',
    QUARANTINE_ID VARCHAR(64) NOT NULL COMMENT 'Target quarantine ID',
    COMMAND_FINGERPRINT CHAR(64) NOT NULL COMMENT 'SHA-256 command fingerprint',
    RESULT_PAYLOAD LONGBLOB NOT NULL COMMENT 'Durable replay result JSON',
    ACTOR_ID VARCHAR(64) NOT NULL COMMENT 'Actor ID',
    ACTION_REASON VARCHAR(1000) NOT NULL COMMENT 'Action reason',
    CREATED_AT TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    CONSTRAINT pk_CPF_DATA_QUALITY_OPERATION PRIMARY KEY (OPERATION_ID),
    CONSTRAINT CK_CPF_DQ_OPERATION_HASH CHECK (CHAR_LENGTH(COMMAND_FINGERPRINT) = 64),
    CONSTRAINT FK_CPF_DQ_OPERATION_QUARANTINE FOREIGN KEY (QUARANTINE_ID) REFERENCES CPF_DATA_QUALITY_QUARANTINE (QUARANTINE_ID),
    INDEX IX_CPF_DQ_OPERATION_TARGET (QUARANTINE_ID, OPERATION_TYPE, CREATED_AT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Persistent DQ replay/reconcile idempotency ledger';

CREATE TABLE IF NOT EXISTS CPF_NOTIFICATION_DELIVERY_LOG (
    delivery_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 발송 로그 순번',
    rule_id BIGINT NULL COMMENT '알림 규칙 순번',
    event_type VARCHAR(80) NOT NULL COMMENT '알림 이벤트 유형',
    target_type VARCHAR(80) NULL COMMENT '알림 대상 유형',
    target_id VARCHAR(120) NULL COMMENT '알림 대상 ID',
    receiver VARCHAR(200) NULL COMMENT '수신자',
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '발송 상태',
    delivery_message VARCHAR(2000) NULL COMMENT '발송 메시지',
    requested_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '발송 요청 일시',
    delivered_at DATETIME(3) NULL COMMENT '발송 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    operation_id VARCHAR(100) NOT NULL COMMENT '멱등 발송 작업 ID',
    request_hash VARCHAR(64) NOT NULL COMMENT '발송 요청 SHA-256',
    payload_body VARCHAR(2000) NOT NULL COMMENT 'Provider 전달 Payload(ADM 응답 비노출)',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '발송 시도 횟수',
    max_attempts INT NOT NULL DEFAULT 3 COMMENT '최대 발송 시도 횟수',
    next_attempt_at DATETIME(3) NULL COMMENT '다음 재시도 시각',
    lease_owner VARCHAR(100) NULL COMMENT '발송 Lease 소유 인스턴스',
    lease_until DATETIME(3) NULL COMMENT '발송 Lease 만료 시각',
    version BIGINT NOT NULL DEFAULT 0 COMMENT 'CAS 버전',
    last_error_code VARCHAR(80) NULL COMMENT '마지막 Provider 오류 코드',
    CONSTRAINT pk_CPF_NOTIFICATION_DELIVERY_LOG PRIMARY KEY (delivery_id),
    CONSTRAINT uk_cpf_notification_delivery_operation UNIQUE (operation_id),
    CONSTRAINT fk_cpf_notification_delivery_rule FOREIGN KEY (rule_id) REFERENCES CPF_NOTIFICATION_RULE (rule_id) ON DELETE SET NULL,
    INDEX ix_cpf_notification_delivery_target (target_type, target_id, requested_at),
    INDEX ix_cpf_notification_delivery_status (delivery_status, requested_at),
    INDEX ix_cpf_notification_delivery_due (delivery_status, next_attempt_at, lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 운영 알림 발송 로그';

CREATE TABLE IF NOT EXISTS CPF_SAGA_MANUAL_ACTION (
    action_id VARCHAR(36) NOT NULL COMMENT 'Action identifier',
    saga_id VARCHAR(100) NOT NULL COMMENT 'Saga identifier',
    action_type VARCHAR(40) NOT NULL COMMENT 'Action type',
    operator_id VARCHAR(100) NOT NULL COMMENT 'Operator identifier',
    reason VARCHAR(1000) NOT NULL COMMENT 'Reason',
    before_status VARCHAR(40) NULL COMMENT 'Before status',
    after_status VARCHAR(40) NULL COMMENT 'After status',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    CONSTRAINT pk_CPF_SAGA_MANUAL_ACTION PRIMARY KEY (action_id),
    CONSTRAINT fk_cpf_saga_manual_action FOREIGN KEY (saga_id) REFERENCES CPF_SAGA_EXECUTION (saga_id),
    INDEX idx_cpf_saga_manual (saga_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Saga 수동 복구 조치';

CREATE TABLE IF NOT EXISTS CPF_SAGA_STEP_EXECUTION (
    saga_id VARCHAR(100) NOT NULL COMMENT 'Saga identifier',
    step_no INT NOT NULL COMMENT 'Step number',
    step_id VARCHAR(100) NOT NULL COMMENT 'Step identifier',
    step_status VARCHAR(40) NOT NULL COMMENT 'Step status',
    result_code VARCHAR(100) NULL COMMENT 'Result code',
    result_snapshot TEXT NULL COMMENT 'Result snapshot',
    error_message VARCHAR(2000) NULL COMMENT 'Error message',
    execute_attempts INT NOT NULL DEFAULT 0 COMMENT 'Execute attempts',
    compensation_attempts INT NOT NULL DEFAULT 0 COMMENT 'Compensation attempts',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Creation time',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Last update time',
    CONSTRAINT pk_CPF_SAGA_STEP_EXECUTION PRIMARY KEY (saga_id, step_no),
    CONSTRAINT fk_cpf_saga_step_execution FOREIGN KEY (saga_id) REFERENCES CPF_SAGA_EXECUTION (saga_id),
    INDEX idx_cpf_saga_step_status (step_status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Saga 단계 실행 원장';

CREATE TABLE IF NOT EXISTS CPF_TRANSACTION_LOG_DETAIL (
    DETAIL_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '거래 상세 로그 순번',
    LOG_IDX BIGINT NOT NULL COMMENT '거래 로그 순번',
    DETAIL_KEY VARCHAR(100) NOT NULL DEFAULT 'N/A' COMMENT '상세 항목 키',
    DETAIL_VALUE MEDIUMTEXT NOT NULL COMMENT '상세 항목 값',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_CPF_TRANSACTION_LOG_DETAIL PRIMARY KEY (DETAIL_ID),
    CONSTRAINT fk_cpf_transaction_log_detail_log FOREIGN KEY (LOG_IDX) REFERENCES CPF_TRANSACTION_LOG (LOG_IDX) ON DELETE CASCADE,
    INDEX ix_cpf_transaction_log_detail_log_key (LOG_IDX, DETAIL_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 거래 상세 로그';

CREATE TABLE IF NOT EXISTS GW_BINDING (
    binding_id VARCHAR(100) NOT NULL COMMENT 'Binding ID',
    route_id VARCHAR(100) NOT NULL COMMENT 'Route ID',
    environment_code VARCHAR(50) NOT NULL COMMENT '환경 코드',
    host_pattern VARCHAR(300) NOT NULL COMMENT 'Host Pattern',
    path_pattern VARCHAR(500) NOT NULL COMMENT 'Path Pattern',
    target_path VARCHAR(500) NOT NULL COMMENT 'Ingress Path Pattern과 분리된 실제 Target Request Path Template',
    http_method VARCHAR(20) NOT NULL DEFAULT '*' COMMENT 'HTTP Method',
    api_version VARCHAR(50) NOT NULL COMMENT 'API Version',
    ingress_protocol VARCHAR(30) NOT NULL COMMENT 'Ingress Protocol',
    target_protocol VARCHAR(30) NOT NULL COMMENT 'Target Protocol',
    service_id VARCHAR(100) NOT NULL COMMENT '서비스 ID',
    server_group_id VARCHAR(100) NOT NULL COMMENT '서버 그룹 ID',
    route_version VARCHAR(100) NOT NULL COMMENT 'Route Version',
    tls_policy_id VARCHAR(100) NULL COMMENT 'TLS 정책',
    authentication_policy_id VARCHAR(100) NULL COMMENT '인증 정책',
    authorization_policy_id VARCHAR(100) NULL COMMENT '권한 정책',
    header_policy_id VARCHAR(100) NULL COMMENT 'Header 정책',
    rate_limit_policy_id VARCHAR(100) NULL COMMENT 'Rate Limit 정책',
    health_policy_id VARCHAR(100) NULL COMMENT 'Health 정책',
    connect_timeout_ms INT NOT NULL COMMENT 'Connect Timeout',
    response_timeout_ms INT NOT NULL COMMENT 'Response Timeout',
    overall_timeout_ms INT NOT NULL COMMENT 'Overall Timeout',
    max_retry_count INT NOT NULL DEFAULT 0 COMMENT '최대 재시도',
    idempotent_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '멱등 여부',
    failover_group_id VARCHAR(100) NULL COMMENT 'Failover 그룹',
    gateway_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Gateway 공개 허용',
    direct_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '직접 호출 허용',
    binding_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT 'Binding 상태',
    approval_id VARCHAR(100) NULL COMMENT '승인 ID',
    effective_from DATETIME NULL COMMENT '시행 시작',
    effective_to DATETIME NULL COMMENT '시행 종료',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 잠금 버전',
    binding_key_hash CHAR(64) NOT NULL COMMENT 'Route Match Key SHA-256',
    binding_checksum VARCHAR(64) NULL COMMENT '승인/적용 대상 Snapshot SHA-256',
    retired_by VARCHAR(100) NULL COMMENT 'Retire 운영자',
    retired_at DATETIME NULL COMMENT 'Retire 시각',
    CONSTRAINT pk_GW_BINDING PRIMARY KEY (binding_id),
    CONSTRAINT uk_cpf_gwy_binding_key UNIQUE (binding_key_hash),
    CONSTRAINT ck_cpf_gwy_binding_gateway CHECK (gateway_allowed_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_gwy_binding_direct CHECK (direct_allowed_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_gwy_binding_idempotent CHECK (idempotent_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_binding_group FOREIGN KEY (server_group_id) REFERENCES GW_SERVER_GROUP (server_group_id),
    INDEX ix_cpf_gwy_binding_route (environment_code, route_id, binding_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Versioned Binding';

CREATE TABLE IF NOT EXISTS GW_HEALTH_HISTORY (
    health_history_id VARCHAR(100) NOT NULL COMMENT 'Health 이력 ID',
    server_group_id VARCHAR(100) NOT NULL COMMENT 'Server Group ID',
    instance_id VARCHAR(100) NOT NULL COMMENT 'Instance ID',
    gateway_instance_id VARCHAR(100) NOT NULL COMMENT 'Probe Gateway Instance',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Probe Fencing Token',
    network_status VARCHAR(30) NOT NULL COMMENT 'Network 상태',
    tcp_status VARCHAR(30) NOT NULL COMMENT 'TCP 상태',
    tls_status VARCHAR(30) NOT NULL COMMENT 'TLS 상태',
    application_status VARCHAR(30) NOT NULL COMMENT 'Application 상태',
    overall_status VARCHAR(30) NOT NULL COMMENT '합성 상태',
    result_code VARCHAR(100) NULL COMMENT '결과 코드',
    duration_ms BIGINT NOT NULL DEFAULT 0 COMMENT 'Probe 소요시간',
    observed_at DATETIME NOT NULL COMMENT '관측 시각',
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 시각',
    CONSTRAINT pk_GW_HEALTH_HISTORY PRIMARY KEY (health_history_id),
    CONSTRAINT fk_cpf_gwy_health_group FOREIGN KEY (server_group_id) REFERENCES GW_SERVER_GROUP (server_group_id),
    INDEX ix_cpf_gwy_health_member (server_group_id, instance_id, observed_at),
    INDEX ix_cpf_gwy_health_status (overall_status, observed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Protocol 단계별 Health 불변 이력';

CREATE TABLE IF NOT EXISTS GW_RATE_LIMIT_REQUEST (
    counter_key VARCHAR(300) NOT NULL COMMENT 'Rate Limit scope 식별 키',
    policy_version BIGINT NOT NULL COMMENT 'Rate Limit 정책 버전',
    window_start_ms BIGINT NOT NULL COMMENT 'Window 시작 epoch millisecond',
    request_id VARCHAR(200) NOT NULL COMMENT '멱등 요청 ID',
    request_hash VARCHAR(64) NOT NULL COMMENT '요청 payload SHA-256',
    accepted INT NOT NULL COMMENT '허용 여부 1/0',
    used_units BIGINT NOT NULL COMMENT '판정 후 소비 Unit',
    remaining_units BIGINT NOT NULL COMMENT '판정 후 잔여 Unit',
    reset_at_ms BIGINT NOT NULL COMMENT 'Window reset epoch millisecond',
    blocked_until_ms BIGINT NOT NULL DEFAULT 0 COMMENT 'Block 종료 epoch millisecond',
    rejected_count INT NOT NULL DEFAULT 0 COMMENT '거절 누적 횟수',
    reason VARCHAR(200) NOT NULL COMMENT 'Rate Limit 판정 사유',
    limiting_index INT NOT NULL DEFAULT -1 COMMENT 'Atomic batch 제한 Scope index',
    CONSTRAINT pk_GW_RATE_LIMIT_REQUEST PRIMARY KEY (counter_key, policy_version, window_start_ms, request_id),
    CONSTRAINT ck_gw_rate_limit_request_accepted CHECK (accepted IN (0,1)),
    CONSTRAINT fk_gw_rate_limit_request_counter FOREIGN KEY (counter_key, policy_version, window_start_ms) REFERENCES GW_RATE_LIMIT_COUNTER (counter_key, policy_version, window_start_ms),
    INDEX ix_gw_rate_limit_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Rate Limit 멱등 요청 Journal';

CREATE TABLE IF NOT EXISTS GW_SERVER_GROUP_MEMBER (
    server_group_id VARCHAR(100) NOT NULL COMMENT '서버 그룹 ID',
    instance_id VARCHAR(100) NOT NULL COMMENT 'Instance ID',
    weight INT NOT NULL DEFAULT 1 COMMENT '가중치',
    priority_no INT NOT NULL DEFAULT 0 COMMENT '우선순위',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    effective_status VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN' COMMENT '합성 Health 상태',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Fencing Token',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    canary_percent INT NOT NULL DEFAULT 0 COMMENT '결정적 Canary 비율',
    probe_owner_id VARCHAR(100) NULL COMMENT 'Health Probe Lease Owner',
    probe_lease_until DATETIME NULL COMMENT 'Health Probe Lease 만료',
    last_probe_at DATETIME NULL COMMENT '마지막 Probe 시각',
    last_probe_code VARCHAR(100) NULL COMMENT '마지막 Probe 결과 코드',
    consecutive_successes INT NOT NULL DEFAULT 0 COMMENT '연속 성공 횟수',
    consecutive_failures INT NOT NULL DEFAULT 0 COMMENT '연속 실패 횟수',
    active_requests BIGINT NOT NULL DEFAULT 0 COMMENT '활성 요청 수',
    ewma_latency_ms DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT 'EWMA 지연시간',
    CONSTRAINT pk_GW_SERVER_GROUP_MEMBER PRIMARY KEY (server_group_id, instance_id),
    CONSTRAINT ck_cpf_gwy_member_enabled CHECK (enabled_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_member_group FOREIGN KEY (server_group_id) REFERENCES GW_SERVER_GROUP (server_group_id),
    INDEX ix_cpf_gwy_member_status (server_group_id, enabled_yn, effective_status, priority_no),
    INDEX ix_cpf_gwy_member_probe_lease (enabled_yn, probe_lease_until, server_group_id, instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Server Group Member';

CREATE TABLE IF NOT EXISTS OPS_LOG_POLICY_OVERRIDE (
    override_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 override 순번',
    policy_id BIGINT NULL COMMENT '기본 로그 정책 순번',
    target_type VARCHAR(30) NOT NULL COMMENT 'override 대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT 'override 대상 ID',
    override_reason VARCHAR(500) NOT NULL COMMENT 'override 사유',
    log_level VARCHAR(20) NULL COMMENT '임시 로그 레벨',
    db_log_enabled_yn CHAR(1) NULL COMMENT 'DB 로그 임시 적재 여부',
    file_log_enabled_yn CHAR(1) NULL COMMENT '파일 로그 임시 출력 여부',
    policy_schema_version INT NULL COMMENT '로그 정책 Schema Version',
    query_capture_mode VARCHAR(30) NULL COMMENT 'Query Capture Mode',
    request_header_capture_mode VARCHAR(30) NULL COMMENT '요청 Header Capture Mode',
    response_header_capture_mode VARCHAR(30) NULL COMMENT '응답 Header Capture Mode',
    request_body_capture_mode VARCHAR(30) NULL COMMENT '요청 Body Capture Mode',
    response_body_capture_mode VARCHAR(30) NULL COMMENT '응답 Body Capture Mode',
    error_stack_capture_mode VARCHAR(30) NULL COMMENT '오류 Stack Capture Mode',
    query_allowlist VARCHAR(2000) NULL COMMENT 'Query 허용 목록',
    header_allowlist VARCHAR(2000) NULL COMMENT 'Header 허용 목록',
    field_allowlist VARCHAR(2000) NULL COMMENT 'JSONPath/XPath/Fixed Field 허용 목록',
    max_query_bytes INT NULL COMMENT 'Query 최대 Byte',
    max_header_bytes INT NULL COMMENT 'Header 최대 Byte',
    max_request_body_bytes INT NULL COMMENT '요청 Body 최대 Byte',
    max_response_body_bytes INT NULL COMMENT '응답 Body 최대 Byte',
    max_stack_bytes INT NULL COMMENT '오류 Stack 최대 Byte',
    policy_checksum VARCHAR(64) NULL COMMENT '정책 SHA-256 Checksum',
    request_body_log_yn CHAR(1) NULL COMMENT '요청 본문 임시 로그 여부',
    response_body_log_yn CHAR(1) NULL COMMENT '응답 본문 임시 로그 여부',
    error_stack_log_yn CHAR(1) NULL COMMENT '오류 stack 임시 로그 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '임시 마스킹 정책 키',
    effective_start_at DATETIME(3) NOT NULL COMMENT '적용 시작일시',
    effective_end_at DATETIME(3) NOT NULL COMMENT '적용 종료일시',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청자',
    approved_by VARCHAR(100) NULL COMMENT '승인자',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_LOG_POLICY_OVERRIDE PRIMARY KEY (override_id),
    CONSTRAINT fk_cpf_log_policy_override_policy FOREIGN KEY (policy_id) REFERENCES OPS_LOG_POLICY (policy_id) ON DELETE SET NULL,
    INDEX ix_cpf_log_policy_override_target (target_type, target_id, active_yn),
    INDEX ix_cpf_log_policy_override_period (effective_start_at, effective_end_at, active_yn),
    INDEX ix_cpf_log_policy_override_policy (policy_id, active_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 로그 정책 임시 override';

CREATE TABLE IF NOT EXISTS OPS_OPERATION_CATALOG (
    operation_id VARCHAR(160) NOT NULL COMMENT '업무 Domain Canonical operationId',
    operation_name VARCHAR(150) NOT NULL COMMENT '업무 Operation 명',
    description VARCHAR(1000) NULL COMMENT '업무 Operation 설명',
    system_code VARCHAR(32) NOT NULL COMMENT 'Operation 소유 System 코드',
    domain_code VARCHAR(50) NULL COMMENT 'Operation 소유 Domain 코드',
    application_code VARCHAR(100) NULL COMMENT '발견 Application 코드',
    http_method VARCHAR(20) NOT NULL DEFAULT 'ANY' COMMENT 'HTTP 메서드',
    api_path VARCHAR(500) NOT NULL COMMENT 'Canonical API 경로',
    controller_class VARCHAR(255) NOT NULL COMMENT 'Controller 클래스명',
    handler_method VARCHAR(150) NOT NULL COMMENT 'Handler 메서드명',
    openapi_operation_id VARCHAR(160) NULL COMMENT '업무 API OpenAPI operationId',
    source_fingerprint VARCHAR(64) NULL COMMENT 'Source/Handler 계약 SHA-256 fingerprint',
    discovery_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 또는 NOT_DISCOVERED 발견상태',
    first_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 발견 시각',
    last_seen_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 발견 시각',
    last_instance_id VARCHAR(160) NULL COMMENT '최근 발견 Runtime instanceId',
    metadata_version BIGINT NOT NULL DEFAULT 1 COMMENT 'Source metadata optimistic version',
    log_policy_key VARCHAR(120) NULL COMMENT '연결 로그 정책 키',
    sensitive_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감 Operation 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '마스킹 정책 키',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '최종 수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    CONSTRAINT pk_OPS_OPERATION_CATALOG PRIMARY KEY (operation_id),
    CONSTRAINT ck_ops_operation_catalog_discovery CHECK (discovery_status IN ('ACTIVE', 'NOT_DISCOVERED')),
    CONSTRAINT ck_ops_operation_catalog_sensitive CHECK (sensitive_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_catalog_system FOREIGN KEY (system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code),
    INDEX ix_ops_operation_catalog_system (system_code, domain_code, discovery_status),
    INDEX ix_ops_operation_catalog_path (http_method, api_path),
    INDEX ix_ops_operation_catalog_seen (last_seen_at, discovery_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 업무 Online Operation Canonical Catalog';

CREATE TABLE IF NOT EXISTS OPS_RETENTION_RUN (
    run_id VARCHAR(64) NOT NULL COMMENT 'Retention execution identity',
    policy_id VARCHAR(80) NOT NULL COMMENT 'Retention policy',
    trigger_type VARCHAR(16) NOT NULL COMMENT 'SCHEDULED/MANUAL/RESUME',
    status VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'Run lifecycle',
    runtime_instance_id VARCHAR(128) NOT NULL COMMENT 'Central runtime instance executing the run',
    actor_id VARCHAR(100) NOT NULL COMMENT 'Operator/scheduler actor',
    reason VARCHAR(500) NOT NULL COMMENT 'Execution reason',
    policy_version BIGINT NOT NULL COMMENT 'Policy version captured at execution',
    cutoff_at DATETIME(3) NULL COMMENT 'Retention cutoff',
    started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Run start',
    completed_at DATETIME(3) NULL COMMENT 'Run completion',
    matched_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Eligible rows observed',
    archived_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Archived rows',
    deleted_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Deleted rows',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Committed processed rows',
    compressed_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Compressed artifacts if applicable',
    freed_bytes BIGINT NOT NULL DEFAULT 0 COMMENT 'Freed bytes if measurable',
    pause_requested_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Pause requested; honored at chunk boundary',
    control_actor_id VARCHAR(100) NULL COMMENT 'Last pause/resume control actor',
    control_reason VARCHAR(500) NULL COMMENT 'Last pause/resume control reason',
    error_code VARCHAR(100) NULL COMMENT 'Sanitized failure code',
    error_summary VARCHAR(500) NULL COMMENT 'Sanitized failure summary',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last updated time',
    CONSTRAINT pk_OPS_RETENTION_RUN PRIMARY KEY (run_id),
    CONSTRAINT ck_ops_retention_run_trigger CHECK (trigger_type IN ('SCHEDULED','MANUAL','RESUME')),
    CONSTRAINT ck_ops_retention_run_status CHECK (status IN ('RUNNING','SUCCESS','PARTIAL','PAUSED','SKIPPED','FAILED')),
    CONSTRAINT ck_ops_retention_run_pause CHECK (pause_requested_yn IN ('Y','N')),
    CONSTRAINT fk_ops_retention_run_policy FOREIGN KEY (policy_id) REFERENCES OPS_RETENTION_POLICY (policy_id),
    INDEX ix_ops_retention_run_policy_time (policy_id, started_at),
    INDEX ix_ops_retention_run_status_time (status, started_at),
    INDEX ix_ops_retention_run_runtime (runtime_instance_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Retention run history and actual execution result';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_CHANGE (
    change_id VARCHAR(80) NOT NULL COMMENT 'Runtime change identifier',
    operation_id VARCHAR(100) NOT NULL COMMENT 'Idempotent operation identifier',
    change_type VARCHAR(80) NOT NULL COMMENT 'Runtime change type',
    payload_schema_version INT NOT NULL DEFAULT 1 COMMENT 'Payload schema version',
    request_hash VARCHAR(64) NOT NULL COMMENT 'Request fingerprint checksum',
    payload_hash VARCHAR(64) NOT NULL COMMENT '실제 payload canonical hash',
    payload_json LONGTEXT NOT NULL COMMENT 'Change payload JSON',
    rollback_payload_json LONGTEXT NULL COMMENT 'Rollback payload JSON',
    target_snapshot_json LONGTEXT NOT NULL COMMENT 'Change target snapshot JSON',
    desired_version BIGINT NOT NULL COMMENT 'Desired state version',
    rollout_mode VARCHAR(30) NOT NULL DEFAULT 'ALL_AT_ONCE' COMMENT 'Rollout mode',
    wave_size INT NOT NULL DEFAULT 100 COMMENT 'Rollout wave size',
    quorum_percent INT NOT NULL DEFAULT 100 COMMENT 'Required rollout quorum percent',
    change_state VARCHAR(30) NOT NULL DEFAULT 'APPLYING' COMMENT 'Runtime change lifecycle state',
    scheduled_at DATETIME(3) NULL COMMENT 'Scheduled execution time',
    expires_at DATETIME(3) NULL COMMENT 'Expiry time',
    reason VARCHAR(1000) NOT NULL COMMENT 'Mandatory operation reason',
    approval_id VARCHAR(100) NULL COMMENT 'Approval request identifier',
    break_glass_id VARCHAR(100) NULL COMMENT 'Break-glass authorization identifier',
    requested_by VARCHAR(100) NOT NULL COMMENT 'Requester identifier',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_CHANGE PRIMARY KEY (change_id),
    CONSTRAINT ux_cpf_runtime_change_operation UNIQUE (operation_id),
    CONSTRAINT ck_cpf_runtime_change_state CHECK (change_state IN ('SCHEDULED','APPLYING','PARTIAL','SUCCESS','FAILED','CANCELLED','EXPIRED','ROLLBACK_PENDING','ROLLED_BACK','SUPERSEDED','UNKNOWN_RESULT','RECOVERED')),
    CONSTRAINT fk_cpf_runtime_change_operation FOREIGN KEY (operation_id) REFERENCES OPS_CONTROL_OPERATION (operation_id),
    INDEX ix_cpf_runtime_change_state (change_state, scheduled_at, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime 변경 immutable target snapshot/desired state';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_POLICY_DELIVERY (
    event_id VARCHAR(64) NOT NULL COMMENT '정책 이벤트 ID',
    consumer_id VARCHAR(100) NOT NULL COMMENT 'Runtime Instance ID',
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '전달 상태',
    attempt_count INT NOT NULL DEFAULT 0 COMMENT '전달 시도 횟수',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Claim Fencing Token',
    leased_until DATETIME(3) NULL COMMENT 'Claim 만료시각',
    error_code VARCHAR(100) NULL COMMENT '적용 오류 코드',
    error_message VARCHAR(1000) NULL COMMENT '민감정보 제거 오류 메시지',
    acknowledged_at DATETIME(3) NULL COMMENT 'ACK 시각',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_OPS_RUNTIME_POLICY_DELIVERY PRIMARY KEY (event_id, consumer_id),
    CONSTRAINT ck_cpf_runtime_policy_delivery_status CHECK (delivery_status IN ('PENDING', 'CLAIMED', 'APPLIED', 'FAILED', 'IGNORED')),
    CONSTRAINT ck_cpf_runtime_policy_delivery_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_cpf_runtime_policy_delivery_fencing CHECK (fencing_token >= 0),
    CONSTRAINT fk_cpf_runtime_policy_delivery_event FOREIGN KEY (event_id) REFERENCES OPS_RUNTIME_POLICY_EVENT (event_id) ON DELETE CASCADE,
    INDEX ix_cpf_runtime_policy_delivery_status (consumer_id, delivery_status, leased_until, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Policy Consumer Delivery ACK';

CREATE TABLE IF NOT EXISTS OPS_SERVICE_ENDPOINT (
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_name VARCHAR(150) NOT NULL COMMENT 'Endpoint명',
    endpoint_type VARCHAR(30) NOT NULL DEFAULT 'HTTP' COMMENT 'Endpoint 유형',
    base_url VARCHAR(500) NOT NULL COMMENT '기본 URL',
    context_path VARCHAR(200) NULL COMMENT 'Context path',
    default_timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '기본 timeout 밀리초',
    default_retry_count INT NOT NULL DEFAULT 0 COMMENT '기본 retry 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 row version',
    CONSTRAINT pk_OPS_SERVICE_ENDPOINT PRIMARY KEY (endpoint_code),
    CONSTRAINT fk_cpf_service_endpoint_service FOREIGN KEY (service_id) REFERENCES OPS_SERVICE (service_id),
    INDEX ix_cpf_service_endpoint_service (service_id, use_yn),
    INDEX ix_cpf_service_endpoint_type (endpoint_type, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 Endpoint 레지스트리';

CREATE TABLE IF NOT EXISTS OPS_SYSTEM_DOMAIN_ACCESS (
    caller_system_code VARCHAR(32) NOT NULL COMMENT '호출 System 코드',
    target_system_code VARCHAR(32) NOT NULL COMMENT '대상 System 코드',
    allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'System/Domain 1차 호출 허용 여부',
    policy_version BIGINT NOT NULL DEFAULT 1 COMMENT '정책 버전',
    change_reason VARCHAR(500) NULL COMMENT '최근 변경 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '최종 수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    CONSTRAINT pk_OPS_SYSTEM_DOMAIN_ACCESS PRIMARY KEY (caller_system_code, target_system_code),
    CONSTRAINT ck_ops_system_domain_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_sys_domain_caller FOREIGN KEY (caller_system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code) ON DELETE CASCADE,
    CONSTRAINT fk_ops_sys_domain_target FOREIGN KEY (target_system_code) REFERENCES OPS_SYSTEM_REGISTRY (system_code) ON DELETE CASCADE,
    INDEX ix_ops_system_domain_access_target (target_system_code, allowed_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF System/Domain 1차 호출 정책';

CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL COMMENT '세션 기본 식별자',
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL COMMENT '세션 속성 이름',
    ATTRIBUTE_BYTES BLOB NOT NULL COMMENT '직렬화된 세션 속성 값',
    CONSTRAINT pk_SPRING_SESSION_ATTRIBUTES PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서버 세션 속성 저장소(Spring Session JDBC 표준 규격)';

CREATE TABLE IF NOT EXISTS ADM_API_PERMISSION (
    API_PERMISSION_ID VARCHAR(120) NOT NULL COMMENT 'API 권한 ID',
    API_GROUP_CODE VARCHAR(50) NOT NULL COMMENT 'API 그룹 코드',
    HTTP_METHOD VARCHAR(10) NOT NULL COMMENT 'HTTP 메서드',
    API_PATH VARCHAR(300) NOT NULL COMMENT 'API 경로 패턴',
    API_NAME VARCHAR(150) NOT NULL COMMENT 'API명',
    PERMISSION_CODE VARCHAR(50) NOT NULL COMMENT '권한 코드',
    MENU_ID VARCHAR(50) NULL COMMENT '연결 메뉴 ID',
    BUTTON_ID VARCHAR(80) NULL COMMENT '연결 버튼/행위 ID',
    USE_YN CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_API_PERMISSION PRIMARY KEY (API_PERMISSION_ID),
    CONSTRAINT uk_adm_api_permission_method_path UNIQUE (HTTP_METHOD, API_PATH),
    CONSTRAINT fk_adm_api_permission_menu FOREIGN KEY (MENU_ID) REFERENCES ADM_MENU (MENU_ID) ON DELETE SET NULL,
    CONSTRAINT fk_adm_api_permission_button FOREIGN KEY (BUTTON_ID) REFERENCES ADM_BUTTON (BUTTON_ID) ON DELETE SET NULL,
    INDEX ix_adm_api_permission_group (API_GROUP_CODE, USE_YN),
    INDEX ix_adm_api_permission_menu (MENU_ID, BUTTON_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM API 권한';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_EXECUTION (
    APPROVAL_REQUEST_ID BIGINT NOT NULL COMMENT '승인 요청 순번',
    COMMAND_REQUEST_ID VARCHAR(120) NOT NULL COMMENT 'Owner Command 멱등 요청 ID',
    EXECUTION_STATUS VARCHAR(30) NOT NULL COMMENT 'PENDING/RUNNING/SUCCEEDED/FAILED/UNKNOWN/RECOVERED',
    OWNER_RESULT_CODE VARCHAR(80) NULL COMMENT 'Owner 응답 코드',
    OWNER_RESULT_MESSAGE VARCHAR(1000) NULL COMMENT '마스킹된 Owner 응답 메시지',
    STARTED_AT DATETIME(3) NULL COMMENT '실행 시작시각',
    COMPLETED_AT DATETIME(3) NULL COMMENT '실행 종료시각',
    RECOVERY_REQUIRED_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과불명/복구 필요 여부',
    LEASE_OWNER VARCHAR(120) NULL COMMENT '실행 Lease 소유 인스턴스',
    LEASE_EXPIRES_AT DATETIME(3) NULL COMMENT '실행 Lease 만료시각',
    FENCE_TOKEN BIGINT NOT NULL DEFAULT 0 COMMENT '실행/Reconcile fencing token',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_EXECUTION PRIMARY KEY (APPROVAL_REQUEST_ID),
    CONSTRAINT uk_adm_approval_execution_command UNIQUE (COMMAND_REQUEST_ID),
    CONSTRAINT ck_adm_approval_execution_status CHECK (EXECUTION_STATUS IN ('PENDING','RUNNING','SUCCEEDED','FAILED','UNKNOWN','RECOVERED')),
    CONSTRAINT ck_adm_approval_execution_recovery CHECK (RECOVERY_REQUIRED_YN IN ('Y','N')),
    CONSTRAINT ck_adm_approval_execution_time CHECK (COMPLETED_AT IS NULL OR STARTED_AT IS NULL OR COMPLETED_AT >= STARTED_AT),
    CONSTRAINT ck_adm_approval_execution_fence CHECK (FENCE_TOKEN >= 0),
    CONSTRAINT fk_adm_approval_execution_request FOREIGN KEY (APPROVAL_REQUEST_ID) REFERENCES ADM_APPROVAL_REQUEST (APPROVAL_REQUEST_ID),
    INDEX ix_adm_approval_execution_recovery (RECOVERY_REQUIRED_YN, EXECUTION_STATUS),
    INDEX ix_adm_approval_execution_lease (EXECUTION_STATUS, LEASE_EXPIRES_AT, APPROVAL_REQUEST_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 승인 후 Owner Command 실행 상태';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_HISTORY (
    APPROVAL_HISTORY_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '승인 이력 순번',
    APPROVAL_REQUEST_ID BIGINT NOT NULL COMMENT '승인 요청 순번',
    EVENT_TYPE VARCHAR(40) NOT NULL COMMENT 'REQUEST/APPROVE/REJECT/CANCEL/EXPIRE/BREAK_GLASS/EXECUTE/RESULT/REVIEW',
    ACTOR_ID VARCHAR(50) NOT NULL COMMENT '행위 운영자/시스템 ID',
    BEFORE_STATUS VARCHAR(30) NULL COMMENT '변경 전 상태',
    AFTER_STATUS VARCHAR(30) NOT NULL COMMENT '변경 후 상태',
    REASON VARCHAR(1000) NOT NULL COMMENT '행위 사유',
    EVENT_DATA LONGTEXT NULL COMMENT '마스킹된 사건 Snapshot',
    TRANSACTION_ID CHAR(34) NULL COMMENT 'CPF transactionId',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    CONSTRAINT pk_ADM_APPROVAL_HISTORY PRIMARY KEY (APPROVAL_HISTORY_ID),
    CONSTRAINT fk_adm_approval_history_request FOREIGN KEY (APPROVAL_REQUEST_ID) REFERENCES ADM_APPROVAL_REQUEST (APPROVAL_REQUEST_ID),
    INDEX ix_adm_approval_history_request (APPROVAL_REQUEST_ID, APPROVAL_HISTORY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 승인 Immutable 이력';

CREATE TABLE IF NOT EXISTS ADM_APPROVAL_PARTICIPANT (
    APPROVAL_PARTICIPANT_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT '승인 참여자 순번',
    APPROVAL_REQUEST_ID BIGINT NOT NULL COMMENT '승인 요청 순번',
    STEP_NO INT NOT NULL COMMENT '승인 단계',
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '해석된 실제 승인 운영자',
    SOURCE_TARGET_TYPE VARCHAR(30) NOT NULL COMMENT '정책 대상 유형 Snapshot',
    SOURCE_TARGET_CODE VARCHAR(100) NOT NULL COMMENT '정책 대상 코드 Snapshot',
    ORGANIZATION_CODE_SNAPSHOT VARCHAR(50) NULL COMMENT '승인 시 조직 Snapshot',
    POSITION_CODE_SNAPSHOT VARCHAR(50) NULL COMMENT '승인 시 직급 Snapshot',
    JOB_TITLE_CODE_SNAPSHOT VARCHAR(50) NULL COMMENT '승인 시 직책 Snapshot',
    DECISION_STATUS VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/APPROVED/REJECTED/SKIPPED',
    IDEMPOTENCY_KEY VARCHAR(120) NULL COMMENT '결정 멱등 키',
    DECISION_REASON VARCHAR(1000) NULL COMMENT '승인/반려 사유',
    DECIDED_AT DATETIME(3) NULL COMMENT '결정 시각',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_APPROVAL_PARTICIPANT PRIMARY KEY (APPROVAL_PARTICIPANT_ID),
    CONSTRAINT uk_adm_approval_participant UNIQUE (APPROVAL_REQUEST_ID, STEP_NO, OPERATOR_ID),
    CONSTRAINT uk_adm_approval_participant_idem UNIQUE (IDEMPOTENCY_KEY),
    CONSTRAINT ck_adm_approval_participant_status CHECK (DECISION_STATUS IN ('WAITING','APPROVED','REJECTED','SKIPPED')),
    CONSTRAINT ck_adm_approval_participant_step CHECK (STEP_NO >= 1),
    CONSTRAINT fk_adm_approval_participant_request FOREIGN KEY (APPROVAL_REQUEST_ID) REFERENCES ADM_APPROVAL_REQUEST (APPROVAL_REQUEST_ID) ON DELETE CASCADE,
    INDEX ix_adm_approval_participant_inbox (OPERATOR_ID, DECISION_STATUS, APPROVAL_REQUEST_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 승인 참여자 Snapshot';

CREATE TABLE IF NOT EXISTS ADM_INCIDENT_TIMELINE (
    timeline_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Incident Timeline ID',
    incident_id BIGINT NOT NULL COMMENT 'Incident ID',
    action_type VARCHAR(40) NOT NULL COMMENT '조치 유형',
    before_status VARCHAR(30) NULL COMMENT '조치 전 상태',
    after_status VARCHAR(30) NOT NULL COMMENT '조치 후 상태',
    reason VARCHAR(1000) NULL COMMENT '조치 사유',
    approval_request_id VARCHAR(160) NULL COMMENT '승인 요청 ID',
    actor_id VARCHAR(100) NOT NULL COMMENT '실행자 ID',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    CONSTRAINT pk_ADM_INCIDENT_TIMELINE PRIMARY KEY (timeline_id),
    CONSTRAINT fk_adm_incident_timeline_inc FOREIGN KEY (incident_id) REFERENCES ADM_INCIDENT_LIFECYCLE (incident_id) ON DELETE CASCADE,
    INDEX ix_adm_incident_timeline_inc (incident_id, timeline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM Notification Incident immutable Timeline';

CREATE TABLE IF NOT EXISTS ADM_OPERATOR_PROFILE (
    OPERATOR_ID VARCHAR(50) NOT NULL COMMENT '운영자 ID',
    DISPLAY_NAME VARCHAR(100) NULL COMMENT 'Directory/Profile 표시 이름',
    EMPLOYEE_NO VARCHAR(50) NULL COMMENT '외부/내부 사번',
    EXTERNAL_SUBJECT VARCHAR(200) NULL COMMENT 'LDAP/IAM 등 외부 Identity Subject',
    ORGANIZATION_CODE VARCHAR(50) NULL COMMENT '대표 운영 조직 코드',
    POSITION_CODE VARCHAR(50) NULL COMMENT '직급 코드',
    POSITION_NAME VARCHAR(100) NULL COMMENT '직급명 Snapshot/표시값',
    JOB_TITLE_CODE VARCHAR(50) NULL COMMENT '직책 코드',
    JOB_TITLE_NAME VARCHAR(100) NULL COMMENT '직책명 Snapshot/표시값',
    EMAIL VARCHAR(200) NULL COMMENT '업무 이메일',
    MOBILE_NO VARCHAR(50) NULL COMMENT '연락처(휴대폰); 숫자형이 아닌 문자열로 국가번호와 선행 0을 보존',
    OFFICE_PHONE_NO VARCHAR(50) NULL COMMENT '내부 전화번호/내선; 휴대폰 연락처와 분리',
    EFFECTIVE_FROM DATETIME(3) NULL COMMENT 'Profile 적용 시작시각',
    EFFECTIVE_TO DATETIME(3) NULL COMMENT 'Profile 적용 종료시각',
    VERSION_NO BIGINT NOT NULL DEFAULT 0 COMMENT 'Profile 낙관적 잠금 버전',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_ADM_OPERATOR_PROFILE PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT uk_adm_operator_profile_employee UNIQUE (EMPLOYEE_NO),
    CONSTRAINT ck_adm_operator_profile_effective CHECK (EFFECTIVE_TO IS NULL OR EFFECTIVE_FROM IS NULL OR EFFECTIVE_TO > EFFECTIVE_FROM),
    CONSTRAINT fk_adm_operator_profile_operator FOREIGN KEY (OPERATOR_ID) REFERENCES ADM_OPERATOR (OPERATOR_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_operator_profile_org FOREIGN KEY (ORGANIZATION_CODE) REFERENCES ADM_ORGANIZATION (ORGANIZATION_CODE) ON DELETE SET NULL,
    INDEX ix_adm_operator_profile_org (ORGANIZATION_CODE, EFFECTIVE_TO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 운영자 조직/직급 Profile';

CREATE TABLE IF NOT EXISTS ADM_ROLE_BUTTON (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    BUTTON_ID VARCHAR(80) NOT NULL COMMENT '버튼/행위 ID',
    ALLOW_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_ROLE_BUTTON PRIMARY KEY (ROLE_ID, BUTTON_ID),
    CONSTRAINT fk_adm_role_button_role FOREIGN KEY (ROLE_ID) REFERENCES ADM_ROLE (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_button_button FOREIGN KEY (BUTTON_ID) REFERENCES ADM_BUTTON (BUTTON_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할별 버튼/행위 권한';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_JOB (
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    batch_job_id VARCHAR(100) NULL COMMENT '연결된 BAT 배치 Job ID',
    center_cut_job_name VARCHAR(150) NOT NULL COMMENT '센터컷 Job 명',
    provider_key VARCHAR(100) NOT NULL COMMENT '대상 조회 Provider 식별자',
    handler_key VARCHAR(100) NOT NULL COMMENT '처리 Handler 식별자',
    chunk_size INT NOT NULL DEFAULT 100 COMMENT '한 번에 조회할 대상 건수',
    retry_limit INT NOT NULL DEFAULT 3 COMMENT '최대 재처리 횟수',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    description VARCHAR(500) NULL COMMENT '센터컷 Job 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_CENTER_CUT_JOB PRIMARY KEY (center_cut_job_id),
    CONSTRAINT fk_bat_center_cut_job_batch FOREIGN KEY (batch_job_id) REFERENCES BAT_JOB (job_id) ON DELETE SET NULL,
    INDEX ix_bat_center_cut_job_batch (batch_job_id, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 Job 정의';

CREATE TABLE IF NOT EXISTS BAT_DEPLOYMENT_INSTANCE_RESULT (
    deployment_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Instance result identifier',
    deployment_id VARCHAR(80) NOT NULL COMMENT 'Deployment execution identifier',
    sequence_no INT NOT NULL COMMENT 'Ordered result sequence',
    instance_id VARCHAR(160) NOT NULL COMMENT 'Target runtime instance identifier',
    stage_code VARCHAR(80) NOT NULL COMMENT 'Deployment stage code',
    result_state VARCHAR(40) NOT NULL COMMENT 'Instance stage result state',
    result_message VARCHAR(4000) NULL COMMENT 'Instance stage result detail',
    recorded_at DATETIME(6) NOT NULL COMMENT 'Result record time',
    CONSTRAINT pk_BAT_DEPLOYMENT_INSTANCE_RESULT PRIMARY KEY (deployment_result_id),
    CONSTRAINT uk_bat_deployment_instance_result UNIQUE (deployment_id, sequence_no),
    CONSTRAINT fk_bat_deployment_instance_result_execution FOREIGN KEY (deployment_id) REFERENCES BAT_DEPLOYMENT_EXECUTION (deployment_id) ON DELETE CASCADE,
    INDEX ix_bat_deployment_instance_result_instance (instance_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT per-instance deployment result';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION (
    execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    job_parameters VARCHAR(2000) NULL COMMENT '배치 파라미터',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    spring_batch_job_instance_id BIGINT NULL COMMENT 'Spring Batch JobInstance ID',
    business_date DATE NULL COMMENT 'JobInstance 시작 시 확정한 업무일자',
    run_id VARCHAR(120) NULL COMMENT '최초 실행 회차 ID',
    rerun_id VARCHAR(120) NULL COMMENT '운영 재수행 ID',
    original_job_execution_id BIGINT NULL COMMENT '재시작 기준 원 JobExecution ID',
    restart_attempt INT NOT NULL DEFAULT 0 COMMENT '동일 JobInstance 재시작 회차',
    batch_instance_id VARCHAR(100) NULL COMMENT '배치 인스턴스 ID',
    instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    required_worker_version VARCHAR(80) NULL COMMENT '실행에 필요한 worker 버전',
    required_capability VARCHAR(120) NULL COMMENT '실행에 필요한 worker capability',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    transaction_segment_id VARCHAR(120) NULL COMMENT '배치 Job 거래 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '상위 거래 구간 ID',
    job_log_relative_path VARCHAR(1000) NULL COMMENT 'CPF_LOG_ROOT 기준 JobInstance 로그 상대 경로',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수',
    stop_requested_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '운영 중지 요청 여부',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률',
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수',
    avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초',
    max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초',
    last_heartbeat_at DATETIME(3) NULL COMMENT '실행 메타 마지막 heartbeat 일시',
    current_step_name VARCHAR(150) NULL COMMENT '현재 실행 중인 Step 이름',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    requested_by VARCHAR(100) NULL COMMENT '실행 요청자',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '운영 위험조치 낙관적 잠금 Version',
    definition_version BIGINT NULL COMMENT 'Execution 생성 시 고정된 Job Definition Version',
    definition_checksum VARCHAR(128) NULL COMMENT 'Execution 생성 시 고정된 Definition Checksum',
    CONSTRAINT pk_BAT_EXECUTION PRIMARY KEY (execution_id),
    CONSTRAINT fk_bat_execution_job FOREIGN KEY (job_id) REFERENCES BAT_JOB (job_id),
    CONSTRAINT fk_bat_execution_instance FOREIGN KEY (batch_instance_id) REFERENCES BAT_INSTANCE (instance_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_worker FOREIGN KEY (worker_id) REFERENCES BAT_WORKER (worker_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_definition FOREIGN KEY (job_id, definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version),
    INDEX ix_bat_execution_job_time (job_id, start_time),
    INDEX ix_bat_execution_status (execution_status, start_time),
    INDEX ix_bat_execution_spring (spring_batch_execution_id),
    INDEX ix_bat_execution_job_instance (spring_batch_job_instance_id, business_date),
    INDEX ix_bat_execution_worker (worker_id, execution_status, start_time),
    INDEX ix_bat_execution_instance (instance_id, start_time),
    INDEX ix_bat_execution_claim (execution_status, required_worker_version, required_capability, execution_id),
    INDEX ix_bat_execution_transaction (transaction_id),
    INDEX ix_bat_execution_segment (transaction_segment_id, parent_segment_id),
    INDEX ix_bat_execution_heartbeat (execution_status, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 실행 이력';

CREATE TABLE IF NOT EXISTS BAT_JOB_RELATION (
    relation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 관계 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '기준 배치 Job ID',
    related_job_id VARCHAR(100) NOT NULL COMMENT '연관 배치 Job ID',
    relation_type VARCHAR(30) NOT NULL COMMENT '관계 유형',
    trigger_condition VARCHAR(50) NOT NULL DEFAULT 'COMPLETED' COMMENT '트리거 조건',
    required_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' COMMENT '필수 선행 상태',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '관계 표시 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_JOB_RELATION PRIMARY KEY (relation_id),
    CONSTRAINT uk_bat_job_relation UNIQUE (job_id, related_job_id, relation_type),
    CONSTRAINT fk_bat_job_relation_job FOREIGN KEY (job_id) REFERENCES BAT_JOB (job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_job_relation_related FOREIGN KEY (related_job_id) REFERENCES BAT_JOB (job_id) ON DELETE CASCADE,
    INDEX ix_bat_job_relation_job (job_id, relation_type, use_yn),
    INDEX ix_bat_job_relation_related (related_job_id, relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 선행/후행/트리거 관계';

CREATE TABLE IF NOT EXISTS BAT_SB_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    CONSTRAINT pk_BAT_SB_JOB_EXECUTION_CONTEXT PRIMARY KEY (JOB_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BAT_SB_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS BAT_SB_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    PARAMETER_NAME VARCHAR(100) NOT NULL COMMENT '파라미터 이름',
    PARAMETER_TYPE VARCHAR(100) NOT NULL COMMENT '파라미터 Java 유형',
    PARAMETER_VALUE VARCHAR(2500) NULL COMMENT '파라미터 값',
    IDENTIFYING CHAR(1) NOT NULL COMMENT 'JobInstance 식별 파라미터 여부',
    CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BAT_SB_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Job 파라미터 저장소';

CREATE TABLE IF NOT EXISTS BAT_SB_STEP_EXECUTION (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    VERSION BIGINT NOT NULL COMMENT '낙관적 잠금 버전',
    STEP_NAME VARCHAR(100) NOT NULL COMMENT 'Step 이름',
    JOB_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch JobExecution 순번',
    CREATE_TIME DATETIME(6) NOT NULL COMMENT 'Step 생성 일시',
    START_TIME DATETIME(6) NULL DEFAULT NULL COMMENT 'Step 시작 일시',
    END_TIME DATETIME(6) NULL DEFAULT NULL COMMENT 'Step 종료 일시',
    STATUS VARCHAR(10) NULL COMMENT 'Step 상태',
    COMMIT_COUNT BIGINT NULL COMMENT '커밋 횟수',
    READ_COUNT BIGINT NULL COMMENT '읽은 건수',
    FILTER_COUNT BIGINT NULL COMMENT '필터 건수',
    WRITE_COUNT BIGINT NULL COMMENT '쓴 건수',
    READ_SKIP_COUNT BIGINT NULL COMMENT '읽기 skip 건수',
    WRITE_SKIP_COUNT BIGINT NULL COMMENT '쓰기 skip 건수',
    PROCESS_SKIP_COUNT BIGINT NULL COMMENT '처리 skip 건수',
    ROLLBACK_COUNT BIGINT NULL COMMENT 'rollback 건수',
    EXIT_CODE VARCHAR(2500) NULL COMMENT '종료 코드',
    EXIT_MESSAGE VARCHAR(2500) NULL COMMENT '종료 메시지',
    LAST_UPDATED DATETIME(6) NULL COMMENT '마지막 수정 일시',
    CONSTRAINT pk_BAT_SB_STEP_EXECUTION PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID) REFERENCES BAT_SB_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 StepExecution 저장소';

CREATE TABLE IF NOT EXISTS BAT_SCHEDULE (
    schedule_id VARCHAR(100) NOT NULL COMMENT '배치 스케줄 ID',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron 표현식',
    calendar_id VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '적용 영업일 캘린더 ID',
    business_day_only_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '영업일에만 수행 여부',
    holiday_policy VARCHAR(30) NOT NULL DEFAULT 'SKIP' COMMENT '휴일 처리 정책',
    available_start_time TIME NULL COMMENT '수행 가능 시작 시각',
    available_end_time TIME NULL COMMENT '수행 가능 종료 시각',
    run_date_pattern VARCHAR(80) NULL COMMENT '수행 일자 패턴',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '스케줄 기준 시간대',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '스케줄 활성 여부',
    last_fire_at DATETIME NULL COMMENT '마지막 실행 예정 일시',
    next_fire_at DATETIME NULL COMMENT '다음 실행 예정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT '운영 위험조치 낙관적 잠금 Version',
    definition_version BIGINT NULL COMMENT 'Schedule이 실행해야 하는 고정 Job Definition Version',
    definition_checksum VARCHAR(128) NULL COMMENT 'Schedule 생성 시 고정된 Definition Checksum',
    CONSTRAINT pk_BAT_SCHEDULE PRIMARY KEY (schedule_id),
    CONSTRAINT fk_bat_schedule_job FOREIGN KEY (job_id) REFERENCES BAT_JOB (job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_schedule_definition FOREIGN KEY (job_id, definition_version) REFERENCES BAT_JOB_DEFINITION_VERSION (job_id, definition_version),
    INDEX ix_bat_schedule_job (job_id, enabled_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 스케줄';

CREATE TABLE IF NOT EXISTS CPF_NOTIFICATION_DELIVERY_ATTEMPT (
    delivery_id BIGINT NOT NULL COMMENT '알림 발송 ID',
    attempt_no INT NOT NULL COMMENT 'Provider 호출 시도 순번',
    operation_id VARCHAR(100) NOT NULL COMMENT '멱등 작업 ID',
    worker_id VARCHAR(100) NOT NULL COMMENT '호출 소유 Worker',
    attempt_status VARCHAR(30) NOT NULL COMMENT 'Attempt 처리 상태',
    provider_status VARCHAR(80) NULL COMMENT 'Provider 결과 코드',
    provider_message VARCHAR(2000) NULL COMMENT '민감정보 제거 Provider 결과',
    started_at DATETIME(3) NOT NULL COMMENT 'Provider 호출 시작 일시',
    completed_at DATETIME(3) NULL COMMENT 'Provider 결과 확정 일시',
    lease_version BIGINT NOT NULL COMMENT 'Claim 시점 CAS Version',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '기록 주체',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 일시',
    CONSTRAINT pk_CPF_NOTIFICATION_DELIVERY_ATTEMPT PRIMARY KEY (delivery_id, attempt_no),
    CONSTRAINT fk_cpf_notification_attempt_delivery FOREIGN KEY (delivery_id) REFERENCES CPF_NOTIFICATION_DELIVERY_LOG (delivery_id) ON DELETE CASCADE,
    INDEX ix_cpf_notification_attempt_operation (operation_id, attempt_no),
    INDEX ix_cpf_notification_attempt_status (attempt_status, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Durable Notification Provider 호출 Attempt 불변 이력';

CREATE TABLE IF NOT EXISTS GW_APPLY_STATUS (
    binding_id VARCHAR(100) NOT NULL COMMENT 'Binding ID',
    gateway_instance_id VARCHAR(100) NOT NULL COMMENT 'Gateway Instance ID',
    expected_version VARCHAR(100) NOT NULL COMMENT '기대 Version',
    applied_version VARCHAR(100) NULL COMMENT '적용 Version',
    apply_status VARCHAR(30) NOT NULL COMMENT '적용 상태',
    error_code VARCHAR(100) NULL COMMENT '오류 코드',
    error_message VARCHAR(1000) NULL COMMENT '오류 메시지',
    acknowledged_at DATETIME NULL COMMENT 'ACK 시각',
    last_seen_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 상태 시각',
    CONSTRAINT pk_GW_APPLY_STATUS PRIMARY KEY (binding_id, gateway_instance_id),
    CONSTRAINT fk_cpf_gwy_apply_binding FOREIGN KEY (binding_id) REFERENCES GW_BINDING (binding_id),
    INDEX ix_cpf_gwy_apply_status (apply_status, last_seen_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Instance별 적용 ACK/Drift';

CREATE TABLE IF NOT EXISTS GW_CONNECTION_TEST (
    test_id VARCHAR(100) NOT NULL COMMENT '시험 ID',
    binding_id VARCHAR(100) NOT NULL COMMENT 'Binding ID',
    gateway_instance_id VARCHAR(100) NULL COMMENT 'Gateway Instance ID',
    instance_id VARCHAR(100) NULL COMMENT 'Target Instance ID',
    test_type VARCHAR(50) NOT NULL COMMENT '시험 유형',
    test_status VARCHAR(30) NOT NULL COMMENT '시험 상태',
    failure_stage VARCHAR(50) NULL COMMENT '실패 단계',
    duration_ms BIGINT NOT NULL DEFAULT 0 COMMENT '소요시간',
    trace_id VARCHAR(100) NULL COMMENT 'Trace ID',
    operation_id VARCHAR(100) NULL COMMENT 'Operation ID',
    tested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시험 시각',
    tested_by VARCHAR(100) NOT NULL COMMENT '시험자',
    CONSTRAINT pk_GW_CONNECTION_TEST PRIMARY KEY (test_id),
    CONSTRAINT fk_cpf_gwy_test_binding FOREIGN KEY (binding_id) REFERENCES GW_BINDING (binding_id),
    INDEX ix_cpf_gwy_test_binding (binding_id, tested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway 직접/E2E 연결시험 결과';

CREATE TABLE IF NOT EXISTS GW_CONNECTION_TEST_OPERATION (
    operation_id VARCHAR(100) NOT NULL COMMENT '연결시험 Operation ID',
    binding_id VARCHAR(100) NOT NULL COMMENT 'Binding ID',
    test_type VARCHAR(50) NOT NULL COMMENT 'DIRECT/E2E/LB_DISTRIBUTION',
    operation_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' COMMENT 'Operation 상태',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청자',
    request_reason VARCHAR(1000) NOT NULL COMMENT '요청 사유',
    request_payload_hash VARCHAR(64) NOT NULL COMMENT '요청 Payload Hash',
    expires_at DATETIME NOT NULL COMMENT 'Operation 만료',
    cancel_requested_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '취소 요청',
    result_summary VARCHAR(2000) NULL COMMENT '결과 요약',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    started_at DATETIME NULL COMMENT '시작 시각',
    completed_at DATETIME NULL COMMENT '완료 시각',
    row_version BIGINT NOT NULL DEFAULT 1 COMMENT '낙관적 버전',
    CONSTRAINT pk_GW_CONNECTION_TEST_OPERATION PRIMARY KEY (operation_id),
    CONSTRAINT ck_cpf_gwy_test_cancel CHECK (cancel_requested_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_test_op_binding FOREIGN KEY (binding_id) REFERENCES GW_BINDING (binding_id),
    INDEX ix_cpf_gwy_test_op_binding (binding_id, created_at),
    INDEX ix_cpf_gwy_test_op_status (operation_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway 비동기 연결시험 Operation';

CREATE TABLE IF NOT EXISTS GW_TRANSACTION (
    gateway_transaction_id VARCHAR(100) NOT NULL COMMENT 'Gateway 거래 ID',
    transaction_id VARCHAR(100) NOT NULL COMMENT 'CPF 거래 ID',
    trace_id VARCHAR(100) NOT NULL COMMENT 'Trace ID',
    channel_id VARCHAR(100) NULL COMMENT 'Channel ID',
    source_ip VARCHAR(100) NULL COMMENT 'Source IP',
    source_port INT NULL COMMENT 'Source Port',
    gateway_instance_id VARCHAR(100) NOT NULL COMMENT 'Gateway Instance',
    binding_id VARCHAR(100) NOT NULL COMMENT 'Binding ID',
    route_id VARCHAR(100) NOT NULL COMMENT 'Route ID',
    route_version VARCHAR(100) NOT NULL COMMENT 'Route Version',
    server_group_id VARCHAR(100) NOT NULL COMMENT 'Server Group',
    final_instance_id VARCHAR(100) NULL COMMENT '최종 Instance',
    result_status VARCHAR(30) NOT NULL COMMENT '최종 상태',
    protocol_status VARCHAR(30) NULL COMMENT 'Protocol 상태',
    business_code VARCHAR(100) NULL COMMENT '업무 코드',
    failure_stage VARCHAR(50) NULL COMMENT '실패 단계',
    unknown_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과 불명 여부',
    total_duration_ms BIGINT NOT NULL DEFAULT 0 COMMENT '전체 소요시간',
    request_size BIGINT NOT NULL DEFAULT 0 COMMENT '요청 크기',
    response_size BIGINT NOT NULL DEFAULT 0 COMMENT '응답 크기',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    binding_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Binding Row Version Snapshot',
    config_checksum VARCHAR(64) NULL COMMENT 'Gateway Config Checksum Snapshot',
    request_method VARCHAR(20) NULL COMMENT '요청 Method',
    request_path VARCHAR(1000) NULL COMMENT '요청 Path',
    completed_at DATETIME NULL COMMENT '완료 시각',
    CONSTRAINT pk_GW_TRANSACTION PRIMARY KEY (gateway_transaction_id),
    CONSTRAINT ck_cpf_gwy_tx_unknown CHECK (unknown_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_tx_binding FOREIGN KEY (binding_id) REFERENCES GW_BINDING (binding_id),
    INDEX ix_cpf_gwy_tx_trace (transaction_id, trace_id, created_at),
    INDEX ix_cpf_gwy_tx_route (route_id, result_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway IN/GATEWAY/OUT/RESULT 거래 원장';

CREATE TABLE IF NOT EXISTS OPS_LOG_POLICY_AUDIT (
    audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 감사 순번',
    policy_id BIGINT NULL COMMENT '로그 정책 순번',
    override_id BIGINT NULL COMMENT '로그 정책 override 순번',
    action_type VARCHAR(30) NOT NULL COMMENT '감사 행위 유형',
    target_type VARCHAR(30) NOT NULL COMMENT '대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT '대상 ID',
    reason VARCHAR(500) NOT NULL COMMENT '감사 사유',
    before_data MEDIUMTEXT NULL COMMENT '변경 전 데이터',
    after_data MEDIUMTEXT NULL COMMENT '변경 후 데이터',
    diff_data MEDIUMTEXT NULL COMMENT '변경 차이',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    client_ip VARCHAR(100) NULL COMMENT '클라이언트 IP',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_LOG_POLICY_AUDIT PRIMARY KEY (audit_id),
    CONSTRAINT fk_cpf_log_policy_audit_policy FOREIGN KEY (policy_id) REFERENCES OPS_LOG_POLICY (policy_id) ON DELETE SET NULL,
    CONSTRAINT fk_cpf_log_policy_audit_override FOREIGN KEY (override_id) REFERENCES OPS_LOG_POLICY_OVERRIDE (override_id) ON DELETE SET NULL,
    INDEX ix_cpf_log_policy_audit_target (target_type, target_id, created_at),
    INDEX ix_cpf_log_policy_audit_operator (operator_id, created_at),
    INDEX ix_cpf_log_policy_audit_policy (policy_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 로그 정책 감사 로그';

CREATE TABLE IF NOT EXISTS OPS_OPERATION_CALLER_POLICY (
    operation_id VARCHAR(160) NOT NULL COMMENT '정책 대상 operationId',
    caller_system_code VARCHAR(32) NOT NULL COMMENT '허용/거부 Caller System 코드',
    allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Caller별 Operation 호출 허용 여부',
    policy_version BIGINT NOT NULL DEFAULT 1 COMMENT 'Caller 정책 버전',
    seed_source VARCHAR(80) NULL COMMENT '최초 Seed 출처',
    seed_revision VARCHAR(120) NULL COMMENT '최초 Seed revision',
    seeded_at DATETIME(3) NULL COMMENT '최초 Seed 적용 시각',
    change_reason VARCHAR(500) NULL COMMENT '최근 변경 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '최종 수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    CONSTRAINT pk_OPS_OPERATION_CALLER_POLICY PRIMARY KEY (operation_id, caller_system_code),
    CONSTRAINT ck_ops_operation_caller_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_caller_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE,
    INDEX ix_ops_operation_caller_caller (caller_system_code, allowed_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 업무 Operation Caller별 ADM-owned 정책';

CREATE TABLE IF NOT EXISTS OPS_OPERATION_DISCOVERY_INSTANCE (
    operation_id VARCHAR(160) NOT NULL COMMENT '업무 Domain Canonical operationId',
    instance_id VARCHAR(160) NOT NULL COMMENT 'Operation을 스캔한 Runtime instanceId',
    system_code VARCHAR(32) NOT NULL COMMENT '스캔 Runtime System 코드',
    application_code VARCHAR(100) NULL COMMENT '스캔 Application 코드',
    artifact_version VARCHAR(120) NULL COMMENT '스캔 Artifact version',
    artifact_commit VARCHAR(120) NULL COMMENT '스캔 Artifact commit/SHA',
    discovered_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '해당 Runtime/Artifact에서 발견 여부',
    last_reported_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 discovery report 시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '최종 수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    CONSTRAINT pk_OPS_OPERATION_DISCOVERY_INSTANCE PRIMARY KEY (operation_id, instance_id),
    CONSTRAINT ck_ops_operation_discovery_yn CHECK (discovered_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_discovery_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE,
    INDEX ix_ops_operation_discovery_instance (instance_id, discovered_yn, last_reported_at),
    INDEX ix_ops_operation_discovery_scope (system_code, application_code, discovered_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 업무 Operation Runtime/Artifact별 Source discovery evidence';

CREATE TABLE IF NOT EXISTS OPS_OPERATION_POLICY (
    operation_id VARCHAR(160) NOT NULL COMMENT '정책 대상 operationId',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Operation 실행 허용 여부',
    all_callers_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '등록·활성 Caller 전체 허용 여부',
    channel_policy_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Channel 3차 정책 적용 필요 여부',
    policy_version BIGINT NOT NULL DEFAULT 1 COMMENT 'Operation 정책 버전',
    seed_source VARCHAR(80) NULL COMMENT '최초 Seed 출처',
    seed_revision VARCHAR(120) NULL COMMENT '최초 Seed revision',
    seeded_at DATETIME(3) NULL COMMENT '최초 Seed 적용 시각',
    change_reason VARCHAR(500) NULL COMMENT '최근 변경 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '최종 수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시',
    CONSTRAINT pk_OPS_OPERATION_POLICY PRIMARY KEY (operation_id),
    CONSTRAINT ck_ops_operation_policy_enabled CHECK (enabled_yn IN ('Y', 'N')),
    CONSTRAINT ck_ops_operation_policy_all CHECK (all_callers_yn IN ('Y', 'N')),
    CONSTRAINT ck_ops_operation_policy_channel CHECK (channel_policy_required_yn IN ('Y', 'N')),
    CONSTRAINT fk_ops_operation_policy_catalog FOREIGN KEY (operation_id) REFERENCES OPS_OPERATION_CATALOG (operation_id) ON DELETE CASCADE,
    INDEX ix_ops_operation_policy_enabled (enabled_yn, policy_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 업무 Operation ADM-owned 실행 정책';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_CHANGE_AUDIT (
    audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Audit event identifier',
    change_id VARCHAR(80) NOT NULL COMMENT 'Runtime change identifier',
    event_type VARCHAR(60) NOT NULL COMMENT 'Audit event type',
    actor_id VARCHAR(100) NOT NULL COMMENT 'Actor identifier',
    reason VARCHAR(500) NULL COMMENT 'Mandatory operation reason',
    evidence_hash VARCHAR(64) NULL COMMENT 'Evidence checksum',
    previous_hash VARCHAR(64) NOT NULL COMMENT 'Previous audit chain checksum',
    chain_hash VARCHAR(64) NOT NULL COMMENT 'Audit chain checksum',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    CONSTRAINT pk_OPS_RUNTIME_CHANGE_AUDIT PRIMARY KEY (audit_id),
    CONSTRAINT fk_cpf_runtime_audit_change FOREIGN KEY (change_id) REFERENCES OPS_RUNTIME_CHANGE (change_id) ON DELETE CASCADE,
    INDEX ix_cpf_runtime_change_audit_change (change_id, audit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Control immutable hash-chain audit';

CREATE TABLE IF NOT EXISTS OPS_SERVICE_INSTANCE (
    instance_id VARCHAR(120) NOT NULL COMMENT '서비스 인스턴스 ID',
    managed_server_id VARCHAR(80) NULL COMMENT 'Central managed server reference; nullable for unresolved legacy/discovery rows',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_name VARCHAR(150) NOT NULL COMMENT '서비스 인스턴스명',
    base_url VARCHAR(500) NOT NULL COMMENT '인스턴스 기본 URL',
    host_name VARCHAR(150) NULL COMMENT 'Host명',
    port_no INT NULL COMMENT 'Port 번호',
    instance_status VARCHAR(30) NOT NULL DEFAULT 'UP' COMMENT '인스턴스 상태',
    weight INT NOT NULL DEFAULT 100 COMMENT '라우팅 가중치',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    last_heartbeat_at DATETIME(3) NULL COMMENT '마지막 heartbeat 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    environment_code VARCHAR(40) NOT NULL DEFAULT 'default' COMMENT '배포 환경 코드',
    zone_code VARCHAR(60) NULL COMMENT '가용 영역/Zone 코드',
    cell_code VARCHAR(60) NULL COMMENT '운영 Cell 코드',
    priority_no INT NOT NULL DEFAULT 100 COMMENT '라우팅 우선순위(낮을수록 우선)',
    maintenance_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '유지보수 제외 여부',
    drain_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '신규 요청 Drain 여부',
    drain_deadline_at DATETIME(3) NULL COMMENT 'Drain 완료 목표 시각',
    system_code VARCHAR(16) NULL COMMENT 'Generated domain/runtime systemCode; not server identity',
    application_name VARCHAR(150) NULL COMMENT 'Runtime application name',
    application_role VARCHAR(80) NULL COMMENT 'Runtime application role',
    runtime_hostname VARCHAR(200) NULL COMMENT 'Runtime-reported hostname',
    process_id VARCHAR(80) NULL COMMENT 'Runtime process identifier when available',
    java_version VARCHAR(80) NULL COMMENT 'Runtime Java version',
    cpf_version VARCHAR(80) NULL COMMENT 'CPF runtime version',
    application_version VARCHAR(100) NULL COMMENT 'Application version',
    started_at DATETIME(3) NULL COMMENT 'Runtime process start time',
    row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic lock 버전',
    CONSTRAINT pk_OPS_SERVICE_INSTANCE PRIMARY KEY (instance_id),
    CONSTRAINT ck_cpf_service_instance_active CHECK (active_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_service_instance_maintenance CHECK (maintenance_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_service_instance_drain CHECK (drain_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_service_instance_service FOREIGN KEY (service_id) REFERENCES OPS_SERVICE (service_id),
    CONSTRAINT fk_cpf_service_instance_endpoint FOREIGN KEY (endpoint_code) REFERENCES OPS_SERVICE_ENDPOINT (endpoint_code),
    CONSTRAINT fk_cpf_service_instance_managed_server FOREIGN KEY (managed_server_id) REFERENCES OPS_MANAGED_SERVER (managed_server_id),
    INDEX ix_cpf_service_instance_endpoint (service_id, endpoint_code, active_yn, instance_status),
    INDEX ix_cpf_service_instance_weight (endpoint_code, weight),
    INDEX ix_cpf_service_instance_placement (environment_code, zone_code, cell_code, active_yn, instance_status),
    INDEX ix_cpf_service_instance_route (endpoint_code, priority_no, maintenance_yn, drain_yn, active_yn, instance_status),
    INDEX ix_cpf_service_instance_managed_server (managed_server_id, instance_status, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 인스턴스 레지스트리';

CREATE TABLE IF NOT EXISTS OPS_SERVICE_ROUTING_POLICY (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '라우팅 정책 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    routing_mode VARCHAR(30) NOT NULL DEFAULT 'PRIMARY' COMMENT '라우팅 모드',
    load_balance_type VARCHAR(30) NOT NULL DEFAULT 'WEIGHT' COMMENT '부하 분산 유형',
    failover_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Failover 사용 여부',
    health_check_required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Health check 필수 여부',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    priority INT NOT NULL DEFAULT 100 COMMENT '우선순위',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_SERVICE_ROUTING_POLICY PRIMARY KEY (policy_id),
    CONSTRAINT uk_cpf_service_routing_policy UNIQUE (service_id, endpoint_code, priority),
    CONSTRAINT fk_cpf_service_routing_service FOREIGN KEY (service_id) REFERENCES OPS_SERVICE (service_id),
    CONSTRAINT fk_cpf_service_routing_endpoint FOREIGN KEY (endpoint_code) REFERENCES OPS_SERVICE_ENDPOINT (endpoint_code),
    INDEX ix_cpf_service_routing_active (service_id, endpoint_code, active_yn, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 라우팅 정책';

CREATE TABLE IF NOT EXISTS ADM_ROLE_API_PERMISSION (
    ROLE_ID VARCHAR(50) NOT NULL COMMENT '역할 ID',
    API_PERMISSION_ID VARCHAR(120) NOT NULL COMMENT 'API 권한 ID',
    ALLOW_YN CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    created_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(50) NOT NULL DEFAULT 'ADM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_ADM_ROLE_API_PERMISSION PRIMARY KEY (ROLE_ID, API_PERMISSION_ID),
    CONSTRAINT fk_adm_role_api_permission_role FOREIGN KEY (ROLE_ID) REFERENCES ADM_ROLE (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_api_permission_api FOREIGN KEY (API_PERMISSION_ID) REFERENCES ADM_API_PERMISSION (API_PERMISSION_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADM 역할별 API 권한';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_EXECUTION (
    center_cut_execution_id VARCHAR(80) NOT NULL COMMENT 'Center-cut execution identifier',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT 'Center-cut job definition identifier',
    idempotency_key VARCHAR(160) NOT NULL COMMENT 'Execution idempotency key',
    execution_state VARCHAR(30) NOT NULL COMMENT 'Center-cut execution state',
    parameter_ciphertext LONGTEXT NOT NULL COMMENT 'Encrypted immutable parameter snapshot',
    parameter_hash VARCHAR(64) NOT NULL COMMENT 'Parameter snapshot SHA-256',
    parameter_schema_version VARCHAR(80) NOT NULL COMMENT 'Parameter schema version',
    target_cursor VARCHAR(1000) NULL COMMENT 'Last generated target cursor',
    target_complete_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Target generation completion flag',
    target_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Generated target count',
    tps_limit INT NOT NULL DEFAULT 0 COMMENT 'Global transactions-per-second limit',
    concurrency_limit INT NOT NULL DEFAULT 1 COMMENT 'Global runner concurrency limit',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Processed item count',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Successful item count',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Failed item count',
    unknown_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Unknown-result item count',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    parent_segment_id VARCHAR(120) NULL COMMENT 'Parent trace segment identifier',
    requested_by VARCHAR(120) NOT NULL COMMENT 'Execution requester',
    reason_text VARCHAR(1000) NOT NULL COMMENT 'Mandatory execution reason',
    last_error_message VARCHAR(1000) NULL COMMENT 'Last execution error detail',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Execution request time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last execution state update time',
    completed_at DATETIME(6) NULL COMMENT 'Execution completion time',
    CONSTRAINT pk_BAT_CENTER_CUT_EXECUTION PRIMARY KEY (center_cut_execution_id),
    CONSTRAINT uk_bat_center_cut_execution_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_bat_center_cut_execution_job FOREIGN KEY (center_cut_job_id) REFERENCES BAT_CENTER_CUT_JOB (center_cut_job_id),
    INDEX ix_bat_center_cut_execution_job_state (center_cut_job_id, execution_state, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT center-cut immutable execution policy';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_PARAMETER (
    parameter_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 파라미터 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    parameter_key VARCHAR(100) NOT NULL COMMENT '파라미터 키',
    parameter_value VARCHAR(1000) NULL COMMENT '파라미터 값',
    encrypted_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '암호화 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_CENTER_CUT_PARAMETER PRIMARY KEY (parameter_id),
    CONSTRAINT uk_bat_center_cut_parameter UNIQUE (center_cut_job_id, parameter_key),
    CONSTRAINT fk_bat_center_cut_parameter_job FOREIGN KEY (center_cut_job_id) REFERENCES BAT_CENTER_CUT_JOB (center_cut_job_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 파라미터';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_ATTEMPT (
    attempt_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 시도 식별자',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 식별자',
    attempt_no INT NOT NULL COMMENT '1부터 시작하는 실행 시도 번호',
    definition_version BIGINT NOT NULL COMMENT '시도에 고정된 Definition Version',
    definition_checksum VARCHAR(128) NOT NULL COMMENT '시도에 고정된 Definition Checksum',
    worker_id VARCHAR(160) NOT NULL COMMENT '시도를 소유한 Worker',
    fencing_token BIGINT NOT NULL COMMENT '시도 소유권 Fencing Token',
    attempt_status VARCHAR(40) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/COMPLETED/FAILED/TIMEOUT/RETRYABLE_FAILURE/UNKNOWN_RESULT',
    result_message MEDIUMTEXT NULL COMMENT '마스킹된 시도 결과 메시지',
    executor_type VARCHAR(40) NULL COMMENT '실제 실행 Adapter 유형',
    exit_code INT NULL COMMENT 'Shell/Process 종료 코드',
    stdout_text MEDIUMTEXT NULL COMMENT '마스킹된 표준 출력',
    stderr_text MEDIUMTEXT NULL COMMENT '마스킹된 표준 오류',
    output_truncated_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '출력 길이 제한 적용 여부',
    duration_ms BIGINT NULL COMMENT '실행 소요 시간(ms)',
    artifact_hash VARCHAR(128) NULL COMMENT '승인 Script/File Artifact SHA-256',
    unknown_result_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과 불명 여부',
    started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시도 시작 일시',
    finished_at DATETIME(3) NULL COMMENT '시도 종료 일시',
    CONSTRAINT pk_BAT_EXECUTION_ATTEMPT PRIMARY KEY (attempt_id),
    CONSTRAINT uk_bat_execution_attempt UNIQUE (execution_id, attempt_no),
    CONSTRAINT ck_bat_execution_attempt_status CHECK (attempt_status IN ('RUNNING','COMPLETED','FAILED','TIMEOUT','RETRYABLE_FAILURE','UNKNOWN_RESULT')),
    CONSTRAINT ck_bat_execution_attempt_truncated CHECK (output_truncated_yn IN ('Y','N')),
    CONSTRAINT ck_bat_execution_attempt_unknown CHECK (unknown_result_yn IN ('Y','N')),
    CONSTRAINT fk_bat_execution_attempt_execution FOREIGN KEY (execution_id) REFERENCES BAT_EXECUTION (execution_id) ON DELETE CASCADE,
    INDEX ix_bat_execution_attempt_status (attempt_status, started_at),
    INDEX ix_bat_execution_attempt_worker (worker_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 실행별 재시도 및 결과 불명 원장';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_LEASE (
    lease_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 실행 lease 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    worker_id VARCHAR(160) NOT NULL COMMENT '현재 lease 소유 worker ID',
    lease_token VARCHAR(80) NOT NULL COMMENT 'lease 갱신·완료 검증 토큰',
    lease_status VARCHAR(30) NOT NULL DEFAULT 'CLAIMED' COMMENT 'CLAIMED, RUNNING, RELEASED, EXPIRED 상태',
    claimed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 claim 일시',
    lease_until DATETIME(3) NOT NULL COMMENT 'lease 만료 일시',
    last_heartbeat_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '마지막 lease heartbeat 일시',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT 'claim 시도 회차',
    takeover_count INT NOT NULL DEFAULT 0 COMMENT '만료 후 다른 worker 인수 횟수',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'monotonic fencing token',
    released_at DATETIME(3) NULL COMMENT '정상 또는 실패 완료 일시',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실행 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_EXECUTION_LEASE PRIMARY KEY (lease_id),
    CONSTRAINT uk_bat_execution_lease_execution UNIQUE (execution_id),
    CONSTRAINT uk_bat_execution_lease_token UNIQUE (lease_token),
    CONSTRAINT fk_bat_execution_lease_execution FOREIGN KEY (execution_id) REFERENCES BAT_EXECUTION (execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_execution_lease_worker FOREIGN KEY (worker_id) REFERENCES BAT_WORKER (worker_id) ON DELETE RESTRICT,
    INDEX ix_bat_execution_lease_owner (worker_id, lease_status, lease_until),
    INDEX ix_bat_execution_lease_expire (lease_status, lease_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 worker 실행 claim과 lease';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_RESULT_DETAIL (
    execution_id BIGINT NOT NULL COMMENT 'BAT 실행 ID',
    definition_version BIGINT NULL COMMENT 'Definition Version Snapshot',
    definition_checksum VARCHAR(64) NULL COMMENT 'Definition Checksum Snapshot',
    executor_status VARCHAR(40) NOT NULL COMMENT 'Executor 상세 상태',
    exit_code INT NULL COMMENT 'Process Exit Code',
    timeout_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Timeout 여부',
    unknown_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과 불명 여부',
    output_truncated_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '출력 절단 여부',
    output_hash VARCHAR(64) NULL COMMENT '출력 Hash',
    artifact_hash VARCHAR(64) NULL COMMENT '실행 Artifact Hash',
    parameter_snapshot_hash VARCHAR(64) NULL COMMENT 'Parameter Snapshot Hash',
    result_message VARCHAR(2000) NULL COMMENT '마스킹 결과',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '갱신 시각',
    CONSTRAINT pk_BAT_EXECUTION_RESULT_DETAIL PRIMARY KEY (execution_id),
    CONSTRAINT ck_bat_result_timeout CHECK (timeout_yn IN ('Y','N')),
    CONSTRAINT ck_bat_result_unknown CHECK (unknown_yn IN ('Y','N')),
    CONSTRAINT ck_bat_result_truncated CHECK (output_truncated_yn IN ('Y','N')),
    CONSTRAINT fk_bat_result_execution FOREIGN KEY (execution_id) REFERENCES BAT_EXECUTION (execution_id),
    INDEX ix_bat_result_status (executor_status, created_at),
    INDEX ix_bat_result_unknown (unknown_yn, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Batch Executor 상세 결과 원장';

CREATE TABLE IF NOT EXISTS BAT_EXECUTION_TARGET (
    target_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 수행 대상 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    schedule_id VARCHAR(100) NULL COMMENT '배치 스케줄 ID',
    target_instance_id VARCHAR(100) NULL COMMENT '수행 대상 인스턴스 ID',
    business_date DATE NULL COMMENT '업무 기준일',
    planned_run_at DATETIME(3) NULL COMMENT '예정 수행 일시',
    dispatch_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '배정 상태',
    dispatch_reason VARCHAR(500) NULL COMMENT '배정 또는 제외 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_EXECUTION_TARGET PRIMARY KEY (target_id),
    CONSTRAINT fk_bat_execution_target_execution FOREIGN KEY (execution_id) REFERENCES BAT_EXECUTION (execution_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_job FOREIGN KEY (job_id) REFERENCES BAT_JOB (job_id),
    CONSTRAINT fk_bat_execution_target_schedule FOREIGN KEY (schedule_id) REFERENCES BAT_SCHEDULE (schedule_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_execution_target_instance FOREIGN KEY (target_instance_id) REFERENCES BAT_INSTANCE (instance_id) ON DELETE SET NULL,
    INDEX ix_bat_execution_target_job (job_id, dispatch_status, planned_run_at),
    INDEX ix_bat_execution_target_execution (execution_id),
    INDEX ix_bat_execution_target_instance (target_instance_id, dispatch_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 수행 대상/대기 인스턴스';

CREATE TABLE IF NOT EXISTS BAT_GHOST_EVENT (
    ghost_event_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 ghost 이벤트 순번',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    spring_batch_execution_id BIGINT NULL COMMENT 'Spring Batch JobExecution ID',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    instance_id VARCHAR(160) NULL COMMENT '서버 인스턴스 ID',
    worker_id VARCHAR(160) NULL COMMENT 'worker ID',
    ghost_status VARCHAR(30) NOT NULL DEFAULT 'DETECTED' COMMENT 'ghost 이벤트 상태',
    detected_reason VARCHAR(1000) NOT NULL COMMENT '감지 사유',
    action_type VARCHAR(30) NULL COMMENT '조치 유형',
    action_reason VARCHAR(1000) NULL COMMENT '조치 사유',
    action_by VARCHAR(100) NULL COMMENT '조치 운영자',
    detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '감지 일시',
    action_at DATETIME(3) NULL COMMENT '조치 일시',
    lock_released_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 해제 여부',
    retryable_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '재수행 가능 여부',
    before_data LONGTEXT NULL COMMENT '조치 전 데이터',
    after_data LONGTEXT NULL COMMENT '조치 후 데이터',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_GHOST_EVENT PRIMARY KEY (ghost_event_id),
    CONSTRAINT fk_bat_ghost_event_execution FOREIGN KEY (execution_id) REFERENCES BAT_EXECUTION (execution_id) ON DELETE SET NULL,
    CONSTRAINT fk_bat_ghost_event_job FOREIGN KEY (job_id) REFERENCES BAT_JOB (job_id),
    CONSTRAINT fk_bat_ghost_event_worker FOREIGN KEY (worker_id) REFERENCES BAT_WORKER (worker_id) ON DELETE SET NULL,
    INDEX ix_bat_ghost_event_execution (execution_id, ghost_status),
    INDEX ix_bat_ghost_event_job (job_id, detected_at),
    INDEX ix_bat_ghost_event_worker (worker_id, detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 ghost 감지와 조치 이력';

CREATE TABLE IF NOT EXISTS BAT_RUNTIME_CAPABILITY (
    instance_id VARCHAR(120) NOT NULL COMMENT 'Runtime instance identifier',
    capability_code VARCHAR(80) NOT NULL COMMENT 'Advertised capability code',
    CONSTRAINT pk_BAT_RUNTIME_CAPABILITY PRIMARY KEY (instance_id, capability_code),
    CONSTRAINT fk_bat_runtime_capability_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT runtime capability projection';

CREATE TABLE IF NOT EXISTS BAT_RUNTIME_HEARTBEAT (
    heartbeat_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Heartbeat event identifier',
    instance_id VARCHAR(120) NOT NULL COMMENT 'Runtime instance identifier',
    heartbeat_at DATETIME(6) NOT NULL COMMENT 'Heartbeat observation time',
    ready_yn CHAR(1) NOT NULL COMMENT 'Readiness flag',
    available_capacity INT NOT NULL DEFAULT 0 COMMENT 'Available execution capacity',
    queue_depth BIGINT NOT NULL DEFAULT 0 COMMENT 'Observed queue depth',
    draining_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Drain mode flag',
    current_execution_count INT NOT NULL DEFAULT 0 COMMENT 'Current execution count',
    active_lease_count INT NOT NULL DEFAULT 0 COMMENT 'Active lease count',
    last_error_code VARCHAR(80) NULL COMMENT 'Last runtime error code',
    deployment_version VARCHAR(80) NULL COMMENT 'Observed deployment version',
    CONSTRAINT pk_BAT_RUNTIME_HEARTBEAT PRIMARY KEY (heartbeat_id),
    CONSTRAINT fk_bat_runtime_heartbeat_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE CASCADE,
    INDEX ix_bat_runtime_heartbeat_instance (instance_id, heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT runtime heartbeat event';

CREATE TABLE IF NOT EXISTS BAT_SB_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID BIGINT NOT NULL COMMENT 'Spring Batch StepExecution 순번',
    SHORT_CONTEXT VARCHAR(2500) NOT NULL COMMENT '짧은 실행 컨텍스트',
    SERIALIZED_CONTEXT TEXT NULL COMMENT '직렬화 실행 컨텍스트',
    CONSTRAINT pk_BAT_SB_STEP_EXECUTION_CONTEXT PRIMARY KEY (STEP_EXECUTION_ID),
    CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID) REFERENCES BAT_SB_STEP_EXECUTION (STEP_EXECUTION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Spring Batch 표준 Step 컨텍스트 저장소';

CREATE TABLE IF NOT EXISTS BAT_SCHEDULE_TRIGGER (
    schedule_id VARCHAR(100) NOT NULL COMMENT 'Schedule identifier',
    scheduled_fire_at DATETIME(6) NOT NULL COMMENT 'Planned fire time',
    fencing_token BIGINT NOT NULL COMMENT 'Scheduler fencing token',
    execution_id BIGINT NULL COMMENT 'Created execution identifier',
    trigger_status VARCHAR(30) NOT NULL COMMENT 'Trigger result status',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Trigger record time',
    job_id VARCHAR(100) NOT NULL COMMENT '실행 Job ID',
    definition_version BIGINT NOT NULL COMMENT '고정 Job definition version',
    definition_checksum VARCHAR(128) NOT NULL COMMENT '고정 Job definition checksum',
    business_date DATE NOT NULL COMMENT '실행 영업일',
    fire_zone VARCHAR(50) NOT NULL COMMENT '예정시각 timezone',
    idempotency_key VARCHAR(200) NOT NULL COMMENT '재시작에도 고정되는 실행 멱등키',
    dispatch_owner VARCHAR(160) NULL COMMENT '현재 dispatch lease owner',
    dispatch_token BIGINT NULL COMMENT 'dispatch fencing token',
    dispatch_lease_until DATETIME(6) NULL COMMENT 'dispatch lease 만료시각',
    attempt_count INTEGER NOT NULL DEFAULT 0 COMMENT 'dispatch 시도 횟수',
    last_error_code VARCHAR(100) NULL COMMENT '최근 오류 코드',
    last_error_at DATETIME(6) NULL COMMENT '최근 오류 시각',
    dispatched_at DATETIME(6) NULL COMMENT '실제 dispatch 완료 시각',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '최근 상태 변경 시각',
    CONSTRAINT pk_BAT_SCHEDULE_TRIGGER PRIMARY KEY (schedule_id, scheduled_fire_at),
    CONSTRAINT uq_bat_schedule_trigger_idem UNIQUE (idempotency_key),
    CONSTRAINT fk_bat_schedule_trigger_schedule FOREIGN KEY (schedule_id) REFERENCES BAT_SCHEDULE (schedule_id) ON DELETE CASCADE,
    INDEX ix_bat_schedule_trigger_dispatch (trigger_status, dispatch_lease_until, scheduled_fire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT scheduled trigger evidence';

CREATE TABLE IF NOT EXISTS BAT_STEP_EXECUTION (
    step_execution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '배치 Step 실행 순번',
    execution_id BIGINT NOT NULL COMMENT '배치 실행 순번',
    spring_batch_step_execution_id BIGINT NULL COMMENT 'Spring Batch StepExecution ID',
    worker_id VARCHAR(160) NULL COMMENT '실행 worker ID',
    step_name VARCHAR(150) NOT NULL COMMENT 'Step 이름',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '실행 상태',
    start_time DATETIME(3) NULL COMMENT '시작 일시',
    end_time DATETIME(3) NULL COMMENT '종료 일시',
    read_count BIGINT NOT NULL DEFAULT 0 COMMENT '읽은 건수',
    write_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 건수',
    skip_count BIGINT NOT NULL DEFAULT 0 COMMENT '건너뛴 건수',
    total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수',
    processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수',
    retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수',
    progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률',
    tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수',
    avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초',
    max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초',
    last_heartbeat_at DATETIME(3) NULL COMMENT 'Step 메타 마지막 heartbeat 일시',
    error_message MEDIUMTEXT NULL COMMENT '오류 메시지',
    step_log MEDIUMTEXT NULL COMMENT 'Step 로그',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_STEP_EXECUTION PRIMARY KEY (step_execution_id),
    CONSTRAINT fk_bat_step_execution_parent FOREIGN KEY (execution_id) REFERENCES BAT_EXECUTION (execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_step_execution_worker FOREIGN KEY (worker_id) REFERENCES BAT_WORKER (worker_id) ON DELETE SET NULL,
    INDEX ix_bat_step_execution_parent (execution_id, step_name),
    INDEX ix_bat_step_execution_spring (spring_batch_step_execution_id),
    INDEX ix_bat_step_execution_worker (worker_id, start_time),
    INDEX ix_bat_step_execution_heartbeat (execution_status, last_heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 배치 Step 실행 이력';

CREATE TABLE IF NOT EXISTS GW_ATTEMPT (
    attempt_id VARCHAR(100) NOT NULL COMMENT 'Attempt ID',
    gateway_transaction_id VARCHAR(100) NOT NULL COMMENT 'Gateway 거래 ID',
    attempt_no INT NOT NULL COMMENT 'Attempt 순번',
    instance_id VARCHAR(100) NOT NULL COMMENT 'Target Instance',
    target_host VARCHAR(300) NULL COMMENT 'Target Host',
    target_port INT NULL COMMENT 'Target Port',
    target_protocol VARCHAR(30) NOT NULL COMMENT 'Target Protocol',
    connect_duration_ms BIGINT NOT NULL DEFAULT 0 COMMENT 'Connect 시간',
    response_duration_ms BIGINT NOT NULL DEFAULT 0 COMMENT 'Response 시간',
    attempt_status VARCHAR(30) NOT NULL COMMENT 'Attempt 상태',
    protocol_status VARCHAR(30) NULL COMMENT 'Protocol 상태',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작 시각',
    finished_at DATETIME NULL COMMENT '종료 시각',
    gateway_instance_id VARCHAR(100) NULL COMMENT 'Gateway Instance',
    selection_reason VARCHAR(100) NULL COMMENT 'Target 선택 사유',
    unknown_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Attempt 결과 불명 여부',
    CONSTRAINT pk_GW_ATTEMPT PRIMARY KEY (attempt_id),
    CONSTRAINT uk_cpf_gwy_attempt_no UNIQUE (gateway_transaction_id, attempt_no),
    CONSTRAINT fk_cpf_gwy_attempt_tx FOREIGN KEY (gateway_transaction_id) REFERENCES GW_TRANSACTION (gateway_transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Retry/Failover Attempt 원장';

CREATE TABLE IF NOT EXISTS GW_TRANSACTION_CAPTURE_SEGMENT (
    gateway_transaction_id VARCHAR(100) NOT NULL COMMENT 'Gateway 거래 ID',
    segment_type VARCHAR(40) NOT NULL COMMENT 'QUERY/REQUEST_HEADERS/REQUEST_BODY/RESPONSE_HEADERS/RESPONSE_BODY/ERROR_STACK',
    policy_schema_version INT NOT NULL DEFAULT 2 COMMENT '로그 정책 Schema Version',
    policy_checksum VARCHAR(64) NOT NULL COMMENT '적용 정책 Checksum',
    captured_value MEDIUMTEXT NOT NULL COMMENT '마스킹/보호된 Capture 값',
    truncated_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '상한 초과 절단 여부',
    metadata_only_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Metadata only 여부',
    observed_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '원본 관측 Byte',
    captured_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Capture 시각',
    CONSTRAINT pk_GW_TRANSACTION_CAPTURE_SEGMENT PRIMARY KEY (gateway_transaction_id, segment_type),
    CONSTRAINT ck_cpf_gwy_capture_truncated CHECK (truncated_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_gwy_capture_metadata CHECK (metadata_only_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_capture_tx FOREIGN KEY (gateway_transaction_id) REFERENCES GW_TRANSACTION (gateway_transaction_id),
    INDEX ix_cpf_gwy_capture_time (captured_at, segment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway 정책 기반 Capture Segment 원장';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_DELIVERY (
    delivery_id VARCHAR(80) NOT NULL COMMENT 'Runtime delivery identifier',
    change_id VARCHAR(80) NOT NULL COMMENT 'Runtime change identifier',
    instance_id VARCHAR(120) NOT NULL COMMENT 'Runtime instance identifier',
    sequence_no INT NOT NULL COMMENT 'Delivery sequence number',
    desired_version BIGINT NOT NULL COMMENT 'Desired state version',
    delivery_state VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'Runtime delivery lifecycle state',
    attempt_no INT NOT NULL DEFAULT 0 COMMENT 'Delivery attempt number',
    next_attempt_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'Next delivery attempt time',
    fencing_token BIGINT NULL COMMENT 'Monotonic fencing token',
    claimed_at DATETIME(3) NULL COMMENT 'Delivery claim time',
    acknowledged_at DATETIME(3) NULL COMMENT 'Delivery acknowledgment time',
    actual_hash VARCHAR(64) NULL COMMENT 'Applied state checksum',
    error_code VARCHAR(80) NULL COMMENT 'Failure code',
    error_message VARCHAR(900) NULL COMMENT 'Masked failure message',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_DELIVERY PRIMARY KEY (delivery_id),
    CONSTRAINT ck_cpf_runtime_delivery_state CHECK (delivery_state IN ('PENDING','CLAIMED','ACKED','FAILED','POISONED','UNKNOWN_RESULT','RESTART_REQUIRED','CANCELLED','EXPIRED','SUPERSEDED')),
    CONSTRAINT fk_cpf_runtime_delivery_change FOREIGN KEY (change_id) REFERENCES OPS_RUNTIME_CHANGE (change_id) ON DELETE CASCADE,
    CONSTRAINT fk_cpf_runtime_delivery_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE CASCADE,
    INDEX ix_cpf_runtime_delivery_claim (instance_id, delivery_state, next_attempt_at, sequence_no),
    INDEX ix_cpf_runtime_delivery_change (change_id, delivery_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime durable per-instance delivery inbox';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_GROUP_MEMBER (
    group_id VARCHAR(80) NOT NULL COMMENT 'Runtime instance group identifier',
    instance_id VARCHAR(120) NOT NULL COMMENT 'Runtime instance identifier',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'Active flag',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_GROUP_MEMBER PRIMARY KEY (group_id, instance_id),
    CONSTRAINT ck_cpf_runtime_group_member_active CHECK (active_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_runtime_group_member_group FOREIGN KEY (group_id) REFERENCES OPS_RUNTIME_INSTANCE_GROUP (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_cpf_runtime_group_member_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE CASCADE,
    INDEX ix_cpf_runtime_group_member_instance (instance_id, active_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Instance Group membership';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_INSTANCE_FEATURE_STATE (
    instance_id VARCHAR(120) NOT NULL COMMENT 'Runtime instance identifier',
    change_type VARCHAR(80) NOT NULL COMMENT 'Runtime change type',
    desired_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Desired state version',
    actual_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Applied state version',
    desired_hash VARCHAR(64) NULL COMMENT 'Desired state checksum',
    actual_hash VARCHAR(64) NULL COMMENT 'Applied state checksum',
    drift_state VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'Desired and actual state drift',
    source_delivery_id VARCHAR(80) NULL COMMENT 'Source delivery identifier',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_INSTANCE_FEATURE_STATE PRIMARY KEY (instance_id, change_type),
    CONSTRAINT ck_cpf_runtime_feature_drift CHECK (drift_state IN ('IN_SYNC','PENDING','DRIFT','UNKNOWN','UNKNOWN_RESULT','PENDING_RESTART','EXCLUDED')),
    CONSTRAINT fk_cpf_runtime_feature_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE CASCADE,
    INDEX ix_cpf_runtime_feature_drift (drift_state, change_type),
    INDEX ix_cpf_runtime_feature_delivery (source_delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime Instance 기능별 desired/actual/drift 상태';

CREATE TABLE IF NOT EXISTS OPS_RUNTIME_INSTANCE_STATE (
    instance_id VARCHAR(120) NOT NULL COMMENT 'Runtime instance identifier',
    fencing_token BIGINT NOT NULL DEFAULT 0 COMMENT 'Monotonic fencing token',
    lease_until DATETIME(3) NULL COMMENT 'Lease expiry time',
    desired_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Desired state version',
    actual_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Applied state version',
    desired_hash VARCHAR(64) NULL COMMENT 'Desired state checksum',
    actual_hash VARCHAR(64) NULL COMMENT 'Applied state checksum',
    drift_state VARCHAR(30) NOT NULL DEFAULT 'IN_SYNC' COMMENT 'Desired and actual state drift',
    capabilities_json LONGTEXT NULL COMMENT 'Runtime capabilities JSON',
    labels_json LONGTEXT NULL COMMENT 'Runtime labels JSON',
    artifact_version VARCHAR(100) NULL COMMENT 'Runtime artifact version',
    artifact_commit VARCHAR(64) NULL COMMENT '실행 Artifact 기준 Commit',
    runtime_role VARCHAR(40) NULL COMMENT 'APPLICATION/GATEWAY/BATCH/AGENT 등 Runtime 역할',
    desired_runtime_state VARCHAR(32) NOT NULL DEFAULT 'RUNNING' COMMENT 'Central runtime desired lifecycle state',
    actual_runtime_state VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'Central runtime actual lifecycle state',
    control_row_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Central runtime lifecycle optimistic version',
    registration_source VARCHAR(120) NULL COMMENT '배포/Discovery/Self registration identity source',
    schema_version VARCHAR(100) NULL COMMENT 'Runtime schema version',
    config_hash VARCHAR(64) NULL COMMENT 'Runtime configuration checksum',
    clock_skew_ms BIGINT NOT NULL DEFAULT 0 COMMENT 'Agent-Controller clock skew milliseconds',
    last_ack_change_id VARCHAR(80) NULL COMMENT 'Last acknowledged change identifier',
    last_ack_at DATETIME(3) NULL COMMENT 'Last acknowledgment time',
    heartbeat_at DATETIME(3) NULL COMMENT 'Last heartbeat time',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Creator identifier',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT 'Last updater identifier',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    CONSTRAINT pk_OPS_RUNTIME_INSTANCE_STATE PRIMARY KEY (instance_id),
    CONSTRAINT ck_cpf_runtime_instance_drift CHECK (drift_state IN ('IN_SYNC','PENDING','DRIFT','UNKNOWN','UNKNOWN_RESULT','PENDING_RESTART','EXCLUDED')),
    CONSTRAINT ck_ops_runtime_instance_role CHECK (runtime_role IS NULL OR BINARY runtime_role IN ('APPLICATION','CONTROL_PLANE','SCHEDULER','WORKER','CENTER_CUT_RUNNER','AGENT')),
    CONSTRAINT fk_cpf_runtime_instance_state_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE CASCADE,
    INDEX ix_cpf_runtime_instance_lease (lease_until),
    INDEX ix_cpf_runtime_instance_drift (drift_state, heartbeat_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Runtime desired/actual/lease/fencing 상태';

CREATE TABLE IF NOT EXISTS OPS_SERVICE_CALL_HISTORY (
    call_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '서비스 호출 이력 ID',
    transaction_id CHAR(34) NULL COMMENT '전역 거래 ID',
    trace_id VARCHAR(100) NULL COMMENT 'Trace ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    http_method VARCHAR(10) NOT NULL DEFAULT 'GET' COMMENT 'HTTP Method',
    request_path VARCHAR(500) NOT NULL DEFAULT '/' COMMENT '요청 경로',
    call_status VARCHAR(30) NOT NULL COMMENT '호출 상태',
    http_status INT NULL COMMENT 'HTTP 상태 코드',
    duration_ms BIGINT NULL COMMENT '소요 시간 밀리초',
    timeout_ms INT NULL COMMENT 'Timeout 밀리초',
    retry_count INT NULL COMMENT 'Retry 횟수',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_SERVICE_CALL_HISTORY PRIMARY KEY (call_id),
    CONSTRAINT fk_cpf_service_call_history_service FOREIGN KEY (service_id) REFERENCES OPS_SERVICE (service_id),
    CONSTRAINT fk_cpf_service_call_history_endpoint FOREIGN KEY (endpoint_code) REFERENCES OPS_SERVICE_ENDPOINT (endpoint_code) ON DELETE SET NULL,
    CONSTRAINT fk_cpf_service_call_history_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE SET NULL,
    INDEX ix_cpf_service_call_history_tx (transaction_id, call_id),
    INDEX ix_cpf_service_call_history_service (service_id, endpoint_code, created_at),
    INDEX ix_cpf_service_call_history_status (call_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 호출 이력';

CREATE TABLE IF NOT EXISTS OPS_SERVICE_CIRCUIT_STATE (
    circuit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Circuit 상태 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    circuit_state VARCHAR(30) NOT NULL DEFAULT 'CLOSED' COMMENT 'Circuit 상태',
    failure_count INT NOT NULL DEFAULT 0 COMMENT '실패 횟수',
    success_count INT NOT NULL DEFAULT 0 COMMENT '성공 횟수',
    opened_at DATETIME(3) NULL COMMENT 'Open 일시',
    half_opened_at DATETIME(3) NULL COMMENT 'Half-open 일시',
    closed_at DATETIME(3) NULL COMMENT 'Close 일시',
    last_failure_message VARCHAR(1000) NULL COMMENT '마지막 실패 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_SERVICE_CIRCUIT_STATE PRIMARY KEY (circuit_id),
    CONSTRAINT uk_cpf_service_circuit_state UNIQUE (service_id, endpoint_code, instance_id),
    CONSTRAINT fk_cpf_service_circuit_service FOREIGN KEY (service_id) REFERENCES OPS_SERVICE (service_id),
    CONSTRAINT fk_cpf_service_circuit_endpoint FOREIGN KEY (endpoint_code) REFERENCES OPS_SERVICE_ENDPOINT (endpoint_code),
    CONSTRAINT fk_cpf_service_circuit_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE SET NULL,
    INDEX ix_cpf_service_circuit_state (circuit_state, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 Circuit 상태';

CREATE TABLE IF NOT EXISTS OPS_SERVICE_HEALTH_STATUS (
    health_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '서비스 health 이력 ID',
    service_id VARCHAR(40) NOT NULL COMMENT '서비스 ID',
    endpoint_code VARCHAR(80) NOT NULL COMMENT 'Endpoint 코드',
    instance_id VARCHAR(120) NULL COMMENT '서비스 인스턴스 ID',
    health_status VARCHAR(30) NOT NULL COMMENT 'Health 상태',
    http_status INT NULL COMMENT 'HTTP 상태 코드',
    response_time_ms BIGINT NULL COMMENT '응답 시간 밀리초',
    failure_message VARCHAR(1000) NULL COMMENT '실패 메시지',
    checked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '점검 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_OPS_SERVICE_HEALTH_STATUS PRIMARY KEY (health_id),
    CONSTRAINT fk_cpf_service_health_service FOREIGN KEY (service_id) REFERENCES OPS_SERVICE (service_id),
    CONSTRAINT fk_cpf_service_health_endpoint FOREIGN KEY (endpoint_code) REFERENCES OPS_SERVICE_ENDPOINT (endpoint_code),
    CONSTRAINT fk_cpf_service_health_instance FOREIGN KEY (instance_id) REFERENCES OPS_SERVICE_INSTANCE (instance_id) ON DELETE SET NULL,
    INDEX ix_cpf_service_health_target (service_id, endpoint_code, instance_id, checked_at),
    INDEX ix_cpf_service_health_status (health_status, checked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 서비스 Health 상태 이력';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_ITEM (
    center_cut_item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    center_cut_execution_id VARCHAR(80) NULL COMMENT 'Center-cut execution identifier',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NULL COMMENT '업무 기준일',
    item_status VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태',
    transaction_id CHAR(34) NULL COMMENT '센터컷 실행 전체가 승계하는 CPF transactionId',
    transaction_segment_id VARCHAR(120) NULL COMMENT '현재 센터컷 Item 실행 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '부모 센터컷/Worker 실행 구간 ID',
    item_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    started_at DATETIME(3) NULL COMMENT '처리 시작 일시',
    completed_at DATETIME(3) NULL COMMENT '처리 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_CENTER_CUT_ITEM PRIMARY KEY (center_cut_item_id),
    CONSTRAINT uk_bat_center_cut_item_execution_business UNIQUE (center_cut_execution_id, business_key),
    CONSTRAINT fk_bat_center_cut_item_job FOREIGN KEY (center_cut_job_id) REFERENCES BAT_CENTER_CUT_JOB (center_cut_job_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_item_execution FOREIGN KEY (center_cut_execution_id) REFERENCES BAT_CENTER_CUT_EXECUTION (center_cut_execution_id) ON DELETE CASCADE,
    INDEX ix_bat_center_cut_item_status (center_cut_job_id, item_status, business_date),
    INDEX ix_bat_center_cut_item_transaction (transaction_id, transaction_segment_id),
    INDEX ix_bat_center_cut_item_parent_segment (parent_segment_id),
    INDEX ix_bat_center_cut_item_execution_status (center_cut_execution_id, item_status, center_cut_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 대상';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_RATE_WINDOW (
    center_cut_execution_id VARCHAR(80) NOT NULL COMMENT 'Center-cut execution identifier',
    window_second BIGINT NOT NULL COMMENT 'UTC epoch-second rate window',
    admitted_count INT NOT NULL DEFAULT 0 COMMENT 'Items admitted in this window',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last bucket update time',
    CONSTRAINT pk_BAT_CENTER_CUT_RATE_WINDOW PRIMARY KEY (center_cut_execution_id, window_second),
    CONSTRAINT fk_bat_center_cut_rate_execution FOREIGN KEY (center_cut_execution_id) REFERENCES BAT_CENTER_CUT_EXECUTION (center_cut_execution_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT center-cut global rate window';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_CLAIM (
    center_cut_item_id BIGINT NULL COMMENT 'Claimed center-cut item identifier',
    runner_id VARCHAR(160) NOT NULL COMMENT 'Owning runner identifier',
    pool_id VARCHAR(80) NULL COMMENT 'Owning runner pool identifier',
    claim_token VARCHAR(80) NOT NULL COMMENT 'Unique claim token',
    claim_status VARCHAR(30) NOT NULL COMMENT 'Claim lifecycle status',
    fencing_token BIGINT NOT NULL COMMENT 'Monotonic claim fencing token',
    lease_until DATETIME(6) NOT NULL COMMENT 'Claim lease expiry time',
    last_heartbeat_at DATETIME(6) NOT NULL COMMENT 'Claim heartbeat time',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT 'Claim attempt number',
    takeover_count INT NOT NULL DEFAULT 0 COMMENT 'Claim takeover count',
    released_at DATETIME(6) NULL COMMENT 'Claim release time',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last claim update time',
    CONSTRAINT pk_BAT_CENTER_CUT_CLAIM PRIMARY KEY (center_cut_item_id),
    CONSTRAINT claim_token UNIQUE (claim_token),
    CONSTRAINT fk_bat_center_cut_claim_item FOREIGN KEY (center_cut_item_id) REFERENCES BAT_CENTER_CUT_ITEM (center_cut_item_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT center-cut item lease claim';

CREATE TABLE IF NOT EXISTS BAT_CENTER_CUT_RESULT (
    center_cut_result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 결과 순번',
    center_cut_item_id BIGINT NOT NULL COMMENT '센터컷 대상 순번',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    transaction_id CHAR(34) NULL COMMENT '센터컷 실행 전체가 승계하는 CPF transactionId',
    transaction_segment_id VARCHAR(120) NULL COMMENT '결과를 생성한 거래 구간 ID',
    parent_segment_id VARCHAR(120) NULL COMMENT '부모 센터컷/Worker 실행 구간 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_BAT_CENTER_CUT_RESULT PRIMARY KEY (center_cut_result_id),
    CONSTRAINT fk_bat_center_cut_result_item FOREIGN KEY (center_cut_item_id) REFERENCES BAT_CENTER_CUT_ITEM (center_cut_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_bat_center_cut_result_job FOREIGN KEY (center_cut_job_id) REFERENCES BAT_CENTER_CUT_JOB (center_cut_job_id) ON DELETE CASCADE,
    INDEX ix_bat_center_cut_result_item (center_cut_item_id, result_status),
    INDEX ix_bat_center_cut_result_transaction (transaction_id, transaction_segment_id),
    INDEX ix_bat_center_cut_result_parent_segment (parent_segment_id),
    INDEX ix_bat_center_cut_result_job (center_cut_job_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 센터컷 처리 결과';

-- CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences
-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.
-- Spring Batch 6.0.4 JobRepository sequence contract; do not edit vendor SQL directly.
CREATE SEQUENCE IF NOT EXISTS BAT_SB_JOB_INSTANCE_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BAT_SB_JOB_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

CREATE SEQUENCE IF NOT EXISTS BAT_SB_STEP_EXECUTION_SEQ
    START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806
    INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;
-- CPF_CANONICAL_OBJECTS_END spring-batch-6-sequences
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=mariadb
-- DO NOT EDIT generated DDL directly.

-- CPF_LOGICAL_DATABASE=mbwDB
USE mbwDB;
CREATE TABLE IF NOT EXISTS MBW_ADMIN_USER (
    admin_user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 사용자 순번',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    admin_name VARCHAR(100) NOT NULL COMMENT '업무 관리자명',
    password_hash VARCHAR(300) NULL COMMENT '업무 관리자 비밀번호 hash',
    role_code VARCHAR(50) NULL COMMENT '호환용 대표 역할 코드; 신규 계정은 Role 미부여가 기본이며 실제 권한은 MBW_USER_ROLE 다중 매핑이 정본',
    account_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION' COMMENT '계정 상태: PENDING_ACTIVATION/ACTIVE/LOCKED/SUSPENDED/DISABLED',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    create_operation_id VARCHAR(100) NULL COMMENT '관리자 생성 멱등 Operation ID',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    lock_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금 여부',
    login_fail_count INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    password_change_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '비밀번호 강제 변경 여부',
    password_expire_at DATETIME NULL COMMENT '비밀번호 만료 일시',
    last_login_at DATETIME NULL COMMENT '최근 로그인 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_ADMIN_USER PRIMARY KEY (admin_user_id),
    CONSTRAINT uk_mbw_admin_user_login UNIQUE (admin_login_id),
    CONSTRAINT uk_mbw_admin_user_create_operation UNIQUE (create_operation_id),
    CONSTRAINT ck_mbw_admin_user_status CHECK (account_status IN ('PENDING_ACTIVATION','ACTIVE','LOCKED','SUSPENDED','DISABLED')),
    INDEX ix_mbw_admin_user_role (role_code, use_yn),
    INDEX ix_mbw_admin_user_status (account_status, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 관리자 사용자';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_POLICY (
    policy_code VARCHAR(80) NOT NULL COMMENT '업무 결재 정책 코드',
    policy_version INT NOT NULL COMMENT '정책 버전',
    policy_name VARCHAR(150) NOT NULL COMMENT '정책명',
    business_domain VARCHAR(30) NOT NULL COMMENT '적용 업무 영역',
    approval_type VARCHAR(50) NOT NULL COMMENT '적용 결재 유형',
    effective_from DATETIME(3) NOT NULL COMMENT '시행 시작시각',
    effective_to DATETIME(3) NULL COMMENT '시행 종료시각',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    self_approval_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '자기승인 허용 여부',
    description VARCHAR(1000) NULL COMMENT '정책 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_POLICY PRIMARY KEY (policy_code, policy_version),
    CONSTRAINT ck_mbw_approval_policy_version CHECK (policy_version > 0),
    CONSTRAINT ck_mbw_approval_policy_flags CHECK (enabled_yn IN ('Y','N') AND self_approval_allowed_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_approval_policy_effective CHECK (effective_to IS NULL OR effective_to > effective_from),
    INDEX ix_mbw_approval_policy_lookup (business_domain, approval_type, enabled_yn, effective_from, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 결재 정책 Version';

CREATE TABLE IF NOT EXISTS MBW_ATTACHMENT (
    attachment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '첨부파일 순번',
    attachment_group_id VARCHAR(80) NOT NULL COMMENT '첨부파일 그룹 ID',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장 파일명',
    storage_key VARCHAR(500) NOT NULL COMMENT '저장소 상대 key',
    content_type VARCHAR(120) NOT NULL COMMENT '파일 Content-Type',
    file_size BIGINT NOT NULL COMMENT '파일 크기 byte',
    checksum_sha256 CHAR(64) NOT NULL COMMENT '파일 SHA-256 checksum',
    scan_status VARCHAR(40) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CLEAN/INFECTED/FAILED/QUARANTINED',
    data_classification VARCHAR(30) NOT NULL DEFAULT 'INTERNAL' COMMENT 'PUBLIC/INTERNAL/CONFIDENTIAL/RESTRICTED',
    retention_until DATETIME(3) NULL COMMENT '보존 만료시각',
    quarantine_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '격리 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_ATTACHMENT PRIMARY KEY (attachment_id),
    CONSTRAINT uk_mbw_attachment_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_mbw_attachment_scan CHECK (scan_status IN ('PENDING','CLEAN','INFECTED','FAILED','QUARANTINED')),
    CONSTRAINT ck_mbw_attachment_classification CHECK (data_classification IN ('PUBLIC','INTERNAL','CONFIDENTIAL','RESTRICTED')),
    CONSTRAINT ck_mbw_attachment_quarantine CHECK (quarantine_yn IN ('Y','N')),
    INDEX ix_mbw_attachment_group (attachment_group_id, use_yn, created_at),
    INDEX ix_mbw_attachment_checksum (checksum_sha256),
    INDEX ix_mbw_attachment_retention (retention_until, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 첨부파일 메타';

CREATE TABLE IF NOT EXISTS MBW_AUDIT_CHAIN_LOCK (
    chain_id BIGINT NOT NULL COMMENT '감사 체인 식별자. 기본 체인은 1',
    current_hash CHAR(64) NULL COMMENT '현재 감사 체인 head SHA-256',
    last_audit_id BIGINT NULL COMMENT '현재 체인의 마지막 감사 ID',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '체인 갱신 버전',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '마지막 갱신자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '마지막 갱신시각',
    CONSTRAINT pk_MBW_AUDIT_CHAIN_LOCK PRIMARY KEY (chain_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 감사 체인 동시성/무결성 head';

CREATE TABLE IF NOT EXISTS MBW_BOOTSTRAP_APPROVAL (
    token_hash VARCHAR(64) NOT NULL COMMENT '1회 승인 Token SHA-256',
    env_fingerprint VARCHAR(64) NOT NULL COMMENT '환경 및 승인 Scope Fingerprint',
    status VARCHAR(20) NOT NULL COMMENT 'APPROVED/CLAIMED/COMPLETED/FAILED/EXPIRED',
    operation_id VARCHAR(100) NULL COMMENT 'Bootstrap 멱등 Operation ID',
    expires_at DATETIME(6) NOT NULL COMMENT '승인 Token 만료 시각',
    claimed_at DATETIME(6) NULL COMMENT 'Claim 시각',
    claim_owner_id VARCHAR(100) NULL COMMENT 'Claim한 Runtime Instance ID',
    claim_expires_at DATETIME(6) NULL COMMENT 'Claim Lease 만료 시각',
    completed_at DATETIME(6) NULL COMMENT 'Terminal 처리 시각',
    admin_user_id BIGINT NULL COMMENT '생성된 Backoffice 관리자 사용자 순번',
    failure_code VARCHAR(100) NULL COMMENT '마스킹된 Bootstrap 실패 코드',
    cleanup_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Secret Cleanup 상태',
    cleanup_failure_code VARCHAR(100) NULL COMMENT '마스킹된 Secret Cleanup 실패 코드',
    cleanup_updated_at DATETIME(6) NULL COMMENT 'Secret Cleanup 상태 갱신 시각',
    requested_by VARCHAR(100) NOT NULL COMMENT '승인 요청자',
    approved_by VARCHAR(100) NOT NULL COMMENT '승인자',
    approval_reason VARCHAR(500) NOT NULL COMMENT '승인 사유',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록 일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정 일시',
    CONSTRAINT pk_MBW_BOOTSTRAP_APPROVAL PRIMARY KEY (token_hash),
    CONSTRAINT ux_mbw_bootstrap_operation UNIQUE (operation_id),
    CONSTRAINT ck_mbw_bootstrap_status CHECK (status IN ('APPROVED','CLAIMED','COMPLETED','FAILED','EXPIRED')),
    CONSTRAINT ck_mbw_bootstrap_maker_checker CHECK (requested_by <> approved_by),
    CONSTRAINT ck_mbw_bootstrap_cleanup_status CHECK (cleanup_status IN ('PENDING','COMPLETED','FAILED')),
    INDEX ix_mbw_bootstrap_expiry (status, expires_at),
    INDEX ix_mbw_bootstrap_claim_lease (status, claim_expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 최초 특권 운영자 Bootstrap 승인 및 복구 원장';

CREATE TABLE IF NOT EXISTS MBW_BUSINESS_AUDIT (
    audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 감사 순번',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    actor_id VARCHAR(100) NOT NULL COMMENT '처리 사용자 ID',
    action_type VARCHAR(50) NOT NULL COMMENT '업무 행위 유형',
    target_type VARCHAR(80) NOT NULL COMMENT '대상 유형',
    target_id VARCHAR(120) NOT NULL COMMENT '대상 ID',
    reason VARCHAR(500) NOT NULL COMMENT '업무 처리 사유',
    before_data LONGTEXT NULL COMMENT '변경 전 데이터',
    after_data LONGTEXT NULL COMMENT '변경 후 데이터',
    previous_record_hash CHAR(64) NULL COMMENT '동일 감사 스트림의 이전 레코드 SHA-256',
    record_hash CHAR(64) NULL COMMENT '감사 레코드 tamper detection SHA-256',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_BUSINESS_AUDIT PRIMARY KEY (audit_id),
    INDEX ix_mbw_business_audit_target (target_type, target_id, created_at),
    INDEX ix_mbw_business_audit_actor (actor_id, created_at),
    INDEX ix_mbw_business_audit_transaction (transaction_id),
    INDEX ix_mbw_business_audit_hash (record_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 감사';

CREATE TABLE IF NOT EXISTS MBW_DOWNLOAD_AUDIT (
    download_audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '다운로드 감사 순번',
    actor_id VARCHAR(100) NOT NULL COMMENT '다운로드 처리 로그인 ID',
    download_code VARCHAR(80) NOT NULL COMMENT '다운로드 기능 코드',
    reason VARCHAR(500) NOT NULL COMMENT '다운로드 사유',
    filter_json LONGTEXT NULL COMMENT '다운로드 검색 조건 JSON',
    row_count BIGINT NOT NULL DEFAULT 0 COMMENT '다운로드 결과 건수',
    result_status VARCHAR(40) NOT NULL COMMENT '다운로드 결과 상태',
    file_name VARCHAR(255) NULL COMMENT '다운로드 파일명',
    masking_applied_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '마스킹 적용 여부',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_DOWNLOAD_AUDIT PRIMARY KEY (download_audit_id),
    INDEX ix_mbw_download_audit_actor (actor_id, created_at),
    INDEX ix_mbw_download_audit_transaction (transaction_id),
    INDEX ix_mbw_download_audit_status (result_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 다운로드 감사';

CREATE TABLE IF NOT EXISTS MBW_JOB_TITLE (
    job_title_code VARCHAR(50) NOT NULL COMMENT '직책 코드',
    job_title_name VARCHAR(100) NOT NULL COMMENT '직책명',
    manager_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '조직 책임자 성격 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_JOB_TITLE PRIMARY KEY (job_title_code),
    CONSTRAINT ck_mbw_job_title_flags CHECK (manager_yn IN ('Y','N') AND use_yn IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 직책 기준정보';

CREATE TABLE IF NOT EXISTS MBW_MENU (
    menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 메뉴 순번',
    menu_code VARCHAR(80) NOT NULL COMMENT '업무 메뉴 코드',
    menu_name VARCHAR(120) NOT NULL COMMENT '업무 메뉴명',
    parent_menu_code VARCHAR(80) NULL COMMENT '상위 업무 메뉴 코드',
    module_code VARCHAR(20) NOT NULL DEFAULT 'MBW' COMMENT '소유 업무 모듈 코드',
    route_path VARCHAR(300) NULL COMMENT '화면 이동 경로',
    icon_code VARCHAR(80) NULL COMMENT '화면 아이콘 코드',
    environment_code VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '적용 환경 코드',
    api_path VARCHAR(300) NULL COMMENT '연결 API 경로',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_MENU PRIMARY KEY (menu_id),
    CONSTRAINT uk_mbw_menu_code UNIQUE (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 메뉴';

CREATE TABLE IF NOT EXISTS MBW_NOTIFICATION (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 알림 순번',
    recipient_login_id VARCHAR(100) NOT NULL COMMENT '수신 Backoffice 로그인 ID',
    notification_type VARCHAR(40) NOT NULL COMMENT '업무 알림 유형',
    title VARCHAR(200) NOT NULL COMMENT '업무 알림 제목',
    message_body VARCHAR(2000) NOT NULL COMMENT '업무 알림 내용',
    reference_type VARCHAR(80) NULL COMMENT '참조 업무 유형',
    reference_id VARCHAR(120) NULL COMMENT '참조 업무 ID',
    read_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '읽음 여부',
    read_at DATETIME NULL COMMENT '읽음 일시',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_NOTIFICATION PRIMARY KEY (notification_id),
    INDEX ix_mbw_notification_recipient (recipient_login_id, read_yn, use_yn, created_at),
    INDEX ix_mbw_notification_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 알림';

CREATE TABLE IF NOT EXISTS MBW_ORGANIZATION (
    organization_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '조직 순번',
    organization_code VARCHAR(50) NOT NULL COMMENT '조직 코드',
    parent_organization_code VARCHAR(50) NULL COMMENT '상위 조직 코드',
    organization_name VARCHAR(120) NOT NULL COMMENT '조직명',
    organization_type VARCHAR(30) NOT NULL DEFAULT 'DEPARTMENT' COMMENT '조직 유형',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '조직 정렬 순서',
    effective_from DATETIME(3) NULL COMMENT '조직 적용 시작시각',
    effective_to DATETIME(3) NULL COMMENT '조직 적용 종료시각',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_ORGANIZATION PRIMARY KEY (organization_id),
    CONSTRAINT uk_mbw_organization_code UNIQUE (organization_code),
    CONSTRAINT ck_mbw_organization_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_organization_effective CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to > effective_from),
    INDEX ix_mbw_organization_parent (parent_organization_code, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 조직';

CREATE TABLE IF NOT EXISTS MBW_PERMISSION (
    permission_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 권한 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    menu_code VARCHAR(80) NOT NULL COMMENT '업무 메뉴 코드',
    button_code VARCHAR(80) NOT NULL COMMENT '버튼/행위 코드',
    permission_type VARCHAR(30) NOT NULL DEFAULT 'BUTTON' COMMENT '권한 유형 SCREEN, BUTTON, API',
    http_method VARCHAR(10) NULL COMMENT 'API HTTP 메서드',
    api_pattern VARCHAR(300) NULL COMMENT 'API 경로 패턴',
    domain_code VARCHAR(30) NULL COMMENT '적용 업무 영역 코드',
    environment_code VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '적용 환경 코드',
    data_scope VARCHAR(30) NOT NULL DEFAULT 'ROLE' COMMENT '권한 데이터 범위',
    allow_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '허용 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_PERMISSION PRIMARY KEY (permission_id),
    CONSTRAINT uk_mbw_permission_scope UNIQUE (role_code, menu_code, button_code, permission_type, environment_code),
    INDEX ix_mbw_permission_scope (role_code, menu_code, button_code, environment_code, domain_code, http_method),
    INDEX ix_mbw_permission_menu (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 권한';

CREATE TABLE IF NOT EXISTS MBW_POSITION (
    position_code VARCHAR(50) NOT NULL COMMENT '직급 코드',
    position_name VARCHAR(100) NOT NULL COMMENT '직급명',
    rank_order INT NOT NULL DEFAULT 0 COMMENT '직급 정렬/서열 값',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_POSITION PRIMARY KEY (position_code),
    CONSTRAINT ck_mbw_position_use CHECK (use_yn IN ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 직급 기준정보';

CREATE TABLE IF NOT EXISTS MBW_PROJECT_SETTING (
    setting_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 설정 순번',
    setting_key VARCHAR(120) NOT NULL COMMENT '업무 설정 키',
    setting_value VARCHAR(1000) NULL COMMENT '업무 설정 값',
    description VARCHAR(500) NULL COMMENT '업무 설정 설명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_PROJECT_SETTING PRIMARY KEY (setting_id),
    CONSTRAINT uk_mbw_project_setting_key UNIQUE (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 프로젝트 설정';

CREATE TABLE IF NOT EXISTS MBW_ROLE (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 역할 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    role_name VARCHAR(120) NOT NULL COMMENT '업무 역할명',
    write_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '쓰기 허용 여부',
    data_scope VARCHAR(30) NOT NULL DEFAULT 'OWN' COMMENT '기본 데이터 접근 범위',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_ROLE PRIMARY KEY (role_id),
    CONSTRAINT uk_mbw_role_code UNIQUE (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 역할';

CREATE TABLE IF NOT EXISTS MBW_SAVED_SEARCH (
    saved_search_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '저장 검색 순번',
    owner_login_id VARCHAR(100) NOT NULL COMMENT '저장 검색 소유 로그인 ID',
    screen_code VARCHAR(80) NOT NULL COMMENT '적용 화면 코드',
    search_name VARCHAR(120) NOT NULL COMMENT '저장 검색명',
    criteria_json LONGTEXT NOT NULL COMMENT '검색 조건 JSON',
    shared_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '공유 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_SAVED_SEARCH PRIMARY KEY (saved_search_id),
    CONSTRAINT uk_mbw_saved_search_owner UNIQUE (owner_login_id, screen_code, search_name),
    INDEX ix_mbw_saved_search_screen (screen_code, shared_yn, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 저장 검색';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_DOCUMENT (
    approval_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 문서 순번',
    approval_no VARCHAR(50) NOT NULL COMMENT '결재 문서 번호',
    approval_type VARCHAR(50) NOT NULL COMMENT '결재 유형',
    business_domain VARCHAR(30) NOT NULL COMMENT '요청 업무 영역',
    policy_code VARCHAR(80) NULL COMMENT '적용 결재 정책 코드',
    policy_version INT NULL COMMENT '적용 결재 정책 버전 Snapshot',
    policy_snapshot_json LONGTEXT NULL COMMENT '상신 시 결재 정책/경로 불변 Snapshot JSON',
    title VARCHAR(200) NOT NULL COMMENT '결재 제목',
    requester_employee_no VARCHAR(50) NOT NULL COMMENT '요청자 직원 번호',
    requester_organization_code VARCHAR(50) NULL COMMENT '상신 시 요청자 조직 Snapshot',
    requester_position_code VARCHAR(50) NULL COMMENT '상신 시 요청자 직급 Snapshot',
    requester_job_title_code VARCHAR(50) NULL COMMENT '상신 시 요청자 직책 Snapshot',
    approval_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '결재 상태',
    approval_mode VARCHAR(30) NOT NULL DEFAULT 'SEQUENTIAL' COMMENT '결재 방식',
    current_step_no INT NOT NULL DEFAULT 0 COMMENT '현재 결재 단계',
    due_at DATETIME NULL COMMENT '결재 기한',
    payload_json LONGTEXT NULL COMMENT '결재 업무 데이터 JSON',
    payload_hash CHAR(64) NULL COMMENT '결재 대상 Command/Payload SHA-256',
    request_idempotency_key VARCHAR(120) NULL COMMENT '상신 중복 방지 Key',
    attachment_group_id VARCHAR(100) NULL COMMENT '첨부파일 그룹 ID',
    resubmitted_from_approval_id BIGINT NULL COMMENT '재상신 원본 결재 ID; 원본 Snapshot은 변경하지 않음',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    submitted_at DATETIME(3) NULL COMMENT '상신 시각',
    completed_at DATETIME(3) NULL COMMENT '최종 승인/반려/취소 완료 시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_DOCUMENT PRIMARY KEY (approval_id),
    CONSTRAINT uk_mbw_approval_document_no UNIQUE (approval_no),
    CONSTRAINT uk_mbw_approval_document_idempotency UNIQUE (request_idempotency_key),
    CONSTRAINT ck_mbw_approval_document_policy_pair CHECK ((policy_code IS NULL AND policy_version IS NULL) OR (policy_code IS NOT NULL AND policy_version IS NOT NULL)),
    CONSTRAINT ck_mbw_approval_document_status CHECK (approval_status IN ('DRAFT','IN_REVIEW','APPROVED','REJECTED','WITHDRAWN','CANCELED','EXPIRED')),
    CONSTRAINT ck_mbw_approval_document_mode CHECK (approval_mode IN ('SEQUENTIAL','PARALLEL')),
    CONSTRAINT ck_mbw_approval_document_step CHECK (current_step_no >= 0),
    CONSTRAINT ck_mbw_approval_document_version CHECK (version_no >= 0),
    CONSTRAINT fk_mbw_approval_document_policy FOREIGN KEY (policy_code, policy_version) REFERENCES MBW_APPROVAL_POLICY (policy_code, policy_version),
    CONSTRAINT fk_mbw_approval_document_resubmit FOREIGN KEY (resubmitted_from_approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id),
    INDEX ix_mbw_approval_document_status (approval_status, due_at),
    INDEX ix_mbw_approval_document_requester (requester_employee_no, created_at),
    INDEX ix_mbw_approval_document_transaction (transaction_id, created_at),
    INDEX ix_mbw_approval_document_resubmit (resubmitted_from_approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 결재 문서';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_POLICY_STEP (
    policy_code VARCHAR(80) NOT NULL COMMENT '업무 결재 정책 코드',
    policy_version INT NOT NULL COMMENT '정책 버전',
    step_no INT NOT NULL COMMENT '결재 단계',
    step_type VARCHAR(30) NOT NULL DEFAULT 'APPROVAL' COMMENT 'APPROVAL/AGREEMENT/REVIEW',
    target_type VARCHAR(30) NOT NULL COMMENT 'EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION',
    target_code VARCHAR(100) NOT NULL COMMENT '대상 코드',
    decision_rule VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY/N_OF_M',
    required_count INT NULL COMMENT 'N_OF_M 최소 결정 수',
    required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 대상 여부',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '동일 단계 표시 순서',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_POLICY_STEP PRIMARY KEY (policy_code, policy_version, step_no, target_type, target_code),
    CONSTRAINT ck_mbw_approval_policy_step_no CHECK (step_no >= 1),
    CONSTRAINT ck_mbw_approval_policy_step_type CHECK (step_type IN ('APPROVAL','AGREEMENT','REVIEW')),
    CONSTRAINT ck_mbw_approval_policy_step_target CHECK (target_type IN ('EMPLOYEE','ROLE','ORGANIZATION','ORG_MANAGER','POSITION')),
    CONSTRAINT ck_mbw_approval_policy_step_rule CHECK (decision_rule IN ('ALL','ANY','N_OF_M')),
    CONSTRAINT ck_mbw_approval_policy_step_required CHECK (required_yn IN ('Y','N') AND ( (decision_rule = 'N_OF_M' AND required_count IS NOT NULL AND required_count > 0) OR (decision_rule <> 'N_OF_M' AND required_count IS NULL) )),
    CONSTRAINT fk_mbw_approval_policy_step_policy FOREIGN KEY (policy_code, policy_version) REFERENCES MBW_APPROVAL_POLICY (policy_code, policy_version) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 결재 정책 단계';

CREATE TABLE IF NOT EXISTS MBW_EMPLOYEE (
    employee_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '직원 순번',
    employee_no VARCHAR(50) NOT NULL COMMENT '직원 번호',
    admin_user_id BIGINT NULL COMMENT '연결 업무 관리자 사용자 순번',
    organization_code VARCHAR(50) NOT NULL COMMENT '대표 조직 코드; 유효 소속 정본은 MBW_EMPLOYEE_ASSIGNMENT',
    employee_name VARCHAR(100) NOT NULL COMMENT '직원명',
    position_code VARCHAR(50) NULL COMMENT '직급 코드',
    job_title_code VARCHAR(50) NULL COMMENT '직책 코드',
    manager_employee_no VARCHAR(50) NULL COMMENT '상위 관리자 직원 번호',
    employment_status VARCHAR(30) NOT NULL DEFAULT 'EMPLOYED' COMMENT '재직 상태; 신규 직원 기본값 EMPLOYED',
    join_date DATE NULL COMMENT '입사일',
    leave_date DATE NULL COMMENT '퇴사일',
    email VARCHAR(200) NULL COMMENT '업무 이메일',
    mobile_no VARCHAR(50) NULL COMMENT '연락처(휴대폰); 숫자형이 아닌 문자열로 국가번호와 선행 0을 보존',
    office_phone_no VARCHAR(50) NULL COMMENT '내부 전화번호/내선; 휴대폰 연락처와 분리',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_EMPLOYEE PRIMARY KEY (employee_id),
    CONSTRAINT uk_mbw_employee_no UNIQUE (employee_no),
    CONSTRAINT uk_mbw_employee_admin_user UNIQUE (admin_user_id),
    CONSTRAINT ck_mbw_employee_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_employee_status CHECK (employment_status IN ('EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED')),
    CONSTRAINT ck_mbw_employee_employment_period CHECK (leave_date IS NULL OR join_date IS NULL OR leave_date >= join_date),
    CONSTRAINT fk_mbw_employee_admin_user FOREIGN KEY (admin_user_id) REFERENCES MBW_ADMIN_USER (admin_user_id) ON DELETE SET NULL,
    CONSTRAINT fk_mbw_employee_organization FOREIGN KEY (organization_code) REFERENCES MBW_ORGANIZATION (organization_code),
    CONSTRAINT fk_mbw_employee_position FOREIGN KEY (position_code) REFERENCES MBW_POSITION (position_code) ON DELETE SET NULL,
    CONSTRAINT fk_mbw_employee_job_title FOREIGN KEY (job_title_code) REFERENCES MBW_JOB_TITLE (job_title_code) ON DELETE SET NULL,
    INDEX ix_mbw_employee_organization (organization_code, employment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 직원 프로필';

CREATE TABLE IF NOT EXISTS MBW_LOGIN_HISTORY (
    login_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 로그인 이력 순번',
    admin_user_id BIGINT NULL COMMENT '업무 관리자 사용자 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'MBW' COMMENT '로그인 도메인',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    login_result VARCHAR(30) NOT NULL COMMENT '로그인 결과',
    failure_reason VARCHAR(500) NULL COMMENT '로그인 실패 사유',
    client_ip VARCHAR(50) NULL COMMENT '클라이언트 IP',
    user_agent VARCHAR(500) NULL COMMENT 'User-Agent',
    transaction_id CHAR(34) NULL COMMENT 'CPF 전역 transactionId',
    system_code VARCHAR(20) NULL COMMENT '논리 Backoffice System Code (MBW)',
    application_name VARCHAR(200) NULL COMMENT '현재 요청 처리 Application 이름',
    instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_LOGIN_HISTORY PRIMARY KEY (login_history_id),
    CONSTRAINT fk_mbw_login_history_user FOREIGN KEY (admin_user_id) REFERENCES MBW_ADMIN_USER (admin_user_id) ON DELETE SET NULL,
    INDEX ix_mbw_login_history_user_time (admin_user_id, created_at),
    INDEX ix_mbw_login_history_result_time (login_result, created_at),
    INDEX ix_mbw_login_history_global (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 관리자 로그인 이력';

CREATE TABLE IF NOT EXISTS MBW_LOGIN_OPERATION (
    operation_id VARCHAR(100) NOT NULL COMMENT '로그인 멱등 Operation ID',
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    admin_login_id VARCHAR(80) NOT NULL COMMENT '업무 관리자 로그인 ID',
    operation_status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/SUCCESS',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    request_hash VARCHAR(64) NOT NULL COMMENT '요청 payload canonical SHA-256',
    result_access_token_enc LONGTEXT NULL COMMENT '재시도 결과 복구용 암호화 Access Token',
    result_refresh_token_enc LONGTEXT NULL COMMENT '재시도 결과 복구용 암호화 Refresh Token',
    result_refresh_expires_at DATETIME(3) NULL COMMENT 'Refresh Token 만료 시각',
    result_expires_at DATETIME(3) NULL COMMENT 'Operation 결과 보존 만료 시각',
    failure_code VARCHAR(80) NULL COMMENT '실패 코드',
    failure_message VARCHAR(500) NULL COMMENT '마스킹된 실패 설명',
    CONSTRAINT pk_MBW_LOGIN_OPERATION PRIMARY KEY (operation_id),
    CONSTRAINT ck_mbw_login_operation_status CHECK (operation_status IN ('PROCESSING','SUCCESS','FAILED','UNKNOWN','EXPIRED')),
    CONSTRAINT fk_mbw_login_operation_user FOREIGN KEY (admin_user_id) REFERENCES MBW_ADMIN_USER (admin_user_id) ON DELETE CASCADE,
    INDEX ix_mbw_login_operation_user_time (admin_user_id, created_at),
    INDEX ix_mbw_login_operation_expiry (operation_status, result_expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 로그인 멱등 처리 이력';

CREATE TABLE IF NOT EXISTS MBW_REFRESH_TOKEN (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 관리자 refresh token 순번',
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    login_domain VARCHAR(30) NOT NULL DEFAULT 'MBW' COMMENT '로그인 도메인',
    refresh_token_hash VARCHAR(300) NOT NULL COMMENT 'refresh token hash',
    transaction_id CHAR(34) NULL COMMENT '발급 전역 transactionId',
    login_operation_id VARCHAR(100) NULL COMMENT '로그인 멱등 Operation ID',
    expire_at DATETIME NOT NULL COMMENT '만료 일시',
    revoked_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '폐기 여부',
    revoked_at DATETIME NULL COMMENT '폐기 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_REFRESH_TOKEN PRIMARY KEY (refresh_token_id),
    CONSTRAINT uk_mbw_refresh_token_hash UNIQUE (refresh_token_hash),
    CONSTRAINT fk_mbw_refresh_token_user FOREIGN KEY (admin_user_id) REFERENCES MBW_ADMIN_USER (admin_user_id) ON DELETE CASCADE,
    INDEX ix_mbw_refresh_token_user (admin_user_id, revoked_yn, expire_at),
    INDEX ix_mbw_refresh_token_login_operation (login_operation_id, revoked_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 업무 관리자 refresh token hash 저장소';

CREATE TABLE IF NOT EXISTS MBW_USER_ROLE (
    user_role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 역할 이력 순번',
    admin_user_id BIGINT NOT NULL COMMENT '업무 관리자 사용자 순번',
    role_code VARCHAR(50) NOT NULL COMMENT '업무 역할 코드',
    valid_from DATETIME(3) NULL COMMENT '역할 적용 시작시각',
    valid_to DATETIME(3) NULL COMMENT '역할 적용 종료시각',
    primary_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '대표 역할 여부',
    grant_reason VARCHAR(500) NOT NULL DEFAULT 'INITIAL' COMMENT '부여/변경 사유',
    operation_id VARCHAR(100) NULL COMMENT '멱등 작업 식별자',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_USER_ROLE PRIMARY KEY (user_role_id),
    CONSTRAINT uk_mbw_user_role_operation UNIQUE (operation_id),
    CONSTRAINT ck_mbw_user_role_primary CHECK (primary_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_user_role_effective CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to > valid_from),
    CONSTRAINT fk_mbw_user_role_user FOREIGN KEY (admin_user_id) REFERENCES MBW_ADMIN_USER (admin_user_id) ON DELETE CASCADE,
    CONSTRAINT fk_mbw_user_role_role FOREIGN KEY (role_code) REFERENCES MBW_ROLE (role_code),
    INDEX ix_mbw_user_role_user (admin_user_id, valid_to, primary_yn, user_role_id),
    INDEX ix_mbw_user_role_role (role_code, valid_to, admin_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 사용자 다중 역할 이력';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_DELEGATION (
    delegation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 위임 순번',
    delegator_employee_no VARCHAR(50) NOT NULL COMMENT '위임자 직원 번호',
    delegate_employee_no VARCHAR(50) NOT NULL COMMENT '대결/대리 직원 번호',
    business_domain VARCHAR(30) NULL COMMENT '제한 업무 영역; NULL이면 공통',
    approval_type VARCHAR(50) NULL COMMENT '제한 결재 유형; NULL이면 공통',
    valid_from DATETIME(3) NOT NULL COMMENT '위임 시작시각',
    valid_to DATETIME(3) NOT NULL COMMENT '위임 종료시각',
    reason VARCHAR(500) NOT NULL COMMENT '위임 사유',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_DELEGATION PRIMARY KEY (delegation_id),
    CONSTRAINT ck_mbw_approval_delegation_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_approval_delegation_period CHECK (valid_to > valid_from),
    CONSTRAINT ck_mbw_approval_delegation_self CHECK (delegator_employee_no <> delegate_employee_no),
    CONSTRAINT fk_mbw_approval_delegation_from FOREIGN KEY (delegator_employee_no) REFERENCES MBW_EMPLOYEE (employee_no),
    CONSTRAINT fk_mbw_approval_delegation_to FOREIGN KEY (delegate_employee_no) REFERENCES MBW_EMPLOYEE (employee_no),
    INDEX ix_mbw_approval_delegation_active (delegator_employee_no, use_yn, valid_from, valid_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 결재 위임/대결 유효기간';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_EXECUTION (
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    command_request_id VARCHAR(120) NOT NULL COMMENT 'Owner 실행 멱등 요청 ID',
    owner_action VARCHAR(80) NOT NULL COMMENT '실제 업무 Owner Action',
    execution_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCEEDED/FAILED/UNKNOWN/RECONCILING/RECOVERED',
    owner_result_code VARCHAR(80) NULL COMMENT 'Owner 실행 결과 코드',
    owner_result_message VARCHAR(1000) NULL COMMENT '마스킹된 Owner 실행 결과 메시지',
    started_at DATETIME(3) NULL COMMENT '실제 실행 시작 시각',
    completed_at DATETIME(3) NULL COMMENT '실제 실행 종료 시각',
    recovery_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '결과불명/복구 필요 여부',
    fence_token BIGINT NOT NULL DEFAULT 0 COMMENT '실행/Reconcile fencing token',
    approved_by VARCHAR(100) NOT NULL COMMENT '최종 승인 처리 운영자',
    transaction_id CHAR(34) NULL COMMENT '승인/실행 상관관계 transactionId',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_EXECUTION PRIMARY KEY (approval_id),
    CONSTRAINT uk_mbw_approval_execution_command UNIQUE (command_request_id),
    CONSTRAINT ck_mbw_approval_execution_status CHECK (execution_status IN ('PENDING','RUNNING','SUCCEEDED','FAILED','UNKNOWN','RECONCILING','RECOVERED')),
    CONSTRAINT ck_mbw_approval_execution_recovery CHECK (recovery_required_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_approval_execution_fence CHECK (fence_token >= 0),
    CONSTRAINT ck_mbw_approval_execution_time CHECK (completed_at IS NULL OR started_at IS NULL OR completed_at >= started_at),
    CONSTRAINT fk_mbw_approval_execution_document FOREIGN KEY (approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id),
    INDEX ix_mbw_approval_execution_status (execution_status, recovery_required_yn, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 결재 승인 후 실제 업무 Owner 실행 상태';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_HISTORY (
    approval_history_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재 이력 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    action_type VARCHAR(30) NOT NULL COMMENT '결재 행위 유형',
    actor_employee_no VARCHAR(50) NOT NULL COMMENT '처리 직원 번호',
    idempotency_key VARCHAR(120) NOT NULL COMMENT '중복 행위 방지 키',
    reason VARCHAR(500) NOT NULL COMMENT '결재 행위 사유',
    before_status VARCHAR(30) NULL COMMENT '변경 전 상태',
    after_status VARCHAR(30) NOT NULL COMMENT '변경 후 상태',
    comment_text VARCHAR(1000) NULL COMMENT '결재 의견',
    transaction_id CHAR(34) NULL COMMENT 'CPF transactionId',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_HISTORY PRIMARY KEY (approval_history_id),
    CONSTRAINT uk_mbw_approval_history_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_mbw_approval_history_document FOREIGN KEY (approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id),
    INDEX ix_mbw_approval_history_document (approval_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 결재 상태 변경 이력';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_LINE (
    approval_line_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '결재선 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    step_no INT NOT NULL COMMENT '결재 단계',
    approver_employee_no VARCHAR(50) NULL COMMENT '직접 직원 대상 호환 필드; 정책 기반 결재는 participant Snapshot 사용',
    step_type VARCHAR(30) NOT NULL DEFAULT 'APPROVAL' COMMENT 'APPROVAL/AGREEMENT/REVIEW',
    target_type VARCHAR(30) NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION',
    target_code VARCHAR(100) NOT NULL COMMENT '정책 Target 코드 Snapshot; EMPLOYEE이면 직원번호',
    target_name_snapshot VARCHAR(150) NULL COMMENT '정책 Target 표시명 Snapshot',
    decision_rule VARCHAR(30) NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY/N_OF_M',
    required_count INT NULL COMMENT 'N_OF_M 최소 결정 수',
    required_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '필수 Target 여부',
    decision_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT '결재자 결정 상태',
    decision_comment VARCHAR(1000) NULL COMMENT '결재 의견',
    decided_at DATETIME NULL COMMENT '결정 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_LINE PRIMARY KEY (approval_line_id),
    CONSTRAINT uk_mbw_approval_line UNIQUE (approval_id, step_no, target_type, target_code),
    CONSTRAINT ck_mbw_approval_line_step CHECK (step_no >= 1),
    CONSTRAINT ck_mbw_approval_line_step_type CHECK (step_type IN ('APPROVAL','AGREEMENT','REVIEW')),
    CONSTRAINT ck_mbw_approval_line_target CHECK (target_type IN ('EMPLOYEE','ROLE','ORGANIZATION','ORG_MANAGER','POSITION')),
    CONSTRAINT ck_mbw_approval_line_rule CHECK (decision_rule IN ('ALL','ANY','N_OF_M')),
    CONSTRAINT ck_mbw_approval_line_required CHECK (required_yn IN ('Y','N') AND ( (decision_rule = 'N_OF_M' AND required_count IS NOT NULL AND required_count > 0) OR (decision_rule <> 'N_OF_M' AND required_count IS NULL) )),
    CONSTRAINT ck_mbw_approval_line_status CHECK (decision_status IN ('WAITING','APPROVED','AGREED','REJECTED','SKIPPED')),
    CONSTRAINT fk_mbw_approval_line_document FOREIGN KEY (approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id) ON DELETE CASCADE,
    INDEX ix_mbw_approval_line_approver (approver_employee_no, decision_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 결재선';

CREATE TABLE IF NOT EXISTS MBW_EMPLOYEE_ASSIGNMENT (
    assignment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '직원 소속/직무 발령 순번',
    employee_no VARCHAR(50) NOT NULL COMMENT '직원 번호',
    organization_code VARCHAR(50) NOT NULL COMMENT '소속 조직 코드',
    position_code VARCHAR(50) NULL COMMENT '직급 코드',
    job_title_code VARCHAR(50) NULL COMMENT '직책 코드',
    assignment_type VARCHAR(30) NOT NULL DEFAULT 'PRIMARY' COMMENT 'PRIMARY/CONCURRENT/SECONDMENT/ACTING',
    primary_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '대표 소속 여부',
    effective_from DATETIME(3) NOT NULL COMMENT '발령 적용 시작시각',
    effective_to DATETIME(3) NULL COMMENT '발령 적용 종료시각',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_EMPLOYEE_ASSIGNMENT PRIMARY KEY (assignment_id),
    CONSTRAINT ck_mbw_employee_assignment_type CHECK (assignment_type IN ('PRIMARY','CONCURRENT','SECONDMENT','ACTING')),
    CONSTRAINT ck_mbw_employee_assignment_primary CHECK (primary_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_employee_assignment_effective CHECK (effective_to IS NULL OR effective_to > effective_from),
    CONSTRAINT fk_mbw_employee_assignment_employee FOREIGN KEY (employee_no) REFERENCES MBW_EMPLOYEE (employee_no) ON DELETE CASCADE,
    CONSTRAINT fk_mbw_employee_assignment_org FOREIGN KEY (organization_code) REFERENCES MBW_ORGANIZATION (organization_code),
    CONSTRAINT fk_mbw_employee_assignment_position FOREIGN KEY (position_code) REFERENCES MBW_POSITION (position_code) ON DELETE SET NULL,
    CONSTRAINT fk_mbw_employee_assignment_job_title FOREIGN KEY (job_title_code) REFERENCES MBW_JOB_TITLE (job_title_code) ON DELETE SET NULL,
    INDEX ix_mbw_employee_assignment_current (employee_no, effective_to, primary_yn),
    INDEX ix_mbw_employee_assignment_org (organization_code, effective_to, job_title_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 직원 유효기간 기반 조직/직급/직책 Assignment';

CREATE TABLE IF NOT EXISTS MBW_ORGANIZATION_RESPONSIBILITY (
    responsibility_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '조직 책임/대행 순번',
    organization_code VARCHAR(50) NOT NULL COMMENT '대상 조직 코드',
    responsibility_type VARCHAR(30) NOT NULL DEFAULT 'MANAGER' COMMENT 'MANAGER/DEPUTY/ACTING/APPROVAL_OWNER',
    employee_no VARCHAR(50) NOT NULL COMMENT '책임 직원 번호',
    effective_from DATETIME(3) NOT NULL COMMENT '책임 시작시각',
    effective_to DATETIME(3) NULL COMMENT '책임 종료시각',
    priority_no INT NOT NULL DEFAULT 1 COMMENT '동일 책임 우선순위',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_ORGANIZATION_RESPONSIBILITY PRIMARY KEY (responsibility_id),
    CONSTRAINT ck_mbw_org_responsibility_type CHECK (responsibility_type IN ('MANAGER','DEPUTY','ACTING','APPROVAL_OWNER')),
    CONSTRAINT ck_mbw_org_responsibility_use CHECK (use_yn IN ('Y','N')),
    CONSTRAINT ck_mbw_org_responsibility_priority CHECK (priority_no >= 1),
    CONSTRAINT ck_mbw_org_responsibility_effective CHECK (effective_to IS NULL OR effective_to > effective_from),
    CONSTRAINT fk_mbw_org_responsibility_org FOREIGN KEY (organization_code) REFERENCES MBW_ORGANIZATION (organization_code),
    CONSTRAINT fk_mbw_org_responsibility_employee FOREIGN KEY (employee_no) REFERENCES MBW_EMPLOYEE (employee_no),
    INDEX ix_mbw_org_responsibility_active (organization_code, responsibility_type, use_yn, effective_to, priority_no),
    INDEX ix_mbw_org_responsibility_employee (employee_no, use_yn, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 조직 책임자/대행/결재 책임자 유효기간 모델';

CREATE TABLE IF NOT EXISTS MBW_APPROVAL_PARTICIPANT (
    approval_participant_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '실제 결재 참여자 순번',
    approval_id BIGINT NOT NULL COMMENT '결재 문서 순번',
    approval_line_id BIGINT NOT NULL COMMENT '정책 Target/결재선 순번',
    step_no INT NOT NULL COMMENT '결재 단계',
    approver_employee_no VARCHAR(50) NOT NULL COMMENT '상신 시 해석된 실제 결재자',
    approver_name_snapshot VARCHAR(100) NULL COMMENT '상신 시 결재자 표시명 Snapshot',
    organization_code_snapshot VARCHAR(50) NULL COMMENT '결재자 조직 Snapshot',
    position_code_snapshot VARCHAR(50) NULL COMMENT '결재자 직급 Snapshot',
    job_title_code_snapshot VARCHAR(50) NULL COMMENT '결재자 직책 Snapshot',
    delegated_from_employee_no VARCHAR(50) NULL COMMENT '위임 원 결재자',
    resolution_source VARCHAR(30) NOT NULL DEFAULT 'DIRECT' COMMENT 'DIRECT/ROLE/ORG/ORG_MANAGER/POSITION/DELEGATION/ACTING',
    decision_status VARCHAR(30) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/APPROVED/AGREED/REJECTED/SKIPPED',
    idempotency_key VARCHAR(120) NULL COMMENT '결정 멱등 키',
    decision_comment VARCHAR(1000) NULL COMMENT '결재 의견',
    decided_at DATETIME(3) NULL COMMENT '결정 시각',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    CONSTRAINT pk_MBW_APPROVAL_PARTICIPANT PRIMARY KEY (approval_participant_id),
    CONSTRAINT uk_mbw_approval_participant UNIQUE (approval_line_id, approver_employee_no),
    CONSTRAINT uk_mbw_approval_participant_idem UNIQUE (idempotency_key),
    CONSTRAINT ck_mbw_approval_participant_step CHECK (step_no >= 1),
    CONSTRAINT ck_mbw_approval_participant_source CHECK (resolution_source IN ('DIRECT','ROLE','ORG','ORG_MANAGER','POSITION','DELEGATION','ACTING')),
    CONSTRAINT ck_mbw_approval_participant_status CHECK (decision_status IN ('WAITING','APPROVED','AGREED','REJECTED','SKIPPED')),
    CONSTRAINT fk_mbw_approval_participant_document FOREIGN KEY (approval_id) REFERENCES MBW_APPROVAL_DOCUMENT (approval_id) ON DELETE CASCADE,
    CONSTRAINT fk_mbw_approval_participant_line FOREIGN KEY (approval_line_id) REFERENCES MBW_APPROVAL_LINE (approval_line_id) ON DELETE CASCADE,
    INDEX ix_mbw_approval_participant_inbox (approver_employee_no, decision_status, approval_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backoffice 결재 참여자 Snapshot';
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/50_framework_seed_data.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=50_framework_seed_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
INSERT INTO OPS_SYSTEM_REGISTRY (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by)
VALUES ('CPF', 'CPF Core Platform', 'CPF', 'Y', 'CPF core platform system', 1, 'SYSTEM', 'SYSTEM'),
    ('CMN', 'CPF Common', 'CMN', 'Y', 'CPF mandatory common system', 1, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'CPF Administration', 'ADM', 'Y', 'CPF administration system', 1, 'SYSTEM', 'SYSTEM'),
    ('MBW', 'CPF Backoffice', 'MBW', 'Y', 'CPF business backoffice system', 1, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'CPF Batch', 'BAT', 'Y', 'CPF batch runtime system', 1, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'CPF Education', 'EDU', 'Y', 'CPF education reference system', 1, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE system_name=VALUES(system_name), domain_code=VALUES(domain_code), enabled_yn=VALUES(enabled_yn), description=VALUES(description), policy_version=VALUES(policy_version), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_CHANNEL_REGISTRY (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by)
VALUES ('WEB', '웹', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '웹 브라우저 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('MOBILE', '모바일', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '모바일 애플리케이션 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('ADM', '관리자', 'OPERATOR', 'INTERNAL', 'Y', 'Y', 'Y', 'N', 'Y', 'ADM 운영 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('BATCH', '배치', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '배치 실행 채널', 0, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE channel_name=VALUES(channel_name), channel_type=VALUES(channel_type), trust_level=VALUES(trust_level), client_channel_yn=VALUES(client_channel_yn), internal_channel_yn=VALUES(internal_channel_yn), authentication_required_yn=VALUES(authentication_required_yn), signature_required_yn=VALUES(signature_required_yn), active_yn=VALUES(active_yn), description=VALUES(description), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_CHANNEL_EXECUTION_POLICY (policy_key, operation_id, caller_channel, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by)
VALUES (
    'CPF.DEFAULT', '*', '*', 'Y', 'N', 'N', 0,
    NULL, NULL, 'Y', 0, 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE operation_id=VALUES(operation_id), caller_channel=VALUES(caller_channel), allowed_yn=VALUES(allowed_yn), authentication_required_yn=VALUES(authentication_required_yn), signature_required_yn=VALUES(signature_required_yn), max_tps=VALUES(max_tps), active_yn=VALUES(active_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (NULL, 'CODE_GROUP', 'MODULE', '서비스 모듈 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'REQUEST_TYPE', '요청 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CHANNEL_CODE', '채널 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RESULT_TYPE', '응답 결과 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', '메시지 포맷 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'LOG_LEVEL', '동적 로그 레벨 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CACHE_NAME', '캐시 이름 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'BATCH_JOB_TYPE', '배치 Job 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'YN', '여부 코드 그룹', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE description=VALUES(description), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CPF', '프레임워크 공통 엔진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', '업무 공통 라이브러리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', '관리자 운영 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBW', '업무 백오피스 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'BAT', '선택 배치 실행 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'EDU', '교육 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', '일반 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', '보상 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', '재시도 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', '웹 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', '모바일 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', '배치 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', '관리자 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', '성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', '오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', '고정 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', '인덱스 파라미터 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'TRACE 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'DEBUG 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'INFO 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'WARN 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'ERROR 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', '전체 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', '코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', '메시지 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', '응답코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', '설정 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'TASKLET', 'Tasklet 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'CHUNK', 'Chunk 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'RETRY', '재처리 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', '예', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', '아니오', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id), description=VALUES(description), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_MESSAGE (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES ('MCPF000000', 'ko', 'FIXED', '정상 처리되었습니다.', 'CPF 공통 요청이 정상 처리되었습니다.', 0, NULL, 'CPF 공통 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', '요청 파라미터 검증에 실패했습니다. field={0}, value={1}', 2, '["field","invalid"]', 'CPF 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', '조회 대상 데이터가 존재하지 않습니다. target={0}', 1, '["sample-item"]', 'CPF 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010003', 'ko', 'INDEXED', '이미 등록된 정보입니다.', '중복 데이터가 감지되었습니다. key={0}', 1, '["sampleKey"]', 'CPF 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010004', 'ko', 'INDEXED', '입력값을 확인해 주세요.', 'Bean Validation 검증에 실패했습니다. field={0}', 1, '["name"]', 'CPF 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010005', 'ko', 'FIXED', '인증이 필요합니다.', '인증되지 않은 요청입니다.', 0, NULL, 'CPF 인증 필요 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010006', 'ko', 'INDEXED', '처리 권한이 없습니다.', '인가되지 않은 요청입니다. user={0}', 1, '["guest"]', 'CPF 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF020001', 'ko', 'INDEXED', '요청을 처리할 수 없습니다.', '업무 규칙 위반이 발생했습니다. rule={0}', 1, '["business-rule"]', 'CPF 업무 규칙 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF030001', 'ko', 'INDEXED', '일시적으로 처리할 수 없습니다.', '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}', 1, '["generated-service"]', 'CPF 외부 연계 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900001', 'ko', 'INDEXED', '필수 거래 헤더가 누락되었습니다.', 'CPF 거래 헤더 검증에 실패했습니다. header={0}, uri={1}', 2, '["X-Request-Type","/api/sample-items"]', 'CPF 헤더 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900002', 'ko', 'INDEXED', '거래 메타데이터 설정이 올바르지 않습니다.', 'CPF @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}', 1, '["OCPFSM0001"]', 'CPF 메타데이터 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900003', 'ko', 'INDEXED', '서비스 접속 정보가 없습니다.', 'CPF 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}', 1, '["generated-service"]', 'CPF endpoint 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900004', 'ko', 'INDEXED', '동적 로그레벨 요청이 올바르지 않습니다.', 'CPF 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'CPF 동적 로그 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900005', 'ko', 'INDEXED', '내부 공유 API에 접근할 수 없습니다.', 'CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}', 1, '["service identity verification failed"]', 'CPF 내부 공유 API 접근 거부 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF990000', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', 'CPF 내부 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'CPF 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF990001', 'ko', 'INDEXED', '데이터베이스 오류가 발생했습니다.', '데이터베이스 처리 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'CPF 데이터베이스 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBW000000', 'ko', 'FIXED', '성공', 'MBW 요청이 정상 처리되었습니다.', 0, NULL, 'MBW 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBW010001', 'ko', 'INDEXED', '업무 요청 값이 올바르지 않습니다.', 'MBW 입력값 검증에 실패했습니다. field={0}', 1, '["field"]', 'MBW 입력값 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBW010002', 'ko', 'FIXED', '처리 권한이 없습니다.', 'MBW 서버 권한 검사에 실패했습니다.', 0, NULL, 'MBW 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MEDU010001', 'ko', 'INDEXED', '이미 등록된 {0}입니다.', '{0}={1} 값이 이미 존재합니다. duplicateCheck=EDU_SAMPLE', 2, '["샘플키","SAMPLE-0001"]', 'EDU 동적 중복 교육 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'ko', 'FIXED', 'CPF 교육 시스템에 오신 것을 환영합니다.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'en', 'FIXED', 'Welcome to the CPF education system.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE message_format_type=VALUES(message_format_type), external_message=VALUES(external_message), internal_message=VALUES(internal_message), parameter_count=VALUES(parameter_count), parameter_sample=VALUES(parameter_sample), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_RESPONSE_CODE (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES ('SCPF000000', 'MCPF000000', 'S', 'CPF', '00', '0000', 200, 'CPF 공통 성공', 'SYSTEM', 'SYSTEM'),
    ('ECPF010001', 'MCPF010001', 'E', 'CPF', '01', '0001', 400, '파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF010002', 'MCPF010002', 'E', 'CPF', '01', '0002', 404, '미존재 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF010003', 'MCPF010003', 'E', 'CPF', '01', '0003', 409, '중복 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF010004', 'MCPF010004', 'E', 'CPF', '01', '0004', 400, '검증 실패', 'SYSTEM', 'SYSTEM'),
    ('ECPF010005', 'MCPF010005', 'E', 'CPF', '01', '0005', 401, '인증 필요', 'SYSTEM', 'SYSTEM'),
    ('ECPF010006', 'MCPF010006', 'E', 'CPF', '01', '0006', 403, '권한 없음', 'SYSTEM', 'SYSTEM'),
    ('ECPF020001', 'MCPF020001', 'E', 'CPF', '02', '0001', 400, '업무 규칙 위반', 'SYSTEM', 'SYSTEM'),
    ('ECPF030001', 'MCPF030001', 'E', 'CPF', '03', '0001', 502, '외부 연계 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF900001', 'MCPF900001', 'E', 'CPF', '90', '0001', 400, '필수 거래 헤더 누락', 'SYSTEM', 'SYSTEM'),
    ('ECPF900002', 'MCPF900002', 'E', 'CPF', '90', '0002', 500, '거래 메타데이터 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF900003', 'MCPF900003', 'E', 'CPF', '90', '0003', 500, '서비스 endpoint 미등록', 'SYSTEM', 'SYSTEM'),
    ('ECPF900004', 'MCPF900004', 'E', 'CPF', '90', '0004', 400, '동적 로그 규칙 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF900005', 'MCPF900005', 'E', 'CPF', '90', '0005', 403, '내부 공유 API 접근 거부', 'SYSTEM', 'SYSTEM'),
    ('ECPF990000', 'MCPF990000', 'E', 'CPF', '99', '0000', 500, '내부 서버 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF990001', 'MCPF990001', 'E', 'CPF', '99', '0001', 500, '데이터베이스 오류', 'SYSTEM', 'SYSTEM'),
    ('SMBW000000', 'MMBW000000', 'S', 'MBW', '00', '0000', 200, 'MBW 성공', 'SYSTEM', 'SYSTEM'),
    ('EMBW010001', 'MMBW010001', 'E', 'MBW', '01', '0001', 400, 'MBW 입력값 오류', 'SYSTEM', 'SYSTEM'),
    ('EMBW010002', 'MMBW010002', 'E', 'MBW', '01', '0002', 403, 'MBW 권한 오류', 'SYSTEM', 'SYSTEM'),
    ('EEDU010001', 'MEDU010001', 'E', 'EDU', '01', '0001', 409, 'EDU 샘플 중복 오류', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE message_code=VALUES(message_code), result_type=VALUES(result_type), module_id=VALUES(module_id), response_group=VALUES(response_group), sequence_no=VALUES(sequence_no), http_status=VALUES(http_status), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_PARAMETER (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'CMN 캐시 기동 시 선적재 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP', 'N', 'BOOLEAN', '캐시 선적재 실패 시 기동 실패 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.REFRESH_POLL_MILLIS', '5000', 'NUMBER', '캐시 갱신 이벤트 polling 주기', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.BROKER', 'IN_MEMORY', 'STRING', '기본 CMN 메시지 브로커 유형', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.CONNECT_TIMEOUT_MS', '3000', 'NUMBER', 'CPF HTTP client 연결 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.READ_TIMEOUT_MS', '5000', 'NUMBER', 'CPF HTTP client 읽기 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.SESSION_TTL_SECONDS', '3600', 'NUMBER', 'ADM 세션 TTL 초', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_EXPIRE_DAYS', '90', 'NUMBER', 'ADM 비밀번호 만료 일수', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MIN_LENGTH', '10', 'NUMBER', 'ADM 비밀번호 최소 길이', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MAX_FAIL_COUNT', '5', 'NUMBER', 'ADM 로그인 실패 잠금 기준', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.BATCH.DEFAULT_LOCK_SECONDS', '3600', 'NUMBER', '배치 기본 lock 만료 초', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.FEATURE.SAMPLE_ENABLED', 'Y', 'BOOLEAN', '샘플 API와 교육 flow 활성화 여부', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), config_type=VALUES(config_type), description=VALUES(description), encrypted_yn=VALUES(encrypted_yn), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (NULL, 'CODE_GROUP', 'HTTP_METHOD', 'HTTP Method 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'EXECUTION_STATUS', '실행 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'ASYNC_STATUS', '비동기 처리 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RETRY_STATUS', '재시도 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'IDEMPOTENCY_STATUS', '멱등 처리 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'HEALTH_STATUS', 'Health 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CIRCUIT_STATUS', 'Circuit Breaker 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'FILE_SCAN_STATUS', '첨부/파일 검사 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'DATA_CLASSIFICATION', '데이터 민감도 등급 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'APPROVAL_STATUS', '결재 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'ERROR_CATEGORY', '오류 분류 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RETENTION_ACTION', '보존 정책 실행 유형 코드 그룹', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE description=VALUES(description), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'GET', '조회', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'POST', '등록/명령', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'PUT', '전체 수정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'PATCH', '부분 수정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'DELETE', '삭제/회수', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'READY', '실행 준비', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'RUNNING', '실행 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'SUCCESS', '정상 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'FAILED', '실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'UNKNOWN_RESULT', '결과 미확정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'WAITING', '비동기 대기', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'PROCESSING', '비동기 처리 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'COMPLETED', '비동기 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'DLQ', 'Dead Letter Queue', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'RETRYABLE', '재시도 가능', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'NON_RETRYABLE', '재시도 금지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'EXHAUSTED', '재시도 소진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'PROCESSING', '멱등 처리 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'COMPLETED', '멱등 처리 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'FAILED', '멱등 처리 실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'UNKNOWN_RESULT', '멱등 결과 미확정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'UP', '정상', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'DOWN', '장애', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'DEGRADED', '부분 저하', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'CLOSED', '정상 호출', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'OPEN', '호출 차단', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'HALF_OPEN', '복구 시험', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'PENDING', '검사 대기', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'CLEAN', '검사 정상', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'INFECTED', '악성 탐지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'FAILED', '검사 실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'QUARANTINED', '격리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'PUBLIC', '공개 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'INTERNAL', '내부 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'CONFIDENTIAL', '기밀 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'RESTRICTED', '제한/민감 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'DRAFT', '작성 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'IN_REVIEW', '결재 진행', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'APPROVED', '승인 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'REJECTED', '반려', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'WITHDRAWN', '철회', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'CANCELED', '취소', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'EXPIRED', '만료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'VALIDATION', '입력/계약 검증 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'AUTHENTICATION', '인증 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'AUTHORIZATION', '인가 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'CONFLICT', '동시성/중복 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'TIMEOUT', 'Timeout', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'TARGET_DOWN', '호출 대상 장애', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'UNKNOWN_RESULT', '결과 미확정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'ARCHIVE', '보관소 이관', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'PURGE', '정책 삭제', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'LEGAL_HOLD', '법적 보존', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id), description=VALUES(description), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_MESSAGE (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES ('MCPF030002','ko','FIXED','요청 시간이 초과되었습니다.','대상 호출 timeout이 발생했습니다.',0,NULL,'공통 Timeout 메시지','SYSTEM','SYSTEM'),
    ('MCPF030003','ko','FIXED','연결 대상이 일시적으로 사용할 수 없습니다.','대상 서비스가 DOWN/OPEN 상태입니다.',0,NULL,'Target down 메시지','SYSTEM','SYSTEM'),
    ('MCPF030004','ko','FIXED','처리 결과를 확인 중입니다.','요청 결과가 UNKNOWN_RESULT로 분류되어 대사가 필요합니다.',0,NULL,'결과 미확정 메시지','SYSTEM','SYSTEM'),
    ('MCPF020002','ko','FIXED','다른 사용자가 먼저 변경했습니다. 다시 조회해 주세요.','낙관적 잠금 Version 충돌이 발생했습니다.',0,NULL,'동시성 충돌 메시지','SYSTEM','SYSTEM'),
    ('MCPF020003','ko','FIXED','동일 요청이 이미 처리되었습니다.','Idempotency key가 이미 완료된 요청입니다.',0,NULL,'멱등 중복 메시지','SYSTEM','SYSTEM'),
    ('MCPF040001','ko','FIXED','첨부파일 검사가 완료되지 않았습니다.','첨부 다운로드는 CLEAN 상태에서만 허용됩니다.',0,NULL,'첨부 보안 메시지','SYSTEM','SYSTEM'),
    ('MCPF040002','ko','FIXED','첨부파일이 보안 정책에 의해 격리되었습니다.','INFECTED/QUARANTINED 파일 접근이 차단되었습니다.',0,NULL,'첨부 격리 메시지','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE message_format_type=VALUES(message_format_type), external_message=VALUES(external_message), internal_message=VALUES(internal_message), parameter_count=VALUES(parameter_count), parameter_sample=VALUES(parameter_sample), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_RESPONSE_CODE (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES ('ECPF030002','MCPF030002','E','CPF','03','0002',504,'Timeout','SYSTEM','SYSTEM'),
    ('ECPF030003','MCPF030003','E','CPF','03','0003',503,'Target down','SYSTEM','SYSTEM'),
    ('ECPF030004','MCPF030004','E','CPF','03','0004',202,'UNKNOWN_RESULT','SYSTEM','SYSTEM'),
    ('ECPF020002','MCPF020002','E','CPF','02','0002',409,'Optimistic lock conflict','SYSTEM','SYSTEM'),
    ('ECPF020003','MCPF020003','E','CPF','02','0003',409,'Idempotency duplicate','SYSTEM','SYSTEM'),
    ('ECPF040001','MCPF040001','E','CPF','04','0001',423,'File scan pending','SYSTEM','SYSTEM'),
    ('ECPF040002','MCPF040002','E','CPF','04','0002',403,'File quarantined','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE message_code=VALUES(message_code), result_type=VALUES(result_type), module_id=VALUES(module_id), response_group=VALUES(response_group), sequence_no=VALUES(sequence_no), http_status=VALUES(http_status), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_PARAMETER (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES ('CPF.MBW.SECURITY.MAX_LOGIN_FAIL_COUNT','5','NUMBER','MBW 로그인 실패 잠금 기준','N','SYSTEM','SYSTEM'),
    ('CPF.MBW.SECURITY.ACCESS_TOKEN_TTL_SECONDS','600','NUMBER','MBW Access Token TTL','N','SYSTEM','SYSTEM'),
    ('CPF.MBW.SECURITY.REFRESH_TOKEN_TTL_SECONDS','7200','NUMBER','MBW Refresh Token TTL','N','SYSTEM','SYSTEM'),
    ('CPF.RETENTION.EXECUTE_ENABLED','N','BOOLEAN','실제 Archive/Purge 실행 Kill Switch 기본 OFF','N','SYSTEM','SYSTEM'),
    ('CPF.FILE.DOWNLOAD_REQUIRE_CLEAN','Y','BOOLEAN','첨부 다운로드 CLEAN 상태 강제','N','SYSTEM','SYSTEM'),
    ('CPF.HEALTH.INSTANCE_ID_REQUIRED','Y','BOOLEAN','운영 Health 응답 인스턴스 식별자 필수','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), config_type=VALUES(config_type), description=VALUES(description), encrypted_yn=VALUES(encrypted_yn), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_LOG_POLICY (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by)
VALUES ('ONLINE_DEFAULT', '온라인 거래 기본 로그 정책', 'ONLINE_TRANSACTION', '*', 'INFO', 'Y', 'Y', 2, 'NONE', 'ALLOWLIST', 'ALLOWLIST', 'NONE', 'NONE', 'SUMMARY', 'content-type,x-cpf-trace-id,x-cpf-transaction-id', 4096, 8192, 65536, 65536, 32768, 'N', 'N', 'Y', 'DEFAULT', '04aec0a6adbf48c269e1538ca571819dc54400391e33d5b497ec05406bccd445', 90, 100.00, 100, 'Y', '온라인 Controller/API 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEFAULT', '배치 기본 로그 정책', 'BATCH_JOB', '*', 'INFO', 'Y', 'Y', 2, 'NONE', 'ALLOWLIST', 'ALLOWLIST', 'NONE', 'NONE', 'SUMMARY', 'content-type,x-cpf-trace-id,x-cpf-transaction-id', 4096, 8192, 65536, 65536, 32768, 'N', 'N', 'Y', 'DEFAULT', '0eca9ff2359e55290f01c2594d399c32e4af9decd34541a6f571a4345f36ca08', 180, 100.00, 100, 'Y', 'Spring Batch Job 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATION_DEFAULT', 'ADM 운영 기본 로그 정책', 'MODULE', 'ADM', 'INFO', 'Y', 'Y', 2, 'NONE', 'ALLOWLIST', 'ALLOWLIST', 'NONE', 'NONE', 'SUMMARY', 'content-type,x-cpf-trace-id,x-cpf-transaction-id', 4096, 8192, 65536, 65536, 32768, 'N', 'N', 'Y', 'DEFAULT', '9ea15a6d3c662bcaf9295a2512cef8fc12da0e77eea6f07b3c5e55e5fb79e705', 365, 100.00, 50, 'Y', 'ADM 운영 API 기본 로그 정책', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE policy_name=VALUES(policy_name), target_type=VALUES(target_type), target_id=VALUES(target_id), log_level=VALUES(log_level), db_log_enabled_yn=VALUES(db_log_enabled_yn), file_log_enabled_yn=VALUES(file_log_enabled_yn), policy_schema_version=VALUES(policy_schema_version), query_capture_mode=VALUES(query_capture_mode), request_header_capture_mode=VALUES(request_header_capture_mode), response_header_capture_mode=VALUES(response_header_capture_mode), request_body_capture_mode=VALUES(request_body_capture_mode), response_body_capture_mode=VALUES(response_body_capture_mode), error_stack_capture_mode=VALUES(error_stack_capture_mode), header_allowlist=VALUES(header_allowlist), max_query_bytes=VALUES(max_query_bytes), max_header_bytes=VALUES(max_header_bytes), max_request_body_bytes=VALUES(max_request_body_bytes), max_response_body_bytes=VALUES(max_response_body_bytes), max_stack_bytes=VALUES(max_stack_bytes), request_body_log_yn=VALUES(request_body_log_yn), response_body_log_yn=VALUES(response_body_log_yn), error_stack_log_yn=VALUES(error_stack_log_yn), masking_policy_key=VALUES(masking_policy_key), policy_checksum=VALUES(policy_checksum), retention_days=VALUES(retention_days), sampling_rate=VALUES(sampling_rate), priority=VALUES(priority), active_yn=VALUES(active_yn), description=VALUES(description), updated_by=VALUES(updated_by);
INSERT INTO SEC_JWT_KEY (KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by)
VALUES (
    'local-cpf-hs256-001',
    'CPF',
    'HS256',
    'ENV:CPF_CMN_SECURITY_JWT_SECRET',
    'Y',
    NULL,
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE ISSUER=VALUES(ISSUER), ALGORITHM=VALUES(ALGORITHM), SECRET_REF=VALUES(SECRET_REF), ACTIVE_YN=VALUES(ACTIVE_YN), EXPIRE_AT=VALUES(EXPIRE_AT), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CACHE_REFRESH_EVENT (cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM CMN_CACHE_REFRESH_EVENT
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);
INSERT INTO CPF_NOTIFICATION_RULE (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by)
VALUES ('BATCH_EXECUTION', 'FAILED', 'ADM', 'BATCH_FAILED_DEFAULT', 'ERROR', 'ADM_BATCH_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_EVENT', 'LOGIN_FAILURE', 'ADM', 'SECURITY_LOGIN_FAILURE', 'WARN', 'ADM_SECURITY_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE template_code=VALUES(template_code), severity=VALUES(severity), receiver_group=VALUES(receiver_group), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x), 'SORT_DIRECTION', 'ASC', '오름차순', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x), 'SORT_DIRECTION', 'DESC', '내림차순', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id), description=VALUES(description), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_MESSAGE (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES ('MCPF020004','ko','FIXED','요청 사용자 정보가 인증 사용자와 일치하지 않습니다.','Body requester spoofing이 차단되었습니다.',0,NULL,'Requester spoof 차단','SYSTEM','SYSTEM'),
    ('MCPF020005','ko','FIXED','이미 사용된 정책 버전은 직접 수정할 수 없습니다.','사용된 Approval Policy version은 immutable입니다.',0,NULL,'정책 버전 불변성','SYSTEM','SYSTEM'),
    ('MCPF020006','ko','FIXED','동일 작업 식별자가 다른 요청에 사용되었습니다.','operationId payload 충돌입니다.',0,NULL,'멱등 작업 충돌','SYSTEM','SYSTEM'),
    ('MCPF020007','ko','FIXED','현재 데이터가 다른 요청에서 변경되었습니다.','expectedVersion CAS가 실패했습니다.',0,NULL,'낙관적 잠금 재조회','SYSTEM','SYSTEM'),
    ('MCPF040003','ko','FIXED','보존 정책에 의해 해당 데이터는 삭제할 수 없습니다.','LEGAL_HOLD가 적용되어 destructive retention을 차단했습니다.',0,NULL,'Legal hold','SYSTEM','SYSTEM'),
    ('MCPF040004','ko','FIXED','보존 작업 실행이 비활성화되어 있습니다.','CPF.RETENTION.EXECUTE_ENABLED kill switch가 OFF입니다.',0,NULL,'Retention kill switch','SYSTEM','SYSTEM'),
    ('MCPF050001','ko','FIXED','Secret 원문은 조회할 수 없습니다.','Secret API는 metadata/reference만 노출합니다.',0,NULL,'Secret 비노출','SYSTEM','SYSTEM'),
    ('MCPF050002','ko','FIXED','테넌트 식별정보가 필요합니다.','Tenant mode에서 resolver가 tenantId를 결정하지 못했습니다.',0,NULL,'Tenant 필수','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE message_format_type=VALUES(message_format_type), external_message=VALUES(external_message), internal_message=VALUES(internal_message), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_RESPONSE_CODE (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES ('ECPF020004','MCPF020004','E','CPF','02','0004',403,'Requester spoof blocked','SYSTEM','SYSTEM'),
    ('ECPF020005','MCPF020005','E','CPF','02','0005',409,'Policy version immutable','SYSTEM','SYSTEM'),
    ('ECPF020006','MCPF020006','E','CPF','02','0006',409,'Operation id conflict','SYSTEM','SYSTEM'),
    ('ECPF020007','MCPF020007','E','CPF','02','0007',409,'Optimistic lock retry','SYSTEM','SYSTEM'),
    ('ECPF040003','MCPF040003','E','CPF','04','0003',423,'Legal hold','SYSTEM','SYSTEM'),
    ('ECPF040004','MCPF040004','E','CPF','04','0004',403,'Retention disabled','SYSTEM','SYSTEM'),
    ('ECPF050001','MCPF050001','E','CPF','05','0001',403,'Secret value hidden','SYSTEM','SYSTEM'),
    ('ECPF050002','MCPF050002','E','CPF','05','0002',400,'Tenant required','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE message_code=VALUES(message_code), result_type=VALUES(result_type), module_id=VALUES(module_id), response_group=VALUES(response_group), sequence_no=VALUES(sequence_no), http_status=VALUES(http_status), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_PARAMETER (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES ('CPF.PAGING.DEFAULT_SIZE','20','NUMBER','공통 Page 기본 크기','N','SYSTEM','SYSTEM'),
    ('CPF.PAGING.MAX_SIZE','200','NUMBER','공통 Page 최대 크기','N','SYSTEM','SYSTEM'),
    ('CPF.RETENTION.DRY_RUN_DEFAULT','Y','BOOLEAN','Retention 기본 Dry-run','N','SYSTEM','SYSTEM'),
    ('CPF.RETENTION.EXECUTE_ENABLED','N','BOOLEAN','실제 Archive/Purge 실행 Kill Switch 기본 OFF','N','SYSTEM','SYSTEM'),
    ('CPF.SECRET.CACHE_TTL_SECONDS','300','NUMBER','Secret metadata/cache 기본 TTL','N','SYSTEM','SYSTEM'),
    ('CPF.TENANT.ENABLED','N','BOOLEAN','Tenant context 기능 기본 OFF','N','SYSTEM','SYSTEM'),
    ('CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS','N','BOOLEAN','Remote owner 장애가 local readiness를 직접 차단하지 않음','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), config_type=VALUES(config_type), description=VALUES(description), encrypted_yn=VALUES(encrypted_yn), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'O', '온라인 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'S', '공유 내부 서비스 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'B', '배치 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x), 'CHANNEL_CODE', 'APP', '모바일 앱 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x), 'CHANNEL_CODE', 'JUT', 'JUnit/자동 테스트 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x), 'RESULT_TYPE', 'W', '경고/부분 성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x), 'MESSAGE_FORMAT_TYPE', 'PARAMETER', 'Named parameter 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'FAILED', '비동기 처리 실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'SPRING_BATCH', 'Spring Batch Job', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'WORKER', '지속 Worker', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'SCHEDULER', 'Scheduler Job', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'CENTER_CUT', 'Center-Cut 대량 처리', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id), description=VALUES(description), use_yn='Y', updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/52_standard_execution_alias_seed.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=52_standard_execution_alias_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
DELETE FROM CPF_STANDARD_EXECUTION_ALIAS WHERE legacy_execution_id LIKE 'OADM-MBR-%' OR standard_execution_id LIKE 'OADMMB%';
INSERT INTO CPF_STANDARD_EXECUTION_ALIAS (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by)
VALUES ('BADM-RLG-EX-0001', 'BADMRL0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-CUT-CL-0001', 'BBATCU0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-OPS-FL-0001', 'BBATOP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-OPS-HB-0001', 'BBATOP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-OPS-SM-0001', 'BBATOP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BREF-EDU-CH-0001', 'BREFAA0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BREF-EDU-RT-0001', 'BREFAA0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BREF-EDU-TS-0001', 'BREFAA0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0010', 'OADMBA0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0012', 'OADMBA0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0013', 'OADMBA0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0014', 'OADMBA0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0015', 'OADMBA0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0016', 'OADMBA0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0023', 'OADMBA0023', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0024', 'OADMBA0024', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0025', 'OADMBA0025', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0027', 'OADMBA0027', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0028', 'OADMBA0028', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0029', 'OADMBA0029', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0030', 'OADMBA0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0032', 'OADMBA0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0034', 'OADMBA0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0011', 'OADMBA0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0017', 'OADMBA0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0018', 'OADMBA0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0019', 'OADMBA0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0026', 'OADMBA0026', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0020', 'OADMBA0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0021', 'OADMBA0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0022', 'OADMBA0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0031', 'OADMBA0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0033', 'OADMBA0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-01-0010', 'OADMCD0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-01-0011', 'OADMCD0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-02-0012', 'OADMCD0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-03-0013', 'OADMCD0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-04-0014', 'OADMCD0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-01-0010', 'OADMCF0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-01-0011', 'OADMCF0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-02-0012', 'OADMCF0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-03-0013', 'OADMCF0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-04-0014', 'OADMCF0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0010', 'OADMCT0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0020', 'OADMCT0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0030', 'OADMCT0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0040', 'OADMCT0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0050', 'OADMCT0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0060', 'OADMCT0060', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0070', 'OADMCT0070', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-DWN-01-0001', 'OADMDW0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-DWN-01-0002', 'OADMDW0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-DWN-02-0003', 'OADMDW0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-EXE-01-0001', 'OADMEX0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-EXE-01-0002', 'OADMEX0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0010', 'OADMLG0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0011', 'OADMLG0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0018', 'OADMLG0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0020', 'OADMLG0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0021', 'OADMLG0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0012', 'OADMLG0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0013', 'OADMLG0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0014', 'OADMLG0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0016', 'OADMLG0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0018', 'OADMLG0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-04-0015', 'OADMLG0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-04-0017', 'OADMLG0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-04-0019', 'OADMLG0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-01-0010', 'OADMMS0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-01-0011', 'OADMMS0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-02-0012', 'OADMMS0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-03-0013', 'OADMMS0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-04-0014', 'OADMMS0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-01-0010', 'OADMNT0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-01-0011', 'OADMNT0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-01-0014', 'OADMNT0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-02-0012', 'OADMNT0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-02-0016', 'OADMNT0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-03-0013', 'OADMNT0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-03-0015', 'OADMNT0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OBS-01-0010', 'OADMOB0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OBS-01-0011', 'OADMOB0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OBS-01-0012', 'OADMOB0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0001', 'OADMOP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0002', 'OADMOP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0010', 'OADMOP0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0020', 'OADMOP0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0030', 'OADMOP0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0034', 'OADMOP0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0035', 'OADMOP0035', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0036', 'OADMOP0036', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0040', 'OADMOP0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0041', 'OADMOP0041', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0042', 'OADMOP0042', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0043', 'OADMOP0043', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0050', 'OADMOP0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-02-0031', 'OADMOP0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-02-0042', 'OADMOP0044', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0032', 'OADMOP0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0037', 'OADMOP0037', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0038', 'OADMOP0038', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0039', 'OADMOP0039', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0043', 'OADMOP0045', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0044', 'OADMOP0046', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0045', 'OADMOP0047', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-04-0022', 'OADMOP0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-04-0044', 'OADMOP0048', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-05-0011', 'OADMOP0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-05-0021', 'OADMOP0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-06-0033', 'OADMOP0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-06-0040', 'OADMOP0049', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-06-0042', 'OADMOP0051', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0010', 'OADMPE0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0011', 'OADMPE0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0014', 'OADMPE0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0015', 'OADMPE0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0019', 'OADMPE0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0020', 'OADMPE0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0024', 'OADMPE0024', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0025', 'OADMPE0025', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0029', 'OADMPE0029', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0030', 'OADMPE0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0034', 'OADMPE0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0016', 'OADMPE0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0021', 'OADMPE0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0026', 'OADMPE0026', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0031', 'OADMPE0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0012', 'OADMPE0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0013', 'OADMPE0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0017', 'OADMPE0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0018', 'OADMPE0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0022', 'OADMPE0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0023', 'OADMPE0023', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0027', 'OADMPE0027', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0028', 'OADMPE0028', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0032', 'OADMPE0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0033', 'OADMPE0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0035', 'OADMPE0035', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0001', 'OADMRE0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0002', 'OADMRE0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0003', 'OADMRE0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0004', 'OADMRE0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0006', 'OADMRE0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0007', 'OADMRE0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0009', 'OADMRE0009', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0010', 'OADMRE0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0011', 'OADMRE0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0005', 'OADMRE0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0008', 'OADMRE0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0012', 'OADMRE0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0013', 'OADMRE0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-CR-0001', 'OADMRL0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-DL-0001', 'OADMRL0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-DL-0002', 'OADMRL0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-DW-0001', 'OADMRL0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-IS-0001', 'OADMRL0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0001', 'OADMRL0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0002', 'OADMRL0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0003', 'OADMRL0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0004', 'OADMRL0009', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-01-0010', 'OADMSE0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-01-0012', 'OADMSE0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0011', 'OADMSE0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0013', 'OADMSE0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0014', 'OADMSE0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0015', 'OADMSE0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0010', 'OADMSV0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0020', 'OADMSV0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0030', 'OADMSV0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0040', 'OADMSV0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0050', 'OADMSV0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0060', 'OADMSV0060', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0070', 'OADMSV0070', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0001', 'OADMTR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0002', 'OADMTR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0003', 'OADMTR0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0004', 'OADMTR0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0005', 'OADMTR0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0006', 'OADMTR0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-01-0010', 'OADMTR0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-01-0011', 'OADMTR0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-04-0013', 'OADMTR0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-05-0012', 'OADMTR0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBAT-OPR-01-0003', 'OBATOP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBAT-OPR-02-0002', 'OBATOP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ADM-01-1001', 'OMBWAD1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ADM-03-1002', 'OMBWAD1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-01-0001', 'OMBWAP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-01-0003', 'OMBWAP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-02-0002', 'OMBWAP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-05-0004', 'OMBWAP0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ATC-01-0001', 'OMBWAT0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ATC-02-0002', 'OMBWAT0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ATC-DL-0003', 'OMBWAT0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUD-01-0001', 'OMBWUD0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-01-0004', 'OMBWAU0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-01-0005', 'OMBWAU0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-01-0007', 'OMBWAU0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-02-0001', 'OMBWAU0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-02-0002', 'OMBWAU0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-02-0003', 'OMBWAU0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-03-0006', 'OMBWAU0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-04-0008', 'OMBWAU0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-CUS-01-1001', 'OMBWCU1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-DSH-01-0001', 'OMBWDS0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-DWN-01-0002', 'OMBWDW0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-DWN-01-1001', 'OMBWDW1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-EMP-01-0001', 'OMBWEM0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-EMP-03-0002', 'OMBWEM0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-MNU-01-1001', 'OMBWMN1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-MNU-03-1002', 'OMBWMN1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-MSK-02-1001', 'OMBWMS1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-NTF-01-0001', 'OMBWNT0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-NTF-02-0002', 'OMBWNT0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-NTF-03-0003', 'OMBWNT0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ORD-01-1001', 'OMBWOR1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ORG-01-0001', 'OMBWOR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ORG-03-0002', 'OMBWOR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-01-0002', 'OMBWPE0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-01-0003', 'OMBWPE0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-01-1001', 'OMBWPE1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-02-0004', 'OMBWPE0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-03-1002', 'OMBWPE1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PRD-01-1001', 'OMBWPR1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ROL-01-1001', 'OMBWRO1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ROL-03-1002', 'OMBWRO1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SCH-01-0001', 'OMBWSC0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SCH-03-0002', 'OMBWSC0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SCH-04-0003', 'OMBWSC0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SET-01-1001', 'OMBWSE1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-USR-QY-0000', 'OMBWUS0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-USR-QY-0001', 'OMBWUS0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0001', 'OEDUAA0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0002', 'OREFAA0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0003', 'OREFAA0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0099', 'OREFAA0099', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0001', 'OREFAA0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0010', 'OREFAA0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0020', 'OREFAA0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0030', 'OREFAA0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-03-0001', 'OREFAA0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-03-0002', 'OREFAA0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-03-0003', 'OREFAA0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-04-0001', 'OREFAA0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-04-0002', 'OREFAA0009', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-05-0001', 'OREFAA0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-05-0002', 'OREFAA0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-05-9001', 'OREFAA9001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-08-0001', 'OREFAA0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-08-0010', 'OREFAA0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-08-9001', 'OREFAA9002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0001', 'OREFAA0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0002', 'OREFAA0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0003', 'OREFAA0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0004', 'OREFAA0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0005', 'OREFAA0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0006', 'OREFAA0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0007', 'OREFAA0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0008', 'OREFAA0023', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0009', 'OREFAA0024', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0010', 'OREFAA0025', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0011', 'OREFAA0026', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0012', 'OREFAA0027', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0013', 'OREFAA0028', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0015', 'OREFAA0029', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0016', 'OREFAA0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0017', 'OREFAA0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0018', 'OREFAA0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0019', 'OREFAA0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0020', 'OREFAA0035', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0030', 'OREFAA0036', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0031', 'OREFAA0037', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0032', 'OREFAA0038', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0033', 'OREFAA0039', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0034', 'OREFAA0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0035', 'OREFAA0041', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0036', 'OREFAA0042', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0040', 'OREFAA0043', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0051', 'OREFAA0051', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0060', 'OREFAA0060', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0072', 'OREFAA0072', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0073', 'OREFAA0073', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0080', 'OREFAA0080', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-12-0001', 'OREFAA0044', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-12-0002', 'OREFAA0045', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-12-0003', 'OREFAA0046', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0001', 'OREFAA0047', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0002', 'OREFAA0048', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0003', 'OREFAA0049', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0004', 'OREFAA0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0005', 'OREFAA0052', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0006', 'OREFAA0053', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0007', 'OREFAA0054', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0008', 'OREFAA0055', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-14-0001', 'OREFAA0056', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-15-0001', 'OREFAA0057', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0001', 'OREFAA0058', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0002', 'OREFAA0059', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0003', 'OREFAA0061', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0004', 'OREFAA0062', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0005', 'OREFAA0063', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0006', 'OREFAA0064', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-17-0001', 'OREFAA0065', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-17-0002', 'OREFAA0066', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0001', 'OREFQR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0002', 'OREFQR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0003', 'OREFQR0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0004', 'OREFQR0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0005', 'OREFQR0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED')
ON DUPLICATE KEY UPDATE standard_execution_id=VALUES(standard_execution_id), migration_reason=VALUES(migration_reason), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/53_runtime_service_registry_seed.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=53_runtime_service_registry_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES ('MBW', '업무 백오피스 서비스', 'INTERNAL', 'MBW', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU', '온라인 교육 서비스', 'INTERNAL', 'EDU', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT', '배치 Worker 서비스', 'INTERNAL', 'BAT', 'CPF 배치 Worker 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM', '운영 콘솔 서비스', 'INTERNAL', 'ADM', 'CPF 운영 콘솔 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CEC', '센터컷 실행 서비스', 'INTERNAL', 'CEC', 'CPF 센터컷 Runner 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE service_name=VALUES(service_name), service_type=VALUES(service_type), owner_module_code=VALUES(owner_module_code), description=VALUES(description), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES ('MBW_API', 'MBW', 'MBW API Endpoint', 'HTTP', 'http://cpf-backoffice', '/api/v1/backoffice', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU_API', 'EDU', 'EDU API Endpoint', 'HTTP', 'http://cpf-education', '/education', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT_API', 'BAT', 'BAT API Endpoint', 'HTTP', 'http://cpf-batch', '/bat', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_API', 'ADM', 'ADM API Endpoint', 'HTTP', 'http://cpf-admin', '/adm', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CEC_API', 'CEC', 'CEC API Endpoint', 'HTTP', 'http://cpf-batch-center-cut', '/cec', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE service_id=VALUES(service_id), endpoint_name=VALUES(endpoint_name), endpoint_type=VALUES(endpoint_type), base_url=VALUES(base_url), context_path=VALUES(context_path), default_timeout_ms=VALUES(default_timeout_ms), default_retry_count=VALUES(default_retry_count), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES ('MBW', 'MBW_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'EDU_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('CEC', 'CEC_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE routing_mode=VALUES(routing_mode), load_balance_type=VALUES(load_balance_type), failover_enabled_yn=VALUES(failover_enabled_yn), health_check_required_yn=VALUES(health_check_required_yn), active_yn=VALUES(active_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/56_backoffice_product_seed.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=56_backoffice_product_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=mbwDB
USE mbwDB;
INSERT INTO MBW_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES ('MBW_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), write_allowed_yn=VALUES(write_allowed_yn), data_scope=VALUES(data_scope), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_MENU (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
VALUES ('MBW_DASHBOARD', '업무 관리자 대시보드', NULL, 'MBW', '/backoffice', 'dashboard', 'ALL', '/api/v1/backoffice/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_ORGANIZATION', '조직 관리', NULL, 'MBW', '/backoffice/organizations', 'organization', 'ALL', '/api/v1/backoffice/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_EMPLOYEE', '직원·소속 관리', NULL, 'MBW', '/backoffice/employees', 'employee', 'ALL', '/api/v1/backoffice/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_AUTHORIZATION', '업무 권한 관리', NULL, 'MBW', '/backoffice/authorization', 'shield', 'ALL', '/api/v1/backoffice/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVAL', '업무 결재 관리', NULL, 'MBW', '/backoffice/approvals', 'approval', 'ALL', '/api/v1/backoffice/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_AUDIT', '업무 감사 조회', NULL, 'MBW', '/backoffice/audits', 'audit', 'ALL', '/api/v1/backoffice/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_ATTACHMENT', '첨부 관리', NULL, 'MBW', '/backoffice/attachments', 'attachment', 'ALL', '/api/v1/backoffice/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_SETTING', '업무 관리자 설정', NULL, 'MBW', '/backoffice/settings', 'setting', 'ALL', '/api/v1/backoffice/settings', 80, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), parent_menu_code=VALUES(parent_menu_code), module_code=VALUES(module_code), route_path=VALUES(route_path), icon_code=VALUES(icon_code), environment_code=VALUES(environment_code), api_path=VALUES(api_path), sort_order=VALUES(sort_order), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
SELECT 'MBW_ADMIN', menu_code, 'ALL', 'API', '*', CONCAT(api_path, '/**'),
       NULL, environment_code, 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM MBW_MENU
WHERE use_yn = 'Y'
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), data_scope=VALUES(data_scope), allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES ('MBW_OPERATOR', 'MBW_DASHBOARD', 'READ', 'API', 'GET', '/api/v1/backoffice/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', 'MBW_ORGANIZATION', 'READ', 'API', 'GET', '/api/v1/backoffice/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', 'MBW_EMPLOYEE', 'READ', 'API', 'GET', '/api/v1/backoffice/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', 'MBW_APPROVAL', 'READ', 'API', 'GET', '/api/v1/backoffice/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', 'MBW_APPROVAL', 'DECIDE', 'API', 'POST', '/api/v1/backoffice/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_VIEWER', 'MBW_DASHBOARD', 'READ', 'API', 'GET', '/api/v1/backoffice/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_VIEWER', 'MBW_AUDIT', 'READ', 'API', 'GET', '/api/v1/backoffice/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), data_scope=VALUES(data_scope), allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES ('MBW.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), description=VALUES(description), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES ('MBW_ADMIN', 'MBW_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/v1/backoffice/permissions/effective', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_ADMIN', 'MBW_EMPLOYEE', 'PII_RAW', 'API', 'POST', '/api/v1/backoffice/employees/*/contacts/raw', NULL, 'ALL', 'ALL', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_OPERATOR', 'MBW_AUTHORIZATION', 'SIMULATE', 'API', 'GET', '/api/v1/backoffice/permissions/effective', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_APPROVER', 'MBW_APPROVAL', 'DECIDE', 'API', 'POST', '/api/v1/backoffice/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), domain_code=VALUES(domain_code), data_scope=VALUES(data_scope), allow_yn=VALUES(allow_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/60_adm_seed_data.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=60_adm_seed_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
INSERT INTO ADM_ROLE (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE ROLE_NAME=VALUES(ROLE_NAME), ROLE_TYPE=VALUES(ROLE_TYPE), DESCRIPTION=VALUES(DESCRIPTION), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE PARENT_MENU_ID=VALUES(PARENT_MENU_ID), MENU_NAME=VALUES(MENU_NAME), MENU_PATH=VALUES(MENU_PATH), SORT_ORDER=VALUES(SORT_ORDER), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE MENU_ID=VALUES(MENU_ID), ACTION_CODE=VALUES(ACTION_CODE), BUTTON_NAME=VALUES(BUTTON_NAME), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATTERN=VALUES(API_PATTERN), SORT_ORDER=VALUES(SORT_ORDER), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_PASSWORD_POLICY (POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by)
VALUES (
    'DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE MIN_LENGTH=VALUES(MIN_LENGTH), REQUIRE_UPPER_YN=VALUES(REQUIRE_UPPER_YN), REQUIRE_LOWER_YN=VALUES(REQUIRE_LOWER_YN), REQUIRE_DIGIT_YN=VALUES(REQUIRE_DIGIT_YN), REQUIRE_SPECIAL_YN=VALUES(REQUIRE_SPECIAL_YN), MAX_FAIL_COUNT=VALUES(MAX_FAIL_COUNT), EXPIRE_DAYS=VALUES(EXPIRE_DAYS), HISTORY_LIMIT=VALUES(HISTORY_LIMIT), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM ADM_MENU
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('BATCH', 'DOWNLOAD', 'CACHE', 'FILE_JOB') THEN 'Y' ELSE 'N' END,
       'N',
       'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE')
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'CAPABILITY_FLEET', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', MENU_ID, READ_YN, WRITE_YN, DELETE_YN, 'SYSTEM', 'SYSTEM'
FROM ADM_ROLE_MENU
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', BUTTON_ID,
       CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', BUTTON_ID,
       CASE
           WHEN BUTTON_ID IN ('BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH', 'FILE_JOB_UPLOAD', 'FILE_JOB_APPLY', 'FILE_JOB_DOWNLOAD') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', BUTTON_ID,
       CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', BUTTON_ID, ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM ADM_ROLE_BUTTON
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE API_GROUP_CODE=VALUES(API_GROUP_CODE), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATH=VALUES(API_PATH), API_NAME=VALUES(API_NAME), PERMISSION_CODE=VALUES(PERMISSION_CODE), MENU_ID=VALUES(MENU_ID), BUTTON_ID=VALUES(BUTTON_ID), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (
    'API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE',
    'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE API_GROUP_CODE=VALUES(API_GROUP_CODE), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATH=VALUES(API_PATH), API_NAME=VALUES(API_NAME), PERMISSION_CODE=VALUES(PERMISSION_CODE), MENU_ID=VALUES(MENU_ID), BUTTON_ID=VALUES(BUTTON_ID), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('AUDIT_LOG_RETRY','AUDIT_LOG','WRITE','감사 전달 재처리','POST','/adm/api/audit-logs/deliveries/*/retry',20,'Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE ACTION_CODE=VALUES(ACTION_CODE), BUTTON_NAME=VALUES(BUTTON_NAME), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATTERN=VALUES(API_PATTERN), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
UPDATE ADM_ROLE_MENU SET WRITE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP WHERE MENU_ID='AUDIT_LOG' AND ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR');
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID,'AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM' FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
ON DUPLICATE KEY UPDATE ALLOW_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES ('API_AUDIT_LOG_RETRY','AUDIT_LOG','POST','/adm/api/audit-logs/deliveries/*/retry','감사 전달 재처리','WRITE','AUDIT_LOG','AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE HTTP_METHOD=VALUES(HTTP_METHOD), API_PATH=VALUES(API_PATH), PERMISSION_CODE=VALUES(PERMISSION_CODE), BUTTON_ID=VALUES(BUTTON_ID), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID,'API_AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM' FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
ON DUPLICATE KEY UPDATE ALLOW_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('SECRET_READ','SECRET','READ','Secret Metadata 조회','GET','/adm/api/secrets/**',10,'Y','SYSTEM','SYSTEM'),
 ('SECRET_ROTATE','SECRET','ROTATE','Secret Rotation','POST','/adm/api/secrets/rotate',20,'Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE ACTION_CODE=VALUES(ACTION_CODE), BUTTON_NAME=VALUES(BUTTON_NAME), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATTERN=VALUES(API_PATTERN), USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','SECRET','Y','Y','N','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','SECRET','Y','N','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','SECRET','Y','N','N','SYSTEM','SYSTEM'),
 ('ADM_VIEWER','SECRET','N','N','N','SYSTEM','SYSTEM'),
 ('ADM_BIZ_OPERATOR','SECRET','N','N','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_ADMIN','SECRET_ROTATE','Y','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_DEV_OPERATOR','SECRET_ROTATE','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_OPERATOR','SECRET_ROTATE','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES ('API_SECRET_READ','SECRET','GET','/adm/api/secrets/**','Secret Metadata 조회','READ','SECRET','SECRET_READ','Y','SYSTEM','SYSTEM'),
 ('API_SECRET_ROTATE','SECRET','POST','/adm/api/secrets/rotate','Secret Rotation','ROTATE','SECRET','SECRET_ROTATE','Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE API_PATH=VALUES(API_PATH), API_NAME=VALUES(API_NAME), PERMISSION_CODE=VALUES(PERMISSION_CODE), BUTTON_ID=VALUES(BUTTON_ID), USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_ADMIN','API_SECRET_ROTATE','Y','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_DEV_OPERATOR','API_SECRET_ROTATE','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_OPERATOR','API_SECRET_ROTATE','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE MENU_ID=VALUES(MENU_ID), ACTION_CODE=VALUES(ACTION_CODE), BUTTON_NAME=VALUES(BUTTON_NAME), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATTERN=VALUES(API_PATTERN), SORT_ORDER=VALUES(SORT_ORDER), USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT r.ROLE_ID,m.MENU_ID,'Y',
       CASE WHEN r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR') THEN 'Y' ELSE 'N' END,
       'N','SYSTEM','SYSTEM'
FROM ADM_ROLE r JOIN ADM_MENU m ON m.PARENT_MENU_ID='BATCH'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN), WRITE_YN=VALUES(WRITE_YN), DELETE_YN=VALUES(DELETE_YN), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE API_GROUP_CODE=VALUES(API_GROUP_CODE), HTTP_METHOD=VALUES(HTTP_METHOD), API_PATH=VALUES(API_PATH), API_NAME=VALUES(API_NAME), PERMISSION_CODE=VALUES(PERMISSION_CODE), MENU_ID=VALUES(MENU_ID), BUTTON_ID=VALUES(BUTTON_ID), USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN), updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/61_adm_gateway_seed.sql
-- ============================================================================
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=61_adm_gateway_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
INSERT INTO ADM_BUTTON (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by)
VALUES ('GATEWAY_READ','GATEWAY_DASHBOARD','READ','Gateway 운영 조회','GET','/adm/api/gateway-registry/**',10,'Y','SYSTEM','SYSTEM'),
('GATEWAY_GROUP_WRITE','GATEWAY_GROUPS','WRITE','Server Group 저장','POST','/adm/api/gateway-registry/server-groups',20,'Y','SYSTEM','SYSTEM'),
('GATEWAY_GROUP_DELETE','GATEWAY_GROUPS','DELETE','Server Group 폐기','DELETE','/adm/api/gateway-registry/server-groups/*',30,'Y','SYSTEM','SYSTEM'),
('GATEWAY_ROUTE_WRITE','GATEWAY_ROUTES','WRITE','Gateway Binding 저장','POST','/adm/api/gateway-registry/bindings',40,'Y','SYSTEM','SYSTEM'),
('GATEWAY_ROUTE_STATE','GATEWAY_ROUTES','CONTROL','Gateway Binding 상태 변경','POST','/adm/api/gateway-registry/bindings/*/state',50,'Y','SYSTEM','SYSTEM'),
('GATEWAY_ROUTE_DELETE','GATEWAY_ROUTES','DELETE','Gateway Binding 폐기','DELETE','/adm/api/gateway-registry/bindings/*',60,'Y','SYSTEM','SYSTEM'),
('GATEWAY_CONNECTION_TEST','GATEWAY_HEALTH','TEST','Gateway 연결시험 요청','POST','/adm/api/gateway-registry/bindings/*/connection-tests',70,'Y','SYSTEM','SYSTEM'),
('GATEWAY_TEST_CONTROL','GATEWAY_HEALTH','CONTROL','Gateway 연결시험 취소·재검증','POST','/adm/api/gateway-registry/connection-test-operations/*/**',80,'Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id), action_code=VALUES(action_code), button_name=VALUES(button_name), http_method=VALUES(http_method), api_pattern=VALUES(api_pattern), sort_order=VALUES(sort_order), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_MENU (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by)
VALUES ('ADM_ADMIN','GATEWAY_DASHBOARD','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_SERVERS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_GROUPS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTES','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_SECURITY','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_HEALTH','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_TRANSACTIONS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_LOG_POLICY','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_APPLY_STATUS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_DASHBOARD','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_SERVERS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_GROUPS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTES','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_SECURITY','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_HEALTH','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_TRANSACTIONS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_LOG_POLICY','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_APPLY_STATUS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_DASHBOARD','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_SERVERS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_GROUPS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTES','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_SECURITY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_HEALTH','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_TRANSACTIONS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_LOG_POLICY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_APPLY_STATUS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_DASHBOARD','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_SERVERS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_GROUPS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTES','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_SECURITY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_HEALTH','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_TRANSACTIONS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_LOG_POLICY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_APPLY_STATUS','Y','N','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE read_yn=VALUES(read_yn), write_yn=VALUES(write_yn), delete_yn=VALUES(delete_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_API_PERMISSION (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by)
VALUES ('API_GATEWAY_READ','GATEWAY','GET','/adm/api/gateway-registry/**','Gateway 운영 조회','READ','GATEWAY_DASHBOARD','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_GROUP_WRITE','GATEWAY','POST','/adm/api/gateway-registry/server-groups','Server Group 저장','WRITE','GATEWAY_GROUPS','GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_GROUP_DELETE','GATEWAY','DELETE','/adm/api/gateway-registry/server-groups/*','Server Group 폐기','DELETE','GATEWAY_GROUPS','GATEWAY_GROUP_DELETE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_ROUTE_WRITE','GATEWAY','POST','/adm/api/gateway-registry/bindings','Gateway Binding 저장','WRITE','GATEWAY_ROUTES','GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_ROUTE_STATE','GATEWAY','POST','/adm/api/gateway-registry/bindings/*/state','Gateway Binding 상태 변경','CONTROL','GATEWAY_ROUTES','GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_ROUTE_DELETE','GATEWAY','DELETE','/adm/api/gateway-registry/bindings/*','Gateway Binding 폐기','DELETE','GATEWAY_ROUTES','GATEWAY_ROUTE_DELETE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_CONNECTION_TEST','GATEWAY','POST','/adm/api/gateway-registry/bindings/*/connection-tests','Gateway 연결시험 요청','TEST','GATEWAY_HEALTH','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_TEST_CONTROL','GATEWAY','POST','/adm/api/gateway-registry/connection-test-operations/*/**','Gateway 연결시험 취소·재검증','CONTROL','GATEWAY_HEALTH','GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE api_group_code=VALUES(api_group_code), http_method=VALUES(http_method), api_path=VALUES(api_path), api_name=VALUES(api_name), permission_code=VALUES(permission_code), menu_id=VALUES(menu_id), button_id=VALUES(button_id), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_BUTTON (role_id, button_id, allow_yn, created_by, updated_by)
VALUES ('ADM_ADMIN','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_GROUP_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTE_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_CONNECTION_TEST','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE allow_yn=VALUES(allow_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO ADM_ROLE_API_PERMISSION (role_id, api_permission_id, allow_yn, created_by, updated_by)
VALUES ('ADM_ADMIN','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_GROUP_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_ROUTE_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_CONNECTION_TEST','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE allow_yn=VALUES(allow_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- ============================================================================
-- cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql
-- ============================================================================
-- AUTO-GENERATED from CPF canonical schema/profile contracts
-- vendor=mariadb; each logical section executes in its profile-selected physical database.
-- DO NOT EDIT generated verify SQL directly.

-- CPF_LOGICAL_DATABASE=cpfDB
SELECT 'cpfDB.table_count' AS check_name,
       IF(COUNT(*) = 207, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';

SELECT 'cpfDB.table_engine_collation' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
  AND (UPPER(COALESCE(engine, '')) <> 'INNODB'
       OR LOWER(COALESCE(table_collation, '')) <> 'utf8mb4_unicode_ci');

SELECT 'cpfDB.runtime_transaction_id_contract' AS check_name,
       IF(COUNT(*) = 28 AND COALESCE(SUM(CASE
           WHEN UPPER(table_name) = 'ADM_APPROVAL_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_APPROVAL_REQUEST' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_AUDIT_DELIVERY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_AUDIT_LOG' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_DYNAMIC_LOG_LEVEL_RULE' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'ADM_INCIDENT_LIFECYCLE' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 100 THEN 1
           WHEN UPPER(table_name) = 'ADM_INCIDENT_SIGNAL' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 100 THEN 1
           WHEN UPPER(table_name) = 'BAT_CENTER_CUT_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_CENTER_CUT_ITEM' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_CENTER_CUT_RESULT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_JOB_DEFINITION_AUDIT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_ON_DEMAND_REQUEST' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'BAT_RUNTIME_COMMAND' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_BROKER_DLQ' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_BROKER_OUTBOX' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_FILE_TRANSFER_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_SAGA_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_LINEAGE' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_LINEAGE_ARCHIVE' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_LOG' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_TRANSACTION_SEGMENT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'CPF_UNKNOWN_RESULT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'GW_TRANSACTION' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 100 THEN 1
           WHEN UPPER(table_name) = 'OPS_ASYNC_OPERATION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'OPS_SERVICE_CALL_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'OPS_TRANSACTION_SUBJECT' AND LOWER(data_type) = 'varchar' AND character_maximum_length = 128 THEN 1
           WHEN UPPER(table_name) = 'SEC_TOKEN_AUDIT_LOG' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           ELSE 0 END), 0) = 28, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema = DATABASE() AND LOWER(column_name) = 'transaction_id';

SELECT 'cpfDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM CMN_CODE WHERE (code_key = 'CODE_GROUP' AND code_value = 'MODULE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') OR
               (code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') OR
               (code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') OR
               (code_key = 'CODE_GROUP' AND code_value = 'YN') OR
               (code_key = 'MODULE' AND code_value = 'CPF') OR
               (code_key = 'MODULE' AND code_value = 'CMN') OR
               (code_key = 'MODULE' AND code_value = 'ADM') OR
               (code_key = 'MODULE' AND code_value = 'MBW') OR
               (code_key = 'MODULE' AND code_value = 'BAT') OR
               (code_key = 'MODULE' AND code_value = 'EDU') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'NORMAL') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'COMPENSATION') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'RETRY') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'WEB') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'MOBILE') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'BATCH') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'ADM') OR
               (code_key = 'RESULT_TYPE' AND code_value = 'S') OR
               (code_key = 'RESULT_TYPE' AND code_value = 'E') OR
               (code_key = 'MESSAGE_FORMAT_TYPE' AND code_value = 'FIXED') OR
               (code_key = 'MESSAGE_FORMAT_TYPE' AND code_value = 'INDEXED') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'TRACE') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'DEBUG') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'INFO') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'WARN') OR
               (code_key = 'LOG_LEVEL' AND code_value = 'ERROR') OR
               (code_key = 'CACHE_NAME' AND code_value = 'ALL') OR
               (code_key = 'CACHE_NAME' AND code_value = 'CODE') OR
               (code_key = 'CACHE_NAME' AND code_value = 'MESSAGE') OR
               (code_key = 'CACHE_NAME' AND code_value = 'RESPONSE_CODE') OR
               (code_key = 'CACHE_NAME' AND code_value = 'CONFIG') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'TASKLET') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'CHUNK') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'RETRY') OR
               (code_key = 'YN' AND code_value = 'Y') OR
               (code_key = 'YN' AND code_value = 'N') OR
               (code_key = 'CODE_GROUP' AND code_value = 'HTTP_METHOD') OR
               (code_key = 'CODE_GROUP' AND code_value = 'EXECUTION_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'ASYNC_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'RETRY_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'IDEMPOTENCY_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'HEALTH_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'CIRCUIT_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'FILE_SCAN_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'DATA_CLASSIFICATION') OR
               (code_key = 'CODE_GROUP' AND code_value = 'APPROVAL_STATUS') OR
               (code_key = 'CODE_GROUP' AND code_value = 'ERROR_CATEGORY') OR
               (code_key = 'CODE_GROUP' AND code_value = 'RETENTION_ACTION') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'GET') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'POST') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'PUT') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'PATCH') OR
               (code_key = 'HTTP_METHOD' AND code_value = 'DELETE') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'READY') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'RUNNING') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'SUCCESS') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'EXECUTION_STATUS' AND code_value = 'UNKNOWN_RESULT') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'WAITING') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'PROCESSING') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'COMPLETED') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'DLQ') OR
               (code_key = 'RETRY_STATUS' AND code_value = 'RETRYABLE') OR
               (code_key = 'RETRY_STATUS' AND code_value = 'NON_RETRYABLE') OR
               (code_key = 'RETRY_STATUS' AND code_value = 'EXHAUSTED') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'PROCESSING') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'COMPLETED') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'IDEMPOTENCY_STATUS' AND code_value = 'UNKNOWN_RESULT') OR
               (code_key = 'HEALTH_STATUS' AND code_value = 'UP') OR
               (code_key = 'HEALTH_STATUS' AND code_value = 'DOWN') OR
               (code_key = 'HEALTH_STATUS' AND code_value = 'DEGRADED') OR
               (code_key = 'CIRCUIT_STATUS' AND code_value = 'CLOSED') OR
               (code_key = 'CIRCUIT_STATUS' AND code_value = 'OPEN') OR
               (code_key = 'CIRCUIT_STATUS' AND code_value = 'HALF_OPEN') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'PENDING') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'CLEAN') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'INFECTED') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'FILE_SCAN_STATUS' AND code_value = 'QUARANTINED') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'PUBLIC') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'INTERNAL') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'CONFIDENTIAL') OR
               (code_key = 'DATA_CLASSIFICATION' AND code_value = 'RESTRICTED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'DRAFT') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'IN_REVIEW') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'APPROVED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'REJECTED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'WITHDRAWN') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'CANCELED') OR
               (code_key = 'APPROVAL_STATUS' AND code_value = 'EXPIRED') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'VALIDATION') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'AUTHENTICATION') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'AUTHORIZATION') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'CONFLICT') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'TIMEOUT') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'TARGET_DOWN') OR
               (code_key = 'ERROR_CATEGORY' AND code_value = 'UNKNOWN_RESULT') OR
               (code_key = 'RETENTION_ACTION' AND code_value = 'ARCHIVE') OR
               (code_key = 'RETENTION_ACTION' AND code_value = 'PURGE') OR
               (code_key = 'RETENTION_ACTION' AND code_value = 'LEGAL_HOLD') OR
               (code_key = 'CODE_GROUP' AND code_value = 'SORT_DIRECTION') OR
               (code_key = 'SORT_DIRECTION' AND code_value = 'ASC') OR
               (code_key = 'SORT_DIRECTION' AND code_value = 'DESC') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'O') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'S') OR
               (code_key = 'REQUEST_TYPE' AND code_value = 'B') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'APP') OR
               (code_key = 'CHANNEL_CODE' AND code_value = 'JUT') OR
               (code_key = 'RESULT_TYPE' AND code_value = 'W') OR
               (code_key = 'MESSAGE_FORMAT_TYPE' AND code_value = 'PARAMETER') OR
               (code_key = 'ASYNC_STATUS' AND code_value = 'FAILED') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'SPRING_BATCH') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'WORKER') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'SCHEDULER') OR
               (code_key = 'BATCH_JOB_TYPE' AND code_value = 'CENTER_CUT')) = 121
           AND (SELECT COUNT(*) FROM CMN_MESSAGE WHERE (message_code = 'MCPF000000' AND locale = 'ko') OR
               (message_code = 'MCPF010001' AND locale = 'ko') OR
               (message_code = 'MCPF010002' AND locale = 'ko') OR
               (message_code = 'MCPF010003' AND locale = 'ko') OR
               (message_code = 'MCPF010004' AND locale = 'ko') OR
               (message_code = 'MCPF010005' AND locale = 'ko') OR
               (message_code = 'MCPF010006' AND locale = 'ko') OR
               (message_code = 'MCPF020001' AND locale = 'ko') OR
               (message_code = 'MCPF030001' AND locale = 'ko') OR
               (message_code = 'MCPF900001' AND locale = 'ko') OR
               (message_code = 'MCPF900002' AND locale = 'ko') OR
               (message_code = 'MCPF900003' AND locale = 'ko') OR
               (message_code = 'MCPF900004' AND locale = 'ko') OR
               (message_code = 'MCPF900005' AND locale = 'ko') OR
               (message_code = 'MCPF990000' AND locale = 'ko') OR
               (message_code = 'MCPF990001' AND locale = 'ko') OR
               (message_code = 'MMBW000000' AND locale = 'ko') OR
               (message_code = 'MMBW010001' AND locale = 'ko') OR
               (message_code = 'MMBW010002' AND locale = 'ko') OR
               (message_code = 'MEDU010001' AND locale = 'ko') OR
               (message_code = 'MCMN000001' AND locale = 'ko') OR
               (message_code = 'MCMN000001' AND locale = 'en') OR
               (message_code = 'MCPF030002' AND locale = 'ko') OR
               (message_code = 'MCPF030003' AND locale = 'ko') OR
               (message_code = 'MCPF030004' AND locale = 'ko') OR
               (message_code = 'MCPF020002' AND locale = 'ko') OR
               (message_code = 'MCPF020003' AND locale = 'ko') OR
               (message_code = 'MCPF040001' AND locale = 'ko') OR
               (message_code = 'MCPF040002' AND locale = 'ko') OR
               (message_code = 'MCPF020004' AND locale = 'ko') OR
               (message_code = 'MCPF020005' AND locale = 'ko') OR
               (message_code = 'MCPF020006' AND locale = 'ko') OR
               (message_code = 'MCPF020007' AND locale = 'ko') OR
               (message_code = 'MCPF040003' AND locale = 'ko') OR
               (message_code = 'MCPF040004' AND locale = 'ko') OR
               (message_code = 'MCPF050001' AND locale = 'ko') OR
               (message_code = 'MCPF050002' AND locale = 'ko')) = 37
           AND (SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE (response_code = 'SCPF000000') OR
               (response_code = 'ECPF010001') OR
               (response_code = 'ECPF010002') OR
               (response_code = 'ECPF010003') OR
               (response_code = 'ECPF010004') OR
               (response_code = 'ECPF010005') OR
               (response_code = 'ECPF010006') OR
               (response_code = 'ECPF020001') OR
               (response_code = 'ECPF030001') OR
               (response_code = 'ECPF900001') OR
               (response_code = 'ECPF900002') OR
               (response_code = 'ECPF900003') OR
               (response_code = 'ECPF900004') OR
               (response_code = 'ECPF900005') OR
               (response_code = 'ECPF990000') OR
               (response_code = 'ECPF990001') OR
               (response_code = 'SMBW000000') OR
               (response_code = 'EMBW010001') OR
               (response_code = 'EMBW010002') OR
               (response_code = 'EEDU010001') OR
               (response_code = 'ECPF030002') OR
               (response_code = 'ECPF030003') OR
               (response_code = 'ECPF030004') OR
               (response_code = 'ECPF020002') OR
               (response_code = 'ECPF020003') OR
               (response_code = 'ECPF040001') OR
               (response_code = 'ECPF040002') OR
               (response_code = 'ECPF020004') OR
               (response_code = 'ECPF020005') OR
               (response_code = 'ECPF020006') OR
               (response_code = 'ECPF020007') OR
               (response_code = 'ECPF040003') OR
               (response_code = 'ECPF040004') OR
               (response_code = 'ECPF050001') OR
               (response_code = 'ECPF050002')) = 35
           AND (SELECT COUNT(*) FROM CMN_PARAMETER WHERE (config_key = 'CPF.CMN.CACHE.PRELOAD_ENABLED') OR
               (config_key = 'CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP') OR
               (config_key = 'CPF.CMN.CACHE.REFRESH_POLL_MILLIS') OR
               (config_key = 'CPF.CMN.MESSAGING.BROKER') OR
               (config_key = 'CPF.HTTP.CONNECT_TIMEOUT_MS') OR
               (config_key = 'CPF.HTTP.READ_TIMEOUT_MS') OR
               (config_key = 'CPF.ADM.SESSION_TTL_SECONDS') OR
               (config_key = 'CPF.ADM.PASSWORD_EXPIRE_DAYS') OR
               (config_key = 'CPF.ADM.PASSWORD_MIN_LENGTH') OR
               (config_key = 'CPF.ADM.PASSWORD_MAX_FAIL_COUNT') OR
               (config_key = 'CPF.BATCH.DEFAULT_LOCK_SECONDS') OR
               (config_key = 'CPF.FEATURE.SAMPLE_ENABLED') OR
               (config_key = 'CPF.MBW.SECURITY.MAX_LOGIN_FAIL_COUNT') OR
               (config_key = 'CPF.MBW.SECURITY.ACCESS_TOKEN_TTL_SECONDS') OR
               (config_key = 'CPF.MBW.SECURITY.REFRESH_TOKEN_TTL_SECONDS') OR
               (config_key = 'CPF.RETENTION.EXECUTE_ENABLED') OR
               (config_key = 'CPF.FILE.DOWNLOAD_REQUIRE_CLEAN') OR
               (config_key = 'CPF.HEALTH.INSTANCE_ID_REQUIRED') OR
               (config_key = 'CPF.PAGING.DEFAULT_SIZE') OR
               (config_key = 'CPF.PAGING.MAX_SIZE') OR
               (config_key = 'CPF.RETENTION.DRY_RUN_DEFAULT') OR
               (config_key = 'CPF.SECRET.CACHE_TTL_SECONDS') OR
               (config_key = 'CPF.TENANT.ENABLED') OR
               (config_key = 'CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS')) = 24,
           1, 0
       ) AS passed;

SELECT 'cpfDB.response_code_http_status' AS check_name,
       IF((SELECT COUNT(*) FROM CMN_RESPONSE_CODE WHERE (response_code = 'SCPF000000') OR
               (response_code = 'ECPF010001') OR
               (response_code = 'ECPF010002') OR
               (response_code = 'ECPF010003') OR
               (response_code = 'ECPF010004') OR
               (response_code = 'ECPF010005') OR
               (response_code = 'ECPF010006') OR
               (response_code = 'ECPF020001') OR
               (response_code = 'ECPF030001') OR
               (response_code = 'ECPF900001') OR
               (response_code = 'ECPF900002') OR
               (response_code = 'ECPF900003') OR
               (response_code = 'ECPF900004') OR
               (response_code = 'ECPF900005') OR
               (response_code = 'ECPF990000') OR
               (response_code = 'ECPF990001') OR
               (response_code = 'SMBW000000') OR
               (response_code = 'EMBW010001') OR
               (response_code = 'EMBW010002') OR
               (response_code = 'EEDU010001') OR
               (response_code = 'ECPF030002') OR
               (response_code = 'ECPF030003') OR
               (response_code = 'ECPF030004') OR
               (response_code = 'ECPF020002') OR
               (response_code = 'ECPF020003') OR
               (response_code = 'ECPF040001') OR
               (response_code = 'ECPF040002') OR
               (response_code = 'ECPF020004') OR
               (response_code = 'ECPF020005') OR
               (response_code = 'ECPF020006') OR
               (response_code = 'ECPF020007') OR
               (response_code = 'ECPF040003') OR
               (response_code = 'ECPF040004') OR
               (response_code = 'ECPF050001') OR
               (response_code = 'ECPF050002')) = 35
          AND NOT EXISTS (SELECT 1 FROM CMN_RESPONSE_CODE WHERE http_status NOT BETWEEN 100 AND 599), 1, 0) AS passed;

SELECT 'cpfDB.admin_product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM ADM_ROLE WHERE USE_YN = 'Y') >= 5
           AND (SELECT COUNT(*) FROM ADM_MENU WHERE USE_YN = 'Y') >= 30
           AND (SELECT COUNT(*) FROM ADM_API_PERMISSION WHERE USE_YN = 'Y') >= 10,
           1, 0
       ) AS passed;

SELECT 'cpfDB.removed_stale_tables_absent' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND LOWER(table_name) IN ('cpf_file_exchange_log','adm_operation_log');

SELECT 'cpfDB.adm_operator_account_safety_columns' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema = DATABASE() AND UPPER(table_name) = 'ADM_OPERATOR'
  AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');

SELECT 'cpfDB.adm_contact_ownership' AS check_name,
       IF(
         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 0
         AND
         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR_PROFILE' AND UPPER(column_name) IN ('MOBILE_NO','OFFICE_PHONE_NO')) = 2,
         1, 0
       ) AS passed;

SELECT 'cpfDB.adm_operator_status_constraint' AS check_name,
       IF(COUNT(*) = 1, 1, 0) AS passed
FROM information_schema.table_constraints
WHERE table_schema=DATABASE() AND UPPER(table_name)='ADM_OPERATOR' AND constraint_name='ck_adm_operator_status';

-- CPF_LOGICAL_DATABASE=mbwDB
SELECT 'mbwDB.table_count' AS check_name,
       IF(COUNT(*) = 30, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE';

SELECT 'mbwDB.table_engine_collation' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
  AND (UPPER(COALESCE(engine, '')) <> 'INNODB'
       OR LOWER(COALESCE(table_collation, '')) <> 'utf8mb4_unicode_ci');

SELECT 'mbwDB.runtime_transaction_id_contract' AS check_name,
       IF(COUNT(*) = 7 AND COALESCE(SUM(CASE
           WHEN UPPER(table_name) = 'MBW_APPROVAL_DOCUMENT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_APPROVAL_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_APPROVAL_EXECUTION' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_BUSINESS_AUDIT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_DOWNLOAD_AUDIT' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_LOGIN_HISTORY' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           WHEN UPPER(table_name) = 'MBW_REFRESH_TOKEN' AND LOWER(data_type) = 'char' AND character_maximum_length = 34 THEN 1
           ELSE 0 END), 0) = 7, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema = DATABASE() AND LOWER(column_name) = 'transaction_id';

SELECT 'mbwDB.product_seed' AS check_name,
       IF(
           (SELECT COUNT(*) FROM MBW_ROLE WHERE use_yn = 'Y') >= 4
           AND (SELECT COUNT(*) FROM MBW_MENU WHERE use_yn = 'Y') >= 8
           AND (SELECT COUNT(*) FROM MBW_PERMISSION WHERE role_code = 'MBW_ADMIN' AND allow_yn = 'Y' AND use_yn = 'Y') >= 8,
           1, 0
       ) AS passed;

SELECT 'mbwDB.removed_stale_tables_absent' AS check_name,
       IF(COUNT(*) = 0, 1, 0) AS passed
FROM information_schema.tables
WHERE table_schema = DATABASE() AND LOWER(table_name) IN ('mbw_customer','mbw_product','mbw_order','mbw_masking_audit');

SELECT 'mbwDB.admin_user_account_safety_columns' AS check_name,
       IF(COUNT(*) = 3, 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_ADMIN_USER'
  AND UPPER(column_name) IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');

SELECT 'mbwDB.employee_status_default' AS check_name,
       IF(MAX(UPPER(REPLACE(COALESCE(column_default,''), CHAR(39), ''))) = 'EMPLOYED', 1, 0) AS passed
FROM information_schema.columns
WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_EMPLOYEE' AND LOWER(column_name)='employment_status';

SELECT 'mbwDB.status_constraints' AS check_name,
       IF(COUNT(*) = 2, 1, 0) AS passed
FROM information_schema.table_constraints
WHERE table_schema=DATABASE() AND ((UPPER(table_name)='MBW_ADMIN_USER' AND constraint_name='ck_mbw_admin_user_status')
   OR (UPPER(table_name)='MBW_EMPLOYEE' AND constraint_name='ck_mbw_employee_status'));

SELECT 'mbwDB.login_operation_contract' AS check_name,
       IF(
         (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_LOGIN_OPERATION' AND table_type='BASE TABLE') = 1
         AND
         (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND UPPER(table_name)='MBW_REFRESH_TOKEN' AND LOWER(column_name)='login_operation_id') = 1,
         1, 0
       ) AS passed;

-- CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences
-- CPF_LOGICAL_DATABASE=cpfDB
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       IF(
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_type = 'SEQUENCE'
               AND LEFT(UPPER(table_name), 7) = 'BAT_SB_') = 3
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_type = 'SEQUENCE'
               AND UPPER(table_name) IN ('BAT_SB_JOB_INSTANCE_SEQ', 'BAT_SB_JOB_EXECUTION_SEQ', 'BAT_SB_STEP_EXECUTION_SEQ')) = 3
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND UPPER(table_name) IN ('BATCH_JOB_EXECUTION_SEQ', 'BATCH_JOB_INSTANCE_SEQ', 'BATCH_JOB_SEQ', 'BATCH_STEP_EXECUTION_SEQ')) = 0,
           1, 0
       ) AS passed;
-- CPF_CANONICAL_OBJECTS_END spring-batch-6-sequences
