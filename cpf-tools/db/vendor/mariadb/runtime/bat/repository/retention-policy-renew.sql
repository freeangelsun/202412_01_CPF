UPDATE OPS_RETENTION_POLICY
SET lease_until = ?, updated_at = CURRENT_TIMESTAMP
WHERE policy_id = ? AND lease_owner = ? AND lease_until >= ?
