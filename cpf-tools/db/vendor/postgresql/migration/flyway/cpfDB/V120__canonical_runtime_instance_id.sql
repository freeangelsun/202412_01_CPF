-- Canonical runtime instance naming. Same runtime identity is instance_id across platform/BAT/BZA.
ALTER TABLE cpf_transaction_log RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN cpf_transaction_log.instance_id IS '처리 Runtime 인스턴스 ID';
ALTER INDEX ix_cpf_transaction_log_server_time RENAME TO ix_cpf_transaction_log_instance_time;

ALTER TABLE bat_execution RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN bat_execution.instance_id IS '실행 Runtime 인스턴스 ID';
ALTER INDEX ix_bat_execution_server RENAME TO ix_bat_execution_instance;

ALTER TABLE bat_worker RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN bat_worker.instance_id IS 'Runtime 인스턴스 ID';
ALTER INDEX ix_bat_worker_server RENAME TO ix_bat_worker_instance;

ALTER TABLE bza_login_history RENAME COLUMN server_instance_id TO instance_id;
COMMENT ON COLUMN bza_login_history.instance_id IS 'Runtime 인스턴스 ID';
