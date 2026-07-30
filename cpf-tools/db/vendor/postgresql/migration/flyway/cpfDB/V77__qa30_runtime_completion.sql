-- CPF source-compatible fix migration generated from canonical schemaVersion 37.
-- CPF_LOGICAL_DATABASE=cpfDB
-- Historical V74-V76 are immutable; all post-QA corrections are applied here.

-- QA30-WP07 Versioned Log Capture Policy Schema V2
ALTER TABLE cpf_log_policy ADD COLUMN policy_schema_version INTEGER NOT NULL DEFAULT 2;
ALTER TABLE cpf_log_policy ADD COLUMN query_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE';
ALTER TABLE cpf_log_policy ADD COLUMN request_header_capture_mode VARCHAR(30) NOT NULL DEFAULT 'ALLOWLIST';
ALTER TABLE cpf_log_policy ADD COLUMN response_header_capture_mode VARCHAR(30) NOT NULL DEFAULT 'ALLOWLIST';
ALTER TABLE cpf_log_policy ADD COLUMN request_body_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE';
ALTER TABLE cpf_log_policy ADD COLUMN response_body_capture_mode VARCHAR(30) NOT NULL DEFAULT 'NONE';
ALTER TABLE cpf_log_policy ADD COLUMN error_stack_capture_mode VARCHAR(30) NOT NULL DEFAULT 'SUMMARY';
ALTER TABLE cpf_log_policy ADD COLUMN query_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy ADD COLUMN header_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy ADD COLUMN field_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy ADD COLUMN max_query_bytes INTEGER NOT NULL DEFAULT 4096;
ALTER TABLE cpf_log_policy ADD COLUMN max_header_bytes INTEGER NOT NULL DEFAULT 8192;
ALTER TABLE cpf_log_policy ADD COLUMN max_request_body_bytes INTEGER NOT NULL DEFAULT 65536;
ALTER TABLE cpf_log_policy ADD COLUMN max_response_body_bytes INTEGER NOT NULL DEFAULT 65536;
ALTER TABLE cpf_log_policy ADD COLUMN max_stack_bytes INTEGER NOT NULL DEFAULT 32768;
ALTER TABLE cpf_log_policy ADD COLUMN policy_checksum VARCHAR(64) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN policy_schema_version INTEGER NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN query_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN request_header_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN response_header_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN request_body_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN response_body_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN error_stack_capture_mode VARCHAR(30) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN query_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN header_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN field_allowlist VARCHAR(2000) NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_query_bytes INTEGER NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_header_bytes INTEGER NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_request_body_bytes INTEGER NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_response_body_bytes INTEGER NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN max_stack_bytes INTEGER NULL;
ALTER TABLE cpf_log_policy_override ADD COLUMN policy_checksum VARCHAR(64) NULL;
ALTER TABLE cpf_log_policy ALTER COLUMN masking_policy_key SET DEFAULT 'DEFAULT';
UPDATE cpf_log_policy SET masking_policy_key='DEFAULT' WHERE masking_policy_key IS NULL OR masking_policy_key='';
ALTER TABLE cpf_log_policy ALTER COLUMN masking_policy_key SET NOT NULL;
UPDATE cpf_log_policy SET request_body_capture_mode=CASE WHEN request_body_log_yn='Y' THEN 'MASKED_BODY' ELSE 'NONE' END, response_body_capture_mode=CASE WHEN response_body_log_yn='Y' THEN 'MASKED_BODY' ELSE 'NONE' END, error_stack_capture_mode=CASE WHEN error_stack_log_yn='Y' THEN 'FULL_MASKED' ELSE 'NONE' END, policy_checksum=COALESCE(policy_checksum,'MIGRATED_V2_' || policy_id::text);


