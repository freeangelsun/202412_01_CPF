INSERT INTO BAT_DEPLOYMENT_LOCK(
    cell_id, owner_deployment_id, fencing_token, locked_at, expires_at
)
VALUES (?, ?, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6) + INTERVAL '30 minutes')
ON CONFLICT (cell_id) DO UPDATE SET
    owner_deployment_id = CASE
        WHEN BAT_DEPLOYMENT_LOCK.expires_at < CURRENT_TIMESTAMP(6)
        THEN EXCLUDED.owner_deployment_id ELSE BAT_DEPLOYMENT_LOCK.owner_deployment_id END,
    fencing_token = CASE
        WHEN BAT_DEPLOYMENT_LOCK.expires_at < CURRENT_TIMESTAMP(6)
        THEN BAT_DEPLOYMENT_LOCK.fencing_token + 1 ELSE BAT_DEPLOYMENT_LOCK.fencing_token END,
    locked_at = CASE
        WHEN BAT_DEPLOYMENT_LOCK.expires_at < CURRENT_TIMESTAMP(6)
        THEN EXCLUDED.locked_at ELSE BAT_DEPLOYMENT_LOCK.locked_at END,
    expires_at = CASE
        WHEN BAT_DEPLOYMENT_LOCK.expires_at < CURRENT_TIMESTAMP(6)
        THEN EXCLUDED.expires_at ELSE BAT_DEPLOYMENT_LOCK.expires_at END
