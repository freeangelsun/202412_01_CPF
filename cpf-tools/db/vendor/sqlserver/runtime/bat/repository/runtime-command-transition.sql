UPDATE bat_runtime_command
SET command_state = ?,
    failure_stage = ?,
    result_text = ?,
    updated_at = SYSUTCDATETIME()
WHERE command_id = ?
  AND command_state <> 'SUCCEEDED'
