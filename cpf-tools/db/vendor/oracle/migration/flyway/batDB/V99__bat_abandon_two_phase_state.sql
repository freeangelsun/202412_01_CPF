-- CPF BAT abandon two-phase state lifecycle upgrade.
-- Adds ABANDONING so the ledger claim is persisted before the external Spring Batch abandon side effect.
ALTER TABLE cpf_batch_execution_control DROP CONSTRAINT ck_cpf_bat_control_status;
ALTER TABLE cpf_batch_execution_control
  ADD CONSTRAINT ck_cpf_bat_control_status
  CHECK (control_status IN ('RESERVED', 'STARTING', 'STARTED', 'STOPPING', 'STOPPED', 'COMPLETED', 'FAILED', 'UNKNOWN_RESULT', 'ABANDONING', 'ABANDONED', 'REJECTED'));
