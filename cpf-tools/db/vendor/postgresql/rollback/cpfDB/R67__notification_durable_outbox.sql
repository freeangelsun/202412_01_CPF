DROP INDEX IF EXISTS ix_cpf_notification_delivery_due;
ALTER TABLE cpf_notification_delivery_log DROP CONSTRAINT IF EXISTS uk_cpf_notification_delivery_operation;
ALTER TABLE cpf_notification_delivery_log
    DROP COLUMN IF EXISTS last_error_code,
    DROP COLUMN IF EXISTS version,
    DROP COLUMN IF EXISTS lease_until,
    DROP COLUMN IF EXISTS lease_owner,
    DROP COLUMN IF EXISTS next_attempt_at,
    DROP COLUMN IF EXISTS max_attempts,
    DROP COLUMN IF EXISTS attempt_count,
    DROP COLUMN IF EXISTS payload_body,
    DROP COLUMN IF EXISTS request_hash,
    DROP COLUMN IF EXISTS operation_id;
