INSERT INTO OPS_RETENTION_POLICY(
  policy_id, target_name, action_name, retention_days, schedule_expression,
  maintenance_start, maintenance_end, enabled_yn, paused_yn, legal_hold_yn,
  chunk_size, throttle_millis, max_rows_per_run, max_runtime_seconds, lease_seconds,
  policy_version, next_run_at, fencing_token, row_version, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'N', ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?)
