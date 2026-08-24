UPDATE OPS_RETENTION_POLICY
SET paused_yn = ?, row_version = row_version + 1, updated_by = ?, updated_at = CURRENT_TIMESTAMP
WHERE policy_id = ? AND row_version = ?
