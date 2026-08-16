-- CPF QA30 canonical completion migration. Historical V74-V76 remain immutable.

USE cpfDB;
-- CPF source-compatible fix migration generated from canonical schemaVersion 37.
-- CPF_LOGICAL_DATABASE=cpfDB
-- Historical V74-V76 are immutable; all post-QA corrections are applied here.

-- QA30-WP07 Versioned Log Capture Policy Schema V2
ALTER TABLE cpf_log_policy ADD COLUMN policy_schema_version INT NOT NULL DEFAULT 2;
ALTER TABLE cpf_log_policy ADD COLUMN query_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE';
ALTER TABLE cpf_log_policy ADD COLUMN request_header_capture_mode VARCHAR(30) NOT NULL DEFAULT 'ALLOWLIST';
ALTER TABLE cpf_log_policy ADD COLUMN response_header_capture_mode VARCHAR(30) NOT NULL DEFAULT 'ALLOWLIST';
ALTER TABLE cpf_log_policy ADD COLUMN request_body_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE';
ALTER TABLE cpf_log_policy ADD COLUMN response_body_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE';
ALTER TABLE cpf_log_policy ADD COLUMN error_stack_capture_mode VARCHAR(30) NOT NULL DEFAULT 'SUMMARY';
ALTER TABLE cpf_log_policy ADD COLUMN query_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy ADD COLUMN header_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy ADD COLUMN field_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy ADD COLUMN max_query_bytes INT NOT NULL DEFAULT 4096;
ALTER TABLE cpf_log_policy ADD COLUMN max_header_bytes INT NOT NULL DEFAULT 8192;
ALTER TABLE cpf_log_policy ADD COLUMN max_request_body_bytes INT NOT NULL DEFAULT 65536;
ALTER TABLE cpf_log_policy ADD COLUMN max_response_body_bytes INT NOT NULL DEFAULT 65536;
ALTER TABLE cpf_log_policy ADD COLUMN max_stack_bytes INT NOT NULL DEFAULT 32768;
ALTER TABLE cpf_log_policy ADD COLUMN policy_checksum VARCHAR(64) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN policy_schema_version INT NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN query_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN request_header_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN response_header_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN request_body_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN response_body_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN error_stack_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN query_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN header_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN field_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_query_bytes INT NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_header_bytes INT NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_request_body_bytes INT NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_response_body_bytes INT NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_stack_bytes INT NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN policy_checksum VARCHAR(64) NULL;
ALTER TABLE cpf_log_policy MODIFY COLUMN masking_policy_key VARCHAR(120) NOT NULL DEFAULT 'DEFAULT';
UPDATE cpf_log_policy SET request_body_capture_mode=CASE WHEN request_body_log_yn='Y' THEN 'MASKED_BODY' ELSE 'NONE' END, response_body_capture_mode=CASE WHEN response_body_log_yn='Y' THEN 'MASKED_BODY' ELSE 'NONE' END, error_stack_capture_mode=CASE WHEN error_stack_log_yn='Y' THEN 'FULL_MASKED' ELSE 'NONE' END, masking_policy_key=COALESCE(NULLIF(masking_policy_key,''),'DEFAULT'), policy_checksum=COALESCE(policy_checksum,CONCAT('MIGRATED_V2_',policy_id));


