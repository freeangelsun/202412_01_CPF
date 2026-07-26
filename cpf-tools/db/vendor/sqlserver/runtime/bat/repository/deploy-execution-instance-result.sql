INSERT INTO bat_deployment_instance_result(
    deployment_id, sequence_no, instance_id, stage_code, result_state, result_message, recorded_at
)
VALUES (?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
