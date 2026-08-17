ALTER TABLE BZA_LOGIN_HISTORY
    CHANGE COLUMN instance_id server_instance_id VARCHAR(200) NULL COMMENT '서버 인스턴스 ID';

DROP INDEX ix_bat_worker_instance ON BAT_WORKER;
ALTER TABLE BAT_WORKER
    CHANGE COLUMN instance_id server_instance_id VARCHAR(160) NOT NULL COMMENT '서버 인스턴스 ID';
CREATE INDEX ix_bat_worker_server ON BAT_WORKER (server_instance_id, active_yn);

DROP INDEX ix_bat_execution_instance ON BAT_EXECUTION;
ALTER TABLE BAT_EXECUTION
    CHANGE COLUMN instance_id server_instance_id VARCHAR(160) NULL COMMENT '실행 서버 인스턴스 ID';
CREATE INDEX ix_bat_execution_server ON BAT_EXECUTION (server_instance_id, start_time);

DROP INDEX ix_cpf_transaction_log_instance_time ON CPF_TRANSACTION_LOG;
ALTER TABLE CPF_TRANSACTION_LOG
    CHANGE COLUMN INSTANCE_ID SERVER_INSTANCE_ID VARCHAR(160) NULL COMMENT '처리 서버 인스턴스 ID';
CREATE INDEX ix_cpf_transaction_log_server_time ON CPF_TRANSACTION_LOG (SERVER_INSTANCE_ID, START_TIME);
