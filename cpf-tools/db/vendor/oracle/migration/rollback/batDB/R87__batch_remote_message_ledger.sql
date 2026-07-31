DECLARE active_count NUMBER;
BEGIN
 SELECT COUNT(*) INTO active_count FROM bat_remote_message_ledger WHERE status_cd='PROCESSING' AND lease_until>SYSTIMESTAMP;
 IF active_count>0 THEN RAISE_APPLICATION_ERROR(-20087,'R87 denied: active remote messages exist'); END IF;
END;
/
DROP TABLE bat_remote_message_ledger PURGE;
