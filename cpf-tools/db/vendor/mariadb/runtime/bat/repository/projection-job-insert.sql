INSERT INTO BAT_JOB (
  job_id,job_name,job_type,published_definition_version,published_definition_checksum,
  executor_reference,definition_published_at,description,restartable_yn,use_yn,
  created_by,created_at,updated_by,updated_at
)
VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?,'Y',?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
