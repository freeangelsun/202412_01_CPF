INSERT INTO BAT_JOB_RUNTIME_PROJECTION (
  job_id,definition_version,definition_checksum,projection_status,executor_type,
  executor_reference,trigger_type,trigger_expression,timezone_id,projection_json,
  projection_hash,effective_from,effective_until,published_by,published_at,row_version
)
VALUES (?,?,?,'ACTIVE',?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,1)
