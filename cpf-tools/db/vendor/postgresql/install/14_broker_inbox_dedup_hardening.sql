ALTER TABLE CPF_BROKER_INBOX ADD COLUMN IF NOT EXISTS consumer_identity VARCHAR(120) NOT NULL DEFAULT 'default';
ALTER TABLE CPF_BROKER_INBOX ADD COLUMN IF NOT EXISTS lease_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE CPF_BROKER_INBOX DROP CONSTRAINT IF EXISTS uk_cpf_broker_inbox_message;
ALTER TABLE CPF_BROKER_INBOX DROP CONSTRAINT IF EXISTS uk_cpf_broker_inbox_consumer_message;
ALTER TABLE CPF_BROKER_INBOX ADD CONSTRAINT uk_cpf_broker_inbox_consumer_message UNIQUE (consumer_identity, message_id);
CREATE INDEX IF NOT EXISTS ix_cpf_broker_inbox_retention ON CPF_BROKER_INBOX (inbox_status, updated_at, inbox_id);
