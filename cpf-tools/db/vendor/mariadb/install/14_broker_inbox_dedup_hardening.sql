ALTER TABLE cpf_broker_inbox ADD COLUMN IF NOT EXISTS consumer_identity VARCHAR(120) NOT NULL DEFAULT 'default';
ALTER TABLE cpf_broker_inbox ADD COLUMN IF NOT EXISTS lease_version BIGINT NOT NULL DEFAULT 0;
SET @has_old := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='cpf_broker_inbox' AND index_name='uk_cpf_broker_inbox_message');
SET @sql := IF(@has_old>0,'ALTER TABLE cpf_broker_inbox DROP INDEX uk_cpf_broker_inbox_message','SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @has_new := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='cpf_broker_inbox' AND index_name='uk_cpf_broker_inbox_consumer_message');
SET @sql := IF(@has_new=0,'ALTER TABLE cpf_broker_inbox ADD CONSTRAINT uk_cpf_broker_inbox_consumer_message UNIQUE (consumer_identity,message_id)','SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
CREATE INDEX IF NOT EXISTS ix_cpf_broker_inbox_retention ON cpf_broker_inbox (inbox_status,updated_at,inbox_id);
