-- CPF 공식 REF 업무 예제와 EXS 대외 연계 기능을 확장합니다.
-- 반복 실행 시에도 동일한 운영 메타와 seed 상태가 유지되도록 멱등 구문으로 구성합니다.

CREATE DATABASE IF NOT EXISTS refDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS exsDB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE refDB;

CREATE TABLE IF NOT EXISTS ref_center_cut_sample_target (
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    business_date DATE NOT NULL COMMENT '업무 기준일',
    target_payload LONGTEXT NULL COMMENT '처리 입력 payload',
    status_code VARCHAR(30) NOT NULL DEFAULT 'READY' COMMENT '대상 상태 코드',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재처리 횟수',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    started_at DATETIME NULL COMMENT '처리 시작 일시',
    completed_at DATETIME NULL COMMENT '처리 완료 일시',
    last_error_message VARCHAR(1000) NULL COMMENT '마지막 오류 메시지',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (target_id),
    UNIQUE KEY uk_ref_center_cut_sample_target_business (center_cut_job_id, business_key),
    INDEX ix_ref_center_cut_sample_target_status (center_cut_job_id, status_code, business_date),
    INDEX ix_ref_center_cut_sample_target_global (parent_transaction_global_id, child_transaction_global_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='REF 센터컷 샘플 대상';

CREATE TABLE IF NOT EXISTS ref_center_cut_sample_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '센터컷 샘플 결과 순번',
    target_id VARCHAR(80) NOT NULL COMMENT '센터컷 샘플 대상 ID',
    center_cut_job_id VARCHAR(100) NOT NULL COMMENT '센터컷 Job ID',
    business_key VARCHAR(200) NOT NULL COMMENT '업무 멱등 키',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 결과 상태',
    result_payload LONGTEXT NULL COMMENT '처리 결과 payload',
    result_message VARCHAR(1000) NULL COMMENT '처리 결과 메시지',
    parent_transaction_global_id VARCHAR(100) NULL COMMENT '부모 거래 글로벌 ID',
    child_transaction_global_id VARCHAR(100) NULL COMMENT '자식 거래 글로벌 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'REF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_ref_center_cut_sample_result_target (target_id),
    INDEX ix_ref_center_cut_sample_result_job (center_cut_job_id, result_status, created_at),
    INDEX ix_ref_center_cut_sample_result_global (parent_transaction_global_id, child_transaction_global_id),
    CONSTRAINT fk_ref_center_cut_sample_result_target
        FOREIGN KEY (target_id) REFERENCES ref_center_cut_sample_target(target_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='REF 센터컷 샘플 결과';

USE exsDB;

-- 기존 EXS 스키마를 결과 불명 복구와 멱등 실행 계약으로 확장합니다.
ALTER TABLE exs_endpoint
    ADD COLUMN IF NOT EXISTS service_id VARCHAR(100) NULL COMMENT 'CPF 서비스 레지스트리 ID' AFTER institution_code,
    ADD COLUMN IF NOT EXISTS result_query_uri VARCHAR(500) NULL COMMENT '결과 불명 재조회 상대 URI' AFTER endpoint_uri,
    ADD COLUMN IF NOT EXISTS auth_profile_code VARCHAR(80) NULL COMMENT '인증 프로파일 코드' AFTER result_query_uri;
UPDATE exs_endpoint SET service_id = 'EXS' WHERE service_id IS NULL OR service_id = '';
ALTER TABLE exs_endpoint MODIFY service_id VARCHAR(100) NOT NULL COMMENT 'CPF 서비스 레지스트리 ID';

ALTER TABLE exs_auth_profile
    ADD COLUMN IF NOT EXISTS certificate_ref VARCHAR(300) NULL COMMENT '외부 인증서 참조 경로' AFTER secret_ref;
ALTER TABLE exs_token_store
    ADD COLUMN IF NOT EXISTS token_secret_ref VARCHAR(300) NULL COMMENT '토큰 원문 외부 secret 참조' AFTER token_status;
ALTER TABLE exs_route_rule
    ADD COLUMN IF NOT EXISTS priority_no INT NOT NULL DEFAULT 100 COMMENT '우선순위' AFTER endpoint_code;

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
    INDEX ix_exs_control_policy_enabled (enabled_yn, institution_code)
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
    INDEX ix_exs_execution_unknown (unknown_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EXS 멱등 대외 실행';

ALTER TABLE exs_transaction_log
    MODIFY transaction_global_id VARCHAR(100) NOT NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    ADD COLUMN IF NOT EXISTS transaction_segment_id VARCHAR(100) NULL COMMENT 'CPF 트랜잭션 구간 ID' AFTER transaction_global_id,
    ADD COLUMN IF NOT EXISTS execution_id VARCHAR(80) NULL COMMENT '대외 실행 ID' AFTER transaction_segment_id;
ALTER TABLE exs_message_log
    MODIFY transaction_global_id VARCHAR(100) NOT NULL COMMENT 'CPF 트랜잭션 글로벌 ID',
    ADD COLUMN IF NOT EXISTS transaction_segment_id VARCHAR(100) NULL COMMENT 'CPF 트랜잭션 구간 ID' AFTER transaction_global_id,
    ADD COLUMN IF NOT EXISTS execution_id VARCHAR(80) NULL COMMENT '대외 실행 ID' AFTER transaction_segment_id;
ALTER TABLE exs_retry_log
    ADD COLUMN IF NOT EXISTS execution_id VARCHAR(80) NULL COMMENT '대외 실행 ID' FIRST,
    MODIFY transaction_global_id VARCHAR(100) NULL COMMENT 'CPF 트랜잭션 글로벌 ID';
UPDATE exs_retry_log
   SET execution_id = CONCAT('LEGACY-RETRY-', retry_log_id)
 WHERE execution_id IS NULL OR execution_id = '';
ALTER TABLE exs_retry_log MODIFY execution_id VARCHAR(80) NOT NULL COMMENT '대외 실행 ID';

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

USE cpfDB;

-- 공식 REF 서비스와 센터컷 Job을 운영 메타에 등록합니다.
INSERT INTO cpf_service (
    service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by
) VALUES (
    'REF', 'CPF 참조 서비스', 'INTERNAL', 'REF', 'CPF 교육·검증용 참조 서비스', 'Y', 'FLYWAY', 'FLYWAY'
) ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name), owner_module_code = VALUES(owner_module_code),
    description = VALUES(description), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES
    ('REF_API', 'REF', 'REF API Endpoint', 'HTTP', 'http://localhost:8099', '/ref', 3000, 0, 'Y', 'FLYWAY', 'FLYWAY'),
    ('REF-EXTERNAL-SIMULATOR', 'REF', 'REF 대외 시뮬레이터', 'HTTP', 'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id), endpoint_name = VALUES(endpoint_name),
    endpoint_type = VALUES(endpoint_type), base_url = VALUES(base_url),
    context_path = VALUES(context_path), default_timeout_ms = VALUES(default_timeout_ms),
    default_retry_count = VALUES(default_retry_count), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_instance (
    instance_id, service_id, endpoint_code, instance_name, base_url, host_name,
    port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by
) VALUES
    ('REF-local-01', 'REF', 'REF_API', 'REF local instance', 'http://localhost:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'FLYWAY', 'FLYWAY'),
    ('REF-EXS-local-01', 'REF', 'REF-EXTERNAL-SIMULATOR', 'REF 대외 시뮬레이터 인스턴스', 'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id), endpoint_code = VALUES(endpoint_code),
    instance_name = VALUES(instance_name), base_url = VALUES(base_url), host_name = VALUES(host_name),
    port_no = VALUES(port_no), instance_status = VALUES(instance_status), active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_routing_policy (
    service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn,
    health_check_required_yn, active_yn, priority, created_by, updated_by
) VALUES
    ('REF', 'REF_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'FLYWAY', 'FLYWAY'),
    ('REF', 'REF-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    routing_mode = VALUES(routing_mode), load_balance_type = VALUES(load_balance_type),
    failover_enabled_yn = VALUES(failover_enabled_yn), health_check_required_yn = VALUES(health_check_required_yn),
    active_yn = VALUES(active_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_batch_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'CPF REF 업무 DB 센터컷 샘플 Job', 'TASKLET',
    'REF 업무 DB adapter를 통해 센터컷 대상과 결과 흐름을 검증합니다.', 'Y', 'Y', 'FLYWAY', 'FLYWAY'
) ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name), description = VALUES(description), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO bat_center_cut_job (
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, updated_by
) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'CPF REF 업무 DB 센터컷 샘플 Job',
    'refCenterCutTargetProvider', 'refCenterCutHandler', 10, 3, 'Y',
    'CPF 표준 계약과 REF 업무 DB adapter를 연결합니다.', 'FLYWAY', 'FLYWAY'
) ON DUPLICATE KEY UPDATE
    batch_job_id = VALUES(batch_job_id), provider_key = VALUES(provider_key), handler_key = VALUES(handler_key),
    use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO bat_center_cut_parameter (
    center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'FLYWAY', 'FLYWAY'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'defaultLimit', '10', 'N', 'Y', 'FLYWAY', 'FLYWAY'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'targetTable', 'ref_center_cut_sample_target', 'N', 'Y', 'FLYWAY', 'FLYWAY'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'resultTable', 'ref_center_cut_sample_result', 'N', 'Y', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    parameter_value = VALUES(parameter_value), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

USE exsDB;

INSERT INTO exs_institution (
    institution_code, institution_name, enabled_yn, created_by, updated_by
) VALUES ('CPF-REF', 'CPF 참조 대외 시뮬레이터', 'Y', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    institution_name = VALUES(institution_name), enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_channel (
    institution_code, channel_code, direction, enabled_yn, created_by, updated_by
) VALUES ('CPF-REF', 'REST', 'BIDIRECTIONAL', 'Y', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    direction = VALUES(direction), enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_endpoint (
    endpoint_code, institution_code, service_id, http_method, endpoint_uri,
    result_query_uri, timeout_ms, retry_count, enabled_yn, created_by, updated_by
) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'CPF-REF', 'REF', 'POST',
    '/api/reference/external-simulator/executions',
    '/api/reference/external-simulator/results/{externalRequestId}',
    3000, 0, 'Y', 'FLYWAY', 'FLYWAY'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id), endpoint_uri = VALUES(endpoint_uri),
    result_query_uri = VALUES(result_query_uri), enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_control_policy (
    institution_code, control_type, enabled_yn, reason, created_by, updated_by
) VALUES ('CPF-REF', 'SEND', 'Y', '로컬 대외 시뮬레이터 송신 허용', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    enabled_yn = VALUES(enabled_yn), reason = VALUES(reason),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
