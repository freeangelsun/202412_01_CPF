-- Broker outbox의 다중 worker lease, 재시도, 실제 DLQ replay를 위한 컬럼과 인덱스입니다.
USE pfwDB;

ALTER TABLE pfw_broker_outbox
    ADD COLUMN IF NOT EXISTS attempt_count INT NOT NULL DEFAULT 0 COMMENT '발행 시도 횟수' AFTER worker_id,
    ADD COLUMN IF NOT EXISTS max_attempts INT NOT NULL DEFAULT 5 COMMENT '최대 발행 시도 횟수' AFTER attempt_count,
    ADD COLUMN IF NOT EXISTS next_attempt_at DATETIME(3) NULL COMMENT '다음 발행 가능 일시' AFTER max_attempts,
    ADD COLUMN IF NOT EXISTS lease_until DATETIME(3) NULL COMMENT 'worker 점유 만료 일시' AFTER next_attempt_at;

SET @cpf_has_broker_lease_index := (
    SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'pfw_broker_outbox'
       AND index_name = 'ix_pfw_broker_outbox_lease'
);
SET @cpf_broker_lease_sql := IF(
    @cpf_has_broker_lease_index = 0,
    'CREATE INDEX ix_pfw_broker_outbox_lease ON pfw_broker_outbox (outbox_status, lease_until)',
    'SELECT 1'
);
PREPARE cpf_broker_lease_stmt FROM @cpf_broker_lease_sql;
EXECUTE cpf_broker_lease_stmt;
DEALLOCATE PREPARE cpf_broker_lease_stmt;

SET @cpf_has_broker_ready_index := (
    SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'pfw_broker_outbox'
       AND index_name = 'ix_pfw_broker_outbox_ready'
);
SET @cpf_broker_ready_sql := IF(
    @cpf_has_broker_ready_index = 0,
    'CREATE INDEX ix_pfw_broker_outbox_ready ON pfw_broker_outbox (outbox_status, next_attempt_at, outbox_id)',
    'SELECT 1'
);
PREPARE cpf_broker_ready_stmt FROM @cpf_broker_ready_sql;
EXECUTE cpf_broker_ready_stmt;
DEALLOCATE PREPARE cpf_broker_ready_stmt;
