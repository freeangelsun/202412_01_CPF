-- R4 BAT dangerous-operation approval/idempotency ledger
CREATE TABLE bat_operation_request (
    idempotency_key VARCHAR2(120 CHAR) NOT NULL, request_hash CHAR(64 CHAR) NOT NULL,
    operation_type VARCHAR2(80 CHAR) NOT NULL, target_type VARCHAR2(80 CHAR) NOT NULL, target_id VARCHAR2(200 CHAR) NOT NULL,
    action_type VARCHAR2(100 CHAR) NOT NULL, approval_request_id VARCHAR2(120 CHAR) NOT NULL, requested_by VARCHAR2(50 CHAR) NOT NULL,
    expected_version NUMBER(19), request_state VARCHAR2(30 CHAR) DEFAULT 'RESERVED' NOT NULL, result_payload CLOB,
    failure_code VARCHAR2(80 CHAR), failure_message VARCHAR2(1000 CHAR), completed_at TIMESTAMP(3),
    created_by VARCHAR2(50 CHAR) DEFAULT 'BAT' NOT NULL, created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR2(50 CHAR) DEFAULT 'BAT' NOT NULL, updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_bat_operation_request PRIMARY KEY (idempotency_key),
    CONSTRAINT ck_bat_operation_request_state CHECK (request_state IN ('RESERVED','COMPLETED','FAILED','UNKNOWN')),
    CONSTRAINT ck_bat_operation_request_hash CHECK (LENGTH(request_hash)=64)
);
CREATE INDEX ix_bat_operation_request_target ON bat_operation_request(target_type,target_id,created_at);
CREATE INDEX ix_bat_operation_request_state ON bat_operation_request(request_state,updated_at);
