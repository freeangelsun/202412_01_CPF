CREATE TABLE cpf_broker_outbox (
    outbox_id BIGINT NOT NULL AUTO_INCREMENT,

    message_id VARCHAR(128) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255),
    transaction_id VARCHAR(64) NOT NULL,
    segment_id VARCHAR(64),
    producer_module VARCHAR(64) NOT NULL,
    consumer_module VARCHAR(64),
    idempotency_key VARCHAR(255) NOT NULL,
    payload LONGBLOB NOT NULL,
    content_type VARCHAR(128),
    header_json LONGTEXT,
    attribute_json LONGTEXT,
    outbox_status VARCHAR(32) NOT NULL,
    attempt_count INTEGER DEFAULT 0 NOT NULL,
    max_attempts INTEGER DEFAULT 5 NOT NULL,
    next_attempt_at TIMESTAMP,
    worker_id VARCHAR(128),
    claimed_at TIMESTAMP,
    lease_until TIMESTAMP,
    broker_name VARCHAR(64),
    partition_key VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP,
    failure_message VARCHAR(2000),
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
,    CONSTRAINT pk_cpf_broker_outbox PRIMARY KEY (outbox_id),
    CONSTRAINT uk_cpf_broker_outbox_message UNIQUE (message_id)
);
CREATE INDEX ix_cpf_broker_outbox_claim ON cpf_broker_outbox(outbox_status,next_attempt_at,lease_until);
CREATE INDEX ix_cpf_broker_outbox_tx ON cpf_broker_outbox(transaction_id,occurred_at);

CREATE TABLE cpf_broker_inbox (
    inbox_id BIGINT NOT NULL AUTO_INCREMENT,
    message_id VARCHAR(128) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    inbox_status VARCHAR(32) NOT NULL,
    received_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    result_detail VARCHAR(2000),
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cpf_broker_inbox PRIMARY KEY (inbox_id),
    CONSTRAINT uk_cpf_broker_inbox_message UNIQUE (message_id),
    CONSTRAINT uk_cpf_broker_inbox_idem UNIQUE (idempotency_key)
);

CREATE TABLE cpf_broker_dlq (
    dlq_id BIGINT NOT NULL AUTO_INCREMENT,
    message_id VARCHAR(128) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(64),
    segment_id VARCHAR(64),
    failure_reason VARCHAR(2000),
    replay_status VARCHAR(32) NOT NULL,
    replay_count INTEGER DEFAULT 0 NOT NULL,
    replay_requested_at TIMESTAMP,
    replay_completed_at TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cpf_broker_dlq PRIMARY KEY (dlq_id),
    CONSTRAINT uk_cpf_broker_dlq_message UNIQUE (message_id)
);
CREATE INDEX ix_cpf_broker_dlq_status ON cpf_broker_dlq(replay_status,created_at);
