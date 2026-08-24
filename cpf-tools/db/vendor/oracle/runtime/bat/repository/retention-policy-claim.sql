UPDATE OPS_RETENTION_POLICY
SET lease_owner = ?, lease_until = ?, fencing_token = fencing_token + 1, updated_at = CURRENT_TIMESTAMP
WHERE policy_id = ? AND enabled_yn = 'Y' AND paused_yn = 'N'
  AND (lease_until IS NULL OR lease_until < ? OR lease_owner = ?)
