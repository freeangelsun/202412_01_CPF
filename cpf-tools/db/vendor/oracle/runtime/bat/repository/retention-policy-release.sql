UPDATE OPS_RETENTION_POLICY
SET lease_owner = NULL, lease_until = NULL, last_run_at = CURRENT_TIMESTAMP,
    next_run_at = ?, updated_at = CURRENT_TIMESTAMP
WHERE policy_id = ? AND lease_owner = ?
