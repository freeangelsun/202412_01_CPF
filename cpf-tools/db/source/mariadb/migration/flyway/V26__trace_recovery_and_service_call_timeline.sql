-- 거래 구간 durable recovery와 서비스 호출 시도별 관제를 위한 확장 컬럼입니다.
USE cpfDB;

ALTER TABLE cpf_transaction_segment
    ADD COLUMN IF NOT EXISTS selected_instance_id VARCHAR(100) NULL COMMENT '선택된 하위 서비스 인스턴스 ID' AFTER external_transaction_id,
    ADD COLUMN IF NOT EXISTS attempt_no INT NULL COMMENT '서비스 호출 시도 순번' AFTER selected_instance_id,
    ADD COLUMN IF NOT EXISTS retry_yn CHAR(1) NULL COMMENT '재시도 여부' AFTER attempt_no,
    ADD COLUMN IF NOT EXISTS failover_yn CHAR(1) NULL COMMENT '다른 인스턴스로 전환한 여부' AFTER retry_yn,
    ADD COLUMN IF NOT EXISTS circuit_state VARCHAR(20) NULL COMMENT '호출 시점 circuit 상태' AFTER failover_yn,
    ADD COLUMN IF NOT EXISTS downstream_http_status INT NULL COMMENT '하위 서비스 HTTP 상태' AFTER circuit_state,
    ADD COLUMN IF NOT EXISTS result_state VARCHAR(30) NULL COMMENT '호출 결과 상태' AFTER downstream_http_status,
    ADD COLUMN IF NOT EXISTS unknown_result_id VARCHAR(100) NULL COMMENT '결과 미확정 관리 ID' AFTER result_state;

-- 재실행 가능한 migration을 위해 index 존재 여부를 확인한 뒤 생성합니다.
SET @cpf_has_segment_instance_index := (
    SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'cpf_transaction_segment'
       AND index_name = 'ix_cpf_transaction_segment_instance'
);
SET @cpf_segment_instance_sql := IF(
    @cpf_has_segment_instance_index = 0,
    'CREATE INDEX ix_cpf_transaction_segment_instance ON cpf_transaction_segment (selected_instance_id, started_at)',
    'SELECT 1'
);
PREPARE cpf_segment_instance_stmt FROM @cpf_segment_instance_sql;
EXECUTE cpf_segment_instance_stmt;
DEALLOCATE PREPARE cpf_segment_instance_stmt;

SET @cpf_has_segment_attempt_index := (
    SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'cpf_transaction_segment'
       AND index_name = 'ix_cpf_transaction_segment_attempt'
);
SET @cpf_segment_attempt_sql := IF(
    @cpf_has_segment_attempt_index = 0,
    'CREATE INDEX ix_cpf_transaction_segment_attempt ON cpf_transaction_segment (transaction_global_id, attempt_no)',
    'SELECT 1'
);
PREPARE cpf_segment_attempt_stmt FROM @cpf_segment_attempt_sql;
EXECUTE cpf_segment_attempt_stmt;
DEALLOCATE PREPARE cpf_segment_attempt_stmt;

SET @cpf_has_segment_unknown_index := (
    SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = 'cpf_transaction_segment'
       AND index_name = 'ix_cpf_transaction_segment_unknown'
);
SET @cpf_segment_unknown_sql := IF(
    @cpf_has_segment_unknown_index = 0,
    'CREATE INDEX ix_cpf_transaction_segment_unknown ON cpf_transaction_segment (unknown_result_id)',
    'SELECT 1'
);
PREPARE cpf_segment_unknown_stmt FROM @cpf_segment_unknown_sql;
EXECUTE cpf_segment_unknown_stmt;
DEALLOCATE PREPARE cpf_segment_unknown_stmt;
