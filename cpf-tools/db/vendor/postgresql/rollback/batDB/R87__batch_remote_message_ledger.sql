DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM bat_remote_message_ledger WHERE status_cd='PROCESSING' AND lease_until>CURRENT_TIMESTAMP) THEN
  RAISE EXCEPTION 'R87 denied: active remote messages exist';
 END IF;
END $$;
DROP TABLE bat_remote_message_ledger;
