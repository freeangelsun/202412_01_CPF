-- 배치 실행 중 heartbeat와 진행률 관제를 위한 증분 migration입니다.
-- 신규 설치본에는 동일 칼럼이 10_cpf_schema.sql과 00_all_install*.sql에 반영되어 있습니다.

USE cpfDB;

ALTER TABLE cpf_batch_execution
    ADD COLUMN IF NOT EXISTS total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수' AFTER skip_count,
    ADD COLUMN IF NOT EXISTS processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수' AFTER total_count,
    ADD COLUMN IF NOT EXISTS success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수' AFTER processed_count,
    ADD COLUMN IF NOT EXISTS failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수' AFTER success_count,
    ADD COLUMN IF NOT EXISTS retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수' AFTER failure_count,
    ADD COLUMN IF NOT EXISTS progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률' AFTER retry_count,
    ADD COLUMN IF NOT EXISTS tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수' AFTER progress_rate,
    ADD COLUMN IF NOT EXISTS avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초' AFTER tps,
    ADD COLUMN IF NOT EXISTS max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초' AFTER avg_elapsed_ms,
    ADD COLUMN IF NOT EXISTS last_heartbeat_at DATETIME(3) NULL COMMENT '실행 메타 마지막 heartbeat 일시' AFTER max_elapsed_ms,
    ADD COLUMN IF NOT EXISTS current_step_name VARCHAR(150) NULL COMMENT '현재 실행 중인 Step 이름' AFTER last_heartbeat_at;

CREATE INDEX IF NOT EXISTS ix_cpf_batch_execution_heartbeat
    ON cpf_batch_execution (execution_status, last_heartbeat_at);

ALTER TABLE cpf_batch_step_execution
    ADD COLUMN IF NOT EXISTS total_count BIGINT NOT NULL DEFAULT 0 COMMENT '전체 처리 대상 건수' AFTER skip_count,
    ADD COLUMN IF NOT EXISTS processed_count BIGINT NOT NULL DEFAULT 0 COMMENT '처리 완료 건수' AFTER total_count,
    ADD COLUMN IF NOT EXISTS success_count BIGINT NOT NULL DEFAULT 0 COMMENT '성공 처리 건수' AFTER processed_count,
    ADD COLUMN IF NOT EXISTS failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '실패 처리 건수' AFTER success_count,
    ADD COLUMN IF NOT EXISTS retry_count BIGINT NOT NULL DEFAULT 0 COMMENT '재시도 또는 rollback 건수' AFTER failure_count,
    ADD COLUMN IF NOT EXISTS progress_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '진행률' AFTER retry_count,
    ADD COLUMN IF NOT EXISTS tps DECIMAL(18,4) NOT NULL DEFAULT 0.0000 COMMENT '초당 처리 건수' AFTER progress_rate,
    ADD COLUMN IF NOT EXISTS avg_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '평균 처리 시간 밀리초' AFTER tps,
    ADD COLUMN IF NOT EXISTS max_elapsed_ms BIGINT NOT NULL DEFAULT 0 COMMENT '최대 처리 시간 밀리초' AFTER avg_elapsed_ms,
    ADD COLUMN IF NOT EXISTS last_heartbeat_at DATETIME(3) NULL COMMENT 'Step 메타 마지막 heartbeat 일시' AFTER max_elapsed_ms;

CREATE INDEX IF NOT EXISTS ix_cpf_batch_step_execution_heartbeat
    ON cpf_batch_step_execution (execution_status, last_heartbeat_at);
