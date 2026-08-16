INSERT INTO bat_deployment_cell(
    cell_id, environment_id, runtime_role, service_id, manifest_version, manifest_hash,
    desired_state, row_version
)
VALUES (?, ?, ?, ?, ?, ?, ?, 0)
ON DUPLICATE KEY UPDATE
    environment_id = VALUES(environment_id),
    runtime_role = VALUES(runtime_role),
    service_id = VALUES(service_id),
    manifest_version = VALUES(manifest_version),
    manifest_hash = VALUES(manifest_hash),
    desired_state = VALUES(desired_state),
    row_version = row_version + 1
