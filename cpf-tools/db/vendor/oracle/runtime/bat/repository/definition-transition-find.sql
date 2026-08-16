SELECT definition_state,checksum,row_version,job_name,executor_type,description,restartable_yn,
       trigger_type,trigger_expression,timezone_id,misfire_policy,executor_reference,
       effective_from,effective_until,created_by,definition_json
FROM bat_job_definition_version
WHERE job_id = ?
  AND definition_version = ?
