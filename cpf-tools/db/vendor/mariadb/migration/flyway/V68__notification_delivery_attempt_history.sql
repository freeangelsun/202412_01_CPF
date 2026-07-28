CREATE TABLE cpf_notification_delivery_attempt (
    delivery_id BIGINT NOT NULL,
    attempt_no INT NOT NULL,
    operation_id VARCHAR(100) NOT NULL,
    worker_id VARCHAR(100) NOT NULL,
    attempt_status VARCHAR(30) NOT NULL,
    provider_status VARCHAR(80) NULL,
    provider_message VARCHAR(2000) NULL,
    started_at DATETIME(3) NOT NULL,
    completed_at DATETIME(3) NULL,
    lease_version BIGINT NOT NULL,
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cpf_notification_delivery_attempt PRIMARY KEY (delivery_id, attempt_no),
    CONSTRAINT fk_cpf_notification_attempt_delivery FOREIGN KEY (delivery_id)
        REFERENCES cpf_notification_delivery_log (delivery_id) ON DELETE CASCADE
);

CREATE INDEX ix_cpf_notification_attempt_operation
    ON cpf_notification_delivery_attempt (operation_id, attempt_no);
CREATE INDEX ix_cpf_notification_attempt_status
    ON cpf_notification_delivery_attempt (attempt_status, started_at);
