-- PRE-GA CANONICAL REPAIR (R9)
-- Legacy fixed BIZADM/EXS DDL was removed. EXS is a Generated Domain and MBW has its own canonical baseline at V29.
-- V6 now owns only the transaction/server identity increment that belongs to cpfDB.

USE cpfDB;

ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS SERVER_INSTANCE_ID VARCHAR(160) NULL COMMENT '처리 서버 인스턴스 ID' AFTER WAS_ID;
ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS HOST_NAME VARCHAR(120) NULL COMMENT '처리 서버 호스트명' AFTER SERVER_INSTANCE_ID;
ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS PROCESS_ID VARCHAR(80) NULL COMMENT '처리 서버 프로세스 ID' AFTER HOST_NAME;
ALTER TABLE cpf_transaction_log ADD COLUMN IF NOT EXISTS THREAD_NAME VARCHAR(160) NULL COMMENT '처리 스레드명' AFTER PROCESS_ID;
CREATE INDEX IF NOT EXISTS ix_cpf_transaction_log_server_time ON cpf_transaction_log (SERVER_INSTANCE_ID, START_TIME);
