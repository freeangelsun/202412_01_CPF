MERGE INTO bat_deployment_lock target
USING (SELECT ? cell_id, ? owner_deployment_id FROM dual) source
ON (target.cell_id = source.cell_id)
WHEN MATCHED THEN UPDATE SET
    target.owner_deployment_id = CASE
        WHEN target.expires_at < CURRENT_TIMESTAMP(6)
        THEN source.owner_deployment_id ELSE target.owner_deployment_id END,
    target.fencing_token = CASE
        WHEN target.expires_at < CURRENT_TIMESTAMP(6)
        THEN target.fencing_token + 1 ELSE target.fencing_token END,
    target.locked_at = CASE
        WHEN target.expires_at < CURRENT_TIMESTAMP(6)
        THEN CURRENT_TIMESTAMP(6) ELSE target.locked_at END,
    target.expires_at = CASE
        WHEN target.expires_at < CURRENT_TIMESTAMP(6)
        THEN CURRENT_TIMESTAMP(6) + INTERVAL '30' MINUTE ELSE target.expires_at END
WHEN NOT MATCHED THEN INSERT (
    cell_id, owner_deployment_id, fencing_token, locked_at, expires_at
) VALUES (
    source.cell_id, source.owner_deployment_id, 1, CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6) + INTERVAL '30' MINUTE
)
