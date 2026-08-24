SELECT consumer_identity,message_id,COUNT(*) duplicate_count FROM CPF_BROKER_INBOX GROUP BY consumer_identity,message_id HAVING COUNT(*)>1;
SELECT column_name FROM user_tab_columns WHERE table_name='CPF_BROKER_INBOX' AND column_name IN ('CONSUMER_IDENTITY','LEASE_VERSION') ORDER BY column_name;
