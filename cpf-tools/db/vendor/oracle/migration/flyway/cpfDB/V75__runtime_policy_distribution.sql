CREATE TABLE cpf_runtime_policy_event (
    event_id VARCHAR2(64 CHAR) NOT NULL,
    event_type VARCHAR2(50 CHAR) NOT NULL,
    aggregate_type VARCHAR2(80 CHAR) NOT NULL,
    aggregate_id VARCHAR2(200 CHAR) NOT NULL,
    aggregate_version NUMBER(19) NOT NULL,
    action_code VARCHAR2(50 CHAR) NOT NULL,
    payload_checksum VARCHAR2(128 CHAR),
    metadata_text CLOB,
    reason VARCHAR2(1000 CHAR) NOT NULL,
    requested_by VARCHAR2(100 CHAR) NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    event_status VARCHAR2(30 CHAR) DEFAULT 'PENDING' NOT NULL,
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cpf_runtime_policy_event PRIMARY KEY (event_id),
    CONSTRAINT ck_cpf_runtime_policy_event_status CHECK (event_status IN ('PENDING','RETIRED'))
);
CREATE INDEX ix_cpf_runtime_policy_event_pending ON cpf_runtime_policy_event(event_status,event_type,occurred_at,event_id);
CREATE INDEX ix_cpf_runtime_policy_event_aggregate ON cpf_runtime_policy_event(aggregate_type,aggregate_id,aggregate_version);
CREATE TABLE cpf_runtime_policy_delivery (
    event_id VARCHAR2(64 CHAR) NOT NULL,
    consumer_id VARCHAR2(100 CHAR) NOT NULL,
    delivery_status VARCHAR2(30 CHAR) DEFAULT 'PENDING' NOT NULL,
    attempt_count NUMBER(10) DEFAULT 0 NOT NULL,
    fencing_token NUMBER(19) DEFAULT 0 NOT NULL,
    leased_until TIMESTAMP(3),
    error_code VARCHAR2(100 CHAR),
    error_message VARCHAR2(1000 CHAR),
    acknowledged_at TIMESTAMP(3),
    created_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cpf_runtime_policy_delivery PRIMARY KEY (event_id,consumer_id),
    CONSTRAINT fk_cpf_runtime_policy_delivery_event FOREIGN KEY (event_id) REFERENCES cpf_runtime_policy_event(event_id) ON DELETE CASCADE,
    CONSTRAINT ck_cpf_runtime_policy_delivery_status CHECK (delivery_status IN ('PENDING','CLAIMED','APPLIED','FAILED','IGNORED')),
    CONSTRAINT ck_cpf_runtime_policy_delivery_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_cpf_runtime_policy_delivery_fencing CHECK (fencing_token >= 0)
);
CREATE INDEX ix_cpf_runtime_policy_delivery_status ON cpf_runtime_policy_delivery(consumer_id,delivery_status,leased_until,updated_at);
