ALTER TABLE cpf_transaction_segment ADD COLUMN execution_id VARCHAR(160);
COMMENT ON COLUMN cpf_transaction_segment.execution_id IS 'CPF 실행 인스턴스 ID';
CREATE INDEX ix_cpf_transaction_segment_execution ON cpf_transaction_segment (execution_id, started_at);
