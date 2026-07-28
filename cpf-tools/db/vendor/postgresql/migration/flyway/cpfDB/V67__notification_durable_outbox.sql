ALTER TABLE cpf_notification_delivery_log
    ADD COLUMN operation_id VARCHAR(100),
    ADD COLUMN request_hash VARCHAR(64),
    ADD COLUMN payload_body VARCHAR(2000),
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN max_attempts INTEGER NOT NULL DEFAULT 3,
    ADD COLUMN next_attempt_at TIMESTAMP(3),
    ADD COLUMN lease_owner VARCHAR(100),
    ADD COLUMN lease_until TIMESTAMP(3),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN last_error_code VARCHAR(80);

UPDATE cpf_notification_delivery_log
SET operation_id = 'LEGACY-' || delivery_id,
    request_hash = md5('LEGACY-' || delivery_id) || md5('CPF-' || delivery_id),
    payload_body = COALESCE(delivery_message, ''),
    next_attempt_at = requested_at
WHERE operation_id IS NULL;

ALTER TABLE cpf_notification_delivery_log
    ALTER COLUMN operation_id SET NOT NULL,
    ALTER COLUMN request_hash SET NOT NULL,
    ALTER COLUMN payload_body SET NOT NULL,
    ADD CONSTRAINT uk_cpf_notification_delivery_operation UNIQUE (operation_id);

CREATE INDEX ix_cpf_notification_delivery_due
    ON cpf_notification_delivery_log (delivery_status, next_attempt_at, lease_until);
