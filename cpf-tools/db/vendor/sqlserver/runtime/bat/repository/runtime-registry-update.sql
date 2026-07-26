UPDATE bat_runtime_instance
SET runtime_role = ?,
    service_id = ?,
    was_id = ?,
    host_alias = ?,
    zone_id = ?,
    pool_id = ?,
    artifact_version = ?,
    git_sha = ?,
    artifact_checksum = ?,
    profile_name = ?,
    config_version = ?,
    schema_compatibility = ?,
    started_at = ?,
    actual_state = 'STARTING',
    updated_at = SYSUTCDATETIME()
WHERE instance_id = ?
