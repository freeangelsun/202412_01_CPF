-- Oracle resolves unquoted lowercase identifiers to the historical V82 objects.
ALTER TABLE cpf_batch_execution_control
  ADD CONSTRAINT ck_cpf_bat_control_status CHECK (
    control_status IN ('RESERVED','STARTING','STARTED','STOPPING','STOPPED','COMPLETED','FAILED','UNKNOWN_RESULT','ABANDONED','REJECTED')
  );
ALTER TABLE cpf_batch_execution_link
  ADD CONSTRAINT ck_cpf_bat_link_fencing CHECK (fencing_token > 0);
ALTER TABLE cpf_batch_execution_link DROP CONSTRAINT fk_cpf_bat_exec_link;
ALTER TABLE cpf_batch_execution_link
  ADD CONSTRAINT fk_cpf_bat_exec_link FOREIGN KEY (cpf_execution_id)
    REFERENCES cpf_batch_execution_control(cpf_execution_id) ON DELETE CASCADE;
ALTER TABLE cpf_batch_approved_launch
  ADD CONSTRAINT ck_cpf_bat_approval_version CHECK (row_version >= 0);
