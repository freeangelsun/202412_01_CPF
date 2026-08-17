ALTER TABLE BZA_LOGIN_HISTORY RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN BZA_LOGIN_HISTORY.server_instance_id IS '서버 인스턴스 ID';

ALTER INDEX ix_bat_worker_instance RENAME TO ix_bat_worker_server;
ALTER TABLE BAT_WORKER RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN BAT_WORKER.server_instance_id IS '서버 인스턴스 ID';

ALTER INDEX ix_bat_execution_instance RENAME TO ix_bat_execution_server;
ALTER TABLE BAT_EXECUTION RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN BAT_EXECUTION.server_instance_id IS '실행 서버 인스턴스 ID';

ALTER INDEX ix_cpf_transaction_log_instance_time RENAME TO ix_cpf_transaction_log_server_time;
ALTER TABLE CPF_TRANSACTION_LOG RENAME COLUMN INSTANCE_ID TO SERVER_INSTANCE_ID;
COMMENT ON COLUMN CPF_TRANSACTION_LOG.SERVER_INSTANCE_ID IS '처리 서버 인스턴스 ID';
