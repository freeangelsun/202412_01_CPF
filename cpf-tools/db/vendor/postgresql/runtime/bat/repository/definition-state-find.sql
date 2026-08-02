SELECT job_id,definition_version,definition_state,row_version,checksum,created_by
FROM bat_job_definition_version
WHERE job_id = ?
  AND definition_version = ?
