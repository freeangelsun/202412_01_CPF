SELECT job_id,definition_version,job_name,executor_type,definition_state,owner_domain,
       trigger_type,trigger_expression,timezone_id,agent_pool,max_concurrency,restartable_yn,
       unknown_result_policy,checksum,row_version,effective_from,effective_until,updated_at
FROM BAT_JOB_DEFINITION_VERSION
WHERE job_id LIKE ?
  AND definition_state = ?
ORDER BY job_id,definition_version DESC
