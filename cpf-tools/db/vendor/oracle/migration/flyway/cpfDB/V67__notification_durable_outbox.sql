ALTER TABLE cpf_notification_delivery_log ADD (
    operation_id VARCHAR2(100),
    request_hash VARCHAR2(64),
    payload_body VARCHAR2(2000),
    attempt_count NUMBER(10) DEFAULT 0 NOT NULL,
    max_attempts NUMBER(10) DEFAULT 3 NOT NULL,
    next_attempt_at TIMESTAMP(3),
    lease_owner VARCHAR2(100),
    lease_until TIMESTAMP(3),
    version NUMBER(19) DEFAULT 0 NOT NULL,
    last_error_code VARCHAR2(80)
);

UPDATE cpf_notification_delivery_log
SET operation_id = 'LEGACY-' || TO_CHAR(delivery_id),
    request_hash = LOWER(RAWTOHEX(STANDARD_HASH('LEGACY-' || TO_CHAR(delivery_id), 'SHA256'))),
    payload_body = NVL(delivery_message, ''),
    next_attempt_at = requested_at
WHERE operation_id IS NULL;

ALTER TABLE cpf_notification_delivery_log MODIFY (
    operation_id NOT NULL,
    request_hash NOT NULL,
    payload_body NOT NULL
);
ALTER TABLE cpf_notification_delivery_log
    ADD CONSTRAINT uk_cpf_notification_delivery_operation UNIQUE (operation_id);

CREATE INDEX ix_cpf_notification_delivery_due
    ON cpf_notification_delivery_log (delivery_status, next_attempt_at, lease_until);
