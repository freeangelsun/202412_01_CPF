CREATE TABLE cpf_notification_delivery_attempt (
    delivery_id NUMBER(19) NOT NULL,
    attempt_no NUMBER(10) NOT NULL,
    operation_id VARCHAR2(100) NOT NULL,
    worker_id VARCHAR2(100) NOT NULL,
    attempt_status VARCHAR2(30) NOT NULL,
    provider_status VARCHAR2(80),
    provider_message VARCHAR2(2000),
    started_at TIMESTAMP(3) NOT NULL,
    completed_at TIMESTAMP(3),
    lease_version NUMBER(19) NOT NULL,
    created_by VARCHAR2(100) DEFAULT 'CPF' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT pk_cpf_notification_delivery_attempt PRIMARY KEY (delivery_id, attempt_no),
    CONSTRAINT fk_cpf_notification_attempt_delivery FOREIGN KEY (delivery_id)
        REFERENCES cpf_notification_delivery_log (delivery_id) ON DELETE CASCADE
);

CREATE INDEX ix_cpf_notification_attempt_operation
    ON cpf_notification_delivery_attempt (operation_id, attempt_no);
CREATE INDEX ix_cpf_notification_attempt_status
    ON cpf_notification_delivery_attempt (attempt_status, started_at);
