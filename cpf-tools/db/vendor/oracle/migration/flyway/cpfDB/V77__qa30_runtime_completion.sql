-- CPF source-compatible fix migration generated from canonical schemaVersion 37.
-- CPF_LOGICAL_DATABASE=cpfDB
-- Historical V74-V76 are immutable; all post-QA corrections are applied here.

-- QA30-WP07 Versioned Log Capture Policy Schema V2
ALTER TABLE cpf_log_policy ADD (policy_schema_version NUMBER(10) NOT NULL DEFAULT 2);
ALTER TABLE cpf_log_policy ADD (query_capture_mode VARCHAR2(30 CHAR) NOT NULL DEFAULT 'NONE');
ALTER TABLE cpf_log_policy ADD (request_header_capture_mode VARCHAR2(30 CHAR) NOT NULL DEFAULT 'ALLOWLIST');
ALTER TABLE cpf_log_policy ADD (response_header_capture_mode VARCHAR2(30 CHAR) NOT NULL DEFAULT 'ALLOWLIST');
ALTER TABLE cpf_log_policy ADD (request_body_capture_mode VARCHAR2(30 CHAR) NOT NULL DEFAULT 'NONE');
ALTER TABLE cpf_log_policy ADD (response_body_capture_mode VARCHAR2(30 CHAR) NOT NULL DEFAULT 'NONE');
ALTER TABLE cpf_log_policy ADD (error_stack_capture_mode VARCHAR2(30 CHAR) NOT NULL DEFAULT 'SUMMARY');
ALTER TABLE cpf_log_policy ADD (query_allowlist VARCHAR2(2000 CHAR) NULL);
ALTER TABLE cpf_log_policy ADD (header_allowlist VARCHAR2(2000 CHAR) NULL);
ALTER TABLE cpf_log_policy ADD (field_allowlist VARCHAR2(2000 CHAR) NULL);
ALTER TABLE cpf_log_policy ADD (max_query_bytes NUMBER(10) NOT NULL DEFAULT 4096);
ALTER TABLE cpf_log_policy ADD (max_header_bytes NUMBER(10) NOT NULL DEFAULT 8192);
ALTER TABLE cpf_log_policy ADD (max_request_body_bytes NUMBER(10) NOT NULL DEFAULT 65536);
ALTER TABLE cpf_log_policy ADD (max_response_body_bytes NUMBER(10) NOT NULL DEFAULT 65536);
ALTER TABLE cpf_log_policy ADD (max_stack_bytes NUMBER(10) NOT NULL DEFAULT 32768);
ALTER TABLE cpf_log_policy ADD (policy_checksum VARCHAR2(64 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (policy_schema_version NUMBER(10) NULL);
ALTER TABLE cpf_log_policy_override ADD (query_capture_mode VARCHAR2(30 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (request_header_capture_mode VARCHAR2(30 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (response_header_capture_mode VARCHAR2(30 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (request_body_capture_mode VARCHAR2(30 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (response_body_capture_mode VARCHAR2(30 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (error_stack_capture_mode VARCHAR2(30 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (query_allowlist VARCHAR2(2000 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (header_allowlist VARCHAR2(2000 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (field_allowlist VARCHAR2(2000 CHAR) NULL);
ALTER TABLE cpf_log_policy_override ADD (max_query_bytes NUMBER(10) NULL);
ALTER TABLE cpf_log_policy_override ADD (max_header_bytes NUMBER(10) NULL);
ALTER TABLE cpf_log_policy_override ADD (max_request_body_bytes NUMBER(10) NULL);
ALTER TABLE cpf_log_policy_override ADD (max_response_body_bytes NUMBER(10) NULL);
ALTER TABLE cpf_log_policy_override ADD (max_stack_bytes NUMBER(10) NULL);
ALTER TABLE cpf_log_policy_override ADD (policy_checksum VARCHAR2(64 CHAR) NULL);
UPDATE cpf_log_policy SET masking_policy_key='DEFAULT' WHERE masking_policy_key IS NULL;
ALTER TABLE cpf_log_policy MODIFY (masking_policy_key DEFAULT 'DEFAULT' NOT NULL);
UPDATE cpf_log_policy SET request_body_capture_mode=CASE WHEN request_body_log_yn='Y' THEN 'MASKED_BODY' ELSE 'NONE' END, response_body_capture_mode=CASE WHEN response_body_log_yn='Y' THEN 'MASKED_BODY' ELSE 'NONE' END, error_stack_capture_mode=CASE WHEN error_stack_log_yn='Y' THEN 'FULL_MASKED' ELSE 'NONE' END, policy_checksum=NVL(policy_checksum,'MIGRATED_V2_' || TO_CHAR(policy_id));


CREATE TABLE cpf_gateway_transaction_capture_segment (
    gateway_transaction_id VARCHAR2(100 CHAR) NOT NULL,
    segment_type VARCHAR2(40 CHAR) NOT NULL,
    policy_schema_version NUMBER(10) NOT NULL DEFAULT 2,
    policy_checksum VARCHAR2(64 CHAR) NOT NULL,
    captured_value CLOB NOT NULL,
    truncated_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    metadata_only_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    observed_bytes NUMBER(19) NOT NULL DEFAULT 0,
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cpf_gateway_transaction_capture_segment PRIMARY KEY (gateway_transaction_id, segment_type),
    CONSTRAINT fk_cpf_gwy_capture_tx FOREIGN KEY (gateway_transaction_id) REFERENCES cpf_gateway_transaction (gateway_transaction_id),
    CONSTRAINT ck_cpf_gwy_capture_truncated CHECK (truncated_yn IN ('Y','N')),
    CONSTRAINT ck_cpf_gwy_capture_metadata CHECK (metadata_only_yn IN ('Y','N'))
);
CREATE INDEX ix_cpf_gwy_capture_time ON cpf_gateway_transaction_capture_segment (captured_at, segment_type);
COMMENT ON TABLE cpf_gateway_transaction_capture_segment IS 'Gateway 정책 기반 Capture Segment 원장';

ALTER TABLE cpf_gateway_server_group MODIFY (hash_key_source NULL);
ALTER TABLE cpf_gateway_server_group MODIFY (failover_group_id NULL);
ALTER TABLE cpf_gateway_server_group MODIFY (health_policy_id NULL);
ALTER TABLE cpf_gateway_transaction ADD (binding_version NUMBER(19) NOT NULL DEFAULT 0);
ALTER TABLE cpf_gateway_transaction ADD (config_checksum VARCHAR2(64 CHAR));
ALTER TABLE cpf_gateway_transaction ADD (request_path VARCHAR2(1000 CHAR));
ALTER TABLE cpf_gateway_transaction ADD (request_method VARCHAR2(20 CHAR));
ALTER TABLE cpf_gateway_transaction ADD (completed_at TIMESTAMP);
ALTER TABLE cpf_gateway_transaction MODIFY (protocol_status NULL);
ALTER TABLE cpf_gateway_transaction MODIFY (business_code NULL);
ALTER TABLE cpf_gateway_transaction MODIFY (source_ip NULL);
ALTER TABLE cpf_gateway_transaction MODIFY (failure_stage NULL);
ALTER TABLE cpf_gateway_transaction MODIFY (channel_id NULL);
ALTER TABLE cpf_gateway_transaction MODIFY (final_instance_id NULL);
ALTER TABLE cpf_gateway_binding ADD (binding_checksum VARCHAR2(64 CHAR));
ALTER TABLE cpf_gateway_binding ADD (retired_by VARCHAR2(100 CHAR));
ALTER TABLE cpf_gateway_binding ADD (retired_at TIMESTAMP);
ALTER TABLE cpf_gateway_binding MODIFY (approval_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (authentication_policy_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (health_policy_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (tls_policy_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (authorization_policy_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (header_policy_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (failover_group_id NULL);
ALTER TABLE cpf_gateway_binding MODIFY (rate_limit_policy_id NULL);
ALTER TABLE cpf_gateway_connection_test MODIFY (trace_id NULL);
ALTER TABLE cpf_gateway_connection_test MODIFY (failure_stage NULL);
ALTER TABLE cpf_gateway_connection_test MODIFY (gateway_instance_id NULL);
ALTER TABLE cpf_gateway_connection_test MODIFY (instance_id NULL);
ALTER TABLE cpf_gateway_connection_test MODIFY (operation_id NULL);
ALTER TABLE cpf_gateway_apply_status MODIFY (error_code NULL);
ALTER TABLE cpf_gateway_apply_status MODIFY (applied_version NULL);
ALTER TABLE cpf_gateway_apply_status MODIFY (error_message NULL);
ALTER TABLE cpf_gateway_attempt ADD (selection_reason VARCHAR2(100 CHAR));
ALTER TABLE cpf_gateway_attempt ADD (unknown_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N');
ALTER TABLE cpf_gateway_attempt ADD (gateway_instance_id VARCHAR2(100 CHAR));
ALTER TABLE cpf_gateway_attempt MODIFY (protocol_status NULL);
ALTER TABLE cpf_gateway_attempt MODIFY (failure_message NULL);
ALTER TABLE cpf_gateway_attempt MODIFY (target_host NULL);
ALTER TABLE cpf_gateway_attempt MODIFY (failure_code NULL);
ALTER TABLE cpf_cache_invalidation_event MODIFY (cache_key NULL);
ALTER TABLE cpf_gateway_server_group_member ADD (canary_percent NUMBER(10) NOT NULL DEFAULT 0);
ALTER TABLE cpf_gateway_server_group_member ADD (probe_owner_id VARCHAR2(100 CHAR));
ALTER TABLE cpf_gateway_server_group_member ADD (consecutive_successes NUMBER(10) NOT NULL DEFAULT 0);
ALTER TABLE cpf_gateway_server_group_member ADD (consecutive_failures NUMBER(10) NOT NULL DEFAULT 0);
ALTER TABLE cpf_gateway_server_group_member ADD (probe_lease_until TIMESTAMP);
ALTER TABLE cpf_gateway_server_group_member ADD (last_probe_at TIMESTAMP);
ALTER TABLE cpf_gateway_server_group_member ADD (ewma_latency_ms DECIMAL(18,4) NOT NULL DEFAULT 0);
ALTER TABLE cpf_gateway_server_group_member ADD (last_probe_code VARCHAR2(100 CHAR));
ALTER TABLE cpf_gateway_server_group_member ADD (active_requests NUMBER(19) NOT NULL DEFAULT 0);

CREATE TABLE cpf_gateway_operation_idempotency (
    operation_id VARCHAR2(100 CHAR) NOT NULL,
    operation_type VARCHAR2(50 CHAR) NOT NULL,
    resource_id VARCHAR2(100 CHAR) NOT NULL,
    payload_hash VARCHAR2(64 CHAR) NOT NULL,
    result_status VARCHAR2(30 CHAR) NOT NULL,
    result_payload CLOB,
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
    gateway_instance_id VARCHAR2(100 CHAR) NOT NULL,
    spool_name VARCHAR2(100 CHAR) NOT NULL,
    last_written_sequence NUMBER(19) NOT NULL DEFAULT 0,
    last_ingested_sequence NUMBER(19) NOT NULL DEFAULT 0,
    backlog_count NUMBER(19) NOT NULL DEFAULT 0,
    backlog_bytes NUMBER(19) NOT NULL DEFAULT 0,
    last_error_code VARCHAR2(100 CHAR),
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
CREATE OR REPLACE TRIGGER trg_touch_cpf_gateway_spool_checkpoint BEFORE UPDATE ON cpf_gateway_spool_checkpoint FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE cpf_gateway_connection_test_operation (
    operation_id VARCHAR2(100 CHAR) NOT NULL,
    binding_id VARCHAR2(100 CHAR) NOT NULL,
    test_type VARCHAR2(50 CHAR) NOT NULL,
    operation_status VARCHAR2(30 CHAR) NOT NULL DEFAULT 'REQUESTED',
    requested_by VARCHAR2(100 CHAR) NOT NULL,
    request_reason VARCHAR2(1000 CHAR) NOT NULL,
    request_payload_hash VARCHAR2(64 CHAR) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    cancel_requested_yn CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    result_summary VARCHAR2(2000 CHAR),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    row_version NUMBER(19) NOT NULL DEFAULT 1,
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
    health_history_id VARCHAR2(100 CHAR) NOT NULL,
    server_group_id VARCHAR2(100 CHAR) NOT NULL,
    instance_id VARCHAR2(100 CHAR) NOT NULL,
    gateway_instance_id VARCHAR2(100 CHAR) NOT NULL,
    fencing_token NUMBER(19) NOT NULL DEFAULT 0,
    network_status VARCHAR2(30 CHAR) NOT NULL,
    tcp_status VARCHAR2(30 CHAR) NOT NULL,
    tls_status VARCHAR2(30 CHAR) NOT NULL,
    application_status VARCHAR2(30 CHAR) NOT NULL,
    overall_status VARCHAR2(30 CHAR) NOT NULL,
    result_code VARCHAR2(100 CHAR),
    duration_ms NUMBER(19) NOT NULL DEFAULT 0,
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
