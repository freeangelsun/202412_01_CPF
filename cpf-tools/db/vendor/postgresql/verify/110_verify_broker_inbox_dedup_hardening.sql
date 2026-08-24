SELECT consumer_identity, message_id, COUNT(*) AS duplicate_count FROM CPF_BROKER_INBOX GROUP BY consumer_identity,message_id HAVING COUNT(*) > 1;
SELECT column_name FROM information_schema.columns WHERE table_name='CPF_BROKER_INBOX' AND column_name IN ('consumer_identity','lease_version') ORDER BY column_name;
