SELECT consumer_identity,message_id,COUNT(*) duplicate_count FROM cpf_broker_inbox GROUP BY consumer_identity,message_id HAVING COUNT(*)>1;
SELECT column_name FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='cpf_broker_inbox' AND column_name IN ('consumer_identity','lease_version') ORDER BY column_name;
