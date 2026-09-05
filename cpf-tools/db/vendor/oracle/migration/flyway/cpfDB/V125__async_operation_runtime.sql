-- V125: CPF durable Async Operation runtime.
CREATE TABLE OPS_ASYNC_OPERATION (
    execution_id VARCHAR2(160 CHAR) NOT NULL,
    operation_id VARCHAR2(160 CHAR) NOT NULL,
    transaction_id CHAR(34 CHAR) NOT NULL,
    idempotency_key VARCHAR2(256 CHAR) NOT NULL,
    command_type VARCHAR2(300 CHAR) NOT NULL,
    command_payload CLOB NOT NULL,
    context_payload CLOB NOT NULL,
    result_type VARCHAR2(300 CHAR) NOT NULL,
    result_payload CLOB NULL,
    state VARCHAR2(30 CHAR) DEFAULT 'ACCEPTED' NOT NULL,
    result_status VARCHAR2(30 CHAR) NULL,
    error_code VARCHAR2(120 CHAR) NULL,
    error_message VARCHAR2(2000 CHAR) NULL,
    recovery_id VARCHAR2(160 CHAR) NULL,
    recovery_action VARCHAR2(120 CHAR) NULL,
    submitted_at TIMESTAMP(3) NOT NULL,
    started_at TIMESTAMP(3) NULL,
    updated_at TIMESTAMP(3) NOT NULL,
    completed_at TIMESTAMP(3) NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    heartbeat_at TIMESTAMP(3) NULL,
    lease_owner VARCHAR2(160 CHAR) NULL,
    lease_until TIMESTAMP(3) NULL,
    cancellation_reason VARCHAR2(500 CHAR) NULL,
    version NUMBER(19) DEFAULT 1 NOT NULL,
    CONSTRAINT PK_OPS_ASYNC_OPERATION PRIMARY KEY (execution_id),
    CONSTRAINT uk_ops_async_operation_idempotency UNIQUE (operation_id, idempotency_key),
    CONSTRAINT ck_ops_async_operation_state CHECK (state IN ('ACCEPTED','RUNNING','SUCCEEDED','FAILED','UNKNOWN','CANCEL_REQUESTED','CANCELLED','EXPIRED')),
    CONSTRAINT ck_ops_async_operation_result CHECK (result_status IS NULL OR result_status IN ('SUCCESS','BUSINESS_FAILURE','TECHNICAL_FAILURE','UNKNOWN','CANCELLED'))
);
CREATE INDEX ix_ops_async_operation_state ON OPS_ASYNC_OPERATION (state, submitted_at);
CREATE INDEX ix_ops_async_operation_tx ON OPS_ASYNC_OPERATION (transaction_id, submitted_at);
CREATE INDEX ix_ops_async_operation_lease ON OPS_ASYNC_OPERATION (state, lease_until);
CREATE INDEX ix_ops_async_operation_expiry ON OPS_ASYNC_OPERATION (expires_at, state);
