CREATE TABLE cpf_runtime_policy_event (
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id VARCHAR(200) NOT NULL,
    aggregate_version BIGINT NOT NULL,
    action_code VARCHAR(50) NOT NULL,
    payload_checksum VARCHAR(128) NULL,
    metadata_text TEXT NULL,
    reason VARCHAR(1000) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    occurred_at DATETIME(3) NOT NULL,
    event_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (event_id),
    CONSTRAINT ck_cpf_runtime_policy_event_status CHECK (event_status IN ('PENDING','RETIRED'))
);
CREATE INDEX ix_cpf_runtime_policy_event_pending ON cpf_runtime_policy_event(event_status,event_type,occurred_at,event_id);
CREATE INDEX ix_cpf_runtime_policy_event_aggregate ON cpf_runtime_policy_event(aggregate_type,aggregate_id,aggregate_version);

CREATE TABLE cpf_runtime_policy_delivery (
    event_id VARCHAR(64) NOT NULL,
    consumer_id VARCHAR(100) NOT NULL,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    fencing_token BIGINT NOT NULL DEFAULT 0,
    leased_until DATETIME(3) NULL,
    error_code VARCHAR(100) NULL,
    error_message VARCHAR(1000) NULL,
    acknowledged_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (event_id,consumer_id),
    CONSTRAINT fk_cpf_runtime_policy_delivery_event FOREIGN KEY (event_id) REFERENCES cpf_runtime_policy_event(event_id) ON DELETE CASCADE,
    CONSTRAINT ck_cpf_runtime_policy_delivery_status CHECK (delivery_status IN ('PENDING','CLAIMED','APPLIED','FAILED','IGNORED')),
    CONSTRAINT ck_cpf_runtime_policy_delivery_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_cpf_runtime_policy_delivery_fencing CHECK (fencing_token >= 0)
);
CREATE INDEX ix_cpf_runtime_policy_delivery_status ON cpf_runtime_policy_delivery(consumer_id,delivery_status,leased_until,updated_at);
