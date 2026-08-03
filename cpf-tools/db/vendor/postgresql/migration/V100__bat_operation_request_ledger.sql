-- R4 BAT dangerous-operation approval/idempotency ledger
CREATE TABLE bat_operation_request (
    idempotency_key VARCHAR(120) NOT NULL, request_hash CHAR(64) NOT NULL,
    operation_type VARCHAR(80) NOT NULL, target_type VARCHAR(80) NOT NULL, target_id VARCHAR(200) NOT NULL,
    action_type VARCHAR(100) NOT NULL, approval_request_id VARCHAR(120) NOT NULL, requested_by VARCHAR(50) NOT NULL,
    expected_version BIGINT, request_state VARCHAR(30) NOT NULL DEFAULT 'RESERVED', result_payload TEXT,
    failure_code VARCHAR(80), failure_message VARCHAR(1000), completed_at TIMESTAMP(3),
    created_by VARCHAR(50) NOT NULL DEFAULT 'BAT', created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'BAT', updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_bat_operation_request PRIMARY KEY (idempotency_key),
    CONSTRAINT ck_bat_operation_request_state CHECK (request_state IN ('RESERVED','COMPLETED','FAILED','UNKNOWN')),
    CONSTRAINT ck_bat_operation_request_hash CHECK (CHAR_LENGTH(request_hash)=64)
);
CREATE INDEX ix_bat_operation_request_target ON bat_operation_request(target_type,target_id,created_at);
CREATE INDEX ix_bat_operation_request_state ON bat_operation_request(request_state,updated_at);
