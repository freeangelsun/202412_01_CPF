ALTER TABLE bza_login_history RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN bza_login_history.server_instance_id IS '서버 인스턴스 ID';

ALTER INDEX ix_bat_worker_instance RENAME TO ix_bat_worker_server;
ALTER TABLE bat_worker RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN bat_worker.server_instance_id IS '서버 인스턴스 ID';

ALTER INDEX ix_bat_execution_instance RENAME TO ix_bat_execution_server;
ALTER TABLE bat_execution RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN bat_execution.server_instance_id IS '실행 서버 인스턴스 ID';

ALTER INDEX ix_cpf_transaction_log_instance_time RENAME TO ix_cpf_transaction_log_server_time;
ALTER TABLE cpf_transaction_log RENAME COLUMN instance_id TO server_instance_id;
COMMENT ON COLUMN cpf_transaction_log.server_instance_id IS '처리 서버 인스턴스 ID';
