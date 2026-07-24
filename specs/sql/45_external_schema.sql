-- EXS 대외연계 스키마입니다.
-- 기관·채널·endpoint 정책과 멱등 실행, 결과 불명 복구 원장을 EXS가 소유합니다.

USE exsDB;

CREATE TABLE IF NOT EXISTS exs_institution (
    institution_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외기관 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    institution_name VARCHAR(120) NOT NULL COMMENT '대외기관명',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '연계 허용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (institution_id),
    UNIQUE KEY uk_exs_institution_code (institution_code),
    CONSTRAINT ck_exs_institution_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외기관';

CREATE TABLE IF NOT EXISTS exs_channel (
    channel_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 채널 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    channel_code VARCHAR(50) NOT NULL COMMENT '대외 채널 코드',
    direction VARCHAR(20) NOT NULL COMMENT '송수신 방향',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '채널 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (channel_id),
    UNIQUE KEY uk_exs_channel_code (institution_code, channel_code),
    INDEX ix_exs_channel_enabled (enabled_yn, institution_code),
    CONSTRAINT ck_exs_channel_direction CHECK (direction IN ('SEND', 'RECEIVE', 'BIDIRECTIONAL')),
    CONSTRAINT ck_exs_channel_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 채널';

CREATE TABLE IF NOT EXISTS exs_endpoint (
    endpoint_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 endpoint 순번',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '대외 endpoint 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    service_id VARCHAR(100) NOT NULL COMMENT 'CPF 서비스 레지스트리 ID',
    http_method VARCHAR(10) NOT NULL COMMENT 'HTTP 메서드',
    endpoint_uri VARCHAR(500) NOT NULL COMMENT '대외 endpoint 상대 URI',
    result_query_uri VARCHAR(500) NULL COMMENT '결과 불명 재조회 상대 URI',
    auth_profile_code VARCHAR(80) NULL COMMENT '인증 프로파일 코드',
    timeout_ms INT NOT NULL DEFAULT 3000 COMMENT '호출 제한 시간 밀리초',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '멱등 호출 재시도 횟수',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT 'endpoint 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (endpoint_id),
    UNIQUE KEY uk_exs_endpoint_code (endpoint_code),
    INDEX ix_exs_endpoint_institution (institution_code, enabled_yn),
    CONSTRAINT ck_exs_endpoint_timeout CHECK (timeout_ms BETWEEN 1 AND 120000),
    CONSTRAINT ck_exs_endpoint_retry CHECK (retry_count BETWEEN 0 AND 10),
    CONSTRAINT ck_exs_endpoint_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 endpoint';

CREATE TABLE IF NOT EXISTS exs_auth_profile (
    auth_profile_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 인증 프로파일 순번',
    auth_profile_code VARCHAR(80) NOT NULL COMMENT '대외 인증 프로파일 코드',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    auth_type VARCHAR(30) NOT NULL COMMENT '인증 유형',
    secret_ref VARCHAR(300) NULL COMMENT '외부 secret 참조 경로',
    certificate_ref VARCHAR(300) NULL COMMENT '외부 인증서 참조 경로',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '인증 프로파일 사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (auth_profile_id),
    UNIQUE KEY uk_exs_auth_profile_code (auth_profile_code),
    INDEX ix_exs_auth_profile_institution (institution_code, enabled_yn),
    CONSTRAINT ck_exs_auth_profile_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 대외 인증 프로파일';

CREATE TABLE IF NOT EXISTS exs_control_policy (
    control_policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 통제 정책 순번',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    control_type VARCHAR(30) NOT NULL COMMENT '통제 유형',
    enabled_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '연계 허용 여부',
    reason VARCHAR(500) NULL COMMENT '통제 사유',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (control_policy_id),
    UNIQUE KEY uk_exs_control_policy (institution_code, control_type),
    INDEX ix_exs_control_policy_enabled (enabled_yn, institution_code),
    CONSTRAINT ck_exs_control_policy_enabled CHECK (enabled_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 기관별 송수신 통제 정책';

CREATE TABLE IF NOT EXISTS exs_execution (
    execution_seq BIGINT NOT NULL AUTO_INCREMENT COMMENT '대외 실행 순번',
    execution_id VARCHAR(80) NOT NULL COMMENT '대외 실행 ID',
    institution_code VARCHAR(50) NOT NULL COMMENT '대외기관 코드',
    endpoint_code VARCHAR(80) NOT NULL COMMENT '대외 endpoint 코드',
    external_request_id VARCHAR(120) NOT NULL COMMENT '기관 요청 ID',
    idempotency_key VARCHAR(160) NOT NULL COMMENT '멱등 요청 키',
    request_hash CHAR(64) NOT NULL COMMENT '정규화 요청 SHA-256',
    execution_status VARCHAR(30) NOT NULL COMMENT '실행 상태',
    response_json LONGTEXT NULL COMMENT '마스킹된 응답 JSON',
    unknown_result_id VARCHAR(80) NULL COMMENT 'CPF 결과 불명 원장 ID',
    failure_code VARCHAR(100) NULL COMMENT '실패 코드',
    failure_message VARCHAR(1000) NULL COMMENT '마스킹된 실패 메시지',
    recovery_operator_id VARCHAR(100) NULL COMMENT '복구 작업자 ID',
    recovery_reason VARCHAR(1000) NULL COMMENT '복구 감사 사유',
    recovered_at DATETIME(3) NULL COMMENT '복구 완료 일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (execution_seq),
    UNIQUE KEY uk_exs_execution_id (execution_id),
    UNIQUE KEY uk_exs_execution_idempotency (idempotency_key),
    UNIQUE KEY uk_exs_execution_external_request (institution_code, external_request_id),
    INDEX ix_exs_execution_status_time (execution_status, created_at),
    INDEX ix_exs_execution_unknown (unknown_result_id),
    CONSTRAINT ck_exs_execution_status CHECK (execution_status IN ('REQUESTED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 멱등 대외 실행';

CREATE TABLE IF NOT EXISTS exs_reconciliation_log (
    reconciliation_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '정합성 확인 로그 순번',
    execution_id VARCHAR(80) NOT NULL COMMENT '대외 실행 ID',
    unknown_result_id VARCHAR(80) NOT NULL COMMENT 'CPF 결과 불명 원장 ID',
    before_status VARCHAR(30) NOT NULL COMMENT '변경 전 상태',
    after_status VARCHAR(30) NOT NULL COMMENT '변경 후 상태',
    operator_id VARCHAR(100) NOT NULL COMMENT '복구 작업자 ID',
    audit_reason VARCHAR(1000) NOT NULL COMMENT '복구 감사 사유',
    source_type VARCHAR(30) NOT NULL COMMENT '기관 조회 또는 수동 확정 구분',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일시',
    PRIMARY KEY (reconciliation_log_id),
    INDEX ix_exs_reconciliation_execution (execution_id, created_at),
    INDEX ix_exs_reconciliation_unknown (unknown_result_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 결과 불명 정합성 확인 이력';
