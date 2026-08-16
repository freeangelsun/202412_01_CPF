UPDATE cpf_batch_execution_link
   SET spring_job_instance_id = ?,
       spring_job_execution_id = ?,
       spring_step_execution_id = ?,
       spring_status = ?,
       updated_at = CURRENT_TIMESTAMP(6)
 WHERE cpf_execution_id = ?
   AND link_key = ?
   AND job_id = ?
   AND definition_version = ?
   AND fencing_token = ?
