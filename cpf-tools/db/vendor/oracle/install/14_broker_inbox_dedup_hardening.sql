DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM user_tab_columns WHERE table_name='CPF_BROKER_INBOX' AND column_name='CONSUMER_IDENTITY'; IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE cpf_broker_inbox ADD (consumer_identity VARCHAR2(120) DEFAULT ''default'' NOT NULL)'; END IF; END;
/
DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM user_tab_columns WHERE table_name='CPF_BROKER_INBOX' AND column_name='LEASE_VERSION'; IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE cpf_broker_inbox ADD (lease_version NUMBER(19) DEFAULT 0 NOT NULL)'; END IF; END;
/
DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM user_constraints WHERE table_name='CPF_BROKER_INBOX' AND constraint_name='UK_CPF_BROKER_INBOX_MESSAGE'; IF n>0 THEN EXECUTE IMMEDIATE 'ALTER TABLE cpf_broker_inbox DROP CONSTRAINT uk_cpf_broker_inbox_message'; END IF; END;
/
DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM user_constraints WHERE table_name='CPF_BROKER_INBOX' AND constraint_name='UK_CPF_BROKER_INBOX_CONSUMER_MESSAGE'; IF n=0 THEN EXECUTE IMMEDIATE 'ALTER TABLE cpf_broker_inbox ADD CONSTRAINT uk_cpf_broker_inbox_consumer_message UNIQUE (consumer_identity,message_id)'; END IF; END;
/
DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM user_indexes WHERE index_name='IX_CPF_BROKER_INBOX_RETENTION'; IF n=0 THEN EXECUTE IMMEDIATE 'CREATE INDEX ix_cpf_broker_inbox_retention ON cpf_broker_inbox (inbox_status,updated_at,inbox_id)'; END IF; END;
/
