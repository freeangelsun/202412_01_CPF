SELECT policy_id
FROM OPS_RETENTION_POLICY
WHERE enabled_yn = 'Y' AND paused_yn = 'N'
  AND next_run_at IS NOT NULL AND next_run_at <= ?
ORDER BY next_run_at, policy_id
