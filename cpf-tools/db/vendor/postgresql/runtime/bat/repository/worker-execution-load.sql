SELECT e.execution_id,
       e.job_id,
       e.job_parameters,
       e.transaction_id,
       e.transaction_segment_id,
       e.business_date,
       e.requested_by,
       e.restart_attempt,
       e.definition_version,
       e.definition_checksum,
       p.executor_type,
       p.executor_reference,
       p.projection_json AS definition_json
  FROM bat_execution e
  JOIN bat_job_runtime_projection p
    ON p.job_id = e.job_id
   AND p.definition_version = e.definition_version
   AND p.definition_checksum = e.definition_checksum
   AND p.projection_hash = e.definition_checksum
 WHERE e.execution_id = ?
   AND p.projection_status IN ('ACTIVE','RETIRED')
