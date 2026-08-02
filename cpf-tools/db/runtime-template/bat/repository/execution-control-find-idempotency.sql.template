SELECT cpf_execution_id, job_id, definition_version, approval_id,
       idempotency_scope, idempotency_key, request_hash, plan_checksum,
       fencing_token, control_status, job_instance_id, job_execution_id,
       reconcile_attempts, reconcile_after, updated_at
  FROM cpf_batch_execution_control
 WHERE idempotency_scope = ?
   AND idempotency_key = ?
