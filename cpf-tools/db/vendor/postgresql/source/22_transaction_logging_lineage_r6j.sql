-- CPF R6J transaction lineage. Raw payload/secret values are prohibited.
-- occurred_at is the canonical monthly partition key; the DEFAULT partition prevents write loss.
CREATE TABLE cpf_transaction_lineage (
    lineage_id VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(128) NOT NULL,
    segment_id VARCHAR(128) NOT NULL,
    parent_segment_id VARCHAR(128),
    attempt_no INTEGER DEFAULT 1 NOT NULL,
    trace_id VARCHAR(128),
    span_id VARCHAR(128),
    request_id VARCHAR(128),
    idempotency_key VARCHAR(160),
    tenant_id VARCHAR(128),
    channel_code VARCHAR(64),
    actor_id_masked VARCHAR(256),
    instance_id VARCHAR(128),
    was_id VARCHAR(128),
    agent_id VARCHAR(128),
    worker_id VARCHAR(128),
    remote_system VARCHAR(128),
    operation_id VARCHAR(160),
    message_id VARCHAR(160),
    consumer_group VARCHAR(160),
    dlq_id VARCHAR(160),
    batch_job_instance_id VARCHAR(128),
    batch_job_execution_id VARCHAR(128),
    batch_step_execution_id VARCHAR(128),
    partition_id VARCHAR(128),
    file_id VARCHAR(160),
    source_type VARCHAR(32) NOT NULL,
    source_ref_id VARCHAR(256),
    lifecycle_state VARCHAR(32) NOT NULL,
    failure_stage VARCHAR(128),
    unknown_yn CHAR(1) DEFAULT 'N' NOT NULL,
    reconcile_state VARCHAR(32),
    occurred_at TIMESTAMP NOT NULL,
    freshness_at TIMESTAMP NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    archived_at TIMESTAMP,
    CONSTRAINT pk_cpf_tx_lineage PRIMARY KEY (lineage_id, occurred_at),
    CONSTRAINT uk_cpf_tx_lineage_event UNIQUE (transaction_id, segment_id, attempt_no, source_type, payload_hash, occurred_at)
) PARTITION BY RANGE (occurred_at);
CREATE TABLE cpf_transaction_lineage_default PARTITION OF cpf_transaction_lineage DEFAULT;

CREATE TABLE cpf_transaction_lineage_archive (
    lineage_id VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(128) NOT NULL,
    segment_id VARCHAR(128) NOT NULL,
    parent_segment_id VARCHAR(128),
    attempt_no INTEGER DEFAULT 1 NOT NULL,
    trace_id VARCHAR(128),
    span_id VARCHAR(128),
    request_id VARCHAR(128),
    idempotency_key VARCHAR(160),
    tenant_id VARCHAR(128),
    channel_code VARCHAR(64),
    actor_id_masked VARCHAR(256),
    instance_id VARCHAR(128),
    was_id VARCHAR(128),
    agent_id VARCHAR(128),
    worker_id VARCHAR(128),
    remote_system VARCHAR(128),
    operation_id VARCHAR(160),
    message_id VARCHAR(160),
    consumer_group VARCHAR(160),
    dlq_id VARCHAR(160),
    batch_job_instance_id VARCHAR(128),
    batch_job_execution_id VARCHAR(128),
    batch_step_execution_id VARCHAR(128),
    partition_id VARCHAR(128),
    file_id VARCHAR(160),
    source_type VARCHAR(32) NOT NULL,
    source_ref_id VARCHAR(256),
    lifecycle_state VARCHAR(32) NOT NULL,
    failure_stage VARCHAR(128),
    unknown_yn CHAR(1) DEFAULT 'N' NOT NULL,
    reconcile_state VARCHAR(32),
    occurred_at TIMESTAMP NOT NULL,
    freshness_at TIMESTAMP NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    archived_at TIMESTAMP,
    archive_reason VARCHAR(64) NOT NULL,
    archived_by VARCHAR(128) NOT NULL,
    CONSTRAINT pk_cpf_tx_lineage_arch PRIMARY KEY (lineage_id, occurred_at)
);

CREATE INDEX idx_cpf_tx_lineage_tx_time ON cpf_transaction_lineage (transaction_id, occurred_at, segment_id, attempt_no);
CREATE INDEX idx_cpf_tx_lineage_trace ON cpf_transaction_lineage (trace_id, span_id, occurred_at);
CREATE INDEX idx_cpf_tx_lineage_request ON cpf_transaction_lineage (request_id, idempotency_key, occurred_at);
CREATE INDEX idx_cpf_tx_lineage_message ON cpf_transaction_lineage (message_id, consumer_group, dlq_id, occurred_at);
CREATE INDEX idx_cpf_tx_lineage_batch ON cpf_transaction_lineage (batch_job_instance_id, batch_job_execution_id, batch_step_execution_id, occurred_at);
CREATE INDEX idx_cpf_tx_lineage_file ON cpf_transaction_lineage (file_id, source_type, source_ref_id, occurred_at);
CREATE INDEX idx_cpf_tx_lineage_retention ON cpf_transaction_lineage (archived_at, occurred_at);
CREATE INDEX idx_cpf_tx_lineage_arch_tx ON cpf_transaction_lineage_archive (transaction_id, occurred_at);
