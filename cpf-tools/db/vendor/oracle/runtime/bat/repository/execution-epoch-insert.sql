INSERT INTO BAT_EXECUTION_EPOCH (
  job_id, current_fencing_token, epoch_version, updated_at
)
VALUES (?, ?, 1, CURRENT_TIMESTAMP(6))
