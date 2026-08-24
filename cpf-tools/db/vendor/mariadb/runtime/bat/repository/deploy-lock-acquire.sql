INSERT INTO BAT_DEPLOYMENT_LOCK(
    cell_id, owner_deployment_id, fencing_token, locked_at, expires_at
)
VALUES (?, ?, 1, CURRENT_TIMESTAMP(6), DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 30 MINUTE))
ON DUPLICATE KEY UPDATE
    owner_deployment_id = IF(expires_at < CURRENT_TIMESTAMP(6), VALUES(owner_deployment_id), owner_deployment_id),
    fencing_token = IF(expires_at < CURRENT_TIMESTAMP(6), fencing_token + 1, fencing_token),
    locked_at = IF(expires_at < CURRENT_TIMESTAMP(6), VALUES(locked_at), locked_at),
    expires_at = IF(expires_at < CURRENT_TIMESTAMP(6), VALUES(expires_at), expires_at)
