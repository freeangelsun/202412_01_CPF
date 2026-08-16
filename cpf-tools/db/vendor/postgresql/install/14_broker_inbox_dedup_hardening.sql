ALTER TABLE cpf_broker_inbox ADD COLUMN IF NOT EXISTS consumer_identity VARCHAR(120) NOT NULL DEFAULT 'default';
ALTER TABLE cpf_broker_inbox ADD COLUMN IF NOT EXISTS lease_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE cpf_broker_inbox DROP CONSTRAINT IF EXISTS uk_cpf_broker_inbox_message;
ALTER TABLE cpf_broker_inbox DROP CONSTRAINT IF EXISTS uk_cpf_broker_inbox_consumer_message;
ALTER TABLE cpf_broker_inbox ADD CONSTRAINT uk_cpf_broker_inbox_consumer_message UNIQUE (consumer_identity, message_id);
CREATE INDEX IF NOT EXISTS ix_cpf_broker_inbox_retention ON cpf_broker_inbox (inbox_status, updated_at, inbox_id);
