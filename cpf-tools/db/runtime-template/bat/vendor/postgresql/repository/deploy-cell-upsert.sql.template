INSERT INTO bat_deployment_cell(
    cell_id, environment_id, runtime_role, service_id, manifest_version, manifest_hash,
    desired_state, row_version
)
VALUES (?, ?, ?, ?, ?, ?, ?, 0)
ON CONFLICT (cell_id) DO UPDATE SET
    environment_id = EXCLUDED.environment_id,
    runtime_role = EXCLUDED.runtime_role,
    service_id = EXCLUDED.service_id,
    manifest_version = EXCLUDED.manifest_version,
    manifest_hash = EXCLUDED.manifest_hash,
    desired_state = EXCLUDED.desired_state,
    row_version = bat_deployment_cell.row_version + 1
