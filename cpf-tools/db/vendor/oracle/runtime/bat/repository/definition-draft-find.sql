SELECT definition_state,row_version,definition_json
FROM BAT_JOB_DEFINITION_VERSION
WHERE job_id = ?
  AND definition_version = ?
