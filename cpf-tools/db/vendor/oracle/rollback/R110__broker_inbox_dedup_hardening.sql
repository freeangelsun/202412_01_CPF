DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM (SELECT message_id FROM cpf_broker_inbox GROUP BY message_id HAVING COUNT(*)>1); IF n>0 THEN RAISE_APPLICATION_ERROR(-20001,'Rollback blocked: duplicate message_id exists across consumer identities'); END IF; END;
/
DROP INDEX ix_cpf_broker_inbox_retention;
ALTER TABLE cpf_broker_inbox DROP CONSTRAINT uk_cpf_broker_inbox_consumer_message;
ALTER TABLE cpf_broker_inbox ADD CONSTRAINT uk_cpf_broker_inbox_message UNIQUE(message_id);
ALTER TABLE cpf_broker_inbox DROP COLUMN lease_version;
ALTER TABLE cpf_broker_inbox DROP COLUMN consumer_identity;
