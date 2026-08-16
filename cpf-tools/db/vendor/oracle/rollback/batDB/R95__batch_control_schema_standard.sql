ALTER TABLE cpf_batch_approved_launch
  DROP CONSTRAINT ck_cpf_bat_approval_version;
ALTER TABLE cpf_batch_execution_link DROP CONSTRAINT fk_cpf_bat_exec_link;
ALTER TABLE cpf_batch_execution_link
  DROP CONSTRAINT ck_cpf_bat_link_fencing;
ALTER TABLE cpf_batch_execution_link
  ADD CONSTRAINT fk_cpf_bat_exec_link FOREIGN KEY (cpf_execution_id)
    REFERENCES cpf_batch_execution_control(cpf_execution_id);
ALTER TABLE cpf_batch_execution_control
  DROP CONSTRAINT ck_cpf_bat_control_status;
