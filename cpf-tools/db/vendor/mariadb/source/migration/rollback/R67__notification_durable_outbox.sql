DROP INDEX ix_cpf_notification_delivery_due ON cpf_notification_delivery_log;
ALTER TABLE cpf_notification_delivery_log
    DROP INDEX uk_cpf_notification_delivery_operation,
    DROP COLUMN last_error_code,
    DROP COLUMN version,
    DROP COLUMN lease_until,
    DROP COLUMN lease_owner,
    DROP COLUMN next_attempt_at,
    DROP COLUMN max_attempts,
    DROP COLUMN attempt_count,
    DROP COLUMN payload_body,
    DROP COLUMN request_hash,
    DROP COLUMN operation_id;
