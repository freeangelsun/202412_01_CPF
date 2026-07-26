UPDATE bat_runtime_command
SET command_state = ?,
    failure_stage = ?,
    result_text = ?,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE command_id = ?
  AND command_state <> 'SUCCEEDED'
