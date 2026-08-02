SELECT cpf_execution_id, job_id, definition_version, spring_job_instance_id,
       spring_job_execution_id, spring_step_execution_id, spring_status,
       fencing_token, updated_at
  FROM cpf_batch_execution_link
 WHERE cpf_execution_id = ?
 ORDER BY spring_job_execution_id, spring_step_execution_id
