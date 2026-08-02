SELECT launch_request_json
  FROM cpf_batch_approved_launch
 WHERE job_id = ?
   AND definition_version = ?
   AND definition_checksum = ?
   AND approval_status = 'APPROVED'
   AND effective_from <= CURRENT_TIMESTAMP(6)
   AND (effective_until IS NULL OR effective_until > CURRENT_TIMESTAMP(6))
