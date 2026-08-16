SELECT COUNT(*)
FROM bat_job_definition_audit
WHERE job_id = ?
  AND definition_version = ?
  AND to_state = 'PUBLISHED'
  AND approval_request_id = ?