CREATE TABLE cpf_gateway_transaction_capture_segment (
    gateway_transaction_id VARCHAR(100) NOT NULL,
    segment_type VARCHAR(40) NOT NULL,
    policy_schema_version INTEGER NOT NULL DEFAULT 2,
    policy_checksum VARCHAR(64) NOT NULL,
    captured_value TEXT NOT NULL,
    truncated_yn CHAR(1) NOT NULL DEFAULT 'N',
    metadata_only_yn CHAR(1) NOT NULL DEFAULT 'N',
    observed_bytes BIGINT NOT NULL DEFAULT 0,
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cpf_gateway_transaction_capture_segment PRIMARY KEY (gateway_transaction_id, segment_type),
    CONSTRAINT fk_cpf_gwy_capture_tx FOREIGN KEY (gateway_transaction_id) REFERENCES cpf_gateway_transaction (gateway_transaction_id),
    CONSTRAINT ck_cpf_gwy_capture_truncated CHECK (truncated_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_gwy_capture_metadata CHECK (metadata_only_yn IN ('Y','N'))
);
CREATE INDEX ix_cpf_gwy_capture_time ON cpf_gateway_transaction_capture_segment (captured_at, segment_type);
COMMENT ON TABLE cpf_gateway_transaction_capture_segment IS 'Gateway 정책 기반 Capture Segment 원장';

ALTER TABLE cpf_gateway_server_group ALTER COLUMN hash_key_source DROP NOT NULL;
ALTER TABLE cpf_gateway_server_group ALTER COLUMN hash_key_source DROP DEFAULT;
ALTER TABLE cpf_gateway_server_group ALTER COLUMN failover_group_id DROP NOT NULL;
ALTER TABLE cpf_gateway_server_group ALTER COLUMN failover_group_id DROP DEFAULT;
ALTER TABLE cpf_gateway_server_group ALTER COLUMN health_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_server_group ALTER COLUMN health_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_transaction ADD COLUMN binding_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_transaction ADD COLUMN config_checksum VARCHAR(64);
ALTER TABLE cpf_gateway_transaction ADD COLUMN request_path VARCHAR(1000);
ALTER TABLE cpf_gateway_transaction ADD COLUMN request_method VARCHAR(20);
ALTER TABLE cpf_gateway_transaction ADD COLUMN completed_at TIMESTAMP;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN protocol_status DROP NOT NULL;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN protocol_status DROP DEFAULT;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN business_code DROP NOT NULL;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN business_code DROP DEFAULT;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN source_ip DROP NOT NULL;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN source_ip DROP DEFAULT;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN failure_stage DROP NOT NULL;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN failure_stage DROP DEFAULT;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN channel_id DROP NOT NULL;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN channel_id DROP DEFAULT;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN final_instance_id DROP NOT NULL;
ALTER TABLE cpf_gateway_transaction ALTER COLUMN final_instance_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ADD COLUMN binding_checksum VARCHAR(64);
ALTER TABLE cpf_gateway_binding ADD COLUMN retired_by VARCHAR(100);
ALTER TABLE cpf_gateway_binding ADD COLUMN retired_at TIMESTAMP;
ALTER TABLE cpf_gateway_binding ALTER COLUMN approval_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN approval_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN authentication_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN authentication_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN health_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN health_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN tls_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN tls_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN authorization_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN authorization_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN header_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN header_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN failover_group_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN failover_group_id DROP DEFAULT;
ALTER TABLE cpf_gateway_binding ALTER COLUMN rate_limit_policy_id DROP NOT NULL;
ALTER TABLE cpf_gateway_binding ALTER COLUMN rate_limit_policy_id DROP DEFAULT;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN trace_id DROP NOT NULL;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN trace_id DROP DEFAULT;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN failure_stage DROP NOT NULL;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN failure_stage DROP DEFAULT;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN gateway_instance_id DROP NOT NULL;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN gateway_instance_id DROP DEFAULT;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN instance_id DROP NOT NULL;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN instance_id DROP DEFAULT;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN operation_id DROP NOT NULL;
ALTER TABLE cpf_gateway_connection_test ALTER COLUMN operation_id DROP DEFAULT;
ALTER TABLE cpf_gateway_apply_status ALTER COLUMN error_code DROP NOT NULL;
ALTER TABLE cpf_gateway_apply_status ALTER COLUMN error_code DROP DEFAULT;
ALTER TABLE cpf_gateway_apply_status ALTER COLUMN applied_version DROP NOT NULL;
ALTER TABLE cpf_gateway_apply_status ALTER COLUMN applied_version DROP DEFAULT;
ALTER TABLE cpf_gateway_apply_status ALTER COLUMN error_message DROP NOT NULL;
ALTER TABLE cpf_gateway_apply_status ALTER COLUMN error_message DROP DEFAULT;
ALTER TABLE cpf_gateway_attempt ADD COLUMN selection_reason VARCHAR(100);
ALTER TABLE cpf_gateway_attempt ADD COLUMN unknown_yn CHAR(1) NOT NULL DEFAULT 'N';
ALTER TABLE cpf_gateway_attempt ADD COLUMN gateway_instance_id VARCHAR(100);
ALTER TABLE cpf_gateway_attempt ALTER COLUMN protocol_status DROP NOT NULL;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN protocol_status DROP DEFAULT;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN failure_message DROP NOT NULL;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN failure_message DROP DEFAULT;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN target_host DROP NOT NULL;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN target_host DROP DEFAULT;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN failure_code DROP NOT NULL;
ALTER TABLE cpf_gateway_attempt ALTER COLUMN failure_code DROP DEFAULT;
ALTER TABLE cpf_cache_invalidation_event ALTER COLUMN cache_key DROP NOT NULL;
ALTER TABLE cpf_cache_invalidation_event ALTER COLUMN cache_key DROP DEFAULT;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN canary_percent INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN probe_owner_id VARCHAR(100);
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN consecutive_successes INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN consecutive_failures INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN probe_lease_until TIMESTAMP;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN last_probe_at TIMESTAMP;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN ewma_latency_ms DECIMAL(18,4) NOT NULL DEFAULT 0;
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN last_probe_code VARCHAR(100);
ALTER TABLE cpf_gateway_server_group_member ADD COLUMN active_requests BIGINT NOT NULL DEFAULT 0;

CREATE TABLE cpf_gateway_operation_idempotency (
    operation_id VARCHAR(100) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    result_status VARCHAR(30) NOT NULL,
    result_payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_cpf_gateway_operation_idempotency PRIMARY KEY (operation_id)
);
CREATE INDEX ix_cpf_gwy_operation_resource ON cpf_gateway_operation_idempotency (operation_type, resource_id, created_at);
CREATE INDEX ix_cpf_gwy_operation_expiry ON cpf_gateway_operation_idempotency (expires_at);
COMMENT ON TABLE cpf_gateway_operation_idempotency IS 'Gateway Command 멱등성 원장';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.operation_id IS 'Operation ID';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.operation_type IS 'Operation 유형';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.resource_id IS '대상 ID';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.payload_hash IS 'Payload SHA-256';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.result_status IS '처리 상태';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.result_payload IS '마스킹된 결과';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.created_at IS '생성 시각';
COMMENT ON COLUMN cpf_gateway_operation_idempotency.expires_at IS '멱등 보존 만료';

CREATE TABLE cpf_gateway_spool_checkpoint (
    gateway_instance_id VARCHAR(100) NOT NULL,
    spool_name VARCHAR(100) NOT NULL,
    last_written_sequence BIGINT NOT NULL DEFAULT 0,
    last_ingested_sequence BIGINT NOT NULL DEFAULT 0,
    backlog_count BIGINT NOT NULL DEFAULT 0,
    backlog_bytes BIGINT NOT NULL DEFAULT 0,
    last_error_code VARCHAR(100),
    last_error_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cpf_gateway_spool_checkpoint PRIMARY KEY (gateway_instance_id, spool_name)
);
CREATE INDEX ix_cpf_gwy_spool_backlog ON cpf_gateway_spool_checkpoint (backlog_count, updated_at);
COMMENT ON TABLE cpf_gateway_spool_checkpoint IS 'Gateway Durable Spool 관제 Checkpoint';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.gateway_instance_id IS 'Gateway Instance ID';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.spool_name IS 'Spool 이름';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.last_written_sequence IS '마지막 기록 Sequence';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.last_ingested_sequence IS '마지막 적재 Sequence';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.backlog_count IS '적체 건수';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.backlog_bytes IS '적체 용량';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.last_error_code IS '마지막 오류';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.last_error_at IS '마지막 오류 시각';
COMMENT ON COLUMN cpf_gateway_spool_checkpoint.updated_at IS '갱신 시각';
CREATE OR REPLACE FUNCTION cpf_touch_cpf_gateway_spool_checkpoint() RETURNS trigger AS $$ BEGIN NEW.updated_at = CURRENT_TIMESTAMP; RETURN NEW; END; $$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_cpf_touch_cpf_gateway_spool_checkpoint ON cpf_gateway_spool_checkpoint;
CREATE TRIGGER trg_cpf_touch_cpf_gateway_spool_checkpoint BEFORE UPDATE ON cpf_gateway_spool_checkpoint FOR EACH ROW EXECUTE FUNCTION cpf_touch_cpf_gateway_spool_checkpoint();

CREATE TABLE cpf_gateway_connection_test_operation (
    operation_id VARCHAR(100) NOT NULL,
    binding_id VARCHAR(100) NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    operation_status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    requested_by VARCHAR(100) NOT NULL,
    request_reason VARCHAR(1000) NOT NULL,
    request_payload_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    cancel_requested_yn CHAR(1) NOT NULL DEFAULT 'N',
    result_summary VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    row_version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_cpf_gateway_connection_test_operation PRIMARY KEY (operation_id),
    CONSTRAINT ck_cpf_gwy_test_cancel CHECK (cancel_requested_yn IN ('Y','N')),
    CONSTRAINT fk_cpf_gwy_test_op_binding FOREIGN KEY (binding_id) REFERENCES cpf_gateway_binding (binding_id)
);
CREATE INDEX ix_cpf_gwy_test_op_binding ON cpf_gateway_connection_test_operation (binding_id, created_at);
CREATE INDEX ix_cpf_gwy_test_op_status ON cpf_gateway_connection_test_operation (operation_status, expires_at);
COMMENT ON TABLE cpf_gateway_connection_test_operation IS 'Gateway 비동기 연결시험 Operation';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.operation_id IS '연결시험 Operation ID';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.binding_id IS 'Binding ID';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.test_type IS 'DIRECT/E2E/LB_DISTRIBUTION';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.operation_status IS 'Operation 상태';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.requested_by IS '요청자';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.request_reason IS '요청 사유';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.request_payload_hash IS '요청 Payload Hash';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.expires_at IS 'Operation 만료';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.cancel_requested_yn IS '취소 요청';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.result_summary IS '결과 요약';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.created_at IS '생성 시각';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.started_at IS '시작 시각';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.completed_at IS '완료 시각';
COMMENT ON COLUMN cpf_gateway_connection_test_operation.row_version IS '낙관적 버전';

CREATE TABLE cpf_gateway_health_history (
    health_history_id VARCHAR(100) NOT NULL,
    server_group_id VARCHAR(100) NOT NULL,
    instance_id VARCHAR(100) NOT NULL,
    gateway_instance_id VARCHAR(100) NOT NULL,
    fencing_token BIGINT NOT NULL DEFAULT 0,
    network_status VARCHAR(30) NOT NULL,
    tcp_status VARCHAR(30) NOT NULL,
    tls_status VARCHAR(30) NOT NULL,
    application_status VARCHAR(30) NOT NULL,
    overall_status VARCHAR(30) NOT NULL,
    result_code VARCHAR(100),
    duration_ms BIGINT NOT NULL DEFAULT 0,
    observed_at TIMESTAMP NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cpf_gateway_health_history PRIMARY KEY (health_history_id),
    CONSTRAINT fk_cpf_gwy_health_group FOREIGN KEY (server_group_id) REFERENCES cpf_gateway_server_group (server_group_id),
    CONSTRAINT fk_cpf_gwy_health_instance FOREIGN KEY (instance_id) REFERENCES cpf_service_instance (instance_id)
);
CREATE INDEX ix_cpf_gwy_health_member ON cpf_gateway_health_history (server_group_id, instance_id, observed_at);
CREATE INDEX ix_cpf_gwy_health_status ON cpf_gateway_health_history (overall_status, observed_at);
COMMENT ON TABLE cpf_gateway_health_history IS 'Gateway Protocol 단계별 Health 불변 이력';
COMMENT ON COLUMN cpf_gateway_health_history.health_history_id IS 'Health 이력 ID';
COMMENT ON COLUMN cpf_gateway_health_history.server_group_id IS 'Server Group ID';
COMMENT ON COLUMN cpf_gateway_health_history.instance_id IS 'Instance ID';
COMMENT ON COLUMN cpf_gateway_health_history.gateway_instance_id IS 'Probe Gateway Instance';
COMMENT ON COLUMN cpf_gateway_health_history.fencing_token IS 'Probe Fencing Token';
COMMENT ON COLUMN cpf_gateway_health_history.network_status IS 'Network 상태';
COMMENT ON COLUMN cpf_gateway_health_history.tcp_status IS 'TCP 상태';
COMMENT ON COLUMN cpf_gateway_health_history.tls_status IS 'TLS 상태';
COMMENT ON COLUMN cpf_gateway_health_history.application_status IS 'Application 상태';
COMMENT ON COLUMN cpf_gateway_health_history.overall_status IS '합성 상태';
COMMENT ON COLUMN cpf_gateway_health_history.result_code IS '결과 코드';
COMMENT ON COLUMN cpf_gateway_health_history.duration_ms IS 'Probe 소요시간';
COMMENT ON COLUMN cpf_gateway_health_history.observed_at IS '관측 시각';
COMMENT ON COLUMN cpf_gateway_health_history.recorded_at IS '기록 시각';
