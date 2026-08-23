INSERT INTO OPS_OPERATION_DISCOVERY_INSTANCE (
    operation_id, instance_id, system_code, application_code, artifact_version,
    artifact_commit, discovered_yn, last_reported_at, created_by, created_at,
    updated_by, updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CPF_RUNTIME', ?, 'CPF_RUNTIME', ?)
