SELECT job_id,definition_version,definition_state,row_version,checksum,created_by
FROM BAT_JOB_DEFINITION_VERSION
WHERE job_id = ?
  AND definition_version = ?
