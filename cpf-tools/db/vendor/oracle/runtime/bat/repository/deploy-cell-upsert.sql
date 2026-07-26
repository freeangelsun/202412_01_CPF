MERGE INTO bat_deployment_cell target
USING (
    SELECT ? cell_id, ? environment_id, ? runtime_role, ? service_id,
           ? manifest_version, ? manifest_hash, ? desired_state
    FROM dual
) source
ON (target.cell_id = source.cell_id)
WHEN MATCHED THEN UPDATE SET
    target.environment_id = source.environment_id,
    target.runtime_role = source.runtime_role,
    target.service_id = source.service_id,
    target.manifest_version = source.manifest_version,
    target.manifest_hash = source.manifest_hash,
    target.desired_state = source.desired_state,
    target.row_version = target.row_version + 1
WHEN NOT MATCHED THEN INSERT (
    cell_id, environment_id, runtime_role, service_id, manifest_version, manifest_hash,
    desired_state, row_version
) VALUES (
    source.cell_id, source.environment_id, source.runtime_role, source.service_id,
    source.manifest_version, source.manifest_hash, source.desired_state, 0
)
