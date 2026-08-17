-- Canonical runtime instance naming. Same runtime identity is INSTANCE_ID across platform/BAT/BZA.
ALTER TABLE CPF_TRANSACTION_LOG RENAME COLUMN SERVER_INSTANCE_ID TO INSTANCE_ID;
COMMENT ON COLUMN CPF_TRANSACTION_LOG.INSTANCE_ID IS '처리 Runtime 인스턴스 ID';
ALTER INDEX ix_cpf_transaction_log_server_time RENAME TO ix_cpf_transaction_log_instance_time;

ALTER TABLE BAT_EXECUTION RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN BAT_EXECUTION.instance_id IS '실행 Runtime 인스턴스 ID';
ALTER INDEX ix_bat_execution_server RENAME TO ix_bat_execution_instance;

ALTER TABLE BAT_WORKER RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN BAT_WORKER.instance_id IS 'Runtime 인스턴스 ID';
ALTER INDEX ix_bat_worker_server RENAME TO ix_bat_worker_instance;

ALTER TABLE BZA_LOGIN_HISTORY RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN BZA_LOGIN_HISTORY.instance_id IS 'Runtime 인스턴스 ID';
