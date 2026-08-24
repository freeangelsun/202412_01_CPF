INSERT INTO BAT_DEPLOYMENT_INSTANCE_RESULT(
    deployment_id, sequence_no, instance_id, stage_code, result_state, result_message, recorded_at
)
VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6))
