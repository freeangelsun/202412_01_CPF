SELECT job_id,definition_version,job_name,executor_type,definition_state,owner_domain,
       trigger_type,trigger_expression,timezone_id,agent_pool,max_concurrency,restartable_yn,
       unknown_result_policy,checksum,row_version,effective_from,effective_until,updated_at
FROM bat_job_definition_version
ORDER BY job_id,definition_version DESC
