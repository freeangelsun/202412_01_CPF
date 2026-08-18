-- V125: CPF durable Async Operation runtime.
USE cpfDB;
CREATE TABLE OPS_ASYNC_OPERATION (
    execution_id VARCHAR(160) NOT NULL,
    operation_id VARCHAR(160) NOT NULL,
    transaction_id CHAR(34) NOT NULL,
    idempotency_key VARCHAR(256) NOT NULL,
    command_type VARCHAR(300) NOT NULL,
    command_payload TEXT NOT NULL,
    context_payload TEXT NOT NULL,
    result_type VARCHAR(300) NOT NULL,
    result_payload TEXT NULL,
    state VARCHAR(30) DEFAULT 'ACCEPTED' NOT NULL,
    result_status VARCHAR(30) NULL,
    error_code VARCHAR(120) NULL,
    error_message VARCHAR(2000) NULL,
    recovery_id VARCHAR(160) NULL,
    recovery_action VARCHAR(120) NULL,
    submitted_at DATETIME(3) NOT NULL,
    started_at DATETIME(3) NULL,
    updated_at DATETIME(3) NOT NULL,
    completed_at DATETIME(3) NULL,
    expires_at DATETIME(3) NOT NULL,
    heartbeat_at DATETIME(3) NULL,
    lease_owner VARCHAR(160) NULL,
    lease_until DATETIME(3) NULL,
    cancellation_reason VARCHAR(500) NULL,
    version BIGINT DEFAULT 1 NOT NULL,
    CONSTRAINT PK_OPS_ASYNC_OPERATION PRIMARY KEY (execution_id),
    CONSTRAINT uk_ops_async_operation_idempotency UNIQUE (operation_id, idempotency_key),
    CONSTRAINT ck_ops_async_operation_state CHECK (state IN ('ACCEPTED','RUNNING','SUCCEEDED','FAILED','UNKNOWN','CANCEL_REQUESTED','CANCELLED','EXPIRED')),
    CONSTRAINT ck_ops_async_operation_result CHECK (result_status IS NULL OR result_status IN ('SUCCESS','BUSINESS_FAILURE','TECHNICAL_FAILURE','UNKNOWN','CANCELLED'))
) ENGINE=InnoDB;
CREATE INDEX ix_ops_async_operation_state ON OPS_ASYNC_OPERATION (state, submitted_at);
CREATE INDEX ix_ops_async_operation_tx ON OPS_ASYNC_OPERATION (transaction_id, submitted_at);
CREATE INDEX ix_ops_async_operation_lease ON OPS_ASYNC_OPERATION (state, lease_until);
CREATE INDEX ix_ops_async_operation_expiry ON OPS_ASYNC_OPERATION (expires_at, state);

