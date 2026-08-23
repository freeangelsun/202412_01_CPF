UPDATE OPS_OPERATION_DISCOVERY_INSTANCE
SET system_code = ?,
    application_code = ?,
    artifact_version = ?,
    artifact_commit = ?,
    discovered_yn = ?,
    last_reported_at = ?,
    updated_by = 'CPF_RUNTIME',
    updated_at = ?
WHERE operation_id = ?
  AND instance_id = ?