CREATE TABLE IF NOT EXISTS cpf_gateway_transaction_capture_segment (
    gateway_transaction_id VARCHAR(100) NOT NULL COMMENT 'Gateway 거래 ID',
    segment_type VARCHAR(40) NOT NULL COMMENT 'Capture Segment 유형',
    policy_schema_version INT NOT NULL DEFAULT 2 COMMENT '로그 정책 Schema Version',
    policy_checksum VARCHAR(64) NOT NULL COMMENT '적용 정책 Checksum',
    captured_value LONGTEXT NOT NULL COMMENT '마스킹/보호된 Capture 값',
    truncated_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '상한 초과 절단 여부',
    metadata_only_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT 'Metadata only 여부',
    observed_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '원본 관측 Byte',
    captured_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Capture 시각',
    CONSTRAINT pk_cpf_gateway_transaction_capture_segment PRIMARY KEY (gateway_transaction_id, segment_type),
    CONSTRAINT fk_cpf_gwy_capture_tx FOREIGN KEY (gateway_transaction_id) REFERENCES cpf_gateway_transaction (gateway_transaction_id),
    CONSTRAINT ck_cpf_gwy_capture_truncated CHECK (truncated_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_gwy_capture_metadata CHECK (metadata_only_yn IN ('Y','N')),
    INDEX ix_cpf_gwy_capture_time (captured_at, segment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway 정책 기반 Capture Segment 원장';

ALTER TABLE cpf_gateway_server_group MODIFY COLUMN hash_key_source VARCHAR(200) NULL;
ALTER TABLE cpf_gateway_server_group MODIFY COLUMN failover_group_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_server_group MODIFY COLUMN health_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_transaction ADD COLUMN binding_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_transaction ADD COLUMN config_checksum VARCHAR(64) NULL;
ALTER TABLE cpf_gateway_transaction ADD COLUMN request_path VARCHAR(1000) NULL;
ALTER TABLE cpf_gateway_transaction ADD COLUMN request_method VARCHAR(20) NULL;
ALTER TABLE cpf_gateway_transaction ADD COLUMN completed_at DATETIME NULL;
ALTER TABLE cpf_gateway_transaction MODIFY COLUMN protocol_status VARCHAR(30) NULL;
ALTER TABLE cpf_gateway_transaction MODIFY COLUMN business_code VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_transaction MODIFY COLUMN source_ip VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_transaction MODIFY COLUMN failure_stage VARCHAR(50) NULL;
ALTER TABLE cpf_gateway_transaction MODIFY COLUMN channel_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_transaction MODIFY COLUMN final_instance_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding ADD COLUMN binding_checksum VARCHAR(64) NULL;
ALTER TABLE cpf_gateway_binding ADD COLUMN retired_by VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding ADD COLUMN retired_at DATETIME NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN approval_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN authentication_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN health_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN tls_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN authorization_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN header_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN failover_group_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_binding MODIFY COLUMN rate_limit_policy_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_connection_test MODIFY COLUMN trace_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_connection_test MODIFY COLUMN failure_stage VARCHAR(50) NULL;
ALTER TABLE cpf_gateway_connection_test MODIFY COLUMN gateway_instance_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_connection_test MODIFY COLUMN instance_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_connection_test MODIFY COLUMN operation_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_apply_status MODIFY COLUMN error_code VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_apply_status MODIFY COLUMN applied_version VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_apply_status MODIFY COLUMN error_message VARCHAR(1000) NULL;
ALTER TABLE cpf_gateway_attempt ADD COLUMN selection_reason VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_attempt ADD COLUMN unknown_yn CHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE cpf_gateway_attempt ADD COLUMN gateway_instance_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_attempt MODIFY COLUMN protocol_status VARCHAR(30) NULL;
ALTER TABLE cpf_gateway_attempt MODIFY COLUMN failure_message VARCHAR(1000) NULL;
ALTER TABLE cpf_gateway_attempt MODIFY COLUMN target_host VARCHAR(300) NULL;
ALTER TABLE cpf_gateway_attempt MODIFY COLUMN failure_code VARCHAR(100) NULL;
ALTER TABLE cpf_cache_invalidation_event MODIFY COLUMN cache_key VARCHAR(512) NULL;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN canary_percent INT NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN probe_owner_id VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN consecutive_successes INT NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN consecutive_failures INT NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN probe_lease_until DATETIME NULL;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN last_probe_at DATETIME NULL;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN ewma_latency_ms DECIMAL(18,4) NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN last_probe_code VARCHAR(100) NULL;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN active_requests BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS cpf_gateway_operation_idempotency (
    operation_id VARCHAR(100) NOT NULL COMMENT 'Operation ID',
    operation_type VARCHAR(50) NOT NULL COMMENT 'Operation 유형',
    resource_id VARCHAR(100) NOT NULL COMMENT '대상 ID',
    payload_hash VARCHAR(64) NOT NULL COMMENT 'Payload SHA-256',
    result_status VARCHAR(30) NOT NULL COMMENT '처리 상태',
    result_payload LONGTEXT NULL COMMENT '마스킹된 결과',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    expires_at DATETIME NOT NULL COMMENT '멱등 보존 만료',
    CONSTRAINT pk_cpf_gateway_operation_idempotency PRIMARY KEY (operation_id),
    INDEX ix_cpf_gwy_operation_resource (operation_type, resource_id, created_at),
    INDEX ix_cpf_gwy_operation_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Command 멱등성 원장';

CREATE TABLE IF NOT EXISTS cpf_gateway_spool_checkpoint (
    gateway_instance_id VARCHAR(100) NOT NULL COMMENT 'Gateway Instance ID',
    spool_name VARCHAR(100) NOT NULL COMMENT 'Spool 이름',
    last_written_sequence BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 기록 Sequence',
    last_ingested_sequence BIGINT NOT NULL DEFAULT 0 COMMENT '마지막 적재 Sequence',
    backlog_count BIGINT NOT NULL DEFAULT 0 COMMENT '적체 건수',
    backlog_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '적체 용량',
    last_error_code VARCHAR(100) NULL COMMENT '마지막 오류',
    last_error_at DATETIME NULL COMMENT '마지막 오류 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '갱신 시각',
    CONSTRAINT pk_cpf_gateway_spool_checkpoint PRIMARY KEY (gateway_instance_id, spool_name),
    INDEX ix_cpf_gwy_spool_backlog (backlog_count, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Durable Spool 관제 Checkpoint';

CREATE TABLE IF NOT EXISTS cpf_gateway_connection_test_operation (
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
    CONSTRAINT pk_cpf_gateway_connection_test_operation PRIMARY KEY (operation_id),
    CONSTRAINT ck_cpf_gwy_test_cancel CHECK (cancel_requested_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_test_op_binding FOREIGN KEY (binding_id) REFERENCES cpf_gateway_binding (binding_id),
    INDEX ix_cpf_gwy_test_op_binding (binding_id, created_at),
    INDEX ix_cpf_gwy_test_op_status (operation_status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway 비동기 연결시험 Operation';

CREATE TABLE IF NOT EXISTS cpf_gateway_health_history (
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
    CONSTRAINT pk_cpf_gateway_health_history PRIMARY KEY (health_history_id),
    CONSTRAINT fk_cpf_gwy_health_group FOREIGN KEY (server_group_id) REFERENCES cpf_gateway_server_group (server_group_id),
    CONSTRAINT fk_cpf_gwy_health_instance FOREIGN KEY (instance_id) REFERENCES cpf_service_instance (instance_id),
    INDEX ix_cpf_gwy_health_member (server_group_id, instance_id, observed_at),
    INDEX ix_cpf_gwy_health_status (overall_status, observed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gateway Protocol 단계별 Health 불변 이력';


USE cmnDB;
-- CPF source-compatible fix migration generated from canonical schemaVersion 37.
-- CPF_LOGICAL_DATABASE=cmnDB
-- Historical V74-V76 are immutable; all post-QA corrections are applied here.

ALTER TABLE cmn_business_calendar_day MODIFY COLUMN reason VARCHAR(500) NULL;
ALTER TABLE cmn_business_calendar_day MODIFY COLUMN institution_code VARCHAR(50) NULL;


USE batDB;
-- CPF source-compatible fix migration generated from canonical schemaVersion 37.
-- CPF_LOGICAL_DATABASE=batDB
-- Historical V74-V76 are immutable; all post-QA corrections are applied here.

ALTER TABLE bat_job_definition_audit MODIFY COLUMN audit_id BIGINT AUTO_INCREMENT NOT NULL;

-- QA30-WP11: unify BAT job identity width before adding the published-version FK.
ALTER TABLE bat_job_dependency DROP FOREIGN KEY fk_bat_job_dep_def;
ALTER TABLE bat_job_parameter_definition DROP FOREIGN KEY fk_bat_job_param_def;
ALTER TABLE bat_job_definition_version MODIFY COLUMN job_id VARCHAR(100) NOT NULL;
ALTER TABLE bat_job_definition_audit MODIFY COLUMN job_id VARCHAR(100) NOT NULL;
ALTER TABLE bat_job_dependency MODIFY COLUMN job_id VARCHAR(100) NOT NULL;
ALTER TABLE bat_job_parameter_definition MODIFY COLUMN job_id VARCHAR(100) NOT NULL;
ALTER TABLE bat_job_dependency ADD CONSTRAINT fk_bat_job_dep_def FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version) ON DELETE CASCADE;
ALTER TABLE bat_job_parameter_definition ADD CONSTRAINT fk_bat_job_param_def FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version) ON DELETE CASCADE;
ALTER TABLE bat_job
    ADD COLUMN published_definition_version BIGINT NULL,
    ADD COLUMN published_definition_checksum VARCHAR(128) NULL,
    ADD COLUMN executor_reference VARCHAR(300) NULL,
    ADD COLUMN definition_published_at DATETIME(3) NULL,
    ADD CONSTRAINT fk_bat_job_published_definition FOREIGN KEY (job_id, published_definition_version) REFERENCES bat_job_definition_version (job_id, definition_version);
ALTER TABLE bat_schedule ADD COLUMN definition_version BIGINT NULL, ADD COLUMN definition_checksum VARCHAR(128) NULL;
ALTER TABLE bat_execution ADD COLUMN definition_version BIGINT NULL, ADD COLUMN definition_checksum VARCHAR(128) NULL;

CREATE TABLE IF NOT EXISTS bat_job_runtime_projection (
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
    CONSTRAINT pk_bat_job_runtime_projection PRIMARY KEY (job_id, definition_version),
    CONSTRAINT fk_bat_job_projection_definition FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version),
    INDEX ix_bat_job_projection_status (projection_status, effective_from, effective_until),
    UNIQUE INDEX ix_bat_job_projection_hash (projection_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Published Batch Definition Runtime 정본';

CREATE TABLE IF NOT EXISTS bat_job_runtime_projection_outbox (
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
    CONSTRAINT pk_bat_job_runtime_projection_outbox PRIMARY KEY (outbox_id),
    CONSTRAINT fk_bat_projection_outbox_definition FOREIGN KEY (job_id, definition_version) REFERENCES bat_job_definition_version (job_id, definition_version),
    INDEX ix_bat_projection_outbox_claim (delivery_status, next_attempt_at, lease_until),
    INDEX ix_bat_projection_outbox_job (job_id, definition_version, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Batch Runtime Projection Durable Outbox';

CREATE TABLE IF NOT EXISTS bat_execution_result_detail (
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
    CONSTRAINT pk_bat_execution_result_detail PRIMARY KEY (execution_id),
    CONSTRAINT ck_bat_result_timeout CHECK (timeout_yn IN ('Y','N')),
    CONSTRAINT ck_bat_result_unknown CHECK (unknown_yn IN ('Y','N')),
    CONSTRAINT ck_bat_result_truncated CHECK (output_truncated_yn IN ('Y','N')),
    CONSTRAINT fk_bat_result_execution FOREIGN KEY (execution_id) REFERENCES bat_execution (execution_id),
    INDEX ix_bat_result_status (executor_status, created_at),
    INDEX ix_bat_result_unknown (unknown_yn, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Batch Executor 상세 결과 원장';
