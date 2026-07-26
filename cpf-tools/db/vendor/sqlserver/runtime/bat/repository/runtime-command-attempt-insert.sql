INSERT INTO bat_runtime_command_attempt(
    command_id, attempt_no, instance_id, stage_code, attempt_state, result_message, started_at, finished_at
)
VALUES (?, ?, ?, ?, ?, ?, SYSUTCDATETIME(), SYSUTCDATETIME())
