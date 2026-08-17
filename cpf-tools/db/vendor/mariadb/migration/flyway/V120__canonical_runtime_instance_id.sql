-- Canonical runtime instance naming. Same runtime identity is INSTANCE_ID across platform/BAT/BZA.
ALTER TABLE CPF_TRANSACTION_LOG
    CHANGE COLUMN SERVER_INSTANCE_ID INSTANCE_ID VARCHAR(160) NULL COMMENT '처리 Runtime 인스턴스 ID';
DROP INDEX ix_cpf_transaction_log_server_time ON CPF_TRANSACTION_LOG;
CREATE INDEX ix_cpf_transaction_log_instance_time ON CPF_TRANSACTION_LOG (INSTANCE_ID, START_TIME);

ALTER TABLE BAT_EXECUTION
    CHANGE COLUMN server_instance_id instance_id VARCHAR(160) NULL COMMENT '실행 Runtime 인스턴스 ID';
DROP INDEX ix_bat_execution_server ON BAT_EXECUTION;
CREATE INDEX ix_bat_execution_instance ON BAT_EXECUTION (instance_id, start_time);

ALTER TABLE BAT_WORKER
    CHANGE COLUMN server_instance_id instance_id VARCHAR(160) NOT NULL COMMENT 'Runtime 인스턴스 ID';
DROP INDEX ix_bat_worker_server ON BAT_WORKER;
CREATE INDEX ix_bat_worker_instance ON BAT_WORKER (instance_id, active_yn);

ALTER TABLE BZA_LOGIN_HISTORY
    CHANGE COLUMN server_instance_id instance_id VARCHAR(200) NULL COMMENT 'Runtime 인스턴스 ID';
