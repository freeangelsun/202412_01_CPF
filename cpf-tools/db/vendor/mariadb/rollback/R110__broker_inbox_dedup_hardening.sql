SELECT IF(COUNT(*)=0,'ROLLBACK_SAFE','ROLLBACK_BLOCKED_DUPLICATE_MESSAGE_ID') AS rollback_guard FROM (SELECT message_id FROM cpf_broker_inbox GROUP BY message_id HAVING COUNT(*)>1) d;
-- Execute the following only when rollback_guard=ROLLBACK_SAFE.
DROP INDEX IF EXISTS ix_cpf_broker_inbox_retention ON cpf_broker_inbox;
ALTER TABLE cpf_broker_inbox DROP INDEX uk_cpf_broker_inbox_consumer_message, ADD CONSTRAINT uk_cpf_broker_inbox_message UNIQUE(message_id), DROP COLUMN lease_version, DROP COLUMN consumer_identity;
