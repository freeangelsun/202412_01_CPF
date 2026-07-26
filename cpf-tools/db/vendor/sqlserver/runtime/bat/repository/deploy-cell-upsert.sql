MERGE INTO bat_deployment_cell AS target
USING (VALUES (?, ?, ?, ?, ?, ?, ?))
    AS source(cell_id, environment_id, runtime_role, service_id, manifest_version, manifest_hash, desired_state)
ON target.cell_id = source.cell_id
WHEN MATCHED THEN UPDATE SET
    environment_id = source.environment_id,
    runtime_role = source.runtime_role,
    service_id = source.service_id,
    manifest_version = source.manifest_version,
    manifest_hash = source.manifest_hash,
    desired_state = source.desired_state,
    row_version = target.row_version + 1
WHEN NOT MATCHED THEN INSERT (
    cell_id, environment_id, runtime_role, service_id, manifest_version, manifest_hash,
    desired_state, row_version
) VALUES (
    source.cell_id, source.environment_id, source.runtime_role, source.service_id,
    source.manifest_version, source.manifest_hash, source.desired_state, 0
);
