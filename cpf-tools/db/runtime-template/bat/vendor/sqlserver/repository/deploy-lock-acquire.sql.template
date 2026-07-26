MERGE INTO bat_deployment_lock WITH (HOLDLOCK) AS target
USING (VALUES (?, ?)) AS source(cell_id, owner_deployment_id)
ON target.cell_id = source.cell_id
WHEN MATCHED AND target.expires_at < SYSUTCDATETIME() THEN UPDATE SET
    owner_deployment_id = source.owner_deployment_id,
    fencing_token = target.fencing_token + 1,
    locked_at = SYSUTCDATETIME(),
    expires_at = DATEADD(MINUTE, 30, SYSUTCDATETIME())
WHEN NOT MATCHED THEN INSERT (
    cell_id, owner_deployment_id, fencing_token, locked_at, expires_at
) VALUES (
    source.cell_id, source.owner_deployment_id, 1, SYSUTCDATETIME(),
    DATEADD(MINUTE, 30, SYSUTCDATETIME())
);
