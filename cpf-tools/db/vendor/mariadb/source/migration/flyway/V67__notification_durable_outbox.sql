ALTER TABLE cpf_notification_delivery_log
    ADD COLUMN operation_id VARCHAR(100) NULL,
    ADD COLUMN request_hash VARCHAR(64) NULL,
    ADD COLUMN payload_body VARCHAR(2000) NULL,
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN max_attempts INT NOT NULL DEFAULT 3,
    ADD COLUMN next_attempt_at DATETIME(3) NULL,
    ADD COLUMN lease_owner VARCHAR(100) NULL,
    ADD COLUMN lease_until DATETIME(3) NULL,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN last_error_code VARCHAR(80) NULL;

UPDATE cpf_notification_delivery_log
SET operation_id = CONCAT('LEGACY-', delivery_id),
    request_hash = SHA2(CONCAT('LEGACY-', delivery_id), 256),
    payload_body = COALESCE(delivery_message, ''),
    next_attempt_at = requested_at
WHERE operation_id IS NULL;

ALTER TABLE cpf_notification_delivery_log
    MODIFY operation_id VARCHAR(100) NOT NULL,
    MODIFY request_hash VARCHAR(64) NOT NULL,
    MODIFY payload_body VARCHAR(2000) NOT NULL,
    ADD CONSTRAINT uk_cpf_notification_delivery_operation UNIQUE (operation_id);

CREATE INDEX ix_cpf_notification_delivery_due
    ON cpf_notification_delivery_log (delivery_status, next_attempt_at, lease_until);
