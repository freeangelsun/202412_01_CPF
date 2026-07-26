INSERT INTO bat_deployment_lock(
    cell_id, owner_deployment_id, fencing_token, locked_at, expires_at
)
VALUES (?, ?, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6) + INTERVAL '30 minutes')
ON CONFLICT (cell_id) DO UPDATE SET
    owner_deployment_id = CASE
        WHEN bat_deployment_lock.expires_at < CURRENT_TIMESTAMP(6)
        THEN EXCLUDED.owner_deployment_id ELSE bat_deployment_lock.owner_deployment_id END,
    fencing_token = CASE
        WHEN bat_deployment_lock.expires_at < CURRENT_TIMESTAMP(6)
        THEN bat_deployment_lock.fencing_token + 1 ELSE bat_deployment_lock.fencing_token END,
    locked_at = CASE
        WHEN bat_deployment_lock.expires_at < CURRENT_TIMESTAMP(6)
        THEN EXCLUDED.locked_at ELSE bat_deployment_lock.locked_at END,
    expires_at = CASE
        WHEN bat_deployment_lock.expires_at < CURRENT_TIMESTAMP(6)
        THEN EXCLUDED.expires_at ELSE bat_deployment_lock.expires_at END
