SELECT * FROM BAT_DEPLOYMENT_EXECUTION
WHERE idempotency_scope = ? AND idempotency_key = ?
