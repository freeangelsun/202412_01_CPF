DROP INDEX IF EXISTS ix_cpf_transaction_segment_execution;
ALTER TABLE cpf_transaction_segment DROP COLUMN IF EXISTS execution_id;
