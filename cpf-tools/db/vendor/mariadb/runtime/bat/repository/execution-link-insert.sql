INSERT INTO BAT_EXECUTION_LINK (
  cpf_execution_id, link_key, job_id, definition_version, spring_job_instance_id,
  spring_job_execution_id, spring_step_execution_id, spring_status,
  fencing_token, created_at, updated_at
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
