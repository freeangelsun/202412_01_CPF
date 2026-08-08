DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM cpf_broker_inbox GROUP BY message_id HAVING COUNT(*) > 1) THEN
   RAISE EXCEPTION 'Rollback blocked: duplicate message_id exists across consumer identities';
 END IF;
END $$;
DROP INDEX IF EXISTS ix_cpf_broker_inbox_retention;
ALTER TABLE cpf_broker_inbox DROP CONSTRAINT IF EXISTS uk_cpf_broker_inbox_consumer_message;
ALTER TABLE cpf_broker_inbox ADD CONSTRAINT uk_cpf_broker_inbox_message UNIQUE (message_id);
ALTER TABLE cpf_broker_inbox DROP COLUMN IF EXISTS lease_version;
ALTER TABLE cpf_broker_inbox DROP COLUMN IF EXISTS consumer_identity;
