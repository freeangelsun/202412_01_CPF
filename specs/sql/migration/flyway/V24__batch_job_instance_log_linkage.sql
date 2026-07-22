-- BAT JobInstance 로그와 CPF 배치 실행 이력을 상호 추적하기 위한 확장 컬럼입니다.

ALTER TABLE cpf_batch_execution
    ADD COLUMN IF NOT EXISTS spring_batch_job_instance_id BIGINT NULL COMMENT 'Spring Batch JobInstance ID' AFTER spring_batch_execution_id,
    ADD COLUMN IF NOT EXISTS business_date DATE NULL COMMENT 'JobInstance 시작 시 확정한 업무일자' AFTER spring_batch_job_instance_id,
    ADD COLUMN IF NOT EXISTS run_id VARCHAR(120) NULL COMMENT '최초 실행 회차 ID' AFTER business_date,
    ADD COLUMN IF NOT EXISTS rerun_id VARCHAR(120) NULL COMMENT '운영 재수행 ID' AFTER run_id,
    ADD COLUMN IF NOT EXISTS original_job_execution_id BIGINT NULL COMMENT '재시작 기준 원 JobExecution ID' AFTER rerun_id,
    ADD COLUMN IF NOT EXISTS restart_attempt INT NOT NULL DEFAULT 0 COMMENT '동일 JobInstance 재시작 회차' AFTER original_job_execution_id,
    ADD COLUMN IF NOT EXISTS parent_transaction_global_id VARCHAR(100) NULL COMMENT '온라인 호출 또는 상위 배치 거래 ID' AFTER transaction_global_id,
    ADD COLUMN IF NOT EXISTS transaction_segment_id VARCHAR(120) NULL COMMENT '배치 Job 거래 구간 ID' AFTER parent_transaction_global_id,
    ADD COLUMN IF NOT EXISTS parent_segment_id VARCHAR(120) NULL COMMENT '상위 거래 구간 ID' AFTER transaction_segment_id,
    ADD COLUMN IF NOT EXISTS job_log_relative_path VARCHAR(1000) NULL COMMENT 'CPF_LOG_ROOT 기준 JobInstance 로그 상대 경로' AFTER parent_segment_id;

CREATE INDEX IF NOT EXISTS ix_cpf_batch_execution_job_instance
    ON cpf_batch_execution (spring_batch_job_instance_id, business_date);

CREATE INDEX IF NOT EXISTS ix_cpf_batch_execution_parent_transaction
    ON cpf_batch_execution (parent_transaction_global_id);

CREATE INDEX IF NOT EXISTS ix_cpf_batch_execution_segment
    ON cpf_batch_execution (transaction_segment_id, parent_segment_id);
