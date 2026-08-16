ALTER TABLE cpf_transaction_segment
    ADD COLUMN execution_id VARCHAR(160) NULL COMMENT 'CPF 실행 인스턴스 ID' AFTER transaction_id;
CREATE INDEX ix_cpf_transaction_segment_execution ON cpf_transaction_segment (execution_id, started_at);
