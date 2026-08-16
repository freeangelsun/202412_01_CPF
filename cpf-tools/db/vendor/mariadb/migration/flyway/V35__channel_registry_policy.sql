-- CPF 통합 채널 레지스트리와 표준 실행별 최초·호출 채널 정책을 추가합니다.
CREATE TABLE IF NOT EXISTS cpf_channel_policy_version (
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
    PRIMARY KEY (version_id),
    INDEX ix_cpf_channel_policy_version_target (change_type, target_key, version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 채널 정책 변경 버전';

CREATE TABLE IF NOT EXISTS cpf_channel_registry (
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
    PRIMARY KEY (channel_code),
    INDEX ix_cpf_channel_registry_active (active_yn, channel_type),
    CONSTRAINT ck_cpf_channel_registry_client CHECK (client_channel_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_internal CHECK (internal_channel_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_auth CHECK (authentication_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_signature CHECK (signature_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_registry_active CHECK (active_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 통합 채널 레지스트리';

CREATE TABLE IF NOT EXISTS cpf_channel_execution_policy (
    policy_key VARCHAR(100) NOT NULL COMMENT '채널 실행 정책 불변 키',
    standard_execution_id VARCHAR(10) NOT NULL COMMENT '10자리 표준 실행 ID 또는 전체 실행 *',
    original_channel_code VARCHAR(30) NOT NULL COMMENT '최초 채널 코드 또는 ANY',
    caller_channel_code VARCHAR(30) NOT NULL COMMENT '현재 호출 채널 코드 또는 ANY',
    request_type VARCHAR(30) NOT NULL DEFAULT '*' COMMENT '요청 유형 또는 전체 유형 *',
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
    PRIMARY KEY (policy_key),
    INDEX ix_cpf_channel_execution_policy_lookup (standard_execution_id, original_channel_code, caller_channel_code, request_type, active_yn),
    INDEX ix_cpf_channel_execution_policy_effective (active_yn, effective_from, effective_to),
    CONSTRAINT fk_cpf_channel_execution_policy_original FOREIGN KEY (original_channel_code) REFERENCES cpf_channel_registry(channel_code),
    CONSTRAINT fk_cpf_channel_execution_policy_caller FOREIGN KEY (caller_channel_code) REFERENCES cpf_channel_registry(channel_code),
    CONSTRAINT ck_cpf_channel_execution_policy_execution CHECK (standard_execution_id = '*' OR standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'),
    CONSTRAINT ck_cpf_channel_execution_policy_allowed CHECK (allowed_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_auth CHECK (authentication_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_signature CHECK (signature_required_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_active CHECK (active_yn IN ('Y', 'N')),
    CONSTRAINT ck_cpf_channel_execution_policy_period CHECK (effective_from IS NULL OR effective_to IS NULL OR effective_from <= effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 표준 실행별 최초·호출 채널 정책';

INSERT INTO cpf_channel_registry (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn,
    authentication_required_yn, signature_required_yn, active_yn, description,
    policy_version, created_by, updated_by
) VALUES
    ('ANY', '전체 채널', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '정책 와일드카드 전용 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('WEB', '웹', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '웹 브라우저 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('MOBILE', '모바일', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '모바일 애플리케이션 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('ADM', '관리자', 'OPERATOR', 'INTERNAL', 'Y', 'Y', 'Y', 'N', 'Y', 'ADM 운영 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('BATCH', '배치', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '배치 실행 채널', 0, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    channel_name = VALUES(channel_name), channel_type = VALUES(channel_type), trust_level = VALUES(trust_level),
    client_channel_yn = VALUES(client_channel_yn), internal_channel_yn = VALUES(internal_channel_yn),
    authentication_required_yn = VALUES(authentication_required_yn), signature_required_yn = VALUES(signature_required_yn),
    active_yn = VALUES(active_yn), description = VALUES(description), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_channel_execution_policy (
    policy_key, standard_execution_id, original_channel_code, caller_channel_code, request_type,
    allowed_yn, authentication_required_yn, signature_required_yn, max_tps,
    effective_from, effective_to, active_yn, policy_version, created_by, updated_by
) VALUES ('CPF.DEFAULT', '*', 'ANY', 'ANY', '*', 'Y', 'N', 'N', 0, NULL, NULL, 'Y', 0, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    standard_execution_id = VALUES(standard_execution_id), original_channel_code = VALUES(original_channel_code),
    caller_channel_code = VALUES(caller_channel_code), request_type = VALUES(request_type),
    allowed_yn = VALUES(allowed_yn), authentication_required_yn = VALUES(authentication_required_yn),
    signature_required_yn = VALUES(signature_required_yn), max_tps = VALUES(max_tps),
    active_yn = VALUES(active_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
