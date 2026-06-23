-- 온라인 거래 메타와 로그 정책 기본 모델을 추가합니다.
-- 대상 DB: pfwDB

USE pfwDB;

CREATE TABLE IF NOT EXISTS pfw_transaction_meta (
    transaction_id VARCHAR(20) NOT NULL COMMENT '업무 거래 ID',
    transaction_name VARCHAR(150) NOT NULL COMMENT '업무 거래명',
    module_code VARCHAR(20) NOT NULL COMMENT '모듈 코드',
    domain_code VARCHAR(50) NULL COMMENT '업무 영역 코드',
    http_method VARCHAR(20) NOT NULL DEFAULT 'ANY' COMMENT 'HTTP 메서드',
    api_path VARCHAR(500) NOT NULL COMMENT 'API 경로',
    controller_class VARCHAR(255) NOT NULL COMMENT 'Controller 클래스명',
    handler_method VARCHAR(150) NOT NULL COMMENT 'Handler 메서드명',
    swagger_operation_id VARCHAR(150) NULL COMMENT 'Swagger operation 식별자',
    log_policy_key VARCHAR(120) NULL COMMENT '연결 로그 정책 키',
    sensitive_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '민감 거래 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '마스킹 정책 키',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    first_detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 감지일시',
    last_detected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 감지일시',
    last_scanned_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 스캔일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (transaction_id),
    INDEX ix_pfw_transaction_meta_module (module_code, domain_code, active_yn),
    INDEX ix_pfw_transaction_meta_path (http_method, api_path),
    INDEX ix_pfw_transaction_meta_policy (log_policy_key, active_yn),
    INDEX ix_pfw_transaction_meta_scan (active_yn, last_scanned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 온라인 거래 메타';

CREATE TABLE IF NOT EXISTS pfw_log_policy (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 순번',
    policy_key VARCHAR(120) NOT NULL COMMENT '로그 정책 키',
    policy_name VARCHAR(150) NOT NULL COMMENT '로그 정책명',
    target_type VARCHAR(30) NOT NULL COMMENT '정책 대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT '정책 대상 ID',
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '기본 로그 레벨',
    db_log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'DB 로그 적재 여부',
    file_log_enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '파일 로그 출력 여부',
    request_body_log_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '요청 본문 로그 여부',
    response_body_log_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '응답 본문 로그 여부',
    error_stack_log_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '오류 stack 로그 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '마스킹 정책 키',
    retention_days INT NOT NULL DEFAULT 90 COMMENT '보존 일수',
    sampling_rate DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '샘플링 비율',
    priority INT NOT NULL DEFAULT 100 COMMENT '정책 우선순위',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    description VARCHAR(500) NULL COMMENT '정책 설명',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (policy_id),
    UNIQUE KEY uk_pfw_log_policy_key (policy_key),
    UNIQUE KEY uk_pfw_log_policy_target (target_type, target_id),
    INDEX ix_pfw_log_policy_active (active_yn, target_type, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 로그 정책';

CREATE TABLE IF NOT EXISTS pfw_log_policy_override (
    override_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 정책 override 순번',
    policy_id BIGINT NULL COMMENT '기본 로그 정책 순번',
    target_type VARCHAR(30) NOT NULL COMMENT 'override 대상 유형',
    target_id VARCHAR(150) NOT NULL COMMENT 'override 대상 ID',
    override_reason VARCHAR(500) NOT NULL COMMENT 'override 사유',
    log_level VARCHAR(20) NULL COMMENT '임시 로그 레벨',
    db_log_enabled_yn CHAR(1) NULL COMMENT 'DB 로그 임시 적재 여부',
    file_log_enabled_yn CHAR(1) NULL COMMENT '파일 로그 임시 출력 여부',
    request_body_log_yn CHAR(1) NULL COMMENT '요청 본문 임시 로그 여부',
    response_body_log_yn CHAR(1) NULL COMMENT '응답 본문 임시 로그 여부',
    error_stack_log_yn CHAR(1) NULL COMMENT '오류 stack 임시 로그 여부',
    masking_policy_key VARCHAR(120) NULL COMMENT '임시 마스킹 정책 키',
    effective_start_at DATETIME(3) NOT NULL COMMENT '적용 시작일시',
    effective_end_at DATETIME(3) NOT NULL COMMENT '적용 종료일시',
    requested_by VARCHAR(100) NOT NULL COMMENT '요청자',
    approved_by VARCHAR(100) NULL COMMENT '승인자',
    active_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '활성 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (override_id),
    INDEX ix_pfw_log_policy_override_target (target_type, target_id, active_yn),
    INDEX ix_pfw_log_policy_override_period (effective_start_at, effective_end_at, active_yn),
    INDEX ix_pfw_log_policy_override_policy (policy_id, active_yn),
    CONSTRAINT fk_pfw_log_policy_override_policy
        FOREIGN KEY (policy_id) REFERENCES pfw_log_policy(policy_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 로그 정책 임시 override';

CREATE TABLE IF NOT EXISTS pfw_log_policy_audit (
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
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (audit_id),
    INDEX ix_pfw_log_policy_audit_target (target_type, target_id, created_at),
    INDEX ix_pfw_log_policy_audit_operator (operator_id, created_at),
    INDEX ix_pfw_log_policy_audit_policy (policy_id, created_at),
    CONSTRAINT fk_pfw_log_policy_audit_policy
        FOREIGN KEY (policy_id) REFERENCES pfw_log_policy(policy_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pfw_log_policy_audit_override
        FOREIGN KEY (override_id) REFERENCES pfw_log_policy_override(override_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 로그 정책 감사 로그';

INSERT INTO pfw_log_policy (
    policy_key, policy_name, target_type, target_id, log_level,
    db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
    error_stack_log_yn, retention_days, sampling_rate, priority, active_yn,
    description, created_by, updated_by
) VALUES
    ('ONLINE_DEFAULT', '온라인 거래 기본 로그 정책', 'ONLINE_TRANSACTION', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 90, 100.00, 100, 'Y', '온라인 Controller/API 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEFAULT', '배치 기본 로그 정책', 'BATCH_JOB', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 180, 100.00, 100, 'Y', 'Spring Batch Job 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATION_DEFAULT', 'ADM 운영 기본 로그 정책', 'MODULE', 'ADM', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 365, 100.00, 50, 'Y', 'ADM 운영 API 기본 로그 정책', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    policy_name = VALUES(policy_name),
    target_type = VALUES(target_type),
    target_id = VALUES(target_id),
    log_level = VALUES(log_level),
    db_log_enabled_yn = VALUES(db_log_enabled_yn),
    file_log_enabled_yn = VALUES(file_log_enabled_yn),
    request_body_log_yn = VALUES(request_body_log_yn),
    response_body_log_yn = VALUES(response_body_log_yn),
    error_stack_log_yn = VALUES(error_stack_log_yn),
    retention_days = VALUES(retention_days),
    sampling_rate = VALUES(sampling_rate),
    priority = VALUES(priority),
    active_yn = VALUES(active_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
